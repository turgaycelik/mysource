package com.atlassian.jira.security.login;

import com.atlassian.core.util.Clock;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginInfoImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.RealClock;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0.1
 */
public class LoginStoreImpl implements LoginStore
{
    private static final Logger log = Logger.getLogger(LoginStoreImpl.class);

    private static final String LAST_LOGIN_TIME = "login.lastLoginMillis";
    private static final String PREV_LOGIN_TIME = "login.previousLoginMillis";
    private static final String LAST_FAILED_TIME = "login.lastFailedLoginMillis";
    private static final String LOGIN_COUNT = "login.count";
    private static final String CURRENT_FAILED_COUNT = "login.currentFailedCount";
    private static final String TOTAL_FAILED_COUNT = "login.totalFailedCount";
    private final Clock clock;
    private final ApplicationProperties applicationProperties;
    private final CrowdService crowdService;


    ///CLOVER:OFF
    public LoginStoreImpl(final ApplicationProperties applicationProperties)
    {
        this(RealClock.getInstance(), applicationProperties, ComponentAccessor.getComponentOfType(CrowdService.class));
    }
    ///CLOVER:ON

    LoginStoreImpl(final Clock clock, final ApplicationProperties applicationProperties, final CrowdService crowdService)
    {
        this.clock = notNull("clock", clock);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.crowdService = crowdService;
    }

    public LoginInfo recordLoginAttempt(final User user, final boolean authenticated)
    {
        notNull("user", user);

        UserWithAttributes userWithAttributes = crowdService.getUserWithAttributes(user.getName());
        if (authenticated)
        {
            // reset the failed count
            setLong(user, CURRENT_FAILED_COUNT, 0);

            Long lastLoginTime = getLong(userWithAttributes, LAST_LOGIN_TIME);

            // set the last login time
            setLong(user, LAST_LOGIN_TIME, now());
            //
            // and bump the previous one along
            if (lastLoginTime != null)
            {
                setLong(user, PREV_LOGIN_TIME, lastLoginTime);
            }

            // bump count
            incrementLong(userWithAttributes, LOGIN_COUNT, 1);
        }
        else
        {
            setLong(user, LAST_FAILED_TIME, now());
            incrementLong(userWithAttributes, CURRENT_FAILED_COUNT, 1);
            incrementLong(userWithAttributes, TOTAL_FAILED_COUNT, 1);
        }
        // Need to get a new copy of the attributes now.
        return getLoginInfo(user);
    }

    public long getMaxAuthenticationAttemptsAllowed()
    {
        final String maxStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED);
        try
        {
            return StringUtils.isBlank(maxStr) ? Long.MAX_VALUE : Long.parseLong(maxStr);
        }
        catch (NumberFormatException e)
        {
            log.error("Unable to read the MaxAuthenticationAttemptsAllowed value ' " + maxStr + "'.  Defaulting to UNLIMITED.  If you really care avbout security you will want to find out why this value cant be read.");
            return Long.MAX_VALUE;
        }
    }

    public void resetFailedLoginCount(final User user)
    {
        setLong(user, CURRENT_FAILED_COUNT, 0);
    }

    public LoginInfo getLoginInfo(final User user)
    {
        notNull("user", user);
        UserWithAttributes userWithAttributes = crowdService.getUserWithAttributes(user.getName());
        return getLoginInfoInternal(userWithAttributes);
    }

    private LoginInfo getLoginInfoInternal(final UserWithAttributes userWithAttributes)
    {
        final Long lastLoginTime = getLong(userWithAttributes, LAST_LOGIN_TIME);
        final Long previousLoginTime = getLong(userWithAttributes, PREV_LOGIN_TIME);
        final Long lastFailedTime = getLong(userWithAttributes, LAST_FAILED_TIME);
        final Long loginCount = getLong(userWithAttributes, LOGIN_COUNT);
        final Long currentFailedCount = getLong(userWithAttributes, CURRENT_FAILED_COUNT);
        final Long totalFailedCount = getLong(userWithAttributes, TOTAL_FAILED_COUNT);
        //
        // we dont know this information in the store since its not concerned with logic.
        // we will leave it to the manager to implement this.
        //
        final boolean elevatedSecurityCheckRequired = false;
        final long maxAuthenticationAttemptsAllowed = getMaxAuthenticationAttemptsAllowed();
        return new LoginInfoImpl(lastLoginTime, previousLoginTime, lastFailedTime, loginCount, currentFailedCount, totalFailedCount, maxAuthenticationAttemptsAllowed, elevatedSecurityCheckRequired);
    }

    //
    // we use getString/setString() because PropertySet is brain dead and getLong cant handle null values
    // on read and always returns 0, which is not what we want.
    //
    private void setLong(final User user, final String key, final long value)
    {
        try
        {
            crowdService.setUserAttribute(user, key, Long.valueOf(value).toString());
        }
        catch (OperationNotPermittedException e)
        {
            // Should never occur as we store all attributes locally.
            log.error(e);
        }
    }

    private Long getLong(final UserWithAttributes userWithAttributes, final String key)
    {
        String attribute = userWithAttributes.getValue(key);

        if (attribute != null)
        {
            return Long.valueOf(attribute);
        }
        return null;
    }

    /**
     * This will increment a long value or initialise it to the default value if it is currently null
     *
     * @param userWithAttributes  UserWithAttributes to increment value for
     * @param key          the key to set
     * @param defaultValue the default value if its currently null
     */
    private void incrementLong(final UserWithAttributes userWithAttributes, final String key, final int defaultValue)
    {
        Long currentValue = getLong(userWithAttributes, key);
        setLong(userWithAttributes, key, currentValue == null ? defaultValue : currentValue + 1);
    }

    private long now()
    {
        return clock.getCurrentDate().getTime();
    }

}
