package de.traber_info.home.ldap2azure.rest.server;

import de.traber_info.home.ldap2azure.model.config.WebConfig;
import de.traber_info.home.ldap2azure.rest.RestApplication;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
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
        jetty = new Server();

        HandlerList handlerList = new HandlerList();

        LOG.info("Management web server will be serving HTTP requests on port {}", config.getHttpPort());

        // Create HttpConfiguration for HTTP
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(config.getHttpsPort());

        // Create ServerConnector for HTTP
        ServerConnector http = new ServerConnector(jetty, new HttpConnectionFactory(httpConfiguration));
        http.setPort(config.getHttpPort());
        jetty.addConnector(http);

        // Activate HTTPs if ldap2azure.jks exists in the jarpath
        String keystoreFile = ConfigUtil.getJarPath() + "/ldap2azure.jks";
        Path keystorePath = Paths.get(keystoreFile);
        if (Files.exists(keystorePath)) {
            LOG.info("Keystore found. Management will also be available via HTTPs on port {}", config.getHttpsPort());
            initHttps(config, keystoreFile, httpConfiguration);

            // Redirect HTTP requests to HTTPs if enabled in config
            if (config.shouldRedirectHttp()) {
                LOG.info("HTTP requests will automatically be redirected to HTTPs");
                SecuredRedirectHandler securedHandler = new SecuredRedirectHandler();
                handlerList.addHandler(securedHandler);
            }
        }

        // Create ServletContextHandler to combine multiple servlets
        ServletContextHandler srvCtxHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        srvCtxHandler.setContextPath("/*");

        // Create the servlet that handles the rest api
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(new RestApplication()));
        srvCtxHandler.addServlet(jerseyServlet, "/api/*");

        //
        String frontendDirectory = ConfigUtil.getJarPath() + "/web-frontend";
        Path frontendPath = Paths.get(frontendDirectory);
        if (Files.isDirectory(frontendPath)) {
            LOG.info("Frontend folder found. Serving static content under web-root");
            // Lastly, the default servlet for static root content.
            // It is important that this is last.
            ServletHolder holderHome = new ServletHolder("default", DefaultServlet.class);
            holderHome.setInitParameter("resourceBase", frontendDirectory);
            holderHome.setInitParameter("dirAllowed","false");
            holderHome.setInitParameter("pathInfoOnly","true");
            holderHome.setInitParameter("welcomeFiles", "index.html");
            srvCtxHandler.addServlet(holderHome,"/*");
        }

        handlerList.addHandler(srvCtxHandler);

        jetty.setHandler(handlerList);

        // Start the server thread
        try {
            jetty.start();
            LOG.info("Management web server started successfully");
        } catch (Exception ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Enable HTTPs on Jetty using the given keystore.
     * @param config Config to get the keystore password.
     * @param keystoreFile Path to the keystore file.
     * @param httpConfiguration HttpConfiguration used as a base for the HTTPs configuration.
     */
    private static void initHttps(WebConfig config, String keystoreFile, HttpConfiguration httpConfiguration) {
        // Create SslContextFactory for HTTPs requests
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystoreFile);
        sslContextFactory.setKeyStorePassword(config.getKeystorePassword());

        // Create HttpConfiguration for HTTPs
        HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

        // Create ServerConnector for HTTPs
        ServerConnector httpsConnector = new ServerConnector(jetty,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(config.getHttpsPort());
        jetty.addConnector(httpsConnector);
    }

    /**
     * Cleanly shutdown the embedded Jetty.
     */
    public static void stop() {
        try {
            jetty.stop();
        } catch (Exception ex) {
            // fail silently
        }
    }

}
