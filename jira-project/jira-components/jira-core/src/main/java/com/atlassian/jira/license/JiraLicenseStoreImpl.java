package com.atlassian.jira.license;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The implementation of {@link JiraLicenseStore}
 *
 * @since v4.0
 */
public class JiraLicenseStoreImpl implements JiraLicenseStore
{
    private static final String ENTERPRISE = "enterprise";

    private final ApplicationProperties applicationProperties;
    private final LicenseStringFactory licenseStringFactory;

    public JiraLicenseStoreImpl(final ApplicationProperties applicationProperties, final LicenseStringFactory licenseStringFactory)
    {
        this.licenseStringFactory = notNull("licenseStringFactory", licenseStringFactory);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
    }

    public String retrieve()
    {
        String licenseString = applicationProperties.getText(APKeys.JIRA_LICENSE);
        if (StringUtils.isBlank(licenseString))
        {
            licenseString = retreiveFromOldKeys();
        }
        if (StringUtils.isBlank(licenseString))
        {
            licenseString = retriveFromReallyOldKeys();
        }
        return licenseString;
    }

    private String retreiveFromOldKeys()
    {
        return retrieveFromMessageAndHash(applicationProperties.getText(APKeys.JIRA_LICENSE_V1_MESSAGE), applicationProperties.getText(APKeys.JIRA_LICENSE_V1_HASH));
    }

    private String retriveFromReallyOldKeys()
    {
        return retrieveFromMessageAndHash(applicationProperties.getString(APKeys.JIRA_OLD_LICENSE_V1_MESSAGE), applicationProperties.getString(APKeys.JIRA_OLD_LICENSE_V1_HASH));
    }

    private String retrieveFromMessageAndHash(final String msg, final String hash)
    {
        if (StringUtils.isNotBlank(msg) && StringUtils.isNotBlank(hash))
        {
            return licenseStringFactory.create(msg, hash);
        }
        else
        {
            return null;
        }
    }

    public void store(final String licenseString)
    {
        if (StringUtils.isBlank(licenseString))
        {
            throw new IllegalArgumentException("You can't store an empty license");
        }
        //
        // Under License 2.0 the WHITESPACE are no longer required.  Also this will cause the
        // value to be stored in a single attribute when exported imported via OfBiz
        // OfBiz exports store values in attributes if it can but sometimes it needs to put them in CDATA sections. However
        // we can easily pre-parse the licence if its in a CDATA section so we store it such that it is guaranteed to be
        // stored in an attribute and not a CDATA section
        //
        applicationProperties.setText(APKeys.JIRA_LICENSE, removeWhitespace(licenseString));
        //
        // This is here for legacy reasons.  The old code use to set this upon storing the
        // license and in the post 4.x world so do we
        //
        applicationProperties.setString(APKeys.JIRA_EDITION, ENTERPRISE);
    }

    @Override
    public void remove()
    {
        applicationProperties.setText(APKeys.JIRA_LICENSE, null);
        applicationProperties.setText(APKeys.JIRA_LICENSE_V1_MESSAGE, null);
        applicationProperties.setText(APKeys.JIRA_LICENSE_V1_HASH, null);
        applicationProperties.setString(APKeys.JIRA_OLD_LICENSE_V1_MESSAGE, null);
        applicationProperties.setString(APKeys.JIRA_OLD_LICENSE_V1_HASH, null);
    }

    private String removeWhitespace(final String licenseString)
    {
        if (StringUtils.isBlank(licenseString))
        {
            return licenseString;
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : licenseString.toCharArray())
        {
            if (!Character.isWhitespace(ch))
            {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public void resetOldBuildConfirmation()
    {
        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, false);
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP, "");
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER, "");
    }

    public void confirmProceedUnderEvaluationTerms(final String userName)
    {
        // Record the confirmation, the user and the time, and proceed
        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER, userName);
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
    }

    public String retrieveServerId()
    {
        return applicationProperties.getString(APKeys.JIRA_SID);
    }

    public void storeServerId(final String serverId)
    {
        applicationProperties.setString(APKeys.JIRA_SID, serverId);
    }
}
