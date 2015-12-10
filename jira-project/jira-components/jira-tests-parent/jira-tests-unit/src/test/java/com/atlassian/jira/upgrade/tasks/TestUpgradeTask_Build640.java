package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;

import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.upgrade.tasks.util.Sequences;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;


/**
 * Responsible for verifying that the upgrade task corrects the value of the &quot;Membership&quot; database sequence.
 * See JRA-24466.
 *
 * @since v4.4
 */
public class TestUpgradeTask_Build640
{
    private Sequences mockSequences;

    @Before
    public void setUpInMemoryDatabase()
    {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    @Before
    public void mockDependencies()
    {
        mockSequences = createMock(Sequences.class);
    }

    @Test
    public void membershipSequenceShouldBeUpdatedOnUpgrade() throws Exception
    {
        final UpgradeTask_Build640 upgradeTask_build640 = new UpgradeTask_Build640(mockSequences);

        mockSequences.update(EasyMock.<Connection>anyObject(), EasyMock.eq("Membership"), EasyMock.eq("cwd_membership"));
        expectLastCall().once();

        replay(mockSequences);

        upgradeTask_build640.doUpgrade(false);

        EasyMock.verify(mockSequences);
    }
}
