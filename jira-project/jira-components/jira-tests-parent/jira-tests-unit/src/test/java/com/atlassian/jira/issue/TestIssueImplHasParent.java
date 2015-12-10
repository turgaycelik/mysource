package com.atlassian.jira.issue;

import java.util.Collections;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.version.VersionManager;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestIssueImplHasParent
{

    @Test
    public void testIssueImplHasNoParent()
    {
        MockGenericValue gv = new MockGenericValue("Issue");

        Mock subTaskManagerMock = new Mock(SubTaskManager.class);
        subTaskManagerMock.expectAndReturn("getParentIssueId", P.args(P.eq(gv)), null);
        SubTaskManager subTaskManager = (SubTaskManager) subTaskManagerMock.proxy();

        IssueImpl issue = new IssueImpl(gv, null, null, null, null, null, subTaskManager, null, null, null, null, null);
        assertNull(issue.getParentId());
    }

    @Test
    public void testIssueImplHasParent()
    {
        MockGenericValue gv = new MockGenericValue("Issue");

        Mock subTaskManagerMock = new Mock(SubTaskManager.class);
        Long parentId = new Long(999);
        subTaskManagerMock.expectAndReturn("getParentIssueId", P.args(P.eq(gv)), parentId);
        SubTaskManager subTaskManager = (SubTaskManager) subTaskManagerMock.proxy();

        IssueImpl issue = new IssueImpl(gv, null, null, null, null, null, subTaskManager, null, null, null, null, null);
        assertEquals(parentId, issue.getParentId());
    }

    /**
     * Test to see if you can override the has parent call.
     * issue.hasNoParentId can be set to ensure in memory issues don't go to the database to get parent issue id
     * Set to true to override.
     */
    @Test
    public void testIssueImplHasParentButOverriden()
    {
        MockGenericValue gv = new MockGenericValue("Issue");

        Mock subTaskManagerMock = new Mock(SubTaskManager.class);
        Long parentId = new Long(999);
        subTaskManagerMock.expectAndReturn("getParentIssueId", P.args(P.eq(gv)), parentId);
        SubTaskManager subTaskManager = (SubTaskManager) subTaskManagerMock.proxy();

        IssueImpl issue = new IssueImpl(gv, null, null, null, null, null, subTaskManager, null, null, null, null, null);
        issue.hasNoParentId = true;

        assertNull(issue.getParentId());
    }

    @Test
    public void testCopyIssueHasSameParent() throws Exception
    {
        MockIssue mock = new MockIssue(new Long(123))
        {
            protected GenericValue getHackedGVThatReturnsId()
            {
                return null;
            }
        };

        final ProjectComponentManager projectComponentManager = createProjectComponentManager();
        // check with no parent id
        IssueImpl copy = new IssueImpl(mock, null, null, null, null, null, null, null, null, projectComponentManager, null, null);
        assertEquals(mock.getParentId(), copy.getParentId());

        // check with parent id
        mock.setParentId(new Long(987));
        copy = new IssueImpl(mock, null, null, null, null, null, null, null, null, projectComponentManager, null, null);
        assertEquals(mock.getParentId(), copy.getParentId());
    }

    @Test
    public void testCopyIssueHasSameParentGVNoParent() throws Exception
    {
        MockGenericValue gv = new MockGenericValue("Issue");

        SubTaskManager subTaskManager = createSubTaskManager(gv, null);
        VersionManager versionManager = createVersionManager();
        IssueManager issueManager = createIssueManager();
        LabelManager labelManager = createLabelManager();
        final ProjectComponentManager projectComponentManager = createProjectComponentManager();

        IssueImpl issue = new IssueImpl(gv, issueManager, null, versionManager, null, null, subTaskManager, null, labelManager, projectComponentManager, null, null);
        assertNull(issue.getParentId());

        IssueImpl copy = new IssueImpl(issue, null, null, null, null, null, null, null, null, projectComponentManager, null, null);
        assertEquals(issue.getParentId(), copy.getParentId());
    }

    @Test
    public void testCopyIssueHasSameParentGVWithParent() throws Exception
    {
        MockGenericValue gv = new MockGenericValue("Issue");

        SubTaskManager subTaskManager = createSubTaskManager(gv, new Long(987));
        VersionManager versionManager = createVersionManager();
        IssueManager issueManager = createIssueManager();
        LabelManager labelManager = createLabelManager();
        final ProjectComponentManager projectComponentManager = createProjectComponentManager();

        IssueImpl issue = new IssueImpl(gv, issueManager, null, versionManager, null, null, subTaskManager, null, labelManager, projectComponentManager, null, null);
        assertEquals(new Long(987), issue.getParentId());

        IssueImpl copy = new IssueImpl(issue, null, null, null, null, null, null, null, null, projectComponentManager, null, null);
        assertEquals(issue.getParentId(), copy.getParentId());
    }

    private SubTaskManager createSubTaskManager(MockGenericValue gv, Long parentIssueId)
    {
        Mock subTaskManagerMock = new Mock(SubTaskManager.class);
        subTaskManagerMock.expectAndReturn("getParentIssueId", P.args(P.eq(gv)), parentIssueId);
        return (SubTaskManager) subTaskManagerMock.proxy();
    }

    private IssueManager createIssueManager()
    {
        Mock issueManagerMock = new Mock(IssueManager.class);
        issueManagerMock.expectAndReturn("getEntitiesByIssue", P.ANY_ARGS, Collections.EMPTY_LIST);
        return (IssueManager) issueManagerMock.proxy();
    }

    private ProjectComponentManager createProjectComponentManager()
    {
        Mock projectComponentManager = new Mock(ProjectComponentManager.class);
        projectComponentManager.expectAndReturn("findComponentsByIssueGV", P.ANY_ARGS, Collections.EMPTY_LIST);
        return (ProjectComponentManager) projectComponentManager.proxy();
    }

    private LabelManager createLabelManager()
    {
        Mock labelManagerMock = new Mock(LabelManager.class);
        labelManagerMock.expectAndReturn("getLabels", P.ANY_ARGS, Collections.emptySet());
        return (LabelManager) labelManagerMock.proxy();
    }

    private VersionManager createVersionManager()
    {
        Mock versionManagerMock = new Mock(VersionManager.class);
        versionManagerMock.expectAndReturn("getAffectedVersionsFor", P.ANY_ARGS, Collections.EMPTY_LIST);
        versionManagerMock.expectAndReturn("getFixVersionsFor", P.ANY_ARGS, Collections.EMPTY_LIST);
        return (VersionManager) versionManagerMock.proxy();
    }
}
