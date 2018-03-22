package com.konradbochnia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationsService {
    
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsService.class);
    private static final String REGISTER_URL = "https://iid.googleapis.com/iid/v1/%s/rel/topics/%s";
    private static final String SEND_URL = "https://fcm.googleapis.com/fcm/send";
    
    private static final Notification SUBSTITUTIONS_UPDATE_NOTIFICATION = 
            new Notification("Nowe zastępstwa", "Sprawdź zmiany", "updates");
    private static final Notification TIMETABLE_UPDATE_NOTIFICATION =
            new Notification("Nowy plan lekcji", "Sprawdź zmiany", "updates");
    
    private final Header[] HEADERS;
    private final ObjectMapper mapper = new ObjectMapper();
    
    private final CloseableHttpClient client;

    public NotificationsService(CloseableHttpClient client, @Value("${FIREBASE_AUTH}") final String firebase_auth) {
        this.client = client;
        
        HEADERS = new Header[] {
            new BasicHeader(HttpHeaders.AUTHORIZATION, firebase_auth),
            new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
        };
    }
    
    
    public void subscribe(String subject, String token) {
        LOG.info("Registered user");
        
        HttpPost httpPost = new HttpPost(String.format(REGISTER_URL, token, subject));
        httpPost.setHeaders(HEADERS);
        
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            LOG.info("status: " + response.getStatusLine().getStatusCode());
        } catch (IOException ex) {
            LOG.error("Problem with connection", ex);
            throw new RuntimeException(ex);
        }
    }
    
    public void sendTimetableNotification() {
        sendNotification(TIMETABLE_UPDATE_NOTIFICATION);
    }
    
    public void sendSubstitutionsNotification() {
        sendNotification(SUBSTITUTIONS_UPDATE_NOTIFICATION);
    }
    
    private void sendNotification(Notification notification) {
        LOG.info("Sended notifications");
        
        String json;
        try {
            json = mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            LOG.error("Error while encoding the notification to JSON", e);
            return;
        }
        
        HttpPost httpPost = new HttpPost(SEND_URL);
        httpPost.setHeaders(HEADERS);
        httpPost.setEntity(new StringEntity(json, "UTF-8"));
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            LOG.info("status: " + response.getStatusLine().getStatusCode());
        } catch(IOException e) {
            LOG.error("Failed with sending notifications", e);
        }
    }
    
    
}
