package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

/**
 * Turn soap on for new installations
 *
 * @since v4.4
 */
public class UpgradeTask_Build642 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build642.class);

    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build642(ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
    return "642";
    }

    public String getShortDescription()
    {
        return "Turn AllowRPC On for new installations only.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        boolean allowRpc;
        if (setupMode)
        {
            allowRpc = true;
        }
        else
        {
            allowRpc = applicationProperties.getOption(APKeys.JIRA_OPTION_RPC_ALLOW);
        }
        applicationProperties.setOption(APKeys.JIRA_OPTION_RPC_ALLOW, allowRpc);
    }

}
