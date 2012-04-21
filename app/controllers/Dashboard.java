package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.dashboard;

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {
  public static Result index() {
      return ok(dashboard.render(User.findByEmail(session("email"))));
  }
}
