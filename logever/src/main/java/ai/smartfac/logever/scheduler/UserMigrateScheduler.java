package ai.smartfac.logever.scheduler;

import ai.smartfac.logever.config.DBPropertyLoader;
import ai.smartfac.logever.entity.Department;
import ai.smartfac.logever.entity.Role;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.repository.RoleRepository;
import ai.smartfac.logever.service.DepartmentService;
import ai.smartfac.logever.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserMigrateScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMigrateScheduler.class);
    @Autowired
    private UserService userService;

    @Value("${custom.property.file.path:user-mapping.properties}")
    private String propertyFilePath;
    @Value("${enable.user.migration:false}")
    private Boolean enableUserMigration;

    @Value("${user.save.limit:1}")
    private Integer userSaveLimit;
    @Value("${save.ldap.users:false}")
    private Boolean saveLdapUsers;

    @Value("${spring.ldap.urls:180.190.51.100}")
    private String adIpAddress;

    @Value("${spring.ldap.username:username}")
    private String ldapUserName;

    @Value("${specific.ldap.username:username}")
    private String specificLdapUserName;


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

    @Autowired
    private DepartmentService departmentService;
    private boolean disableMigrationLog = false;

    private int totalUsersSaved = 0;
    Properties properties = new Properties();

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
    //    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    //    @Scheduled(cron = "0 0 0 * * ?") // Run every 5 minutes
    //    @Scheduled(cron = "0 0 * * * ?") // Run every midnight

    @Scheduled(cron = "${cronExpression}")
    public void runScheduledTask() {
        // Your task logic goes here
        if (enableUserMigration) {
            LOGGER.info("Executing scheduled task...");
            loadPropertiesFromClasspath(propertyFilePath);
            sample();
            LOGGER.info("User migration complete");
        } else {
            if (!disableMigrationLog) {
                LOGGER.info("User migration is disabled");
                disableMigrationLog = true;
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
            LOGGER.info("Error reading properties file: " + e.getMessage());
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
                LOGGER.info("Found " + match.getName() + ":");

// Get the node's attributes
                Attributes attrs = match.getAttributes();

                NamingEnumeration<? extends Attribute> e = attrs.getAll();

// Loop through the attributes
                try {
                    while (e.hasMoreElements()) {
                        populateUserObject(user, e);
                    }
                    createOrUpdateUser(user, match);
                } catch (Exception exc) {
                    exc.printStackTrace();
                    LOGGER.info("Continuing migration script");
                }


            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void populateUserObject(User user, NamingEnumeration<? extends Attribute> e) throws NamingException {
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
//                    LOGGER.info(attr.getID() + " = " + value);

        var mappedName = properties.get(attr.getID());
        if (null != mappedName) {
            switch (mappedName.toString()) {
                case "username":
                    user.setUsername(value.toString());
                    break;
                case "email":
                    user.setEmail(value.toString());
                    break;
                case "first_name":
                    user.setFirst_name(value.toString());
                    break;
                case "last_name":
                    user.setLast_name(value.toString());
                    break;
                case "dateOfBirth":
                    user.setDateOfBirth(Date.valueOf(value.toString()));
                    break;
                case "employeeCode":
                    user.setEmployee_code(value.toString());
                    break;
                case "windows_id":
                    user.setWindows_id(value.toString());
                    break;
                case "reporting_manager":
                    var reportingManagerDN = value.toString();
                    String pattern = "CN=([^,]+)";

                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(reportingManagerDN);

                    if (m.find()) {
                        String result = m.group(1); // Extract the first capturing group
                        user.setReporting_manager(result);
                    } else {
                        user.setReporting_manager(reportingManagerDN);
                    }

                    break;
                case "designation":
                    user.setDesignation(value.toString());
                    break;
                case "department":
                    Optional<Department> departmentOptional = departmentService.getDepartmentByName(value.toString());
                    departmentOptional.ifPresent(user::setDepartment);
                    break;
                default:
                    break;
            }
        }
    }

    private void createOrUpdateUser(User user, SearchResult match) {
        if (null == user) {
            return;
        }
        if (null == user.getUsername() || "".equals(user.getUsername())) {
            LOGGER.info("Username not found for {} ", match.getName());
            return;
        }
        if (null == user.getDepartment()) {
            LOGGER.info("Department Not Found for {}", user.getUsername());
        }
        if (null != user.getUsername() && user.getUsername().length() > 0) {
            if (null == user.getDateOfBirth()) {
                user.setDateOfBirth(Date.valueOf("1990-01-01"));
            }


            if (saveLdapUsers) {
                if (userSaveLimit == -1 || totalUsersSaved < userSaveLimit) {
                    var existingUser = userService.getUserByUsername(user.getUsername());
                    if (existingUser.isPresent()) {
                        user = existingUser.get();
                    } else {
                        user.setPassword(bCryptPasswordEncoder.encode(generateRandomString(10)));
                        user.setIsActive(true);
                        user.setCreatedBy(user.getUsername());
                        user.setUpdatedBy(user.getUsername());
                        if (CollectionUtils.isEmpty(user.getRoles())) {
                            Set<Role> roles = new HashSet<>();
                            var defaultRoleDB = roleRepository.findByRole(defaultRole);
                            defaultRoleDB.ifPresent(roles::add);
                            user.setRoles(roles);
                        }
                    }

                    if (null != specificLdapUserName && !"".equals(specificLdapUserName)) {
                        if (specificLdapUserName.equalsIgnoreCase(user.getUsername())) {
                            LOGGER.info("Saving user {}", user.getUsername());
                            userService.saveUser(user);
                            totalUsersSaved++;
                        }
                    } else {
                        LOGGER.info("Saving user {}", user.getUsername());
                        userService.saveUser(user);
                        totalUsersSaved++;
                    }

                } else {
                    LOGGER.info("User save limit is reached totalUsersSaved {} userSaveLimit {}", totalUsersSaved, userSaveLimit);
                }

            } else {
                LOGGER.info("Save LDAP Users is disabled");
            }


        }
    }


}
