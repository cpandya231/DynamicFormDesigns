package ai.smartfac.logever.scheduler;

import ai.smartfac.logever.config.DBPropertyLoader;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.RoleRepository;
import ai.smartfac.logever.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.*;

@Component
public class UserMigrateScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBPropertyLoader.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserService userService;

    @Value("${custom.property.file.path:user-mapping.properties}")
    private String propertyFilePath;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    Properties properties = new Properties();

    //    @Scheduled(cron = "0 0 0 * * ?") // Run every 5 minutes
    //    @Scheduled(cron = "0 0 * * * ?") // Run every midnight
    @Scheduled(cron = "0 0 * * * ?")
    public void runScheduledTask() {
        // Your task logic goes here
        LOGGER.info("Executing scheduled task...");
        loadPropertiesFromClasspath(propertyFilePath);
        sample();
    }
    public Properties loadPropertiesFromClasspath(String fileName) {

        try {
            // Load the resource file from the classpath
            Resource resource = new ClassPathResource(fileName);

            // Open an input stream to the resource
            InputStream inputStream = resource.getInputStream();

            // Load the properties from the input stream
            properties.load(inputStream);

            // Close the input stream
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error reading properties file: " + e.getMessage());
        }
        return properties;
    }
    public void sample() {
        try {

            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

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
                User user = new User();
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
                    StringBuilder value = new StringBuilder();
                    for (int i = 0; i < attr.size(); i++) {
                        if (i > 0) {
                            value.append(",");
                        }
                        ;
                        value.append(attr.get(i));
                    }

                    var mappedName = properties.get(attr.getID());
                    if(null !=mappedName){
                        switch (mappedName.toString())
                        {
                            case "username":
                                user.setUsername(value.toString());
                                user.setEmployee_code(value.toString());
                                break;
                            case "first_name":
                                user.setFirst_name(value.toString());
                            default:
                                break;
                        }
                    }
                }
                if(null!=user.getUsername() && user.getUsername().length()>0){
                    user.setDateOfBirth(Date.valueOf("1990-01-01"));
                    user.setPassword(bCryptPasswordEncoder.encode("password"));
                    user.setIsActive(true);
                    user.setCreatedBy(user.getUsername());
                    user.setUpdatedBy(user.getUsername());
                    if(CollectionUtils.isEmpty(user.getRoles())){
                        Set<Role> roles = new HashSet<>();
                        roles.add(roleRepository.findById(-1).get());
                        user.setRoles(roles);
                    }
                    var existingUser=userService.getUserByUsername(user.getUsername());
                    existingUser.ifPresent(value -> user.setId(value.getId()));
                    userService.saveUser(user);

                }

            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }


}
