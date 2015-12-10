package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.log4j.Logger;

/**
 * Class provides limit for text fields.
 *
 * NOTE: some sections of code related to OnDemand have been commented out for 5.0.3
 *
 * @since v5.0.3
 */
public class TextFieldLimitProvider
{
//    private static final long DEFAULT_ON_DEMAND_TEXT_FIELD_LIMIT = 30000;
    private static final long DEFAULT_TEXT_FIELD_LIMIT = 30000;
    private final LogOnceLogger propertyMissingLog = new LogOnceLogger(Logger.getLogger(this.getClass()));
    private final LogOnceLogger invalidFormatLog = new LogOnceLogger(Logger.getLogger(this.getClass()));

    private final ApplicationProperties applicationProperties;
//    private final FeatureManager featureManager;

    public TextFieldLimitProvider(ApplicationProperties applicationProperties)
//            , FeatureManager featureManager)
    {
        this.applicationProperties = applicationProperties;
//        this.featureManager = featureManager;
    }
    
    public long getTextFieldLimit()
    {
//        Section below is commented out as patch for the following comment in JRA-27743
//          "we have decided the turning on for Studio is NOT going to happen for 5.0.3."
//        if (featureManager.isEnabled(CoreFeatures.ON_DEMAND)) {
//            return DEFAULT_ON_DEMAND_TEXT_FIELD_LIMIT;
//        }

        return retrieveTextLimitFromApplicationProperties();
    }

    private long retrieveTextLimitFromApplicationProperties()
    {
        final String textFieldLimitString = applicationProperties.getDefaultBackedString(APKeys.JIRA_TEXT_FIELD_CHARACTER_LIMIT);

        if (textFieldLimitString == null || "".equals(textFieldLimitString))
        {
            propertyMissingLog.warnOnlyOnce("Property '" + APKeys.JIRA_TEXT_FIELD_CHARACTER_LIMIT + "' is missing. Using default value of " + DEFAULT_TEXT_FIELD_LIMIT + " characters instead.");
            return DEFAULT_TEXT_FIELD_LIMIT;
        }

        try
        {
            return Long.parseLong(textFieldLimitString);
        }
        catch (NumberFormatException e)
        {
            invalidFormatLog.warnOnlyOnce("Invalid format of '" + APKeys.JIRA_TEXT_FIELD_CHARACTER_LIMIT + "' property: '" + textFieldLimitString + "'. Expected value to be a long. Using default value of " + DEFAULT_TEXT_FIELD_LIMIT + " characters instead.");
            return DEFAULT_TEXT_FIELD_LIMIT;
        }
    }

    private static class LogOnceLogger
    {
        private boolean alreadyLogged = false;
        private final Logger delegatee;

        private LogOnceLogger(final Logger delegatee)
        {
            this.delegatee = delegatee;
        }

        public void warnOnlyOnce(final Object message)
        {
            if (!alreadyLogged)
            {
                alreadyLogged = true;
                delegatee.warn(message);
            }
        }
    }
}
