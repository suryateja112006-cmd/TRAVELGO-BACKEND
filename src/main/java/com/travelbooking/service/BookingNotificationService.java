package com.travelbooking.service;

import com.travelbooking.entity.Booking;
import com.travelbooking.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

@Service
public class BookingNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationService.class);

    private final Map<String, String> environment;
    private final HttpClient httpClient;

    public BookingNotificationService() {
        this(System.getenv(), HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }

    BookingNotificationService(Map<String, String> environment, HttpClient httpClient) {
        this.environment = environment;
        this.httpClient = httpClient;
    }

    public record DeliveryResult(String email, String sms) {
    }

    public DeliveryResult sendBookingConfirmation(Booking booking) {
        return new DeliveryResult(sendEmail(booking), sendSms(booking));
    }

    private String sendEmail(Booking booking) {
        User customer = booking.getUser();
        String host = value("TRAVELGO_SMTP_HOST");
        String username = value("TRAVELGO_SMTP_USERNAME");
        String password = value("TRAVELGO_SMTP_PASSWORD");
        String from = value("TRAVELGO_SMTP_FROM");
        if (host.isEmpty() || username.isEmpty() || password.isEmpty() || from.isEmpty()) {
            return "not_configured";
        }
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            return "no_recipient";
        }

        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(integerValue("TRAVELGO_SMTP_PORT", 587));
            sender.setUsername(username);
            sender.setPassword(password);
            sender.setProtocol("smtp");
            Properties mailProperties = sender.getJavaMailProperties();
            mailProperties.put("mail.smtp.auth", "true");
            mailProperties.put("mail.smtp.starttls.enable", booleanValue("TRAVELGO_SMTP_STARTTLS", true));
            mailProperties.put("mail.smtp.connectiontimeout", "5000");
            mailProperties.put("mail.smtp.timeout", "5000");
            mailProperties.put("mail.smtp.writetimeout", "5000");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(customer.getEmail());
            message.setSubject("TravelGo booking confirmed #" + booking.getId());
            message.setText(emailBody(booking));
            sender.send(message);
            return "sent";
        } catch (Exception exception) {
            logger.warn("Could not deliver booking confirmation email for booking {}", booking.getId());
            return "failed";
        }
    }

    private String sendSms(Booking booking) {
        String accountSid = value("TRAVELGO_TWILIO_ACCOUNT_SID");
        String authToken = value("TRAVELGO_TWILIO_AUTH_TOKEN");
        String fromNumber = value("TRAVELGO_TWILIO_FROM_NUMBER");
        if (accountSid.isEmpty() || authToken.isEmpty() || fromNumber.isEmpty()) {
            return "not_configured";
        }
        User customer = booking.getUser();
        String phone = customer == null ? "" : normalizePhone(customer.getPhone());
        if (phone.isEmpty()) {
            return "no_recipient";
        }

        try {
            String endpoint = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            String form = "To=" + encode(phone)
                    + "&From=" + encode(fromNumber)
                    + "&Body=" + encode(smsBody(booking));
            String authorization = Base64.getEncoder().encodeToString(
                    (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Basic " + authorization)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "sent";
            }
            logger.warn("SMS provider returned status {} for booking {}", response.statusCode(), booking.getId());
            return "failed";
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.warn("SMS delivery was interrupted for booking {}", booking.getId());
            return "failed";
        } catch (Exception exception) {
            logger.warn("Could not deliver booking confirmation SMS for booking {}", booking.getId());
            return "failed";
        }
    }

    private String emailBody(Booking booking) {
        User customer = booking.getUser();
        String customerName = customer == null ? "traveler" : customer.getName();
        String packageName = booking.getTravelPackage().getName();
        return "Hello " + customerName + ",\n\n"
                + "Your TravelGo booking is confirmed.\n"
                + "Booking: #" + booking.getId() + "\n"
                + "Package: " + packageName + "\n"
                + "Travel date: " + booking.getTravelDate() + "\n"
                + "Travelers: " + booking.getNumberOfPersons() + "\n"
                + "Total: INR " + booking.getTotalAmount() + "\n\n"
                + "Thank you for booking with TravelGo.";
    }

    private String smsBody(Booking booking) {
        return "TravelGo: booking #" + booking.getId() + " confirmed for "
                + booking.getTravelPackage().getName() + " on " + booking.getTravelDate()
                + ". Travelers: " + booking.getNumberOfPersons()
                + ". Total INR " + booking.getTotalAmount() + ".";
    }

    private String normalizePhone(String phone) {
        if (phone == null)
            return "";
        String normalized = phone.trim().replaceAll("[\\s()-]", "");
        if (normalized.matches("[6-9]\\d{9}"))
            return "+91" + normalized;
        if (normalized.matches("\\+[1-9]\\d{7,14}"))
            return normalized;
        return "";
    }

    private String value(String key) {
        return environment.getOrDefault(key, "").trim();
    }

    private int integerValue(String key, int defaultValue) {
        try {
            return Integer.parseInt(value(key));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private boolean booleanValue(String key, boolean defaultValue) {
        String configured = value(key);
        return configured.isEmpty() ? defaultValue : Boolean.parseBoolean(configured);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}