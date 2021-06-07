package evgenykuzin.core.util_managers;

import com.github.evgenykuzin.core.cnfg.LogConfig;
import lombok.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.SearchTerm;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class MailManager {
    private final String FROM_EMAIL_ADDRESS = "sellermp@yandex.ru";
    private final Properties yandexProps = PropertiesManager.getProperties("yandexmail");
    private final String yandexLogin = yandexProps.getProperty("login");
    private final String yandexPassword = yandexProps.getProperty("password");
    private final String IMAP_SERVER = "imap.yandex.ru";
    public static final String SUPPLIERS_FOLDER = "Поставщики";
    public static final String PRICES_FOLDER = "Прайсы";
    private static volatile Properties properties;

    public static synchronized MailManager getImapMailManager() {
        properties = new Properties();
        properties.put("mail.imap.host", "imap.yandex.ru");
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.socketFactory.port", "993");
        properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return new MailManager();
    }

    public static synchronized MailManager getSmtpMailManager() {
        properties = new Properties();
        properties.put("mail.smtp.host", "smtp.yandex.ru");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return new MailManager();
    }

    private MailManager() {
    }

    public synchronized boolean sendMessage(String toEmailAddress, String subject, String text, File... files) {
        Session session;
        Message message;
        try {
            session = Session.getDefaultInstance(properties,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(yandexLogin, yandexPassword);
                        }
                    });

            message = new MimeMessage(session);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            message.setFrom(new InternetAddress(FROM_EMAIL_ADDRESS));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmailAddress));
            message.setSubject(subject);
            message.setText(text);
            if (files.length > 0) {
                MimeMultipart mimeMultipart = new MimeMultipart();
                for (File f : files) {
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.attachFile(f);
                    mimeMultipart.addBodyPart(mimeBodyPart);
                }
                message.setContent(mimeMultipart);
            }
            Transport.send(message);
            LogConfig.logger.log("Message request for stocks to email " + toEmailAddress + " was SENT");
            return true;
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            LogConfig.logger.log("Message request for stocks to email " + toEmailAddress + " was FAILED");
            return false;
        } finally {
            properties.clear();
        }
    }

    public synchronized boolean sendMessage(String toEmailAddress, String subject, String text) {
        return sendMessage(toEmailAddress, subject, text, new File[0]);
    }

    public synchronized File downloadFileFromSuppliers(String fromEmailAddress, String fileNameToSearch, String subject, String folderName) {
        try {
            List<YandexMessage> messages = getMessages(fromEmailAddress, folderName, subject, fileNameToSearch);
            for (YandexMessage message : messages) {
                File webFile = message.getAttachment();
                if (webFile != null) {
                    LogConfig.logger.log("File from email " + fromEmailAddress + " has GOTTEN");
                    return webFile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogConfig.logger.log("File from email " + fromEmailAddress + " has FAILED to download");
        }
        return null;
    }

    public synchronized File downloadFileFromSuppliers(String fromEmailAddress, String fileNameToSearch, String folderName) {
        return downloadFileFromSuppliers(fromEmailAddress, fileNameToSearch, null, folderName);
    }

    public synchronized List<YandexMessage> getMessages(String fromEmailAddress, String folderName, String subject, String fileNameToSearch) {
        List<YandexMessage> messages = new ArrayList<>();
        try {
            Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(yandexLogin, yandexPassword);
                }
            });
            System.out.println("session = " + session);
            session.setDebug(false);
            Store store = null;
            Folder customFolder = null;
            try {
                store = session.getStore();
                System.out.println("store = " + store);
                // Подключение к почтовому серверу
                store.connect(IMAP_SERVER, yandexLogin, yandexPassword);
                System.out.println("store connect");

                customFolder = store.getFolder(folderName);
                var fromCustomFolder = getMessagesFromFolder(fromEmailAddress, customFolder, subject, fileNameToSearch);
                if (fromCustomFolder != null) {
                    messages.addAll(fromCustomFolder);
                }

                //Folder defaultFolder = store.getDefaultFolder().getFolder("inbox");
                //var fromDefault = getYandexMessagesFromFolder(fromEmailAddress, defaultFolder);
                //if (fromDefault != null) yandexMessages.addAll(fromDefault);
            } catch (MessagingException e) {
                e.printStackTrace();
            } finally {
                if (store != null && store.isConnected()) {
                    try {
                        store.close();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
                if (customFolder != null && customFolder.isOpen()) {
                    customFolder.close(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.reverse(messages);
        return messages;
    }

    private synchronized Collection<YandexMessage> getMessagesFromFolder(String fromEmailAddress, Folder folder, String subjectForSearch, String fileNameToSearch) throws MessagingException {
        folder.open(Folder.READ_ONLY);
        System.out.println("folder = " + folder.getName());
        if (folder.getMessageCount() == 0) return null;
        var searchMessages = folder.search(new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    var subjectMatch = subjectForSearch == null || containsIgnoreCase(message.getSubject(), subjectForSearch);
                    return Arrays.stream(message.getFrom())
                            .anyMatch(address -> containsIgnoreCase(address.toString(), fromEmailAddress))
                            && subjectMatch;
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        System.out.println("searchMessages count = " + searchMessages.length);
        return Arrays.stream(searchMessages)
                .map(message -> convertToYandexMessage(message, fileNameToSearch))
                .collect(Collectors.toList());
    }

    private synchronized YandexMessage convertToYandexMessage(Message message, String fileNameToSearch) {
        YandexMessage yandexMessage = new YandexMessage();
        try {
            yandexMessage.setAuthor(parseAuthorFomMessage(message));
            yandexMessage.setSubject(message.getSubject());
            yandexMessage.setText(getTextFromMessage(message));
            File attachment = findAttachment(message, fileNameToSearch);
            yandexMessage.setAttachment(attachment);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return yandexMessage;
    }

    private synchronized Collection<YandexMessage> getMessagesFromFolder(String fromEmailAddress, Folder folder, String fileNameToSearch) throws MessagingException {
        return getMessagesFromFolder(fromEmailAddress, folder, null, fileNameToSearch);
    }

    private synchronized String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/csv")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private synchronized String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append("\n").append(org.jsoup.Jsoup.parse(html).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private synchronized String parseAuthorFomMessage(Message message) throws MessagingException, UnsupportedEncodingException {
        return decode(message.getFrom()[0].toString());
    }

    private synchronized String decode(String string) throws UnsupportedEncodingException {
        return MimeUtility.decodeText(string);
    }

    private synchronized File findAttachment(Message message, String fileNameToSearch) throws IOException, MessagingException {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart == null
                    || bodyPart.getDisposition() == null
                    || !bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)
                    && StringUtils.isBlank(bodyPart.getFileName())) {
                continue;
            }
            String fileName = decode(bodyPart.getFileName());
            System.out.println("fileName = " + fileName.toLowerCase());
            if (fileNameToSearch == null || containsIgnoreCase(fileName, fileNameToSearch)) {
                String[] splitName = fileName.split("\\.");
                File file = Files.createTempFile("attachment", "." + splitName[1]).toFile();
                file.deleteOnExit();
                downloadDataToFile(file, bodyPart.getInputStream());
                return file;
            }
        }
        return null;
    }

    private synchronized File findFile(Message message, String fileNameToSearch) throws IOException, MessagingException {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart == null
                    || bodyPart.getDisposition() == null
                    || !bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)
                    && StringUtils.isBlank(bodyPart.getFileName())) {
                continue;
            }
            var fileName = decode(bodyPart.getFileName());
            System.out.println("fileName = " + fileName.toLowerCase());
            if (containsIgnoreCase(fileName, fileNameToSearch)) {
                var splitName = fileName.split("\\.");
                var file = Files.createTempFile(splitName[0], "." + splitName[1]).toFile();
                file.deleteOnExit();
                downloadDataToFile(file, bodyPart.getInputStream());
                return file;
            }
        }
        return null;
    }

    private synchronized boolean downloadDataToFile(File file, InputStream input) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[(int) file.length() + 1];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(out);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private synchronized static boolean containsIgnoreCase(String container, String element) {
        return container != null && element != null && container.toLowerCase().contains(element.toLowerCase());
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    static class YandexMessage {
        private String author;
        private String subject;
        private String text;
        private File attachment;
    }

    public void main(String[] args) throws MessagingException {
        //sendMessage("evgenykuzin21@gmail.com", "Test", "Hello from NuSeller!");
        //getFileFrom("@zooexpress.ru", "Поставщики", "suppliers_products", "zooekspress", ".xls");
    }
}
