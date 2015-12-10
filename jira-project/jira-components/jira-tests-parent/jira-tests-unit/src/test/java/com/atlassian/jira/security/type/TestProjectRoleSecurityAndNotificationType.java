package com.atlassian.jira.security.type;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v5.2
 */
public class TestProjectRoleSecurityAndNotificationType
{
    @Test
    public void testGetQueryWithNullProject() throws Exception
    {
        ProjectRoleSecurityAndNotificationType projectRoleSecurityAndNotificationType = new ProjectRoleSecurityAndNotificationType(null, null, null);
        Query query = projectRoleSecurityAndNotificationType.getQuery(new MockUser("fred"), (Project) null, "developers");
        assertNull(query);
    }

    @Test
    public void testGetQueryWithProjectOnly() throws Exception
    {
        ProjectRoleSecurityAndNotificationType projectRoleSecurityAndNotificationType = new ProjectRoleSecurityAndNotificationType(null, null, null);
        Query query = projectRoleSecurityAndNotificationType.getQuery(new MockUser("fred"), new MockProject(12), "developers");

        assertEquals("projid:12", query.toString());

        // expect a Term Query
        TermQuery actual = (TermQuery) query;
        assertEquals("projid", actual.getTerm().field());
        assertEquals("12", actual.getTerm().text());
    }

    @Test
    public void testGetQueryWithSecurityLevel() throws Exception
    {
        ProjectRoleSecurityAndNotificationType projectRoleSecurityAndNotificationType = new ProjectRoleSecurityAndNotificationType(null, null, null);
        IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        Query query = projectRoleSecurityAndNotificationType.getQuery(new MockUser("fred"), new MockProject(12, "ABC"), securityLevel, "developers");

        assertEquals("+issue_security_level:10100 +projid:12", query.toString());
    }
}
