package controllers;

import models.User;
import play.*;
import play.mvc.*;
import play.data.*;

import views.html.*;

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {
  public static Result index() {
      return ok(dashboard.render(User.findByEmail(session("email"))));
  }
}
