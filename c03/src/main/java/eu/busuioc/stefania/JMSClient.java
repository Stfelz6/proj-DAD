package eu.busuioc.stefania;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JMSClient implements MessageListener {

    public void start() {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://activemq:61616");
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination topicDestination = session.createTopic("filesEnc");

            MessageConsumer topicConsumer = session.createConsumer(topicDestination);
            topicConsumer.setMessageListener(this);

            System.out.println("Waiting for messages on :61616 ...");

        } catch (JMSException e) {
            System.err.println("Error in JMS consumer: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof BytesMessage) {
            processBytesMessage((BytesMessage) message);
        } else {
            System.err.println("Received message is not a BytesMessage");
        }
    }

    private void processBytesMessage(BytesMessage bytesMessage) {
        try {
            byte[] binaryContent = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(binaryContent);

            String operation = bytesMessage.getStringProperty("operation");
            String mode = bytesMessage.getStringProperty("mode");
            String keyBase64 = bytesMessage.getStringProperty("key");

            System.out.println("Binary content length: " + binaryContent.length);
            System.out.println("Key (Base64): " + keyBase64);


            String filename = "uploaded_file.bin";
            writeFile(binaryContent, filename);

            launchNativeProcess(filename, operation, mode, keyBase64);

        } catch (JMSException e) {
            System.err.println("Error reading binary message: " + e.getMessage());
        }
    }

    private void launchNativeProcess(String filename, String operation, String mode, String keyBase64) {
        System.err.println("Should launch here the .cpp file build, but no timeeeeeee ^.^ ");
    }

    private void printProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Native Process Output: " + line);
            }
        }
    }

    private void writeFile(byte[] binaryContent, String filename) {
        Path filePath = Paths.get(filename);
        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(binaryContent);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        JMSClient client = new JMSClient();
        client.start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
