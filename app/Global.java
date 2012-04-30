import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import models.User;
import models.norpneu.Brand;
import models.norpneu.Tire;
import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;
import util.GlobalVars;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        InitialData.insert(app);
    }

    static class InitialData {


        public static void insert(Application app) {
            @SuppressWarnings("unchecked")
            Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("initial-data.yaml");

            if (Ebean.find(User.class).findRowCount() == 0) {
                List<Object> usersList = all.get("users");
                for (Object o : usersList) {
                    User u = (User) o;
                    try {
                        u.password = new String(MessageDigest.getInstance(GlobalVars.MD5).digest(u.password.getBytes(GlobalVars.UTF_8)), GlobalVars.UTF_8);
                    } catch (NoSuchAlgorithmException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // Insert users first
                Ebean.save(all.get("users"));
            }

            if(Ebean.find(Brand.class).findRowCount()==0){
                Ebean.save(all.get("brands"));
            }

            if(Ebean.find(Tire.class).findRowCount()==0){
                Ebean.save(all.get("tires"));
            }
        }

    }

}