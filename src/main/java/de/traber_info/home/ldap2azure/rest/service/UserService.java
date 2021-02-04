package de.traber_info.home.ldap2azure.rest.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.extensions.AssignedLicense;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.PasswordProfile;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.requests.extensions.IDirectoryObjectRestoreRequest;
import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.h2.dao.UserDAOImpl;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.model.type.ChangeState;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import de.traber_info.home.ldap2azure.msgraph.CustomGraphLogger;
import de.traber_info.home.ldap2azure.msgraph.GraphClientUtil;
import de.traber_info.home.ldap2azure.rest.exception.BadRequestException;
import de.traber_info.home.ldap2azure.rest.exception.GenericException;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;
import de.traber_info.home.ldap2azure.rest.model.types.ConflictResolveStrategy;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import de.traber_info.home.ldap2azure.util.RandomString;
import jakarta.ws.rs.core.Response;

// import jakarta.ws.rs.core.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service to handle all backend user actions.
 *
 * @author Oliver Traber
 */
public class UserService {

    /** RandomString used to generate random passwords for new users */
    private static final RandomString random = new RandomString(24);

    /** Instance of the UserDAO used to access the database */
    private static final UserDAOImpl userDAO = H2Helper.getUserDao();

    /** Instance of the GraphServiceClient used to make changed in Azure AD */
    private static final IGraphServiceClient msGraphServiceClient = GraphClientUtil.getGraphServiceClient();

    /**
     * Retry the sync of an failed user.
     * @param userId Id of the user the sync should be retried for.
     * @return {@link User} object if the resync was successful, or an error with further details if it failed.
     */
    public static User retrySync(String userId) {
        User user = H2Helper.getUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        if (user.getSyncState() != SyncState.FAILED) throw new BadRequestException("user_not_failed");
        if (user.getChangeState() != ChangeState.NEW) throw new BadRequestException("user_not_new");
        retryCreateUser(user);
        return userDAO.getByAttributeMatch("id", userId);
    }

    /**
     * Retry the creation of an failed user in Azure AD.
     * @param user {@link User} the creation should be retried for.
     */
    private static void retryCreateUser(User user) {
        com.microsoft.graph.models.extensions.User azureUser = user.toAzureUser();

        PasswordProfile passwordProfile = new PasswordProfile();
        passwordProfile.forceChangePasswordNextSignIn = false;
        passwordProfile.password = random.nextString();

        azureUser.accountEnabled = true;
        azureUser.passwordProfile = passwordProfile;
        azureUser.usageLocation = ConfigUtil.getConfig().getGraphClientConfig().getUsageLocation();
        azureUser.passwordPolicies = "DisablePasswordExpiration";

        String id;
        try {
            id = msGraphServiceClient.users().buildRequest().post(azureUser).id;
        } catch (GraphServiceException ex) {
            user.setSyncState(SyncState.FAILED);
            userDAO.update(user);
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR,
                    "error_from_azure", ex.getServiceError().message);
        }

        // Assign default licence to user
        if (ConfigUtil.getConfig().getAutoLicencingConfig().isEnabled()) {
            List<AssignedLicense> addLicensesList = new ArrayList<>();
            List<UUID> removeLicensesList = new ArrayList<>();
            if (ConfigUtil.getConfig().getAutoLicencingConfig().isEnabled()) {
                for (String licenseSku : ConfigUtil.getConfig().getAutoLicencingConfig().getDefaultLicenceSkuIDs()) {
                    AssignedLicense license = new AssignedLicense();
                    license.skuId = UUID.fromString(licenseSku);
                    addLicensesList.add(license);
                }
            }
            msGraphServiceClient.users(id).assignLicense(addLicensesList, removeLicensesList).buildRequest().post();
        }

        user.setAzureImmutableId(id);
        user.setSyncState(SyncState.OK);
        user.setChangeState(ChangeState.UNCHANGED);
        userDAO.update(user);
    }

    /**
     * Get Azure users that create possible conflicts with the user of the given id.
     * @param userId Id of the user for which possible conflicts should be retrieved for.
     * @return List of Azure users who may be conflicting.
     */
    public static List<User> getPotentialConflicts(String userId) {
        User user = userDAO.getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        if (user.getSyncState() != SyncState.FAILED) throw new BadRequestException("user_not_failed");

        // Prepare OData filter expression to get user conflicts
        String queryParameters = "?$select=id,displayName,givenName,surname,onPremisesImmutableId,userPrincipalName" +
                "&$filter=onPremisesImmutableId%20eq%20%27{opid}%27%20or%20userPrincipalName%20eq%20%27{upn}%27";
        queryParameters = queryParameters.replace("{opid}",
                URLEncoder.encode(user.getOnPremisesImmutableId(), StandardCharsets.UTF_8));
        queryParameters = queryParameters.replace("{upn}",
                URLEncoder.encode(user.getUserPrincipalName(), StandardCharsets.UTF_8));

        // Query for deleted users in the trashbin
        String trashQuery = "/directory/deletedItems/microsoft.graph.user" + queryParameters;
        JsonObject apiResponse = msGraphServiceClient.customRequest(trashQuery).buildRequest().get();
        JsonArray userArray = apiResponse.getAsJsonArray("value");
        List<User> potentialConflicts = new ArrayList<>();

        for (JsonElement jsonUserElement : userArray) {
            JsonObject jsonUserObject = jsonUserElement.getAsJsonObject();
            User deletedUser = new User(null, jsonUserObject.get("onPremisesImmutableId").getAsString(),
                    jsonUserObject.get("id").getAsString(), jsonUserObject.get("givenName").getAsString(),
                    jsonUserObject.get("surname").getAsString(),jsonUserObject.get("displayName").getAsString(),
                    null,jsonUserObject.get("userPrincipalName").getAsString());
            deletedUser.setChangeState(ChangeState.DELETED);
            potentialConflicts.add(deletedUser);
        }

        // Query for existing users
        String normalQuery = "/users" + queryParameters;
        apiResponse = msGraphServiceClient.customRequest(normalQuery).buildRequest().get();
        userArray = apiResponse.getAsJsonArray("value");

        for (JsonElement jsonUserElement : userArray) {
            JsonObject jsonUserObject = jsonUserElement.getAsJsonObject();
            User matchedUser = new User(null, jsonUserObject.get("onPremisesImmutableId").getAsString(),
                    jsonUserObject.get("id").getAsString(), jsonUserObject.get("givenName").getAsString(),
                    jsonUserObject.get("surname").getAsString(),jsonUserObject.get("displayName").getAsString(),
                    null,jsonUserObject.get("userPrincipalName").getAsString());
            matchedUser.setChangeState(ChangeState.UNCHANGED);
            potentialConflicts.add(matchedUser);
        }
        return potentialConflicts;
    }

    /**
     * Resolve an user conflict by merging or recreating user in Azure AD.
     * @param internalUserId Internal id of the failed user whose conflict should be resolved.
     * @param azureUserId Id of the Azure user that should be deleted or merged to resolve the conflict.
     * @param strategy Strategy with which the conflict is to be resolved.
     * @return Updated {@link User} if the resolution of the conflict was successful.
     */
    public static User resolveConflict(String internalUserId, String azureUserId, ConflictResolveStrategy strategy) {
        User user = userDAO.getByAttributeMatch("id", internalUserId);
        if (user == null) throw new NotFoundException("user_not_existing");
        if (user.getSyncState() != SyncState.FAILED) throw new BadRequestException("user_not_failed");

        User tempCheckUser = userDAO.getByAttributeMatch("azureImmutableId", azureUserId);
        if (tempCheckUser != null) throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR,
                "azureId_already_assigned",
                "The provided Azure user id is already assigned to the internal user with id " + tempCheckUser.getId());
        if (strategy == ConflictResolveStrategy.MERGE) {
            user.setAzureImmutableId(azureUserId);
            // Restore user from trashbin. Temporarily disable logging to prevent intentional errors from being logged.
            try {
                ((CustomGraphLogger) msGraphServiceClient.getLogger()).setLogActive(false);
                IDirectoryObjectRestoreRequest request = msGraphServiceClient
                        .directory()
                        .deletedItems(azureUserId)
                        .restore()
                        .buildRequest();
                request.getHeaders().add(new HeaderOption("Content-Type", "application/json"));
                request.post();
                ((CustomGraphLogger) msGraphServiceClient.getLogger()).setLogActive(true);
            } catch (GraphServiceException ex) {
                // Do nothing. The request fails if the user is not in the trashbin.
                ((CustomGraphLogger) msGraphServiceClient.getLogger()).setLogActive(true);
            }
            // Patch user in Azure AD
            try {
                msGraphServiceClient.users(user.getAzureImmutableId()).buildRequest().patch(user.toAzureUser());
            } catch (GraphServiceException ex) {
                throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR,
                        "error_from_azure", ex.getServiceError().message);
            }
            user.setSyncState(SyncState.OK);
            user.setChangeState(ChangeState.UNCHANGED);
            user.resetLastChanged();
            // Update user in local database
            userDAO.update(user);
            return user;
        } else if (strategy == ConflictResolveStrategy.RECREATE) {
            // Delete the conflicting azure user.
            // Temporarily disable logging to prevent intentional errors from being logged.
            try {
                ((CustomGraphLogger) msGraphServiceClient.getLogger()).setLogActive(false);
                msGraphServiceClient.users(azureUserId).buildRequest().delete();
                ((CustomGraphLogger) msGraphServiceClient.getLogger()).setLogActive(true);
            } catch (GraphServiceException ex) {
                // Do nothing. The request fails if the user is already in the trashbin.
                ((CustomGraphLogger) msGraphServiceClient.getLogger()).setLogActive(true);
            }
            msGraphServiceClient.directory().deletedItems(azureUserId).buildRequest().delete();
            retryCreateUser(user);
            return userDAO.getByAttributeMatch("id", user.getId());
        }
        throw new GenericException(Response.Status.BAD_REQUEST, "strategy_not_valid",
                "The provided conflict resolve strategy is not valid.");
    }

}
