package controllers;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import exceptions.FormValidationException;
import models.Login;
import models.User;
import models.norpneu.Tire;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;
import util.EnhancedController;
import views.html.about;
import views.html.login;
import views.html.register;
import views.html.search_results;

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

	public static Result registerForward() {
		return ok(register.render(form(User.class)));
	}

	public static Result registerAction() {
		Form<User> userForm = form(User.class).bindFromRequest();
		try {
			handleSaveForm(userForm, buildErrorMap("DUPLICATE_PK", Messages.get("registration.duplicate.email", safePullModel(userForm).email)));
		} catch (FormValidationException e) {
			return badRequest(register.render(userForm));
		}

		flash("success", Messages.get("registration.pending.approval", userForm.get().email));
		// TODO: send notification to the user by email. If email fails, return
		// form with error
		return redirect(routes.Application.login());
	}

	public static Result aboutForward() {
		return ok(about.render());
	}

	public static Result allUserJson() {
		/*
		 * IF JSON DATA ON REQUEST IS NEEDED JsonNode jsonNodeRequest =
		 * request().body().asJson(); if (jsonNodeRequest == null) return
		 * badRequest("Expecting Json data");
		 */
		// Validate if the header request is json
		if (request().getHeader(CONTENT_TYPE) == null || !request().getHeader(CONTENT_TYPE).equalsIgnoreCase("application/json"))
			return badRequest("Expecting Json request");
		ObjectNode result = Json.newObject();
		List<User> users = User.findAll();
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(users);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
			return badRequest("Unable to jsonify object: "+e.getMessage());
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return badRequest("Unable to jsonify object: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			return badRequest("Unable to jsonify object: "+e.getMessage());
		}
		result.put("users", json);
		return ok(result);
	}

	public static Result allTireJson() {
		/*
		 * IF JSON DATA ON REQUEST IS NEEDED JsonNode jsonNodeRequest =
		 * request().body().asJson(); if (jsonNodeRequest == null) return
		 * badRequest("Expecting Json data");
		 */
		// Validate if the header request is json
		if (request().getHeader(CONTENT_TYPE) == null || !request().getHeader(CONTENT_TYPE).equalsIgnoreCase("application/json"))
			return badRequest("Expecting Json request");
		ObjectNode result = Json.newObject();
		List<Tire> tires = Tire.finder.all();
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(tires);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
			return badRequest("Unable to jsonify object: "+e.getMessage());
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return badRequest("Unable to jsonify object: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			return badRequest("Unable to jsonify object: "+e.getMessage());
		}
		result.put("tires", json);
		return ok(result);
	}

}