package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * JRA-26194: usernames in SearchRequest should be stored lower case only. Update the storage. On the 5.0.x branch
 * this was UT_729. We introduced it on the branch and need to run it again in case people go from 5.0.2 -> 5.1
 * rather that 5.0.3+ -> 5.1.
 *
 * @since v5.1
 */
public class UpgradeTask_Build754 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build754.class);

    private static final class Table
    {
        static final String NAME = "SearchRequest";
    }

    private static final class Column
    {
        private static final String AUTHOR = "author";
        private static final String USER = "user";
    }

    private final OfBizDelegator delegator;

    public UpgradeTask_Build754(OfBizDelegator delegator)
    {
        super(true);
        this.delegator = delegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "754";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        final List<GenericValue> requests = delegator.findAll(Table.NAME);
        if (requests == null || requests.isEmpty())
        {
            return;
        }
        LOG.info(String.format("Analysing %d Search Requests...", requests.size()));
        for (GenericValue gv : requests)
        {
            //Note we are using the '|' or here because we want both methods to run always.
            final boolean store = fixUserColumn(gv, Column.USER) | fixUserColumn(gv, Column.AUTHOR);
            if (store)
            {
                delegator.store(gv);
            }
        }
    }

    private static boolean fixUserColumn(GenericValue gv, String columnName)
    {
        final String username = gv.getString(columnName);
        if (StringUtils.isNotEmpty(username))
        {
            final String lowercase_username = IdentifierUtils.toLowerCase(username);
            if (!username.equals(lowercase_username))
            {
                gv.setString(columnName, lowercase_username);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getShortDescription()
    {
        return "Make the owner and author of a filter lowercase";
    }
}
