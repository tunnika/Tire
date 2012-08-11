package controllers;

import models.Search;
import models.User;
import models.norpneu.Tire;
import play.cache.Cached;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.dashboard;
import views.html.search_results;
import views.html.templates.tire;
import views.html.templates.dashboard_home;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

@Security.Authenticated(DashboardLock.class)
public class Dashboard extends Controller {
	public static Result index() {
		return ok(dashboard.render(User.findByEmail(session("email")),
				form(Search.class)));
	}

	public static Result search() {
		Form<Search> searchForm = form(Search.class).bindFromRequest();
		if (searchForm.hasErrors()) {
			return ok(dashboard.render(User.findByEmail(session("email")),
					searchForm));
		}
		List<Tire> tires = Tire.finder.where()
				.eq("measure", searchForm.get().searchQuery).findList();

		return ok(search_results.render(User.findByEmail(session("email")),
				tires));
	}

	public static Result searchAll() {

		List<Tire> tires = Tire.finder.all();

		return ok(search_results.render(User.findByEmail(session("email")),
				tires));
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
	
	@Cached(key = "tireTemplate")
         public static Result tireTemplate(){
        return ok(tire.render());
    }

    @Cached(key = "tmplDashboardHome")
    public static Result dashboardHomeTemplate(){
        return ok(dashboard_home.render());
    }
}
