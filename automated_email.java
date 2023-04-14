import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AutomatedEmail {
    private static final String EMAIL = "my_email_address";
    private static final String PASSWORD = "auth_num";

    public static void main(String[] args) {
        String temperature = getTemperature();
        if (temperature != null) {
            List<Recipient> recipients = getEmailData();
            for (Recipient recipient : recipients) {
                sendEmail(temperature, recipient.getEmail(), recipient.getName());
            }
        }
    }

    private static String getTemperature() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://wttr.in/Tokyo?format=%t")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string().trim();
            } else {
                System.err.println("Error fetching temperature");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void sendEmail(String temperature, String recipient, String recipientName) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("Automated Email");
            message.setText("Hi " + recipientName + ", how are you? Today's temperature in Japan is " + temperature + ". I will come to NY soon.");

            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    private static List<Recipient> getEmailData() {
        List<Recipient> recipients = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/export_test_database", "my_sql_username", "mySQL_Passward")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Users, `E-mail` FROM Email_database");

            while (resultSet.next()) {
                String name = resultSet.getString("Users");
                String email = resultSet.getString("E-mail");
                recipients.add(new Recipient(name, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recipients;
    }

    static class Recipient {
        private final String name;
        private final String email;

        public Recipient(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
       
                }
    }
}
