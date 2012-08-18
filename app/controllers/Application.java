package controllers;

import exceptions.FormValidationException;
import exceptions.QueueException;
import models.ChangePassword;
import models.Login;
import models.User;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;
import util.EnhancedController;
import util.NotificationManager;
import views.html.about;
import views.html.changepasswordonregister;
import views.html.login;
import views.html.register;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;


public class Application extends EnhancedController {
    private static final String INIT_PASSWORD = "init";
    private static final String RESET_PASSWORD = "reset";

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
            handleSaveForm(userForm, buildErrorMap(
                    "DUPLICATE_PK", Messages.get("registration.duplicate.email", safePullModel(userForm).email)));
        } catch (FormValidationException e) {
            return badRequest(register.render(userForm));
        }
        // Add notification to queue
        try{
            NotificationManager.queueNotification(userForm.get().email,
                    Messages.get("registration.pending.approval.email.subject"),
                    Messages.get("registration.pending.approval.email.body"));
            flash("success", Messages.get("registration.pending.approval", userForm.get().email));
        }catch (QueueException e){
            flash("success", Messages.get("registration.pending.approval.error", userForm.get().email));
            Logger.error("Error sending email for:"+userForm.get().email, e);
        }
        return redirect(routes.Application.login());
    }
    
    public static Result aboutForward(){
    	return ok(about.render());
    }


    public static Result changePasswordForward(String email, String hash){
        User user = User.find.where()
                .eq("email", email)
                .eq("password", hash)
                .findUnique();

        if(user==null)
            flash("error", "Registo não encontrado");

        return ok(changepasswordonregister.render(email, hash, INIT_PASSWORD, form(ChangePassword.class)));
    }

    public static Result resetPasswordForward(String email, String token){
        User user = User.find.where()
                .eq("email", email)
                .eq("resetToken", token)
                .ge("resetTokenExpirationDate", new Timestamp(System.currentTimeMillis()))
               .findUnique();

        if(user==null)
            flash("error", "Registo não encontrado");

        return ok(changepasswordonregister.render(email, token, RESET_PASSWORD, form(ChangePassword.class)));
    }

    public static Result changePasswordAction(){
        Form<ChangePassword> form = form(ChangePassword.class).bindFromRequest();
        ChangePassword changePasswordModel = null;
        try {
            changePasswordModel = safePullModel(form);
            if (changePasswordModel.email != null) {
                if(!changePasswordModel.password.equals(changePasswordModel.confirmPassword)){
                    addGlobalError(form, "A palavra passe e confirmação não são iguais");
                    return badRequest(changepasswordonregister.render(changePasswordModel.email, changePasswordModel.token, changePasswordModel.action, form));
                }

                User user;
                if (changePasswordModel.action.equals(INIT_PASSWORD)) {
                    user = User.find.where()
                            .eq("email", changePasswordModel.email)
                            .eq("password", changePasswordModel.token)
                            .findUnique();
                } else {
                    user = User.find.where()
                            .eq("email", changePasswordModel.email)
                            .eq("resetToken", changePasswordModel.token)
                            .gt("resetTokenExpirationDate",  new Timestamp(System.currentTimeMillis()))
                            .findUnique();
                }

                if (user == null) {
                    addGlobalError(form, "Utilizador não foi encontrado ou passou demasiado tempo desde o envio do email até à tentativa de alteração. " +
                            "P.f. requira nova recuperação de palavra passe");
                    return badRequest(changepasswordonregister.render(changePasswordModel.email,changePasswordModel.token, changePasswordModel.action,form));
                }
                user.password = User.obfuscatePassword(changePasswordModel.password);
                user.resetToken = null;
                user.resetTokenExpirationDate = null;
                //TODO: handle persistence exceptions - should be done in enhanced controller
                user.update();
            }
        } catch (FormValidationException e) {
            //TODO: how can I get the email if form is not valid?!?!
            return badRequest(changepasswordonregister.render("",changePasswordModel.token, changePasswordModel.action,form));
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Error in  obfuscating password.", e);
            addGlobalError(form, "Erro de incriptação da password. Por favor tente mais tarde");
            return badRequest(changepasswordonregister.render(changePasswordModel.email,changePasswordModel.token, changePasswordModel.action,form));
        } catch (UnsupportedEncodingException e) {
            Logger.error("Error in  obfuscating password.", e);
            addGlobalError(form, "Erro de incriptação da password. Por favor tente mais tarde");
            return badRequest(changepasswordonregister.render(changePasswordModel.email,changePasswordModel.token, changePasswordModel.action,form));
        }
        flash("success", "Palavra passe alterada com sucesso. Por favor efectue agora o seu login");
        return redirect(routes.Application.login());
    }

    
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
            Routes.javascriptRouter("jsRoutes",
                // Routes for Projects
                controllers.routes.javascript.Dashboard.allTireJson()
                ));
	}


}