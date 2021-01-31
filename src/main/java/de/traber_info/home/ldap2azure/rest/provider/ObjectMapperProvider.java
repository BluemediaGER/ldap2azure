package de.traber_info.home.ldap2azure.rest.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provider to create and configure the Jackson {@link ObjectMapper} used by JakartaEE.
 *
 * @author Oliver Traber
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    /** {@link ObjectMapper} for serialisation / deserialization */
    private final ObjectMapper defaultObjectMapper;

    /**
     * Default constructor that creates an new {@link ObjectMapper}.
     */
    public ObjectMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
    }

    /**
     * Getter for the {@link ObjectMapper}.
     * @param type Class type that should be returned (only {@link ObjectMapper} in this case).
     * @return {@link ObjectMapper} for further use by JakartaEE.
     */
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return defaultObjectMapper;
    }

    /**
     * Method for configuration of the default {@link ObjectMapper}.
     */
    private static ObjectMapper createDefaultMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }

}
