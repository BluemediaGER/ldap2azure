package de.traber_info.home.ldap2azure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

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
        LOG.info("Init phase finished successfully");
    }

}
