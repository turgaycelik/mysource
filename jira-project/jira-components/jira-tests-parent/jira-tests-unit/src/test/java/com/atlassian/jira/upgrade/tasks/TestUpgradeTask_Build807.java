package com.atlassian.jira.upgrade.tasks;

import java.util.Collections;

import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * @since 5.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build807
{
    @Mock
    private OfBizDelegator delegator;

    @Test
    public void testUpgrade()
    {
        UpgradeTask_Build807 upgradeTask_build807 = new UpgradeTask_Build807(delegator);
        assertEquals("807", upgradeTask_build807.getBuildNumber());

        upgradeTask_build807.doUpgrade(true);

        verify(delegator).removeByAnd("DraftWorkflowSchemeEntity", Collections.<String, String>emptyMap());
        verify(delegator).removeByAnd("DraftWorkflowScheme", Collections.<String, String>emptyMap());

        assertTrue(upgradeTask_build807.getErrors().isEmpty());
        assertFalse(upgradeTask_build807.isReindexRequired());
    }
}
