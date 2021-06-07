package evgenykuzin.core.util_managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class FileManager {
    public static File getFromResources(String name) {
        var str = String.format("%s/%s", getResourcesDir().getAbsolutePath(), name);
        return new File(str);
    }

    public static File getOrCreateIfNotExist(String name, String suffix, boolean deleteOnExit) {
        File file;
        file = getFromResources(name+suffix);
        if (!file.exists()) {
            try {
                file = File.createTempFile(name, suffix, getResourcesDir());
                if (deleteOnExit) {
                    file.deleteOnExit();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File getOrCreateIfNotExist(String name, String suffix) {
        return getOrCreateIfNotExist(name, suffix, true);
    }

    public static boolean containsInResources(String name) {
        var res = getFromResources("");
        var resFiles = res.listFiles();
        if ((resFiles != null ? resFiles.length : 0) > 0) {
            for (File file : resFiles) {
                if (file.getName().contains(name)) return true;
            }
        }
        return false;
    }

    public static File download(String urlString, String fileName, String suffix) throws IOException {
        Path path = Files.createTempFile(fileName, suffix);
        return download(urlString, path.toFile());
    }

    public static File download(String urlString, File file) throws IOException {
        URL url = new URL(urlString);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        return file;
    }

    public static List<String> readFile(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void writeToFile(File file, String text) {
        try {
            Files.writeString(file.toPath(), text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeNextToFile(File file, String string) {
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            var line = string.contains("\n") ? string : string + "\n";
            fos.write(line.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getResourcesDir() {
        var path = String.format("%s/src/main/resources", System.getProperty("user.dir"));
        return new File(path);
    }
}
