package eu.busuioc.stefania;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Frontend {

    private static final Object fileLock = new Object();
    public static void main(String[] args) {
        Javalin app = initializeServer();
        configureRoutes(app);
        app.start(7000);
    }

    private static Javalin initializeServer() {
        return Javalin.create(config -> {
            config.staticFiles.add("./staticHTML", Location.EXTERNAL);
        });
        
    }

    private static void configureRoutes(Javalin app) {
        app.get("/file", ctx -> ctx.redirect("/uploadFile.html"));
        app.post("/upload", Frontend::handleFileUpload);
    }

    private static void handleFileUpload(io.javalin.http.Context ctx) {
        List<UploadedFile> uploadedFiles = ctx.uploadedFiles("files");
        if (uploadedFiles.isEmpty()) {
            ctx.html("No file uploaded");
            return;
        }

        String operation = ctx.formParam("operation");
        String mode = ctx.formParam("mode");
        String keyBase64 = ctx.formParam("key");

        if (operation == null || mode == null || keyBase64 == null) {
            ctx.html("Operation, mode, and key parameters are required");
            return;
        }

        SecretKey secretKey = decodeKey(keyBase64);
        UploadedFile uploadedFile = uploadedFiles.get(0);
        String uploadedFilePath = "upload/" + uploadedFile.filename();

        if (!saveUploadedFile(uploadedFile, uploadedFilePath)) {
            ctx.html("Error saving uploaded file");
            return;
        }

        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(uploadedFilePath));
            sendToBroker(fileContent, operation, mode, keyBase64);
            ctx.html("File processed and sent to broker successfully");
        } catch (IOException e) {
            ctx.html("Error during file processing: " + e.getMessage());
        }
    }

    private static SecretKey decodeKey(String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    private static boolean saveUploadedFile(UploadedFile uploadedFile, String uploadedFilePath) {
        synchronized (fileLock) {
            try (FileOutputStream os = new FileOutputStream(new File(uploadedFilePath))) {
                os.write(uploadedFile.content().readAllBytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static void sendToBroker(byte[] fileContent, String operation, String mode, String keyBase64) {
        String brokerUrl = "tcp://activemq:61616";
        String topicName = "filesEnc";
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            producer = session.createProducer(destination);

            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(fileContent);
            bytesMessage.setStringProperty("operation", operation);
            bytesMessage.setStringProperty("mode", mode);
            bytesMessage.setStringProperty("key", keyBase64);

            producer.send(bytesMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
