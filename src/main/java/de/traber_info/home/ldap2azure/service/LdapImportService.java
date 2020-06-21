package de.traber_info.home.ldap2azure.service;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.ldap.LdapUtil;
import de.traber_info.home.ldap2azure.model.config.LdapConfig;
import de.traber_info.home.ldap2azure.model.config.PatternConfig;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.model.type.ChangeState;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service used to import users and changes from the source ldap into the ldap2azure database.
 *
 * @author Oliver Traber
 */
public class LdapImportService {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(LdapImportService.class.getName());

    /** Regex pattern for matching ldapProperty placeholders in user build pattern */
    private static final Pattern placeholderPattern = Pattern.compile("\\{([^}]+)}");

    /** Instance of the ldap util used to get objects from the source ldap server */
    private static LdapUtil ldapUtil;

    /**
     * Run an import from the source ldap server.
     */
    public static void run() {
        LdapConfig ldapConfig = ConfigUtil.getConfig().getLdapConfig();
        try {
            ldapUtil = new LdapUtil(ldapConfig.getLdapUrl(), ldapConfig.getBindUser(), ldapConfig.getBindPassword(),
                    ldapConfig.getSearchBase(), ldapConfig.getSearchFilter(), ldapConfig.getLdapAttributes(),
                    ldapConfig.isIgnoreSSLErrors());

            Map<String, User> ldapUsers = getLdapUsers();
            updateDatabase(ldapUsers);
        } catch (NamingException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Build actual user attribute value from given pattern and retrieved ldap object.
     * @param parsable Pattern that should be converted to full user attribute value.
     * @param result Result of the LDAP search. This is used to read the actual attribute values.
     * @return Final value that can be set as an user attribute on an user object.
     * @throws NamingException Thrown if reading of an LDAP attribute is unsuccessful.
     */
    private static String getRegexReplacedString(String parsable, SearchResult result) throws NamingException {
        String safeParsable = parsable;
        Matcher matcher = placeholderPattern.matcher(parsable);
        while (matcher.find()) {
            String attributeName = matcher.group(1);
            Object replacement = result.getAttributes().get(attributeName).get();
            String replacementString;
            if (replacement instanceof byte[]) {
                replacementString = Base64.getEncoder().encodeToString((byte[]) replacement);
            } else {
                replacementString = replacement.toString();
            }
            LOG.trace("Replacing placeholder {{}} with actual value {}", attributeName, replacementString);
            safeParsable = safeParsable.replace("{" + attributeName + "}", replacementString);
        }
        return safeParsable;
    }

    /**
     * Read all users matching the specified parameters from the LDAP server and convert them to Azure user objects.
     * @return Map containing the Azure onPremisesImmutableId as the key and the full converted Azure user object as the value.
     * @throws NamingException Thrown if errors occur while retrieving the objects from the LDAP server.
     */
    private static Map<String, User> getLdapUsers() throws NamingException {
        Map<String, User> ldapUsers = new HashMap<>();
        NamingEnumeration<SearchResult> ldapSearchResults = ldapUtil.search();
        while (ldapSearchResults.hasMore()) {
            SearchResult result = ldapSearchResults.next();

            PatternConfig patternConfig = ConfigUtil.getConfig().getPatternConfig();

            String internalId = UUID.randomUUID().toString();
            String onPremisesImmutableId = getRegexReplacedString(
                    patternConfig.getOnPremisesImmutableIdPattern(), result);
            String givenName = getRegexReplacedString(
                    patternConfig.getGivenNamePattern(), result);
            String surname = getRegexReplacedString(
                    patternConfig.getSurnamePattern(), result);
            String displayName = getRegexReplacedString(
                    patternConfig.getDisplayNamePattern(), result);
            String mailNickname = getRegexReplacedString(
                    patternConfig.getMailNicknamePattern(), result);
            String userPrincipalName = getRegexReplacedString(
                    patternConfig.getUserPrincipalNamePattern(), result);

            User user = new User(internalId, onPremisesImmutableId, null, givenName, surname,
                    displayName, mailNickname,userPrincipalName);
            user.resetLastChanged();

            ldapUsers.put(onPremisesImmutableId, user);
        }
        ldapUtil.close();
        return ldapUsers;
    }

    /**
     * Update the internal database.
     * @param users Map containing the users read from the source ldap server.
     */
    private static void updateDatabase(Map<String, User> users) {
        LOG.info("Running import for {} ldap users...", users.size());

        long newUsers = 0L;
        long changedUsers = 0L;
        long deletedUsers = 0L;
        long unchangedUsers = 0L;

        // Check for changed an new users.
        for (Map.Entry<String, User> entry : users.entrySet()) {
            String onPremisesImmutableId = entry.getKey();
            User user = entry.getValue();

            User dbUser = H2Helper.getUserDao()
                    .getByAttributeMatch("onPremisesImmutableId", onPremisesImmutableId);
            if (dbUser != null) {
                if (!dbUser.isHashEqual(user)) {
                    user.setId(dbUser.getId());
                    user.setAzureImmutableId(dbUser.getAzureImmutableId());
                    user.setLastSyncId(dbUser.getLastSyncId());
                    user.setChangeState(ChangeState.CHANGED);
                    user.setSyncState(SyncState.PENDING);
                    H2Helper.getUserDao().update(user);
                    changedUsers++;
                } else {
                    unchangedUsers++;
                }
            } else {
                user.setChangeState(ChangeState.NEW);
                user.setSyncState(SyncState.PENDING);
                H2Helper.getUserDao().persist(user);
                newUsers++;
            }
        }

        // Check for deleted users.
        for (User user : H2Helper.getUserDao().getAll()) {
            if (!users.containsKey(user.getOnPremisesImmutableId())) {
                user.setChangeState(ChangeState.DELETED);
                user.setSyncState(SyncState.PENDING);
                H2Helper.getUserDao().update(user);
                deletedUsers++;
            }
        }

        LOG.info("LDAP import finished. Result: {} NEW, {} CHANGED, {} DELETED, {} UNCHANGED",
                newUsers, changedUsers, deletedUsers, unchangedUsers);
    }

}
