package com.atlassian.jira.jql.resolver;

import java.util.Collections;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.easymock.MockControl;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestResolutionResolver
{
    @Test
    public void testGetAll() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getResolutionObjects();
        mockConstantsManagerControl.setReturnValue(Collections.<Resolution>emptyList());
        mockConstantsManagerControl.replay();

        ResolutionResolver resolutionResolver = new ResolutionResolver(mockConstantsManager);

        resolutionResolver.getAll();
        mockConstantsManagerControl.verify();
    }

    @Test
    public void testNameIsCorrect() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getConstantByNameIgnoreCase(ConstantsManager.RESOLUTION_CONSTANT_TYPE, "test");
        mockConstantsManagerControl.setReturnValue(new ResolutionImpl(new MockGenericValue("blah"), null, null, null));
        mockConstantsManagerControl.replay();

        ResolutionResolver resolutionResolver = new ResolutionResolver(mockConstantsManager);
        resolutionResolver.nameExists("test");

        mockConstantsManagerControl.verify();
    }
}
