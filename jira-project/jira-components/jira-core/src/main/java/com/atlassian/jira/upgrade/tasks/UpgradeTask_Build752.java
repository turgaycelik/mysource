package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * JRA-26194: usernames in PortalPage should be stored lower case only. Update the storage. On the 5.0.x branch
 * this was UT_727. We introduced it on the branch and need to run it again in case people go from 5.0.2 -> 5.1
 * rather that 5.0.3+ -> 5.1.
 *
 * @since v5.0.3.
 */
public class UpgradeTask_Build752 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build752.class);

    private static final class Table
    {
        public static final String NAME = "PortalPage";
    }

    private static final class Column
    {
        public static final String USERNAME = "username";
    }

    private final OfBizDelegator delegator;

    public UpgradeTask_Build752(OfBizDelegator delegator)
    {
        super(false);
        this.delegator = delegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "752";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        final List<GenericValue> pages = delegator.findAll(Table.NAME);
        if (pages != null)
        {
            LOG.info(String.format("Analysing %d Portal Pages...", pages.size()));

            for (GenericValue gv : pages)
            {
                final String username = gv.getString(Column.USERNAME);
                if (StringUtils.isNotEmpty(username))
                {
                    final String lowercase_username = IdentifierUtils.toLowerCase(username);
                    if (!username.equals(lowercase_username))
                    {
                        gv.setString(Column.USERNAME, lowercase_username);
                        delegator.store(gv);
                    }
                }
            }
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Make the owner of a dashboard lowercase";
    }
}
