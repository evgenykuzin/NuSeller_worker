package evgenykuzin.core.util_managers;

import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.SocketException;
import java.nio.file.Files;

public class FTPManager {
    public static final String TMP_DIR = "/tmp/";
    public static final String SUPPLIERS_DIR = TMP_DIR + "suppliers/";

    private synchronized static FTPClient connectAndGetFTP() throws IOException {
        var props = PropertiesManager.getProperties("ftp");
        var address = props.getProperty("address");
        var username = props.getProperty("username");
        var password = props.getProperty("password");
        FTPClient ftpClient = new FTPClient();
        while (true) {
            try {
                ftpClient.connect(address);
                break;
            } catch (SocketException se) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ftpClient.login(username, password);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        return ftpClient;
    }

    public synchronized static File getFile(String ftpDir, String fileName, String suffix) throws IOException {
        FTPClient ftpClient = connectAndGetFTP();
        File file = Files.createTempFile(fileName, suffix).toFile();
        OutputStream fileOutputStream = new FileOutputStream(file);
        String ftpFilePath = ftpDir + fileName + suffix;
        boolean isDownloaded = ftpClient.retrieveFile(ftpFilePath, fileOutputStream);
        if (!isDownloaded) {
            throw new IOException(ftpClient.getReplyString());
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        ftpClient.logout();
        ftpClient.disconnect();
        file.deleteOnExit();
        return file;
    }

    public synchronized static File getFileFromSuppliers(String fileName, String suffix) {
        try {
            return getFile(SUPPLIERS_DIR, fileName, suffix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static boolean uploadFile(InputStream inputStream, String ftpFilePath) throws IOException {
        FTPClient ftpClient = connectAndGetFTP();
        try (inputStream) {
            boolean isDownloaded = ftpClient.storeFile(ftpFilePath, inputStream);
            if (!isDownloaded) {
                throw new IOException(ftpClient.getReplyString());
            }
        } finally {
            ftpClient.logout();
            ftpClient.disconnect();
        }
        return true;
    }

    public synchronized static boolean uploadFile(File file, String ftpFilePath) throws IOException {
        return uploadFile(new FileInputStream(file), ftpFilePath);
    }

    public synchronized static boolean uploadFile(File file, String ftpDir, String fileName, String suffix) throws IOException {
        return uploadFile(file, ftpDir + fileName + suffix);
    }

    public synchronized static boolean uploadFileToSuppliers(File file, String ftpFileName, String suffix) throws IOException {
        return uploadFile(file, "/tmp/suppliers/", ftpFileName, suffix);
    }

    public synchronized static boolean uploadFileToSuppliersWithItsName(File file) throws IOException {
        return uploadFileWithItsName(file, SUPPLIERS_DIR);
    }

    public synchronized static boolean uploadFileWithItsName(File file, String ftpDir) throws IOException {
        var split = file.getName().split("\\.");
        if (split.length < 2) split = new String[]{split[0], ""};
        return uploadFile(file, ftpDir, split[0], split[1]);
    }

    public synchronized static void uploadDirectory(String remoteDirPath,
                                                    String localParentDir,
                                                    String remoteParentDir) throws IOException {
        FTPClient ftpClient = connectAndGetFTP();
        System.out.println("LISTING directory: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath = remoteDirPath + "/" + remoteParentDir
                        + "/" + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteDirPath + "/" + item.getName();
                }

                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("About to upload the file: " + localFilePath);
                    boolean uploaded = uploadFile(item, remoteFilePath);
                    if (uploaded) {
                        System.out.println("UPLOADED a file to: "
                                + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT upload the file: "
                                + localFilePath);
                    }
                } else {
                    // create directory on the server
                    boolean created = ftpClient.makeDirectory(remoteFilePath);
                    if (created) {
                        System.out.println("CREATED the directory: "
                                + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT create the directory: "
                                + remoteFilePath);
                    }

                    // upload the sub directory
                    String parent = remoteParentDir + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    uploadDirectory(remoteDirPath, localParentDir, parent);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String name = "zooekspress_all";
        String suffix = ".xls";
        uploadFile(FileManager.getFromResources(name+suffix), "/tmp/suppliers/", name, suffix);
    }

}
