package com.atlassian.jira.issue;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.EasyMock;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;

public class TestDefaultIssueFactory
{
    @Test
    public void testCopyHasSameParentAsOriginal() throws Exception
    {
        ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        //noinspection deprecation
        EasyMock.expect(projectComponentManager.findComponentsByIssueGV(EasyMock.<Issue>anyObject())).andReturn(CollectionBuilder.<GenericValue>list()).anyTimes();
        EasyMock.replay(projectComponentManager);
        DefaultIssueFactory factory = new DefaultIssueFactory(null, null, null, null, null, null, null, null, null, null, projectComponentManager, null, null, null);

        MockIssue mock = new MockIssue((long) 123)
        {
            protected GenericValue getHackedGVThatReturnsId()
            {
                return null;
            }
        };

        Issue copy = factory.cloneIssue(mock);
        assertEquals(mock.getParentId(), copy.getParentId());

        mock.setParentId(new Long(987));
        copy = factory.cloneIssue(mock);
        assertEquals(mock.getParentId(), copy.getParentId());

        EasyMock.verify(projectComponentManager);
    }
}
