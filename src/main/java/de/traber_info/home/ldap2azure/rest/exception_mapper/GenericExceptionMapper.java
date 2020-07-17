package de.traber_info.home.ldap2azure.rest.exception_mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Oliver Traber
 *
 * {@link ExceptionMapper} implementation used to map any {@link Exception} to a response.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class.getName());

    /**
     * Map an given {@link Exception} to a response.
     * @param ex {@link Exception} that should be mapped.
     * @return {@link Response} containing the stacktrace.
     */
    @Override
    public Response toResponse(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        LOG.error("An unexpected error occurred", ex);
        return Response
                .status(500)
                .type(MediaType.TEXT_PLAIN)
                .entity(sw.toString())
                .build();
    }

}
