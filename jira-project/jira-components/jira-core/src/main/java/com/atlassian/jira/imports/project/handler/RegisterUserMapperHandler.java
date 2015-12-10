package com.atlassian.jira.imports.project.handler;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.user.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This handler records all users that exist in the backup file and register them with the user mapper.
 * The UserMapperHandler flags the required Users.
 *
 * @since v3.13
 * @see com.atlassian.jira.imports.project.handler.UserMapperHandler
 */
public class RegisterUserMapperHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(RegisterUserMapperHandler.class);

    private static final String OS_PROPERTY_ENTRY = "OSPropertyEntry";
    private static final String ENTITY_NAME = "entityName";
    private static final String APPLICATION_USER = "ApplicationUser";
    private static final String USER = "User";
    private static final String ID = "id";
    private static final String ENTITY_ID = "entityId";
    private static final String OS_PROPERTY_STRING = "OSPropertyString";
    private static final String VALUE = "value";
    private static final String USER_KEY = "userKey";
    private static final String USER_NAME = "userName";
    private static final String LOWER_USER_NAME = "lowerUserName";
    private static final String DISPLAY_NAME = "displayName";
    private static final String EMAIL = "emailAddress";
    private static final String CREDENTIAL = "credential";

    private final Map<String, String> appUserIdToUserKeyMap = new HashMap<String,String>();
    private final Map<String, UserPropertyKey> osPropertyEntryMap = new HashMap<String, UserPropertyKey>();
    private final Map<String, String> userNameToUserKeyMap = new HashMap<String,String>();
    private final Map<String, ExternalUser> userMap = new HashMap<String, ExternalUser>();

    private final UserMapper userMapper;
    private final boolean debug;


    public RegisterUserMapperHandler(final UserMapper userMapper)
    {
        this.userMapper = userMapper;
        this.debug = log.isDebugEnabled();
    }

    //<ApplicationUser id="10101" userKey="ID10101" lowerUserName="cc"/>
    private void handleApplicationUser(final Map<String,String> attributes) throws ParseException
    {
        final String id = attributes.get(ID);
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("Missing 'id' field for ApplicationUser entry.");
        }
        final String userKey = attributes.get(USER_KEY);
        if (StringUtils.isEmpty(userKey))
        {
            throw new ParseException("Missing userKey from ApplicationUser id = " + id);
        }
        final String lowerUserName = attributes.get(LOWER_USER_NAME);
        if (StringUtils.isEmpty(lowerUserName))
        {
            throw new ParseException("Missing lowerUserName from ApplicationUser id = " + id);
        }

        appUserIdToUserKeyMap.put(id, userKey);
        userNameToUserKeyMap.put(lowerUserName, userKey);
    }

    // <OSPropertyEntry id="10070" entityName="ApplicationUser" entityId="10002" propertyKey="jira.meta.ice cream" type="5"/>
    private void handleOsPropertyEntry(final Map<String, String> attributes) throws ParseException
    {
        // User properties are stored in OSProperty with entityName="ApplicationUser".  Any others, we don't care about.
        if (!APPLICATION_USER.equals(attributes.get(ENTITY_NAME)))
        {
            return;
        }

        final String osPropertyID = attributes.get(ID);
        if (StringUtils.isEmpty(osPropertyID))
        {
            throw new ParseException("Missing 'id' field for OSPropertyEntry.");
        }
        final String entityId = attributes.get(ENTITY_ID);
        if (StringUtils.isBlank(entityId))
        {
            throw new ParseException("Missing entityId from OSPropertyEntry id = " + osPropertyID);
        }
        final String propertyKey = attributes.get("propertyKey");
        if (StringUtils.isBlank(propertyKey))
        {
            throw new ParseException("Missing propertyKey from OSPropertyEntry id = " + osPropertyID);
        }

        // We need to store the username and propertyKey in a map indexed by OSPropertyEntry ID
        final String userKey = appUserIdToUserKeyMap.get(entityId);
        if (userKey == null)
        {
            log.warn("OSPropertyEntry " + osPropertyID + " is associated with non-existent ApplicationUser ID '" + entityId + '\'');
        }
        else
        {
            // JRA-21453: If propertyKey is not one of email, fullname or jira.meta.*, then ignore it.
            if (UserPropertyKey.isRememberedPropertyKey(propertyKey))
            {
                osPropertyEntryMap.put(osPropertyID, new UserPropertyKey(userKey, propertyKey));
                if (debug)
                {
                    log.debug("Registering property '" + propertyKey + "' belonging to userKey='" + userKey + '\'');
                }
            }
            else
            {
                if (debug)
                {
                    log.debug("Ignoring property '" + propertyKey + "' belonging to userKey='" + userKey + '\'');
                }
            }
        }
    }

    //<OSPropertyString id="10150" value="wsailor@example.com"/>
    //<OSPropertyString id="10151" value="Wendell Sailor"/>
    //<OSPropertyString id="10152" value="Ding Dong Dell"/>
    //<OSPropertyString id="10153" value="Purple"/>
    private void handleOsPropertyString(final Map<String, String> attributes) throws ParseException
    {
        // Using Map.remove here as it may help free up memory a bit quicker.  If
        // we don't get one, then we must not care about this particular value.
        final UserPropertyKey userPropertyKey = osPropertyEntryMap.remove(attributes.get(ID));
        if (userPropertyKey != null)
        {
            userPropertyKey.addPropertyValueToUser(externalUser(userPropertyKey.userKey), attributes.get(VALUE));
            if (debug)
            {
                log.debug("User key '" + userPropertyKey.userKey + "' has property '" + userPropertyKey.propertyKey +
                        "' with value '" + attributes.get(VALUE));
            }
        }
        else if (debug)
        {
            log.debug("OSPropertyString with id '" + attributes.get(ID) + "' did not have an OSPropertyEntry that we cared about");
        }
    }

    //<User id="10011" directoryId="10000" userName="wilma" lowerUserName="wilma" active="1" createdDate="2010-01-04 09:49:04.932591" updatedDate="2010-01-04 09:49:04.932591" firstName="" lowerFirstName="" lastName="" lowerLastName="" displayName="Wilma Flinstone" lowerDisplayName="wilma flinstone" emailAddress="wilma@example.com" lowerEmailAddress="wilma@example.com" credential="jEUdB0f3hSbXaKrvGQrOull9LQ74qN9hVSjNpi5cicihae2b8IIBATUtHNWwyEBCopdv9Uqm5hn+5rpGVJC0Gg=="/>
    private void handleUser(Map<String, String> attributes) throws ParseException
    {
        final String userId = attributes.get(ID);
        if (StringUtils.isEmpty(userId))
        {
            throw new ParseException("Missing 'id' field for User entry.");
        }
        final String name = attributes.get(USER_NAME);
        if (StringUtils.isEmpty(name))
        {
            log.warn("Missing 'userName' field for User entry id = " + userId);
            return;
        }
        final String displayName = attributes.get(DISPLAY_NAME);
        if (StringUtils.isEmpty(displayName))
        {
            log.warn("Missing 'displayName' field for User entry id = " + userId);
        }
        final String email = attributes.get(EMAIL);
        if (StringUtils.isEmpty(email))
        {
            log.warn("Missing 'email' field for User entry id = " + userId);
        }
        final String credential = attributes.get(CREDENTIAL);

        // The user object may already be created because of OSProperty values, etc.
        final String userKey = keyForUserName(name);
        ExternalUser externalUser = externalUser(userKey);
        externalUser.setId(userId);
        externalUser.setName(name);
        externalUser.setFullname(displayName);
        externalUser.setEmail(email);
        externalUser.setPasswordHash(credential);
        // We finally have a fully populated ExternalUser object, add it to the BackupOverviewBuilder.
        userMapper.registerOldValue(externalUser);

        if (debug)
        {
            log.debug("Registering user '" + name + "' for creation");
        }
    }



    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (entityName.equals(APPLICATION_USER))
        {
            handleApplicationUser(attributes);
        }
        else if (entityName.equals(OS_PROPERTY_ENTRY))
        {
            handleOsPropertyEntry(attributes);
        }
        else if (entityName.equals(OS_PROPERTY_STRING))
        {
            handleOsPropertyString(attributes);
        }
        else if (entityName.equals(USER))
        {
            handleUser(attributes);
        }
    }

    public void startDocument()
    {
    // No-op
    }

    public void endDocument()
    {
    // No-op
    }

    private ExternalUser externalUser(String userKey)
    {
        ExternalUser user = userMap.get(userKey);
        if (user == null)
        {
            user = new ExternalUser();
            user.setKey(userKey);
            userMap.put(userKey, user);
        }
        return user;
    }

    private String keyForUserName(String userName)
    {
        final String lowerUserName = IdentifierUtils.toLowerCase(userName);
        if (!userNameToUserKeyMap.isEmpty())
        {
            final String userKey = userNameToUserKeyMap.get(lowerUserName);
            if (userKey != null)
            {
                return userKey;
            }
            log.warn("ApplicationUser entities are present, but User '" + userName + "' does not have one.");
        }

        // Export predates renamed user functionality, or the ApplicationUser is missing.  Key the user
        // by the lowercase of the username instead.
        return IdentifierUtils.toLowerCase(userName);
    }


    static class UserPropertyKey
    {
        String userKey;
        String propertyKey;

        public UserPropertyKey(final String userKey, final String propertyKey)
        {
            this.userKey = userKey;
            this.propertyKey = propertyKey;
        }

        /**
         * Adds the given value for this propertyKey to the given User object.
         * @param externalUser ExternalUser to populate
         * @param value The value.
         */
        public void addPropertyValueToUser(final ExternalUser externalUser, final String value)
        {
            // <OSPropertyEntry id="10150" entityName="ExternalEntity" entityId="10060" propertyKey="jira.meta.colour" type="5"/>
            if (isRememberedPropertyKey(propertyKey))
            {
                final String simplePropertyKey = propertyKey.substring(UserUtil.META_PROPERTY_PREFIX.length());
                externalUser.setUserProperty(simplePropertyKey, value);
            }
        }

        public static boolean isRememberedPropertyKey(final String propertyKey)
        {
            return propertyKey.startsWith(UserUtil.META_PROPERTY_PREFIX);
        }
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final RegisterUserMapperHandler that = (RegisterUserMapperHandler) o;
        if (userMapper != null ? !userMapper.equals(that.userMapper) : that.userMapper != null)
        {
            return false;
        }
        return appUserIdToUserKeyMap.equals(that.appUserIdToUserKeyMap)
            && osPropertyEntryMap.equals(that.osPropertyEntryMap)
            && userNameToUserKeyMap.equals(that.userNameToUserKeyMap)
            && userMap.equals(that.userMap);
    }

    public int hashCode()
    {
        int result;
        result = (userMapper != null ? userMapper.hashCode() : 0);
        result = 31 * result + appUserIdToUserKeyMap.hashCode();
        result = 31 * result + osPropertyEntryMap.hashCode();
        result = 31 * result + userNameToUserKeyMap.hashCode();
        result = 31 * result + userMap.hashCode();
        return result;
    }
}
