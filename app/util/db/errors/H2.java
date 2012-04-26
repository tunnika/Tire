package util.db.errors;


import org.h2.constant.ErrorCode;
import org.h2.jdbc.JdbcSQLException;
import util.db.DBErrorCodes;
import util.db.PersistenceErrorHandler;

import java.util.HashMap;
import java.util.Map;

public class H2 implements DBErrorCodes {
    public static Map<Integer,Enum<PersistenceErrorHandler.CODES>> ERRORS;

    static {
        ERRORS = new HashMap<Integer, Enum<PersistenceErrorHandler.CODES>>();
        // Primary key
        ERRORS.put(ErrorCode.DUPLICATE_KEY_1, PersistenceErrorHandler.CODES.DUPLICATE_PK);
    }

    @Override
    public Enum<PersistenceErrorHandler.CODES> getErrorCode(Throwable e) {
        return ERRORS.get(((JdbcSQLException)e).getErrorCode());
    }
}
