package ai.smartfac.logever.scheduler;

import ai.smartfac.logever.config.DBPropertyLoader;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.UserService;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMigrateScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBPropertyLoader.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserService userService;
    @Value("${user.migrate.url}")
    private String userMigrateUrl;

    @Scheduled(cron = "0 0 0 * * ?") // Run every 5 minutes
    public void runScheduledTask() {
        // Your task logic goes here
        LOGGER.info("Executing scheduled task...");
        List<User> users = getUsers();
        users.forEach(user -> userService.saveUser(user));
    }

    public List<User> getUsers() {
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                    userMigrateUrl,  // Replace with your fake URL
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });
            LOGGER.info("Got response status code {} body {}",response.getStatusCode(), response.getBody());
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error occured while fetching users from {}", userMigrateUrl);
            e.printStackTrace();
        }
        return new ArrayList<>();

    }
}
