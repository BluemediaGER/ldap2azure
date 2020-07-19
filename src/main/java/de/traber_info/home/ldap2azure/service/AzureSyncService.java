package de.traber_info.home.ldap2azure.service;

import com.j256.ormlite.stmt.QueryBuilder;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.AssignedLicense;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.PasswordProfile;
import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.h2.dao.UserDAOImpl;
import de.traber_info.home.ldap2azure.model.object.Sync;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.model.type.ChangeState;
import de.traber_info.home.ldap2azure.model.type.DeleteBehavior;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import de.traber_info.home.ldap2azure.msgraph.GraphClientUtil;
import de.traber_info.home.ldap2azure.util.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service used to synchronize users and changes from the ldap2azure database to Azure AD.
 *
 * @author Oliver Traber
 */
public class AzureSyncService {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(AzureSyncService.class.getName());

    /** RandomString used to generate random passwords for new users */
    private static final RandomString random = new RandomString(24);

    /** Amount of users created by the sync */
    private long usersCreated = 0L;
    /** Amount of users changed by this sync */
    private long usersChanged = 0L;
    /** Amount of users deleted by this sync */
    private long usersDeleted = 0L;
    /** Amount of users that failed to sync */
    private long usersFailing = 0L;

    /** Instance of the UserDAO used to access the database */
    private UserDAOImpl userDAO = H2Helper.getUserDao();

    /** Instance of the GraphServiceClient used to make changed in Azure AD */
    private IGraphServiceClient msGraphServiceClient = GraphClientUtil.getGraphServiceClient();

    /**
     * Run an sync with Azure AD.
     */
    public void run() throws SQLException {
        LOG.info("Beginning sync to Azure AD..");

        String syncId = UUID.randomUUID().toString();
        LocalDateTime syncBegin = LocalDateTime.now();

        createUsers(syncId);
        updateUsers(syncId);
        deleteUsers();

        LocalDateTime syncEnd = LocalDateTime.now();

        H2Helper.getSyncDao().persist(
                new Sync(syncId, syncBegin, syncEnd, usersCreated, usersChanged, usersDeleted, usersFailing));

        LOG.info("Azure AD sync {} finished. Result: {} NEW, {} CHANGED, {} DELETED, {} FAILED",
                syncId, usersCreated, usersChanged, usersDeleted, usersFailing);

    }

    /**
     * Create new users in Azure AD that are pending for synchronization.
     * @param syncId Id of this sync. Used to set the lastSyncId attribute in the User object.
     * @throws SQLException Thrown if an error occurs while querying the database.
     */
    private void createUsers(String syncId) throws SQLException {
        // Query new users, which are pending for synchronization
        QueryBuilder<User, String> newUserQueryBuilder = userDAO.getQueryBuilder();
        newUserQueryBuilder.where()
                .eq("changeState", ChangeState.NEW.toValue())
                .and()
                .eq("syncState", SyncState.PENDING.toValue());
        List<User> newUsers = userDAO.query(newUserQueryBuilder);

        // Prepare default licence if auto licensing is enabled
        List<AssignedLicense> addLicensesList = new ArrayList<>();
        List<UUID> removeLicensesList = new ArrayList<>();
        if (ConfigUtil.getConfig().getAutoLicencingConfig().isEnabled()) {
            AssignedLicense addLicenses = new AssignedLicense();
            addLicenses.skuId = UUID.fromString(
                    ConfigUtil.getConfig().getAutoLicencingConfig().getDefaultLicenceSkuId());
            addLicensesList.add(addLicenses);
        }

        for (User user : newUsers) {
            user.setLastSyncId(syncId);
            LOG.trace("Creating user {} in Azure AD...", user.getDisplayName());
            com.microsoft.graph.models.extensions.User azureUser = user.toAzureUser();

            /* Create random 24 character long password.
               Since this tool is intended to be used in combination with an single sign-on service like Keycloak,
               the value of the password is more or less irrelevant, since it will never be used by the user. */
            PasswordProfile passwordProfile = new PasswordProfile();
            passwordProfile.forceChangePasswordNextSignIn = false;
            passwordProfile.password = random.nextString();

            // Add default settings to Azure AD user object
            azureUser.accountEnabled = true;
            azureUser.passwordProfile = passwordProfile;
            azureUser.usageLocation = ConfigUtil.getConfig().getGraphClientConfig().getUsageLocation();
            azureUser.passwordPolicies = "DisablePasswordExpiration";

            String id;
            try {
                id = msGraphServiceClient.users().buildRequest().post(azureUser).id;
            } catch (ClientException ex) {
                user.setSyncState(SyncState.FAILED);
                LOG.warn("User {} with onPremisesImmutableId {} could not be created. " +
                        "This user probably already exists in Azure AD, but not in the local database. " +
                        "If deleteBehavior SOFT is configured, " +
                        "the user may still exist in the \"Deleted Users\" section of your Azure AD Console. " +
                        "The user was marked in the database.", user.getDisplayName(), user.getOnPremisesImmutableId());
                userDAO.update(user);
                usersFailing++;
                continue;
            }

            // Assign default licence to user
            if (ConfigUtil.getConfig().getAutoLicencingConfig().isEnabled()) {
                LOG.trace("Assigning default license to user {}...", user.getDisplayName());
                msGraphServiceClient.users(id).assignLicense(addLicensesList, removeLicensesList).buildRequest().post();
            }

            user.setAzureImmutableId(id);
            user.setSyncState(SyncState.OK);
            user.setChangeState(ChangeState.UNCHANGED);
            userDAO.update(user);
            LOG.trace("User {} created successfully", user.getDisplayName());
            usersCreated++;
        }

    }

    /**
     * Update users in Azure AD that are pending for synchronization.
     * @param syncId Id of this sync. Used to set the lastSyncId attribute in the User object.
     * @throws SQLException Thrown if an error occurs while querying the database.
     */
    public void updateUsers(String syncId) throws SQLException {
        //Query changed users, which are pending for synchronization
        QueryBuilder<User, String> changedUserQueryBuilder = userDAO.getQueryBuilder();
        changedUserQueryBuilder.where()
                .eq("changeState", ChangeState.CHANGED.toValue())
                .and()
                .eq("syncState", SyncState.PENDING.toValue());
        List<User> changedUsers = userDAO.query(changedUserQueryBuilder);

        for (User user : changedUsers) {
            user.setLastSyncId(syncId);
            // Patch user in Azure AD
            msGraphServiceClient.users(user.getAzureImmutableId()).buildRequest().patch(user.toAzureUser());
            user.setSyncState(SyncState.OK);
            user.setChangeState(ChangeState.UNCHANGED);
            // Update user in local database
            userDAO.update(user);
            usersChanged++;
        }

    }

    /**
     * Delete users from Azure AD that are pending for synchronization.
     * @throws SQLException Thrown if an error occurs while querying the database.
     */
    public void deleteUsers() throws SQLException {
        // Query deleted users, which are pending for synchronization
        QueryBuilder<User, String> deletedUserQueryBuilder = userDAO.getQueryBuilder();
        deletedUserQueryBuilder.where()
                .eq("changeState", ChangeState.DELETED.toValue())
                .and()
                .eq("syncState", SyncState.PENDING.toValue());
        List<User> deletedUsers = userDAO.query(deletedUserQueryBuilder);

        for (User user : deletedUsers) {
            // Delete user from Azure AD
            msGraphServiceClient.users(user.getAzureImmutableId()).buildRequest().delete();

            // Completely remove user if configured
            if (ConfigUtil.getConfig().getGraphClientConfig().getDeleteBehavior() == DeleteBehavior.HARD) {
                msGraphServiceClient.directory().deletedItems(user.getAzureImmutableId()).buildRequest().delete();
            }
            userDAO.delete(user);
            usersDeleted++;
        }

    }

}
