package evgenykuzin.worker.util;

import com.github.evgenykuzin.core.util.loger.Loggable;
import com.github.evgenykuzin.core.util_managers.FTPManager;
import com.github.evgenykuzin.core.util_managers.MailManager;

import java.io.IOException;

public class EmailFilesGrabber implements Runnable, Loggable {

    @Override
    public void run() {
        try {
            updateZooekspress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateZooekspress() {
        try {
            var file = MailManager.getImapMailManager().downloadFileFromSuppliers("@zooexpress.ru", "прайс", null, MailManager.PRICES_FOLDER);
            FTPManager.uploadFileToSuppliers(file, "zooekspress", ".xls");
            log("file zooekspress was downloaded from email and uploaded to FTP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
