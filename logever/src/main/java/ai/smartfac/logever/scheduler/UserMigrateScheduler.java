package ai.smartfac.logever.scheduler;

import ai.smartfac.logever.config.DBPropertyLoader;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.UserService;
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

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
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

    //    @Scheduled(cron = "0 0 0 * * ?") // Run every 5 minutes
    @Scheduled(fixedRate = 5000)
    public void runScheduledTask() {
        // Your task logic goes here
        LOGGER.info("Executing scheduled task...");
//        List<User> users = getUsers();
//        users.forEach(user -> userService.saveUser(user));
        sample();
    }

    public void sample() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        try {
// Get the initial context
            InitialDirContext ctx = new InitialDirContext();

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

// Search for items with the specified attribute starting
// at the top of the search tree
            NamingEnumeration<SearchResult> objs = ctx.search(
                    "dc=springframework,dc=org",
                    "(objectClass=*)", searchControls);

// Loop through the objects returned in the search
            while (objs.hasMoreElements()) {
// Each item is a SearchResult object
                SearchResult match = (SearchResult) objs.nextElement();

// Print out the node name
                System.out.println("Found " + match.getName() + ":");

// Get the node's attributes
                Attributes attrs = match.getAttributes();

                NamingEnumeration<? extends Attribute> e = attrs.getAll();

// Loop through the attributes
                while (e.hasMoreElements()) {
// Get the next attribute
                    Attribute attr = (Attribute) e.nextElement();

// Print out the attribute's value(s)
                    System.out.print(attr.getID() + " = ");
                    for (int i = 0; i < attr.size(); i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(attr.get(i));
                    }
                    System.out.println();
                }
                System.out.println("---------------------------------------");
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public List<User> getUsers() {
        try {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                    userMigrateUrl,  // Replace with your fake URL
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });
            LOGGER.info("Got response status code {} body {}", response.getStatusCode(), response.getBody());
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Error occured while fetching users from {}", userMigrateUrl);
            e.printStackTrace();
        }
        return new ArrayList<>();

    }
}
