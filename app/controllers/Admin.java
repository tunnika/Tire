package controllers;

import com.avaje.ebean.Ebean;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Result;
import play.mvc.Security;
import util.EnhancedController;
import util.email.AWSSimpleEmailService;
import views.html.admin;

import java.io.IOException;
import java.sql.Timestamp;

@Security.Authenticated(AdminLock.class)
public class Admin extends EnhancedController{

    public static Result approveAccount(String email){
        User u = Ebean.find(User.class).where()
                .eq("email", email)
                .eq("active",false).findUnique();
        if(u!=null){
            u.active=true;
            u.activeDate = new Timestamp(System.currentTimeMillis());
            u.update();
            flash("success", "Account "+email+" has been approved");
            try {
                AWSSimpleEmailService.send(email,
                        Messages.get("registration.approved.email.subject"),
                        Messages.get("registration.approved.email.body", u.email, u.password));
            } catch (IOException e) {
                // TODO: put in queue for later processing
                e.printStackTrace();
                Logger.error("Mail notification failed");
            }
        } else {
            flash("error", "Account "+email+" not found or is already active");
        }
        return ok(admin.render(User.findByEmail(session("email"))));
    }
}
