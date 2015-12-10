package com.atlassian.jira.mail.settings;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A mail application property. This just wraps {@link ApplicationProperties} access with some extra logging to make it
 * easier to find out when users enable/disable mail.
 * <p/>
 * Also stores some auditing information for these settings, making it possible to determine which user has last
 * modified the setting.
 *
 * @since 6.0.1
 */
final class MailSetting
{
    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(MailSetting.class);

    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final String name;

    /**
     * Creates a new MailSetting.
     *
     * @param applicationProperties the ApplicationProperties
     * @param jiraAuthenticationContext the JiraAuthenticationContext
     * @param name the property name
     */
    public MailSetting(@Nonnull ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext, @Nonnull String name)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
        this.name = checkNotNull(name);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
    }

    /**
     * @return the name of this MailSetting
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * @return the value for this MailSetting
     * @see ApplicationProperties#getOption(String)
     */
    public boolean getValue()
    {
        return applicationProperties.getOption(name);
    }

    /**
     * Returns the key of the last user to have modified this property, or null if it has never been modified. Returns
     * the empty string if it was modified by an anonymous user.
     *
     * @return the key of the last user to have modified this property or null
     * @see com.atlassian.jira.user.ApplicationUser
     */
    @Nullable
    public String getModifiedBy()
    {
        return applicationProperties.getString(modifiedBy());
    }

    /**
     * Returns the date when this property was last modified, or null if it has never been modified.
     *
     * @return the date when this property was last modified or null.
     */
    @Nullable
    public Date getModifiedDate()
    {
        final String modDateVal = applicationProperties.getString(modifiedDate());
        Date modDate = null;
        if (StringUtils.isNotBlank(modDateVal))
        {
            // If this throws a NumberFormatException now, that's a genuine problem.
            modDate = new Date(Long.parseLong(modDateVal));
            // Perhaps write some unit tests next time?
        }
        return modDate;
    }

    /**
     * Sets the value for this MailSetting.
     *
     * @see ApplicationProperties#setOption(String, boolean)
     */
    public void setValue(boolean value)
    {
        applicationProperties.setOption(name, value);
        log.info("Mail setting '{}' has been set to '{}'", name, value);
        if (log.isDebugEnabled())
        {
            log.trace("Dumping stack trace for mail setting change", new Throwable());
        }

        // save "auditing properties"
        ApplicationUser user = jiraAuthenticationContext.getUser();
        applicationProperties.setString(modifiedBy(), user != null ? user.getKey() : "");
        applicationProperties.setString(modifiedDate(), String.valueOf(System.currentTimeMillis()));
    }

    private String modifiedDate()
    {
        return String.format("mailsetting.%s.modifiedDate", name);
    }

    private String modifiedBy()
    {
        return String.format("mailsetting.%s.modifiedBy", name);
    }
}
