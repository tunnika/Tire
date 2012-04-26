package util.db;

public interface DBErrorCodes {
    public Enum<PersistenceErrorHandler.CODES> getErrorCode(Throwable e);
}
