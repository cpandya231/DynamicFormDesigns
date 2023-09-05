package ai.smartfac.logever.config;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * To  load properties from database before application starts, and make those properties accessible by @Value anywhere
 * in the project.
 *
 */
public class DBPropertyLoader implements EnvironmentPostProcessor {

    //  Name of the custom property source to be added by this post-processor class.
    private static final String PROPERTY_SOURCE_NAME = "databaseProperties";
    private static final String CONFIG_TABLE_NAME = "settings";
    private static final String CONFIG_KEY_COLUMN_NAME = "app_key";
    private static final String CONFIG_VALUE_COLUMN_NAME = "value";

    private static final Logger LOGGER = LoggerFactory.getLogger(DBPropertyLoader.class);

    private static final String[] KEYS = {
            "spring.mail.host",
            "spring.mail.port",
            "spring.mail.username",
            "spring.mail.password"
    };

    /**
     * Adds custom logic to Spring Environment. This custom logic fetches properties from database and sets it to the
     * highest precedence.
     */
    @SneakyThrows
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        Map<String, Object> propertySource = new HashMap<>();

        // Build manually datasource to ServiceConfig
        DataSource dataSource = DataSourceBuilder
                .create()
                .username(environment.getProperty("spring.datasource.username"))
                .password(environment.getProperty("spring.datasource.password"))
                .url(environment.getProperty("spring.datasource.url"))
                .driverClassName(environment.getProperty("spring.datasource.driver-class-name"))
                .build();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT %s FROM %s WHERE %s = ?;", CONFIG_VALUE_COLUMN_NAME, CONFIG_TABLE_NAME,
                             CONFIG_KEY_COLUMN_NAME))) {
            System.out.println("Running query : "+String.format("SELECT %s FROM %s WHERE %s = ?;", CONFIG_VALUE_COLUMN_NAME, CONFIG_TABLE_NAME,
                    CONFIG_KEY_COLUMN_NAME));
            for (int i = 0; i < KEYS.length; i++) {
                final int index = i;
                String key = KEYS[index];
                try {
                    preparedStatement.setString(1, key);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    // Populate all properties into the property source
                    while (resultSet.next()) {
                        propertySource.put(key, resultSet.getString(CONFIG_VALUE_COLUMN_NAME));
                        System.out.println(resultSet.getString(CONFIG_VALUE_COLUMN_NAME));
                    }
                    resultSet.close();
                    preparedStatement.clearParameters();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Unable to fetch value for key"+ e.getMessage());
                }

            }

        }
        // Creating a custom property-source with the highest precedence and adding it to Spring Environment.
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, propertySource));
    }
}