package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.ConstantsManager;

import org.easymock.MockControl;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestResolutionSearchRenderer
{
    @Test
    public void testGetSelectListOptions() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getResolutionObjects();
        mockConstantsManagerControl.setReturnValue(null);
        mockConstantsManagerControl.replay();
        final ResolutionSearchRenderer searchRenderer = new ResolutionSearchRenderer("test", mockConstantsManager, null, null, null, null);

        searchRenderer.getSelectListOptions(null);

        mockConstantsManagerControl.verify();
    }
}
