package controllers;

import exceptions.FormValidationException;
import models.Login;
import models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import util.EnhancedController;
import views.html.about;
import views.html.login;
import views.html.register;

import javax.persistence.PersistenceException;


public class Application extends EnhancedController {

    public static Result login() {
        return ok(login.render(form(Login.class)));
    }

    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.Application.login());
    }

    public static Result authenticate() {

        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session("email", loginForm.get().email);
            return redirect(routes.Dashboard.index());
        }
    }
    
    public static Result registerForward(){
    	return ok(register.render(form(User.class)));
    }

    public static Result registerAction(){
        Form<User> userForm = form(User.class).bindFromRequest();
        try {
            handleSaveForm(userForm);
        } catch (FormValidationException e) {
            return badRequest(register.render(userForm));
        } catch (PersistenceException pe){
            // add global erro to response
            // override system message DUPLICATE_PK with registration.duplicate.email
            addPersistenceError(userForm, pe.getCause(), err(
                    "DUPLICATE_PK", Messages.get("registration.duplicate.email", userForm.get().email)));
            return badRequest(register.render(userForm));
        }

        flash("success", Messages.get("registration.pending.approval", userForm.get().email));
        // TODO: send notification to the user by email. If email fails, return form with error
        return redirect(routes.Application.login());
    }
    
    public static Result aboutForward(){
    	return ok(about.render());
    }

}