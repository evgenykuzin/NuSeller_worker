package evgenykuzin.app;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManager;
import com.github.evgenykuzin.core.marketplace_api_integrations.wildeberries.WildeberriesManager;
import com.github.evgenykuzin.core.util.loger.BasicLogger;
import com.github.evgenykuzin.worker.*;
import com.github.evgenykuzin.worker.util.EmailFilesGrabber;
import com.jcabi.log.VerboseRunnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) {
        LogConfig.setLogger(new BasicLogger());
        //schedule(new GoogleDocViewManager(), 0, 30, TimeUnit.MINUTES);

       // schedule(EmailSender.getInstance(), 0, 1, TimeUnit.MINUTES);
//        schedule(()-> {
//            FromWildeberriesProductsUpdater.getInstance().doUpdates();
//            FromOzonProductsUpdater.getInstance().doUpdates();
//            //FromYamarketProductsUpdater.getInstance().doUpdates();
//        }, 0, 15, TimeUnit.MINUTES);
        StocksUpdater stocksUpdater = StocksUpdater.getInstance();
        schedule(()->{
            stocksUpdater.updateStocks(OzonManager.getInstance());
            stocksUpdater.updateStocks(WildeberriesManager.getInstance());
        }, 0, 5, TimeUnit.MINUTES);
        schedule(new EmailFilesGrabber(), 10, 15, TimeUnit.MINUTES);
    }

    private static void schedule(Runnable runnable, int initialDelay, int period, TimeUnit timeUnit) {
        executorService.scheduleAtFixedRate(new VerboseRunnable(runnable, true), initialDelay, period, timeUnit);
    }

    private static void execute(Runnable runnable) {
        executorService.execute(new VerboseRunnable(runnable, true));
    }

}
