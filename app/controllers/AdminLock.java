package controllers;

import com.avaje.ebean.Ebean;
import models.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class AdminLock extends Security.Authenticator {
    
    @Override
    public String getUsername(Context ctx) {
        String user =ctx.session().get("email");
        if(user!=null) {
            User u = Ebean.find(User.class).where().eq("email", user).findUnique();
            if(u!=null && u.powerUser)
                return user;
        }
        return null;
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Dashboard.index());
    }
    
    
}