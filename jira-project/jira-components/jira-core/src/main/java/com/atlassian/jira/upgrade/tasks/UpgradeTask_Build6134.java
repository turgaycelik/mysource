
package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.SelectQuery;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// JRADEV-22456

/**
 * Upgrade task to convert username fields to lowercase in project role actors.
 * These should have been covered in 6039 but were overlooked, so we have to
 * retry them, here.  Note that this upgrade task exists as 6101 in 6.0.5.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6134 extends UpgradeTask_Build6096
{
    // In case somebody has created recycled users between 6.0 and 6.0.5, we need to exempt them
    // from this upgrade task.  Extending 6096 instead of extending 6039 directly picks up the
    // toUsernameMap override that takes care of that, since it was to fix a similar oversight
    // in worklogs.

    public UpgradeTask_Build6134(EntityEngine entityEngine)
    {
        super(entityEngine);
    }

    @Override
    public String getBuildNumber()
    {
        return "6134";
    }

    @Override
    public String getShortDescription()
    {
        return "Convert project role username references to lowercase so that they can be used as keys";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        doUserProjectRoleActorUpgrade();
    }
}
