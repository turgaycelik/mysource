package com.atlassian.jira.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.TestCase;

import static org.mockito.Mockito.verify;

/**
 * Test the updating of the icon URLs.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build843 extends TestCase
{
    @Mock
    ConstantsManager mockConstantsManager;

    MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(getInitialValues(), getExpectedValues());

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
    }


    @Test
    public void testUpgrade() throws Exception
    {
        UpgradeTask_Build843 upgradeTask_build843 = new UpgradeTask_Build843(mockOfBizDelegator, mockConstantsManager);
        upgradeTask_build843.doUpgrade(false);

        assertTrue(upgradeTask_build843.getErrors().isEmpty());
        assertFalse(upgradeTask_build843.isReindexRequired());

        mockOfBizDelegator.verify();
        verify(mockConstantsManager).refresh();
    }

    List<GenericValue> getInitialValues()
    {
        return Arrays.asList(
                fakeGV("IssueType", 1, "Bug", "/images/icons/bug.gif"),
                fakeGV("IssueType", 2, "Task", "/images/icons/task.gif"),
                fakeGV("IssueType", 3, "Sub-Task", "/images/icons/task.gif"),
                fakeGV("IssueType", 4, "Improvement", "http://jira.atlassian.com/images/icons/improvement.gif"),
                fakeGV("Priority", 1, "Major", "/images/icons/priority_major.gif"),
                fakeGV("Priority", 2, "Trivial", "/images/icons/priority_trivial.gif"),
                fakeGV("Status", 1, "Open", "/images/icons/status_open.gif"),
                fakeGV("Status", 2, "Custom", "/images/icons/blank.gif")
        );
    }

    List<GenericValue> getExpectedValues()
    {
        return Arrays.asList(
                fakeGV("IssueType", 1, "Bug", "/images/icons/issuetypes/bug.png"),
                fakeGV("IssueType", 2, "Task", "/images/icons/issuetypes/task.png"),
                fakeGV("IssueType", 3, "Sub-Task", "/images/icons/issuetypes/task.png"),
                fakeGV("IssueType", 4, "Improvement", "http://jira.atlassian.com/images/icons/improvement.gif"),
                fakeGV("Priority", 1, "Major", "/images/icons/priorities/major.png"),
                fakeGV("Priority", 2, "Trivial", "/images/icons/priorities/trivial.png"),
                fakeGV("Status", 1, "Open", "/images/icons/statuses/open.png"),
                fakeGV("Status", 2, "Custom", "/images/icons/blank.gif")
        );
    }

    private GenericValue fakeGV(String entityName, long id, String name, String iconUrl)
    {
        return new MockGenericValue(entityName, FieldMap.build("id", id).add("name", name).add("iconurl", iconUrl));
    }

}
