package com.atlassian.jira.jql.resolver;

import java.util.Collections;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.priority.PriorityImpl;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.easymock.MockControl;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestPriorityResolver
{
    @Test
    public void testGetAll() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getPriorityObjects();
        mockConstantsManagerControl.setReturnValue(Collections.<Priority>emptyList());
        mockConstantsManagerControl.replay();

        PriorityResolver priorityResolver = new PriorityResolver(mockConstantsManager);

        priorityResolver.getAll();
        mockConstantsManagerControl.verify();
    }

    @Test
    public void testNameIsCorrect() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantByNameIgnoreCase(ConstantsManager.PRIORITY_CONSTANT_TYPE, "test");
        mockConstantsManagerControl.setReturnValue(new PriorityImpl(new MockGenericValue("blah"), null, null, null));
        mockConstantsManagerControl.replay();

        PriorityResolver priorityResolver = new PriorityResolver(mockConstantsManager);
        priorityResolver.nameExists("test");

        mockConstantsManagerControl.verify();
    }
}
