package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;

import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (ListeningMockitoRunner.class)
public class TestFixForVersionRenderer extends TestAbstractVersionRenderer
{
    public TestFixForVersionRenderer()
    {
        super(SystemSearchConstants.forFixForVersion().getUrlParameter());
    }

    @Before
    public void setUp()
    {
        super.setUp();

        renderer = new FixForVersionRenderer(projectManager, versionManager, null, null, null, null, permissionManager, "");
    }
}
