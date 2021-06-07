package evgenykuzin.core.cnfg;

import com.github.evgenykuzin.core.util.loger.BasicLogger;
import com.github.evgenykuzin.core.util.loger.Logger;

public class LogConfig {
    public static Logger logger = new BasicLogger();
    public static void setLogger(Logger Logger1) {
        logger = Logger1;
    }
}
