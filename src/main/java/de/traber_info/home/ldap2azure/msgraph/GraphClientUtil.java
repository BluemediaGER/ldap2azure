package de.traber_info.home.ldap2azure.msgraph;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import java.util.Collections;

/**
 * Utility used to handle authentication against Azure AD and make it easier to use the Microsoft Graph API.
 *
 * @author Oliver Traber
 */
public class GraphClientUtil {

    /**
     * Default scope for calls to Microsoft Graph.
     * Since this tool is intended to run as an daemon app,
     * the actual permissions are set on <a href="https://portal.azure.com">https://portal.azure.com</a>.
     */
    private final static String GRAPH_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";

    /** Instance of the Microsoft Graph service client used for Graph API actions. */
    private static GraphServiceClient mGraphServiceClient;

    /**
     * Initialize the GraphClientUtil.
     * @param tenantId Id of the Azure AD Tenant used to contact the tenant specific OAuth2-Authority.
     * @param clientId Id of the Azure Application this tool should manipulate the Azure AD with.
     * @param clientSecret Application secret of the Azure Application this tool should manipulate the Azure AD with.
     */
    public static void init(String tenantId, String clientId, String clientSecret) {

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(
                Collections.singletonList(GRAPH_DEFAULT_SCOPE),
                clientSecretCredential
        );

        mGraphServiceClient = GraphServiceClient.builder()
                .authenticationProvider(tokenCredentialAuthProvider)
                .logger(new CustomGraphLogger())
                .buildClient();
    }

    /**
     * Get an instance of the Microsoft Graph service client.
     * @return Instance of the Microsoft Graph service client, that can be used to perform Graph API calls.
     */
    public static GraphServiceClient getGraphServiceClient() {
        return mGraphServiceClient;
    }

}
