package evgenykuzin.core.util.loger;

public class BasicLogger implements Logger {
    private static final String baseStartText = ">From (%s):-----[%s].\n";

    @Override
    public void log(String from, String msg) {
        System.out.printf(baseStartText, from, msg);
    }

    @Override
    public void logf(String from, String msg, Object... args) {
        System.out.printf(baseStartText, from, String.format(msg, args));
    }
}
