package eu.busuioc.stefania;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.FileOutputStream;
import java.io.IOException;

public class JMSConsumer {

    public static void main(String[] args) {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://activemq:61616");
        Connection connection = null;

        try {
            connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination topicDestination = session.createTopic("filesEnc");
            Destination queueDestination = session.createQueue("filesQueEnc");

            MessageConsumer topicConsumer = session.createConsumer(topicDestination);
            MessageConsumer queueConsumer = session.createConsumer(queueDestination);

            System.out.println("Listening for all messages on :61616...");

            MessageListener listener = message -> {
                if (message instanceof BytesMessage) {
                    processMessage((BytesMessage) message);
                } else {
                    System.err.println("Unsupported message type received");
                }
            };

            topicConsumer.setMessageListener(listener);
            queueConsumer.setMessageListener(listener);

            System.out.println("Ctrl+C to exit");
            while (true) {
                Thread.sleep(1000);
            }

        } catch (JMSException | InterruptedException e) {
            System.err.println("JMS error: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    
    private static void processMessage(BytesMessage message) {
        try {
            byte[] content = new byte[(int) message.getBodyLength()];
            message.readBytes(content);

            String operation = message.getStringProperty("operation");
            String mode = message.getStringProperty("mode");
            String key = message.getStringProperty("key");

            System.out.println("Binary content length: " + content.length);
            System.out.println("Key (Base64): " + key);

            saveItPlease(content, "output.dat");

        } catch (JMSException e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
    

    private static void saveItPlease(byte[] content, String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(content);
        } catch (IOException e) {
            System.err.println("File writing error: " + e.getMessage());
        }
    }
}
