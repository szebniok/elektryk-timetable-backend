package com.konradbochnia;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TimetableService {
    
    private static final String URL = "http://elektryk.edupage.org/gcall";
    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;
    private static final Logger LOG = LoggerFactory.getLogger(TimetableService.class);
    
    private static final Pattern TIMETABLE_PATTERN = Pattern.compile("jsc_timetable\\.obj\\.loadVersion\\(\"([\\d]+)\"");
    private static final Pattern SUBSTITUTIONS_PATTER = Pattern.compile("obj\\.reloadRows\\(\"\\d{4}-\\d{2}-\\d{2}\",([^)]+)\\)");
    
    private final NotificationsService notificationService;
    private final CloseableHttpClient client;

    public TimetableService(NotificationsService notificationService, CloseableHttpClient client) {
        this.notificationService = notificationService;
        this.client = client;
    }
    
    private Optional<String> version = Optional.empty();
    private Optional<String> substitutionsData = Optional.empty();
    
    @Scheduled(fixedRate = 30 * MINUTE) 
    public void checkForNewVersion() {
        LOG.info("Checking for updates...");
        
        evictClasses();
        try {
            String downloadedData = getClassesAndVersion();
            Matcher matcher = TIMETABLE_PATTERN.matcher(downloadedData);
            matcher.find();
            Optional<String> newVersion = Optional.of(matcher.group(1));

            if (version.isPresent() && !version.equals(newVersion)) {
                LOG.info("Found update");
                
                notificationService.sendTimetableNotification();
                cacheEvict();
            }
            version = newVersion;

            LOG.info("Latest timetable version: {}", newVersion);
        } catch (IOException e) {
            LOG.error("Error while checking for updates", e);
        }
    }
    
    @Scheduled(fixedRate = 10 * MINUTE)
    public void checkForSubstitutions() {
        LOG.info("Checking for new substitutions");
        
        evictSubstitutions();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        try {
            String downloadedData = getSubstitutions(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));
            Matcher matcher = SUBSTITUTIONS_PATTER.matcher(downloadedData);
            matcher.find();
            String newSubstitutionsData = matcher.group(1);

            if (substitutionsData.isPresent() && newSubstitutionsData.length() > 2 && 
                    !substitutionsData.get().equals(newSubstitutionsData)) {
                LOG.info("Changes in substitutions data");

                notificationService.sendSubstitutionsNotification();
            }

            substitutionsData = Optional.of(newSubstitutionsData);
        } catch (IOException e) {
            LOG.error("Error while checking for new substitutions", e);
        }
    }
    
    @CacheEvict(value = {"classes", "substitutions", "lessons"}, allEntries = true)
    private void cacheEvict() {}
    
    @CacheEvict(value = "classes", allEntries = true)
    private void evictClasses() {}
    
    @CacheEvict(value = "substitutions", allEntries = true)
    private void evictSubstitutions() {}
    
    @Cacheable("classes")
    public String getClassesAndVersion() throws IOException {
        HttpPost httpPost = new HttpPost(URL);
        
        List<NameValuePair> params = new ArrayList<>();
        populateWithCommonParams(params);
        params.add(new BasicNameValuePair("gadget", "MobileEdupage"));
        params.add(new BasicNameValuePair("action", "globalReload"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
    
    @Cacheable("substitutions")
    public String getSubstitutions(String date) throws IOException {
        HttpPost httpPost = new HttpPost(URL);
        
        List<NameValuePair> params = new ArrayList<>();
        populateWithCommonParams(params);
        params.add(new BasicNameValuePair("gadget", "MobileSubstBrowser"));
        params.add(new BasicNameValuePair("action", "date_reload"));
        params.add(new BasicNameValuePair("date", date));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    @Cacheable("lessons")
    public String getLessons(String num, String id) throws IOException {
        HttpPost httpPost = new HttpPost(URL);
            
        List<NameValuePair> params = new ArrayList<>();
        populateWithCommonParams(params);
        params.add(new BasicNameValuePair("gadget", "MobileTimetableBrowser"));
        params.add(new BasicNameValuePair("action", "reload"));
        params.add(new BasicNameValuePair("oblast", "trieda"));
        params.add(new BasicNameValuePair("num", num));
        params.add(new BasicNameValuePair("id", id));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
    
    private void populateWithCommonParams(List<NameValuePair> params) {
        params.add(new BasicNameValuePair("jscid", "gi34476"));
        params.add(new BasicNameValuePair("gsh", "6bcf1a53"));
        params.add(new BasicNameValuePair("_LJSL", "2048"));
    }
}
