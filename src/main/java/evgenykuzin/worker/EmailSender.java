package evgenykuzin.worker;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import com.github.evgenykuzin.core.marketplace_api_integrations.ozon.OzonManager;
import com.github.evgenykuzin.core.parser.ZooekspressParser;
import com.github.evgenykuzin.core.util.Task;
import com.github.evgenykuzin.core.util_managers.MailManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class EmailSender implements Runnable {
    private static final Set<Task> taskSet = new HashSet<>();
    private static final String ZOOEKSPRESS_EMAIL = "m.arkhipova@zooexpress.ru";
    private static final String SELLERMP_EMAIL = "sellermp@yandex.ru";
    private static final String TEST_EMAIL = "evgenykuzin21@gmail.com";
    private final MailManager mailManager;

    private EmailSender() {
        mailManager = MailManager.getSmtpMailManager();
        Integer[] workDays = new Integer[]{1, 2, 3, 4, 5};
        Integer[] allDays = new Integer[]{1, 2, 3, 4, 5, 6, 7};
        //taskQueue.add(new Task(()->this.sendMsgToZooekspressForStocks(ZOOEKSPRESS_EMAIL), 9, 0, 59));
        //taskQueue.add(new Task(()->this.sendMsgToZooekspressForStocks(ZOOEKSPRESS_EMAIL), 15, 30, 59));
        taskSet.add(new Task(()->this.sendMsgToZooekspressForStocks(SELLERMP_EMAIL), 9, 5, 59, workDays));
        taskSet.add(new Task(()->this.sendMsgToZooekspressForStocks(SELLERMP_EMAIL), 15, 35, 59, workDays));

        //taskQueue.add(new Task(()->this.sendMsgToZooekspressForOrder(ZOOEKSPRESS_EMAIL), 13, 2, 59));
        //taskQueue.add(new Task(()->this.sendMsgToZooekspressForOrder(ZOOEKSPRESS_EMAIL), 15, 32, 59));
        taskSet.add(new Task(()->this.sendMsgToZooekspressForOrder(SELLERMP_EMAIL), 13, 7, 59, workDays));
        taskSet.add(new Task(()->this.sendMsgToZooekspressForOrder(SELLERMP_EMAIL), 15, 37, 59, workDays));

        taskSet.add(new Task(()->this.sendMsgToZooekspressForOrder(TEST_EMAIL), 9, 2, 59, workDays));
        taskSet.add(new Task(()->this.sendMsgToZooekspressForOrder(TEST_EMAIL), 15, 5, 59, allDays));
    }

    public static EmailSender getInstance() {
        return EmailSenderHolder.EMAIL_SENDER;
    }

    private static class EmailSenderHolder {
        public static final EmailSender EMAIL_SENDER = new EmailSender();
    }

    @Override
    public void run() {
        try {
//            var currentThreadName = Thread.currentThread().getName();
//            Thread.currentThread().setName(String.format("%s-emailSender", currentThreadName));
            taskSet.forEach(Task::execute);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean sendMsgToZooekspressForStocks(String email) {
        String sub = "ЗАПРОС НА ОСТАТКИ, ИП Белов Б.А.";
        String text = "Добрый день, уважаемые партнеры!\n" +
                " \n" +
                "Просьба в ответном письме прислать прайс с актуальными остатками.";
        return mailManager.sendMessage(email, sub, text);
    }

    public boolean sendMsgToZooekspressForOrder(String email) {
        String text = "НОВЫЙ ЗАКАЗ, ИП Белов Б.А.";
        var zooekspressParser = new ZooekspressParser();
        if (zooekspressParser.sendOrders(OzonManager.getInstance().getOrderedProducts())) {
            File orderFile = zooekspressParser.getOrderFile();
            boolean sended = mailManager.sendMessage(email, text, text, orderFile);
            LogConfig.logger.logf(EmailSender.class.toString(), "Order to %s email WAS SUCCESSFULLY SENT", email);
            return sended;
        } else {
            LogConfig.logger.logf("Order to %s email WAS NOT SENT", email);
            return true;
        }
    }

    public static void main(String[] args) {
        String email = "evgenykuzin21@gmail.com";
        String text = "НОВЫЙ ЗАКАЗ, ИП Белов Б.А.";
        var zooekspressParser = new ZooekspressParser();
        if (zooekspressParser.sendOrders(OzonManager.getInstance().getOrderedProducts())) {
            File orderFile = zooekspressParser.getOrderFile();
            MailManager.getSmtpMailManager().sendMessage(email, text, text, orderFile);
            LogConfig.logger.log("Order to Zooekspress email WAS SUCCESSFULLY SENT");
        } else {
            LogConfig.logger.log("Order to Zooekspress email WAS NOT SENT");
        }
    }
}
