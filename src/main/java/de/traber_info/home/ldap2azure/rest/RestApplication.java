package de.traber_info.home.ldap2azure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.rest.model.object.ApiUser;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;
import de.traber_info.home.ldap2azure.util.RandomString;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * Main entry point for the optional ldap2azure REST api.
 *
 * @author Oliver Traber
 */
@ApplicationPath("/api/*")
public class RestApplication extends ResourceConfig {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class.getName());

    /**
     * Create an new entry point instance an initialize all components.
     */
    public RestApplication() {
        LOG.info("Starting init phase...");
        LOG.info("Registering components...");
        packages(
                "de.traber_info.home.ldap2azure.rest.filter",
                "de.traber_info.home.ldap2azure.rest.controller",
                "de.traber_info.home.ldap2azure.rest.exception_mapper"
        );
        LOG.info("Components registered successfully");
        LOG.info("Registering features and providers...");
        register(MultiPartFeature.class);
        JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        jsonProvider.setMapper(objectMapper);
        register(jsonProvider);
        LOG.info("Features and providers registered successfully");

        // Create the default api user if no users exist in the database
        createDefaultApiUserIfNotExistent();
        LOG.info("Init phase finished successfully");
    }

    /**
     * Create the default api user if no user exist in the database.
     */
    private static void createDefaultApiUserIfNotExistent() {
        if (H2Helper.getApiUserDao().getAmount() == 0) {
            String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String lower = upper.toLowerCase(Locale.ROOT);
            String numbers = "0123456789";
            String symbols = upper + lower + numbers;
            String password = new RandomString(12, new SecureRandom(), symbols).nextString();

            ApiUser defaultUser = new ApiUser("admin", password, Permission.READ_WRITE);
            H2Helper.getApiUserDao().persist(defaultUser);
            LOG.info("Created default api user --> Username: admin / Password: {}", password);
        }
    }

}
