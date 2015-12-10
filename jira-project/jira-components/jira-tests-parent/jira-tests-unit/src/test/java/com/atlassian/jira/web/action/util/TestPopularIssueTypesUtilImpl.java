package com.atlassian.jira.web.action.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.MockIssueConstantFactory;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPopularIssueTypesUtilImpl extends MockControllerTestCase
{
    private static final MockIssueType ISSUE_TYPE_1 = new MockIssueType("1", "Not Popular 1", false);
    private static final MockIssueType ISSUE_TYPE_2 = new MockIssueType("2", "Not Popular 2", false);
    private static final MockIssueType ISSUE_TYPE_3 = new MockIssueType("3", "Not Popular 3", false);
    private static final MockIssueType ISSUE_TYPE_4 = new MockIssueType("4", "Not Popular 4", false);

    private static final MockIssueType SUB_ISSUE_TYPE_1 = new MockIssueType("11", "Sub Not Popular 1", true);
    private static final MockIssueType SUB_ISSUE_TYPE_2 = new MockIssueType("22", "Sub Not Popular 2", true);

    @Test
    public void testNoPopularTypesForUserButEnoughFromProject() throws Exception
    {
        MockProject project = new MockProject();

        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_1, ISSUE_TYPE_2));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)
            {
                if (user != null)
                {
                    return Collections.emptySet();
                }
                else
                {
                    return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_1, ISSUE_TYPE_2).asListOrderedSet();
                }
            }
        };

        final List<IssueType> list = util.getPopularIssueTypesForProject(project, new MockUser("admin"));
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_1,  list.get(0));
        assertEquals(ISSUE_TYPE_2,  list.get(1));
        mockController.verify();

    }

    @Test
    public void testNoPopularTypesForUserButTooManyFromProject() throws Exception
    {
        MockProject project = new MockProject();
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3, ISSUE_TYPE_4));

        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)
            {
                if (user != null)
                {
                    return Collections.emptySet();
                }
                else
                {
                    return CollectionBuilder.<IssueType>newBuilder(
                            ISSUE_TYPE_1,
                            ISSUE_TYPE_2,
                            ISSUE_TYPE_3,
                            ISSUE_TYPE_4).asListOrderedSet();
                }
            }
        };

        final List<IssueType> list = util.getPopularIssueTypesForProject(project, new MockUser("admin"));
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_1,  list.get(0));
        assertEquals(ISSUE_TYPE_2,  list.get(1));
        mockController.verify();

    }

    @Test
    public void testOnlySubTaskPopularTypesForUserButEnoughFromProject() throws Exception
    {
        MockProject project = new MockProject();
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3, ISSUE_TYPE_4));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                if (user != null)
                {
                    return CollectionBuilder.<IssueType>newBuilder(SUB_ISSUE_TYPE_1, SUB_ISSUE_TYPE_2).asListOrderedSet();
                }
                else
                {
                    return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_3, ISSUE_TYPE_4).asListOrderedSet();
                }
            }
        };

        final List<IssueType> list = util.getPopularIssueTypesForProject(project, new MockUser("admin"));
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_3,  list.get(0));
        assertEquals(ISSUE_TYPE_4,  list.get(1));
        mockController.verify();

    }

    @Test
    public void testNotEnoughPopularTypesForUser() throws Exception
    {
        final MockProject project = new MockProject(100L);
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_4, ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_4).asListOrderedSet();
            }
        };

        final List<IssueType> list = util.getPopularIssueTypesForProject(project, new MockUser("admin"));
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_4,  list.get(0));
        assertEquals(ISSUE_TYPE_1,  list.get(1));
        mockController.verify();

    }

    @Test
    public void testNotEnoughPopularTypesForProjectOrUser() throws Exception
    {
        final MockProject project = new MockProject(100L);
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_2, ISSUE_TYPE_3, ISSUE_TYPE_4, ISSUE_TYPE_1));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                return Collections.emptySet();
            }
        };

        final List<IssueType> list = util.getPopularIssueTypesForProject(project, new MockUser("admin"));
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_2,  list.get(0));
        assertEquals(ISSUE_TYPE_3,  list.get(1));
        mockController.verify();

    }

    @Test
    public void testOtherIssueTypes() throws Exception
    {
        final MockProject project = new MockProject(100L);
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_4, ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_1, ISSUE_TYPE_2).asListOrderedSet();
            }
        };

        final List<IssueType> list = util.getOtherIssueTypesForProject(new MockProject(100L), new MockUser("admin"));
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_4,  list.get(0));
        assertEquals(ISSUE_TYPE_3,  list.get(1));
        mockController.verify();

    }

    @Test
    public void testConglomerateMethod()
    {
        final MockProject project = new MockProject(100L);
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_4, ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_1, ISSUE_TYPE_2).asListOrderedSet();
            }
        };

        final PopularIssueTypesUtil.PopularIssueTypesHolder lists = util.getPopularAndOtherIssueTypesForProject(new MockProject(100L), new MockUser("admin"));
        List<IssueType> list = lists.getOtherIssueTypes();
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_4,  list.get(0));
        assertEquals(ISSUE_TYPE_3,  list.get(1));

        list = lists.getPopularIssueTypes();
        assertEquals(2, list.size());
        assertEquals(ISSUE_TYPE_1,  list.get(0));
        assertEquals(ISSUE_TYPE_2,  list.get(1));

        mockController.verify();


    }

    @Test
    public void testOverByOneCondition()
    {
        final MockProject project = new MockProject(100L);
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3));
        mockController.replay();


        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3).asListOrderedSet();
            }
        };

        final PopularIssueTypesUtil.PopularIssueTypesHolder lists = util.getPopularAndOtherIssueTypesForProject(new MockProject(100L), new MockUser("admin"));
        List<IssueType> list = lists.getOtherIssueTypes();
        assertTrue(list.isEmpty());

        list = lists.getPopularIssueTypes();
        assertEquals(3, list.size());
        assertEquals(ISSUE_TYPE_1,  list.get(0));
        assertEquals(ISSUE_TYPE_2,  list.get(1));
        assertEquals(ISSUE_TYPE_3,  list.get(2));

    }

    @Test
    public void testOverByOneConditionNotEnough()
    {
        final MockProject project = new MockProject(100L);
        IssueTypeSchemeManager issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        mockController.setReturnValue(EasyList.build(ISSUE_TYPE_1, ISSUE_TYPE_2, ISSUE_TYPE_3));
        mockController.replay();

        PopularIssueTypesUtilImpl util = new PopularIssueTypesUtilImpl(issueTypeSchemeManager, new MockIssueConstantFactory())
        {
            Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)

            {
                return CollectionBuilder.<IssueType>newBuilder(ISSUE_TYPE_2, ISSUE_TYPE_1).asListOrderedSet();
            }
        };

        final PopularIssueTypesUtil.PopularIssueTypesHolder lists = util.getPopularAndOtherIssueTypesForProject(new MockProject(100L), new MockUser("admin"));
        List<IssueType> list = lists.getOtherIssueTypes();
        assertTrue(list.isEmpty());

        list = lists.getPopularIssueTypes();
        assertEquals(3, list.size());
        assertEquals(ISSUE_TYPE_2,  list.get(0));
        assertEquals(ISSUE_TYPE_1,  list.get(1));
        assertEquals(ISSUE_TYPE_3,  list.get(2));

        mockController.verify();

    }

}
