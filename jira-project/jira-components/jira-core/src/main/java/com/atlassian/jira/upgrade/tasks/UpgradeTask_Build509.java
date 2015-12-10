package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.lang.StringUtils;

/**
 * Upgrades link, active link and text color to new default of #3c78b5.  The upgrade task will only modify the link
 * colours if the entry in the database is either empty or set to the old entry.  If customers have changed the value to
 * something different, we leave the value as is.
 *
 * @since v4.1
 */
public class UpgradeTask_Build509 extends AbstractUpgradeTask
{
    private static final String NEW_COLOUR = "#3c78b5";
    private static final String OLD_COLOUR = "#003366";

    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build509(final ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final String linkColour = applicationProperties.getString(APKeys.JIRA_LF_TEXT_LINKCOLOUR);
        final String activeLinkColour = applicationProperties.getString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR);
        final String headingColour = applicationProperties.getString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR);

        if (StringUtils.isEmpty(linkColour) || OLD_COLOUR.equals(linkColour))
        {
            applicationProperties.setString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, NEW_COLOUR);
        }
        if (StringUtils.isEmpty(activeLinkColour) || OLD_COLOUR.equals(activeLinkColour))
        {
            applicationProperties.setString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, NEW_COLOUR);
        }
        if (StringUtils.isEmpty(headingColour) || OLD_COLOUR.equals(headingColour))
        {
            applicationProperties.setString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, NEW_COLOUR);
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Upgrades link, active link and text color to new default of #3c78b5.";
    }

    @Override
    public String getBuildNumber()
    {
        return "509";
    }
}