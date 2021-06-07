package evgenykuzin.core.util.loger;

import com.github.evgenykuzin.core.cnfg.LogConfig;

public interface Loggable {
    default void log(String msg){
        LogConfig.logger.log(getClass().getName(), msg);
    }
    default void logf(String msg, Object... args){
        LogConfig.logger.logf(getClass().getName(), msg, args);
    }
}
