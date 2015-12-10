
package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.SelectQuery;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// JRADEV-21357

/**
 * Upgrade task to convert username fields to lowercase in worklogs.
 * These should have been covered in 6039 but were initially overlooked, so we have to retry
 * them, here.
 *
 * @since v6.0.1
 */
public class UpgradeTask_Build6096 extends UpgradeTask_Build6039
{
    // In case somebody has created recycled users between 6.0 and 6.0.1, we need to exempt them
    // from this upgrade task.
    private static final Pattern REGEX_RECYCLED_USED = Pattern.compile("^ID\\d+$");

    public UpgradeTask_Build6096(EntityEngine entityEngine)
    {
        super(entityEngine);
    }

    @Override
    public String getBuildNumber()
    {
        return "6096";
    }

    @Override
    public String getShortDescription()
    {
        return "Convert worklog username references to lowercase so that they can be used as keys";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        doWorklogUpgrade();
    }

    // Need to filter out any ID12345 entries
    @Override
    protected Map<String, String> toUsernameMap(SelectQuery<String> selectQuery)
    {
        final Map<String,String> map = super.toUsernameMap(selectQuery);
        final Matcher matcher = REGEX_RECYCLED_USED.matcher("");
        final Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext())
        {
            if (matcher.reset(iter.next()).matches())
            {
                iter.remove();
            }
        }
        return map;
    }
}
