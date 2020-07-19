package de.traber_info.home.ldap2azure.msgraph;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Utility used to handle authentication against Azure AD and make it easier to use the Microsoft Graph API.
 *
 * @author Oliver Traber
 */
public class GraphClientUtil implements IAuthenticationProvider {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(GraphClientUtil.class.getName());

    /**
     * Default scope for calls to Microsoft Graph.
     * Since this tool is intended to run as an daemon app,
     * the actual permissions are set on <a href="https://portal.azure.com">https://portal.azure.com</a>.
     */
    private final static String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";

    /** Instance of the Microsoft Graph service client used for Graph API actions. */
    private static IGraphServiceClient mGraphServiceClient;

    /** Tenant specific authorisation authority */
    private static String tenantSpecificAuthority;

    /** Client id of the Azure AD application ldap2azure operates under. */
    private static String clientId;

    /** Client secret of the Azure AD application ldap2azure operates under. */
    private static String clientSecret;

    /** Variable holding the Bearer authentication token obtained from Azure AD */
    private static String confidentialToken;

    /** Date the current token expires on */
    private static Date tokenExpiresOn;

    /**
     * Initialize the GraphClientUtil.
     * @param tenantSpecificAuthority Tenant specific OAuth2-Authority used to acquire the Bearer authentication token.
     * @param clientId Id of the Azure Application this tool should manipulate the Azure AD with.
     * @param clientSecret Application secret of the Azure Application this tool should manipulate the Azure AD with.
     */
    public static void init(String tenantSpecificAuthority, String clientId, String clientSecret) {
        GraphClientUtil.tenantSpecificAuthority = tenantSpecificAuthority;
        GraphClientUtil.clientId = clientId;
        GraphClientUtil.clientSecret = clientSecret;

        CustomClientConfig clientConfig = new CustomClientConfig(new GraphClientUtil());
        mGraphServiceClient = GraphServiceClient.fromConfig(clientConfig);
    }

    /**
     * Acquire Bearer token by using the OAuth2 Client Credential Grant Type
     * @return Current Bearer token if it is still valid, or an newly generated one if the old token was invalid.
     */
    private String getAccessTokenByClientCredentialGrant() {

        // Use cached token if it is present and still valid
        if (confidentialToken != null && tokenExpiresOn.after(new Date())) {
            LOG.trace("Using cached token");
            return confidentialToken;
        }

        LOG.trace("Lifetime of cached token exceeded. Requesting a new token...");

        ConfidentialClientApplication app;
        try {
            app = ConfidentialClientApplication.builder(
                    clientId,
                    ClientCredentialFactory.createFromSecret(clientSecret))
                    .authority(tenantSpecificAuthority)
                    .build();
        } catch (MalformedURLException ex) {
            LOG.error("An unexpected error occurred", ex);
            return null;
        }

        // With client credentials flows the scope is ALWAYS of the shape "resource/.default", as the
        // application permissions need to be set statically (in the portal), and then granted by a tenant administrator
        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters
                .builder(Collections.singleton(GRAPH_DEFAULT_SCOPE))
                .build();

        assert app != null;
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
        try {
            LOG.trace("Token is valid until {}", future.get().expiresOnDate().toString());
            tokenExpiresOn = future.get().expiresOnDate();
            confidentialToken = future.get().accessToken();
            return confidentialToken;
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return null;
    }

    /**
     * Appends an access token obtained from the client credential flow to the Authorization header of the request.
     * @param request Request to which the authorization header should be added to.
     */
    @Override
    public void authenticateRequest(IHttpRequest request) {
        try {
            request.addHeader("Authorization", "Bearer " + getAccessTokenByClientCredentialGrant());
        } catch (ClientException | NullPointerException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Get an instance of the Microsoft Graph service client.
     * @return Instance of the Microsoft Graph service client, that can be used to perform Graph API calls.
     */
    public static IGraphServiceClient getGraphServiceClient() {
        return mGraphServiceClient;
    }

}
