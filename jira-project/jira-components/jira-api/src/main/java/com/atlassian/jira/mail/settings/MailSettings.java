package com.atlassian.jira.mail.settings;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.mail.MailFactory;
import com.google.common.annotations.VisibleForTesting;

import java.util.Date;
import javax.annotation.Nullable;

/**
 * Responsible for holding the mail settings for JIRA.
 *
 * @since v5.2
 */
@PublicApi
public interface MailSettings
{
    /**
     * Retrieves the outgoing mail settings for JIRA.
     *
     * @return An object representing the outgoing mail settings for JIRA.
     */
    MailSettings.Send send();

    /**
     * Retrieves the incoming mail settings for JIRA.
     *
     * @return An object representing the incoming mail settings for JIRA.
     */
    MailSettings.Fetch fetch();

    @Internal
    public class DefaultMailSettings implements MailSettings
    {
        private final Send send;
        private final Fetch fetch;

        public DefaultMailSettings(final ApplicationProperties applicationProperties,
                final JiraAuthenticationContext jiraAuthenticationContext,
                final JiraProperties jiraSystemProperties)
        {
            send = new Send(applicationProperties, jiraAuthenticationContext, jiraSystemProperties);
            fetch= new Fetch(applicationProperties, jiraAuthenticationContext, jiraSystemProperties);
        }

        @Override
        public Send send()
        {
            return send;
        }

        @Override
        public Fetch fetch()
        {
            return fetch;
        }
    }

    /**
     * Represents the state of outgoing mail for this JIRA instance. This can either be enabled or disabled.
     */
    public static class Send
    {
        @VisibleForTesting
        public static final String DISABLED_APPLICATION_PROPERTY = "jira.mail.send.disabled";

        /**
         * Whether outgoing mail is disabled.
         */
        private final MailSetting disableSetting;
        @VisibleForTesting
        public static final String DISABLED_SYSTEM_PROPERTY_KEY = MailFactory.MAIL_DISABLED_KEY;
        private final JiraProperties jiraSystemProperties;

        private Send(final ApplicationProperties applicationProperties, final JiraAuthenticationContext jiraAuthenticationContext, final JiraProperties jiraSystemProperties)
        {
            this.jiraSystemProperties = jiraSystemProperties;
            disableSetting = new MailSetting(applicationProperties, jiraAuthenticationContext, DISABLED_APPLICATION_PROPERTY);
        }

        /**
         * Whether outgoing mail is currently enabled.
         *
         * @return true, if outgoing mail is enabled. Otherwise, false.
         */
        public boolean isEnabled()
        {
            return !isDisabled();
        }

        /**
         * Whether outgoing mail is currently disabled.
         *
         * @return true, if outgoing mail is disabled. Otherwise, false.
         * @see #isEnabled()
         */
        public boolean isDisabled()
        {
            if (jiraSystemProperties.getBoolean(DISABLED_SYSTEM_PROPERTY_KEY))
            {
                return true;
            }
            else
            {
                return isDisabledViaApplicationProperty();
            }
        }

        /**
         * Whether outgoing mail is currently disabled via the application property.
         *
         * @return true, if outgoing mail is disabled. Otherwise, false.
         * @see #isEnabled()
         */
        public boolean isDisabledViaApplicationProperty()
        {
            return disableSetting.getValue();
        }

        /**
         * Whether the outgoing mail setting can be changed from enabled to disabled, or viceversa.
         *
         * @return true, if outgoing mail can be changed. Otherwise, false.
         */
        public boolean isModifiable()
        {
            return !jiraSystemProperties.getBoolean(DISABLED_SYSTEM_PROPERTY_KEY);
        }

        /**
         * Returns the key of the last user to have enabled or disabled outgoing mail, or null if it has never been
         * enabled or disabled.
         *
         * @return the key of the last user to have enabled/disabled outgoing mail, or null
         * @see com.atlassian.jira.user.ApplicationUser
         * @since 6.1
         */
        @Nullable
        public String getModifiedBy()
        {
            return disableSetting.getModifiedBy();
        }

        /**
         * Returns the date when outgoing mail was last enabled or disabled, or null if it has never been modified.
         *
         * @return the date when outgoing mail was last enabled/disabled, or null.
         * @since 6.1
         */
        @Nullable
        public Date getModifiedDate()
        {
            return disableSetting.getModifiedDate();
        }

        /**
         * Enables sending outgoing mail from JIRA.
         *
         * @return true, if outgoing mail was succesfully enabled. Otherwise, false.
         * @see #isModifiable()
         */
        public boolean enable()
        {
            if (!isModifiable())
            {
                return false;
            }
            disableSetting.setValue(false);
            return true;
        }

        /**
         * Disables sending outgoing mail from JIRA.
         *
         * @return true, if outgoing mail was succesfully disabled. Otherwise, false.
         * @see #isModifiable()
         */
        public boolean disable()
        {
            if (!isModifiable())
            {
                return false;
            }
            disableSetting.setValue(true);
            return true;
        }
    }

    /**
     * Represents the state of incoming mail processing in JIRA. This can either be enabled or disabled.
     *
     * @since v5.2
     */
    public static class Fetch
    {
        @VisibleForTesting
        static final String DISABLED_APPLICATION_PROPERTY = "jira.mail.fetch.disabled";

        /**
         * Whether incoming mail is disabled.
         */
        private final MailSetting disabledSetting;
        @VisibleForTesting
        static final String LEGACY_DISABLED_SYSTEM_PROPERTY = "atlassian.mail.popdisabled";

        @VisibleForTesting
        static final String DISABLED_SYSTEM_PROPERTY = "atlassian.mail.fetchdisabled";
        private final JiraProperties jiraSystemProperties;

        @Deprecated
        public Fetch(final ApplicationProperties applicationProperties)
        {
            this(applicationProperties, ComponentAccessor.getJiraAuthenticationContext());
        }

        @Deprecated
        public Fetch(final ApplicationProperties applicationProperties, final JiraAuthenticationContext jiraAuthenticationContext)
        {
            this(applicationProperties, jiraAuthenticationContext, JiraSystemProperties.getInstance());
        }

        private Fetch(final ApplicationProperties applicationProperties, final JiraAuthenticationContext jiraAuthenticationContext, final JiraProperties jiraSystemProperties)
        {
            this.jiraSystemProperties = jiraSystemProperties;
            disabledSetting = new MailSetting(applicationProperties, jiraAuthenticationContext, DISABLED_APPLICATION_PROPERTY);
        }

        /**
         * Whether fetching incoming mail is currently enabled.
         *
         * @return true, if fetching incoming mail is enabled. Otherwise, false.
         */
        public boolean isEnabled()
        {
            return !isDisabled();
        }

        private boolean getSystemPropertyValue()
        {
            return jiraSystemProperties.getBoolean(LEGACY_DISABLED_SYSTEM_PROPERTY) || jiraSystemProperties.getBoolean(DISABLED_SYSTEM_PROPERTY);
        }

        /**
         * Whether fetching incoming mail is currently disabled.
         *
         * @return true, if fetching incoming mail is disabled. Otherwise, false.
         * @see #isEnabled()
         */
        public boolean isDisabled()
        {
            if (getSystemPropertyValue())
            {
                return true;
            }
            else
            {
                return disabledSetting.getValue();
            }
        }

        /**
         * Whether the fetching incoming mail setting can be changed from enabled to disabled, or viceversa.
         *
         * @return true, if fetching incoming mail can be changed. Otherwise, false.
         */
        public boolean isModifiable()
        {
            return !(getSystemPropertyValue());
        }

        /**
         * Enables fetching incoming mail in JIRA.
         *
         * @return true, if incoming mail was succesfully enabled. Otherwise, false.
         * @see #isModifiable()
         */
        public boolean enable()
        {
            if (!isModifiable())
            {
                return false;
            }
            disabledSetting.setValue(false);
            return true;
        }

        /**
         * Disables fetching incoming mail in JIRA.
         *
         * @return true, if incoming mail was succesfully disabled. Otherwise, false.
         * @see #isModifiable()
         */
        public boolean disable()
        {
            if (!isModifiable())
            {
                return false;
            }
            disabledSetting.setValue(true);
            return true;
        }
    }
}
