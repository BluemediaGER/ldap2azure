package de.traber_info.home.ldap2azure.msgraph;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

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
    private static IGraphServiceClient mGraphServiceClient;

    /**
     * Initialize the GraphClientUtil.
     * @param tenantId Id of the Azure AD Tenant used to contact the tenant specific OAuth2-Authority.
     * @param clientId Id of the Azure Application this tool should manipulate the Azure AD with.
     * @param clientSecret Application secret of the Azure Application this tool should manipulate the Azure AD with.
     */
    public static void init(String tenantId, String clientId, String clientSecret, NationalCloud nationalCloud) {

        ClientCredentialProvider authProvider = new ClientCredentialProvider(
                clientId, Collections.singletonList(GRAPH_DEFAULT_SCOPE),
                clientSecret, tenantId, nationalCloud);

        mGraphServiceClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .logger(new CustomGraphLogger())
                .buildClient();
    }

    /**
     * Get an instance of the Microsoft Graph service client.
     * @return Instance of the Microsoft Graph service client, that can be used to perform Graph API calls.
     */
    public static IGraphServiceClient getGraphServiceClient() {
        return mGraphServiceClient;
    }

}
