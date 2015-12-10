package com.atlassian.jira.upgrade;

import java.util.Date;

/**
 * Simple representation of an upgrade performed in history.
 *
 * @since v4.1
 */
public interface UpgradeHistoryItem
{
    /**
     * For historical data, we do not have information about when upgrade tasks were performed. In this case, null may be
     * returned.
     * 
     * @return the time when the upgrade was performed; may be null if it is unknown
     */
    Date getTimePerformed();

    /**
     * For historical data, this number may not represent a released build of JIRA but instead the last upgrade task to
     * run. This is because often the release build number does not have an associated upgrade task.
     *
     * @return the build number that was being upgraded to which represents which version of JIRA the instance was running
     * at the time of upgrade.
     */
    String getTargetBuildNumber();

    /**
     * @return the previous build number of JIRA before this upgrade, or the build number used to infer this history item
     * if it was inferred.
     */
    String getOriginalBuildNumber();

    /**
     * <p>If the target build number was not a released build of JIRA, the version picked will be the next version of JIRA
     * after that build number.
     *
     * <p>For example, if the target build number is <code>207</code>, the version returned would be <code>3.8</code>,
     * as it has a build number of <code>209</code>.
     *
     * @return the version of JIRA that corresponds to the target build number.
     */
    String getTargetVersion();

    /**
     * @return the previous version of JIRA before this upgrade.
     */
    String getOriginalVersion();

    /**
     * @return true if this was inferred from upgrade tasks; false otherwise
     */
    boolean isInferred();
}
