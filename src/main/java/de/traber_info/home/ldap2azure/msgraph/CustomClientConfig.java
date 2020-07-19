package de.traber_info.home.ldap2azure.msgraph;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.IExecutors;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.http.IHttpProvider;
import com.microsoft.graph.logger.ILogger;
import com.microsoft.graph.serializer.ISerializer;

/**
 * Custom implementation of {@link IClientConfig} which allows the use of slf4j/logback.
 *
 * @author Oliver Traber
 */
public class CustomClientConfig implements IClientConfig {

    /** Implementation of the {@link IAuthenticationProvider} used to authenticate requests */
    private IAuthenticationProvider authenticationProvider;
    /** Implementation of {@link IExecutors} that should be used */
    private IExecutors executors;
    /** Implementation of {@link IHttpProvider} that should be used */
    private IHttpProvider httpProvider;
    /** Implementation of {@link ILogger} that should be used */
    private ILogger logger;
    /** Implementation of {@link ISerializer} that should be used */
    private ISerializer serializer;

    /**
     * Create a new {@link IClientConfig} using an custom {@link IAuthenticationProvider} and the slf4j logger.
     * @param authenticationProvider {@link IAuthenticationProvider} that should be used.
     */
    public CustomClientConfig(IAuthenticationProvider authenticationProvider) {
        IClientConfig defaultConfig = DefaultClientConfig.createWithAuthenticationProvider(
                authenticationProvider
        );
        this.authenticationProvider = defaultConfig.getAuthenticationProvider();
        this.executors = defaultConfig.getExecutors();
        this.httpProvider = defaultConfig.getHttpProvider();
        this.serializer = defaultConfig.getSerializer();
        this.logger = new CustomGraphLogger();
    }

    /**
     * Get the {@link IAuthenticationProvider} used to authenticate requests.
     * @return {@link IAuthenticationProvider} used to authenticate requests.
     */
    @Override
    public IAuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    /**
     * Get the {@link IExecutors} that should be used.
     * @return {@link IExecutors} that should be used.
     */
    @Override
    public IExecutors getExecutors() {
        return executors;
    }

    /**
     * Get the {@link IHttpProvider} that should be used.
     * @return {@link IHttpProvider} that should be used.
     */
    @Override
    public IHttpProvider getHttpProvider() {
        return httpProvider;
    }

    /**
     * Get the custom implementation of {@link ILogger} that uses the slf4j library to log messages.
     * @return Custom implementation of {@link ILogger} that uses the slf4j library to log messages.
     */
    @Override
    public ILogger getLogger() {
        return logger;
    }

    /**
     * Get the {@link ISerializer} that should be used.
     * @return {@link ISerializer} that should be used.
     */
    @Override
    public ISerializer getSerializer() {
        return serializer;
    }
}
