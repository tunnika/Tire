import play.*;
import play.libs.*;
import sun.security.provider.MD5;
import util.GlobalVars;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.avaje.ebean.*;

import models.*;

public class Global extends GlobalSettings {
    
    public void onStart(Application app) {
        InitialData.insert(app);
    }
    
    static class InitialData {
        


				public static void insert(Application app) {
            if(Ebean.find(User.class).findRowCount() == 0) {
                
                Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yaml");

                List<Object> usersList = all.get("users");
                for(Object o : usersList){
                	User u = (User)o;
                	try {
	                  u.password = new String(MessageDigest.getInstance(GlobalVars.MD5).digest(u.password.getBytes(GlobalVars.UTF_8)),GlobalVars.UTF_8);
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
        }
        
    }
    
}