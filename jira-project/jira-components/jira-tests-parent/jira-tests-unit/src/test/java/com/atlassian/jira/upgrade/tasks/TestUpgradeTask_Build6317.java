package com.atlassian.jira.upgrade.tasks;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit test to verify only that the upgrade task is skipped when upgrade task 6307 has run.
 * <p>
 * There is a functional test to take care of the actual upgrade logic in upgradeFilterSubscriptionSchedules,
 * so it is short-circuited, here.
 * </p>
 *
 * @since v6.3
 */
public class TestUpgradeTask_Build6317
{
    private MockOfBizDelegator delegator = new MockOfBizDelegator();

    @After
    public void tearDown()
    {
        delegator = null;
    }


    @Test
    public void testSkipsIf6307IsPresent() throws Exception
    {
        delegator.createValue(new MockGenericValue("UpgradeHistory", FieldMap.build(
                "upgradeclass", "com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6307"
        )));

        final UpgradeTask_Build6317 fixture = new UpgradeTask_Build6317(delegator, null)
        {
            @Override
            void upgradeFilterSubscriptionSchedules()
            {
                fail("Upgrade should not happen when 6307 is present");
            }
        };
        fixture.doUpgrade(false);
    }

    @Test
    public void testRunsIf6307IsMissing() throws Exception
    {
        delegator.createValue(new MockGenericValue("UpgradeHistory", FieldMap.build(
                "upgradeclass", "com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6303"
        )));

        final AtomicBoolean called = new AtomicBoolean();
        final UpgradeTask_Build6317 fixture = new UpgradeTask_Build6317(delegator, null)
        {
            @Override
            void upgradeFilterSubscriptionSchedules()
            {
                called.set(true);
            }
        };
        fixture.doUpgrade(false);

        assertThat("Upgrade should proceed when 6307 is missing", called.get(), is(true));
    }
}
