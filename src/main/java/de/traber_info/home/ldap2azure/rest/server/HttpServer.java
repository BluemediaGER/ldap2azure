package de.traber_info.home.ldap2azure.rest.server;

import de.traber_info.home.ldap2azure.model.config.WebConfig;
import de.traber_info.home.ldap2azure.rest.RestApplication;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to handle all actions around the embedded Jetty server.
 *
 * @author Oliver Traber
 */
public class HttpServer {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class.getName());

    /** Embedded Jetty application server */
    private static Server jetty;

    /**
     * Start the internal Jetty server. This also checks if an folder named "web-frontend" exists in the jarpath.
     * If that's the case, Jetty will serve the content within this folder under the root domain.
     */
    public static void start() {
        // Initialize rest api and web-based management interface if enabled in the config file
        WebConfig config = ConfigUtil.getConfig().getWebConfig();
        // Create embedded Jetty server
        jetty = new Server(config.getPort());

        ServletContextHandler srvCtxHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        srvCtxHandler.setContextPath("/*");

        // Create the servlet that handles the rest api
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(new RestApplication()));
        srvCtxHandler.addServlet(jerseyServlet, "/api/*");

        String frontendDirectory = ConfigUtil.getJarPath() + "/web-frontend";
        Path frontendPath = Paths.get(frontendDirectory);
        if (Files.isDirectory(frontendPath)) {
            LOG.info("[Http-Management] Frontend folder found. Serving static content under web-root.");
            // Lastly, the default servlet for static root content.
            // It is important that this is last.
            ServletHolder holderHome = new ServletHolder("default", DefaultServlet.class);
            holderHome.setInitParameter("resourceBase", frontendDirectory);
            holderHome.setInitParameter("dirAllowed","false");
            holderHome.setInitParameter("pathInfoOnly","true");
            holderHome.setInitParameter("welcomeFiles", "index.html");
            srvCtxHandler.addServlet(holderHome,"/*");
        }

        jetty.setHandler(srvCtxHandler);

        // Start the server thread
        try {
            jetty.start();
            LOG.info("[Http-Management] Management interface and rest api successfully started on port {}",
                    config.getPort());
        } catch (Exception ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Cleanly shutdown the embedded Jetty.
     */
    public static void stop() {
        try {
            jetty.stop();
        } catch (Exception ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

}
