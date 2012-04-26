package util.db;


import play.Logger;
import play.Play;
import util.db.errors.H2;

import java.util.HashMap;
import java.util.Map;

public class PersistenceErrorHandler {
    public static enum CODES {UNKNOWN, DUPLICATE_PK};
    private static Map<CODES,String> messageCodes;
    static {
        messageCodes = new HashMap<CODES, String>();

        // Unknown error maps to db.unknown
        messageCodes.put(CODES.UNKNOWN, "db.unknown");
        // DUPLICATE_PK map to error code db.duplicate.pk
        messageCodes.put(CODES.DUPLICATE_PK, "db.duplicate.pk");
    }


    /**
     * Retrieves a handled message given a PersistenceException root cause as an argument
     * Uses default db.
     * @param e                 Throwable instance to be inspected
     * @param overrideMessages  Supply a Map of customized to be used in the context of the callee
     * @return                  CODES.UNKNOWN if inspected throwable didn't contain a known message
     *                          Otherwise, returns message;
     */
    public static String getMessageCode(Throwable e, Map<CODES,String> overrideMessages){
        return getMessageCode(e ,"default",overrideMessages);
    }


    /**
     * Retrieves a handled message given a PersistenceException root cause as an argument
     *
     * @param e                 Throwable instance to be inspected
     * @param db                Database name to be used. Default is default.
     * @param overrideMessages  Supply a Map of customized to be used in the context of the callee
     * @return                  CODES.UNKNOWN if inspected throwable didn't contain a known message
     *                          Otherwise, returns message;
     */
    public static String getMessageCode(Throwable e, String db, Map<CODES,String> overrideMessages){
        if(e==null)
            return messageCodes.get(CODES.UNKNOWN);

        //TODO: have proper database identification... this is a little bit naive
        String driver = Play.application().configuration().getConfig("db").getConfig(db).getString("driver").toLowerCase();
        if(driver.indexOf("h2")>-1){
            //TODO: Create instance of H2 using dependency injection
            DBErrorCodes map = new H2();
            Enum<CODES> err = map.getErrorCode(e);
            if(overrideMessages!=null && overrideMessages.containsKey(err))
                return overrideMessages.get(err);
            else{
                if(messageCodes.containsKey(err))
                    return messageCodes.get(map.getErrorCode(e));
                else {
                    Logger.error(err+" message code not found in messagesCodes in "+PersistenceErrorHandler.class);
                    return messageCodes.get(CODES.UNKNOWN);
                }
            }
        }

        Logger.error("Database driver not found");
        return messageCodes.get(CODES.UNKNOWN);
    }

}
