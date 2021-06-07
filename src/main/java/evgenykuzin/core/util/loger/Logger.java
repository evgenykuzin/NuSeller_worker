package evgenykuzin.core.util.loger;

public interface Logger {

     void log(String from, String msg);

    default void log(String msg) {
        log("main", msg);
    }

    void logf(String from, String msg, Object... args);

    default void logf(String msg, Object... args) {
        logf("main", msg, args);
    }

}
