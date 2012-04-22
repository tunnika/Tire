package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.InvalidValue;
<<<<<<< HEAD

=======
>>>>>>> Merge registerAction() including Ebean property validations
import models.*;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

<<<<<<< HEAD
=======
import javax.persistence.PersistenceException;


>>>>>>> Catch persistence exceptions on user registration. A proper handling with known errors being properly presented to the user is still required
public class Application extends Controller {

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

    //TODO: lots of boilerplate. Refactor!
    public static Result registerAction(){
    	Form<User> userForm = form(User.class).bindFromRequest();
        if (userForm.hasErrors()) {
            return badRequest(register.render(userForm));
        } else {
            // make form complies with all the validations at the "database" level
            InvalidValue invalid = Ebean.validate(userForm.get());
            if(!invalid.isError()){ // database constraints are valid
                try{
                    userForm.get().save();
                    flash("success", Messages.get("registration.pending.approval", userForm.get().email));
                    // TODO: send notification to the user by email. If email fails, return form with error
                    return redirect(routes.Application.login());
                } catch (PersistenceException pe){
                    // unfortunately, we still need to catch
                    // exceptions when writing to database
                    //TODO: how to handle persistence exceptions properly?
                    // Found error when filling in a duplicate email
                    ValidationError ve = new ValidationError("", pe.getMessage(), null);
                    userForm.reject(ve);
                    return badRequest(register.render(userForm));
                }
            } else {
                // failed database validations
                //TODO: PT?
                userForm.reject(invalid.getMessage());
                return badRequest(register.render(userForm));
            }
        }
    }

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

	public static Result registerForward() {
		return ok(register.render(form(User.class)));
	}

	public static Result registerAction() {
		Form<User> userForm = form(User.class).bindFromRequest();
		// userForm.get().password = "1234567899876543211234567";
		// userForm.get().save();
		// TODO: Necessario enviar o email para o email registado pelo
		// utilizador, se falhar o envio, retornar o form em erro
		if (userForm.hasErrors()) {
			return badRequest(register.render(userForm));
		} else {
			InvalidValue invalid = Ebean.validate(userForm.get());
			if (!invalid.isError()) {
				Ebean.save(userForm.get());
				flash("success",
						"Pedido registado. Assim que a sua conta estiver autorizada, receberá um email em "
								+ userForm.get().email
								+ " com as instruções para finalização do registo.");
				return redirect(routes.Application.login());
			}
			//TODO: PT?
			userForm.reject(invalid.getMessage());
			return badRequest(register.render(userForm));
		}
	}

}