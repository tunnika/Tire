package util;


import play.Configuration;

public class PlayUtils {
    private static Configuration application = play.Play.application().configuration().getConfig("application");

    public static String getApplicationConfig(String key){
        return application.getString(key);
    }
}
