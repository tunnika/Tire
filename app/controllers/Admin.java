package controllers;

import com.avaje.ebean.Ebean;
import exceptions.QueueException;
import models.User;
import org.codehaus.jackson.node.ArrayNode;
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

import java.sql.Timestamp;

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
        if (u != null) {
            u.active = true;
            u.activeDate = new Timestamp(System.currentTimeMillis());
            u.update();
            flash("success", "Account " + email + " has been approved");
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
        return ok(admin.render(User.findByEmail(session("email"))));
    }


    public static Result users() {
        if (request().getHeader(CONTENT_TYPE) == null || !request().getHeader(CONTENT_TYPE).equalsIgnoreCase("application/json"))
            return badRequest("Expecting Json request");

        ObjectNode result = Json.newObject();
        ArrayNode users = result.putArray("users");
        for(User u: User.find.where().eq("powerUser", false).order("company").findList()) {
            users.add(Json.toJson(u));
        }
        return ok(result);
    }
}
