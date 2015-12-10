package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;

import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith (ListeningMockitoRunner.class)
public class TestAffectedVersionRenderer extends TestAbstractVersionRenderer
{
    public TestAffectedVersionRenderer()
    {
        super(SystemSearchConstants.forAffectedVersion().getUrlParameter());
    }

    @Before
    public void setUp()
    {
        super.setUp();

        renderer = new AffectedVersionRenderer(projectManager, versionManager, null, null, null, null, permissionManager, "");
    }
}
