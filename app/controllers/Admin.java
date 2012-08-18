package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import exceptions.QueueException;
import models.User;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.MissingNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import util.EnhancedController;
import util.NotificationManager;
import util.PlayUtils;
import views.html.admin;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

@Security.Authenticated(AdminLock.class)
public class Admin extends EnhancedController {

    public static Result index() {
        // TODO: we need to stop accessing the database every time we want to retrieve
        // the user associated with the session
        // see http://www.playframework.org/documentation/2.0.2/JavaCache for how to incorporate caching
        return ok(admin.render(User.findByEmail(session("email"))));
    }

    public static Result approveAccount(String email) {
        User u = Ebean.find(User.class).where()
                .eq("email", email)
                .eq("active", false).findUnique();
        boolean ok = false;
        if (u != null) {
            u.active = true;
            u.activeDate = new Timestamp(System.currentTimeMillis());
            u.update();
            flash("success", "Account " + email + " has been approved");
            ok = true;
            try {
                NotificationManager.queueNotification(email,
                        Messages.get("registration.approved.email.subject"),
                        Messages.get("registration.approved.email.body",
                                PlayUtils.getApplicationConfig("host"), u.email, u.password));
            } catch (QueueException e) {
                Logger.error("Mail notification failed", e);
            }
        } else {
            flash("error", "Account " + email + " not found or is already active");
        }

        ObjectNode result = Json.newObject();
        result.put("status", ok);
        if (!ok) {
            result.put("message", "Account " + email + " not found or is already active");
        }
        return ok(result);
    }

    public static Result declineAccount(String email) {
        User u = Ebean.find(User.class).where()
                .eq("email", email)
                .eq("active", false).findUnique();
        boolean ok = false;
        ObjectNode result = Json.newObject();
        try{
            u.delete();
            ok = true;
        } catch(Exception e){
            ok = false;
            result.put("message", "Erro no sistema no momento de apagar o registo. P.f. tente novamente e caso se mantenha, procure o seu administrador do sistema");
            Logger.error(e.getMessage());
        }
        result.put("status", ok);
        return ok(result);
    }

    public static Result revokeAccount() {
        JsonNode json = request().body().asJson();
        if(json==null){
            return badRequest("Not a json request");
        }

        // check arguments
        JsonNode listOfEmailsJsonNode = json.findPath("listOfEmails");
        String email  = json.findPath("email").getTextValue();
        ArrayList<String> emails;
        if (listOfEmailsJsonNode instanceof MissingNode && email == null) {
            return badRequest("Missing email or listOfEmails");
        } else if (!(listOfEmailsJsonNode instanceof MissingNode)) {
            Iterator<JsonNode> listOfEmails = listOfEmailsJsonNode.getElements();
            emails = new ArrayList<String>(5);
            while(listOfEmails.hasNext()){
                emails.add(listOfEmails.next().getTextValue());
            }
        } else  {
            emails = new ArrayList<String>(1);
            emails.add(email);
        }

        boolean ok;
        ObjectNode result = Json.newObject();
        try{
            Ebean.delete(Ebean.find(User.class).where().in("email", emails).findList());
            ok = true;
        } catch (Exception e) {
            ok = false;
            result.put("message", "Erro no sistema no momento de apagar o registo. " +
                    "P.f. tente novamente e caso se mantenha, procure o seu administrador do sistema");
            Logger.error(e.getMessage());
        }
        result.put("status", ok);
        return ok(result);
    }

    public static Result users() {
        if (request().getHeader(CONTENT_TYPE) == null || !request().getHeader(CONTENT_TYPE).equalsIgnoreCase("application/json"))
            return badRequest("Expecting Json request");

        ObjectNode result = Json.newObject();
        ArrayNode companies = result.putArray("data");
        ObjectNode company = null;
        ObjectNode user = null;
        ArrayNode children = null;
        String companyName = "_";
        for(User u: User.find.where().eq("powerUser", false).eq("active", true).order("company").findList()) {
            if (!companyName.equals(u.company)) {
                if(company!=null) companies.add(company);
                company = Json.newObject();
                companyName = u.company;
                company.put("company", companyName);
                company.put("street", u.street);
                company.put("postalCode", u.postalCode);
                company.put("city", u.city);
                children = company.putArray("_children");
            }
            user = Json.newObject();
            user.put("name", u.name);
            user.put("email", u.email);
            user.put("activeDate", u.activeDate!=null?u.activeDate.getTime():null);
            user.put("phoneNumber", u.phoneNumber);
            children.add(user);
        }
        if(company!=null) companies.add(company);
        return ok(result);
    }

    public static Result userRequests() {
        if (request().getHeader(CONTENT_TYPE) == null || !request().getHeader(CONTENT_TYPE).equalsIgnoreCase("application/json"))
            return badRequest("Expecting Json request");

        ObjectNode result = Json.newObject();
        ArrayNode data = result.putArray("data");
        for (User u : User.find.where().ne("active", true).order("company").findList()) {
            ObjectNode user = Json.newObject();
            user.put("company", u.company);
            user.put("name", u.name);
            user.put("email", u.email);
            user.put("date", u.registryDate!=null?u.registryDate.getTime():null);
            ArrayNode children = user.putArray("_children");
            ObjectNode child = Json.newObject();
            child.put("address", u.street);
            child.put("postalCode", u.postalCode);
            child.put("city", u.city);
            child.put("phoneNumber", u.phoneNumber);
            children.add(child);

            data.add(user);
        }

        return ok(result);
    }

    public static Result resetPassword(String email) {
        User u = Ebean.find(User.class).where()
                .eq("email", email)
                .eq("active", true).findUnique();
        boolean ok;
        ObjectNode result = Json.newObject();
        SecureRandom random = new SecureRandom();
        try {
            u.resetToken = new BigInteger(130, random).toString(32);
            u.resetTokenExpirationDate = new Timestamp(System.currentTimeMillis() + 86400000);
            u.update();
            ok = true;
            // Add notification to queue
            NotificationManager.queueNotification(u.email,
                    Messages.get("admin.members.password_reset.email.subject"),
                    Messages.get("admin.members.password_reset.email.body",
                            PlayUtils.getApplicationConfig("host"), u.email, u.resetToken));
        } catch (QueueException e) {
            ok = true;
            Logger.error("Error sending email for:" + u.email, e);
        } catch (Exception e) {
            ok = false;
            result.put("message", "Erro no sistema no momento de apagar o registo. P.f. tente novamente e caso se mantenha, procure o seu administrador do sistema");
            Logger.error(e.getMessage());
        }
        result.put("status", ok);
        return ok(result);
    }
}
