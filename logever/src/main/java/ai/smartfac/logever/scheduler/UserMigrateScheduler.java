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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.*;

@Component
public class UserMigrateScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBPropertyLoader.class);
    @Autowired
    private UserService userService;

    @Value("${custom.property.file.path:user-mapping.properties}")
    private String propertyFilePath;
    @Value("${enable.user.migration:false}")
    private Boolean enableUserMigration;

    @Value("${spring.ldap.urls:180.190.51.100}")
    private String adIpAddress;

    @Value("${spring.ldap.username:username}")
    private String ldapUserName;

    @Value("${spring.ldap.password:password}")
    private String ldapPassword;

    @Value("${spring.ldap.base:DC=JUBLCORP,DC=COM}")
    private String baseDn;

    @Value("${default_role:ROLE_EUAM_USER}")
    private String defaultRole;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    private boolean disableMigrationLog=false;
    Properties properties = new Properties();
    //    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    //    @Scheduled(cron = "0 0 0 * * ?") // Run every 5 minutes
    //    @Scheduled(cron = "0 0 * * * ?") // Run every midnight

    @Scheduled(cron = "${cronExpression}")
    public void runScheduledTask() {
        // Your task logic goes here
        if(enableUserMigration){
            LOGGER.info("Executing scheduled task...");
            loadPropertiesFromClasspath(propertyFilePath);
            sample();
        }else{
            if(!disableMigrationLog){
                LOGGER.info("User migration is disabled");
                disableMigrationLog=true;
            }

        }

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
            // Set LDAP server address and port
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, adIpAddress);

// Specify authentication credentials if needed
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, ldapUserName);
            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

// Get the initial context
            InitialDirContext ctx = new InitialDirContext(env);

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

// Search for items with the specified attribute starting
// at the top of the search tree
            NamingEnumeration<SearchResult> objs = ctx.search(
                    baseDn,
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

                    StringBuilder value = new StringBuilder();
                    for (int i = 0; i < attr.size(); i++) {
                        if (i > 0) {
                            value.append(",");
                        }
                        ;
                        value.append(attr.get(i));
                    }
                    System.out.println(attr.getID() + " = "+ value);

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
                        var defaultRoleDB=roleRepository.findByRole(defaultRole);
                        defaultRoleDB.ifPresent(roles::add);
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
