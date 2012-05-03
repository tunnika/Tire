package controllers;

import models.Search;
import models.User;
import models.norpneu.Tire;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.dashboard;
import views.html.search_results;

import java.util.List;

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {
    public static Result index() {
        return ok(dashboard.render(User.findByEmail(session("email")), form(Search.class)));
    }
    

    public static Result search(){
        Form<Search> searchForm = form(Search.class).bindFromRequest();
        if(searchForm.hasErrors()){
            return ok(dashboard.render(User.findByEmail(session("email")), searchForm));
        }
        List<Tire> tires = Tire.finder.where()
                .eq("measure", searchForm.get().searchQuery).findList();
        
        return ok(search_results.render(User.findByEmail(session("email")), tires));
    }
}
