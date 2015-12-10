package com.atlassian.jira.user.util;

import com.atlassian.core.util.Clock;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.util.RealClock;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.1
 */
class PasswordResetTokenBuilder
{
    private static final Logger log = Logger.getLogger(PasswordResetTokenBuilder.class);

    private static final int TOKEN_EXPIRY_HOURS = 24;
    private static final String PASSWORD_RESET_REQUEST_TOKEN = "password.reset.request.token";
    private static final String PASSWORD_RESET_REQUEST_EXPIRY = "password.reset.request.expiry";
    private final Clock clock;
    private final CrowdService crowdService;

    PasswordResetTokenBuilder(CrowdService crowdService)
    {
        this(RealClock.getInstance(), crowdService);
    }

    PasswordResetTokenBuilder(Clock clock, CrowdService crowdService)
    {
        this.clock = clock;
        this.crowdService = crowdService;
    }

    UserUtil.PasswordResetToken generateToken(final com.atlassian.crowd.embedded.api.User user)
    {
        notNull("user", user);

        final String token = genSecureToken();
        final long expiryTime = genExpiryTime();

        recordToken(user, token, expiryTime);

        return new UserUtil.PasswordResetToken()
        {
            public com.atlassian.crowd.embedded.api.User getUser()
            {
                return user;
            }

            public String getToken()
            {
                return token;
            }

            public int getExpiryHours()
            {
                return TOKEN_EXPIRY_HOURS;
            }

            public long getExpiryTime()
            {
                return expiryTime;
            }
        };
    }


    /**
     * This will return true if the token is valid for this user.
     *
     * @param user  the user in play
     * @param token the token they have presented
     *
     * @return true if they that
     */
    UserUtil.PasswordResetTokenValidation.Status validateToken(com.atlassian.crowd.embedded.api.User user, String token)
    {
        notNull("user", user);

        String storedToken = getStoredToken(user);
        if (storedToken == null)
        {
            return UserUtil.PasswordResetTokenValidation.Status.EXPIRED;
        }
        if (storedToken.equals(token))
        {
            return UserUtil.PasswordResetTokenValidation.Status.OK;
        }
        else
        {
            return UserUtil.PasswordResetTokenValidation.Status.UNEQUAL;
        }
    }

    /**
     * Called to reset the token for a user
     *
     * @param user the user in play
     */
    void resetToken(final com.atlassian.crowd.embedded.api.User user)
    {
        recordTokenImpl(user, null, null);
    }


    private String genSecureToken()
    {
        return DefaultSecureTokenGenerator.getInstance().generateToken();
    }

    /**
     * If it returns null then there wasnt one or it has expired
     *
     * @param user the user in play
     *
     * @return the token or null
     */
    private String getStoredToken(final com.atlassian.crowd.embedded.api.User user)
    {
        UserWithAttributes userWithAttributes = crowdService.getUserWithAttributes(user.getName());

        String expiryStr = userWithAttributes.getValue(PASSWORD_RESET_REQUEST_EXPIRY);
        if (expiryStr == null)
        {
            return null;
        }

        long expiry = Long.parseLong(expiryStr);
        if (now().getMillis() > expiry)
        {
            return null;
        }

        return userWithAttributes.getValue(PASSWORD_RESET_REQUEST_TOKEN);
    }

    /**
     * @return Expiry time of token in UTC
     */
    private long genExpiryTime()
    {
        return now().plusHours(TOKEN_EXPIRY_HOURS).getMillis();
    }

    private DateTime now()
    {
        return new DateTime(clock.getCurrentDate().getTime(), DateTimeZone.UTC);
    }

    private void recordToken(final com.atlassian.crowd.embedded.api.User user, final String token, final long expiryTime)
    {
        recordTokenImpl(user, token, String.valueOf(expiryTime));
    }

    private void recordTokenImpl(final com.atlassian.crowd.embedded.api.User user, final String token, final String expiryTime)
    {
        try
        {
            if (token == null)
            {
                crowdService.removeUserAttribute(user, PASSWORD_RESET_REQUEST_EXPIRY);
                crowdService.removeUserAttribute(user, PASSWORD_RESET_REQUEST_TOKEN);
            }
            else
            {
                crowdService.setUserAttribute(user, PASSWORD_RESET_REQUEST_EXPIRY, expiryTime);
                crowdService.setUserAttribute(user, PASSWORD_RESET_REQUEST_TOKEN, token);
            }
        }
        catch (OperationNotPermittedException e)
        {
            // Should never occur as we store all attributes locally.
            log.error(e);
        }
    }

    private void safelyRemove(final PropertySet propertySet, final String key)
    {
        if (StringUtils.isNotBlank(propertySet.getString(key)))
        {
            propertySet.remove(key);
        }
    }
}
