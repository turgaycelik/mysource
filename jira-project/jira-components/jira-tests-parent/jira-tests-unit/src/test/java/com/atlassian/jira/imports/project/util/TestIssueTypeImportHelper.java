package com.atlassian.jira.imports.project.util;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueTypeImportHelper
{
    @Test
    public void testGetIssueTypeForNameNullName()
    {
        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper(null, null, null);
        assertNull(issueTypeImportHelper.getIssueTypeForName(null));
    }

    @Test
    public void testGetIssueTypeForNameNotFound()
    {
        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.expectAndReturn("getAllIssueTypeObjects", EasyList.build(new MockIssueType("12", "Bloob"), new MockIssueType("13", "Shnuffle")));
        mockConstantsManager.setStrict(true);

        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper((ConstantsManager) mockConstantsManager.proxy(), null, null);
        assertNull(issueTypeImportHelper.getIssueTypeForName("werrbnock"));

        mockConstantsManager.verify();
    }

    @Test
    public void testGetIssueTypeForName()
    {
        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        final MockIssueType shnuffle = new MockIssueType("13", "Shnuffle");
        mockConstantsManager.expectAndReturn("getAllIssueTypeObjects", EasyList.build(new MockIssueType("12", "Bloob"), shnuffle));
        mockConstantsManager.setStrict(true);

        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper((ConstantsManager) mockConstantsManager.proxy(), null, null);
        assertEquals(shnuffle, issueTypeImportHelper.getIssueTypeForName("Shnuffle"));

        mockConstantsManager.verify();
    }

    @Test
    public void testIsIssueTypeValidForProjectFromDefaultSchemeHappyPath()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq("TST")), null);

        Set issueTypes = new HashSet();
        issueTypes.add(new MockIssueType("1", "Bug"));
        issueTypes.add(new MockIssueType("2", "FUBAR"));

        Mock mockIssueTypeSchemeManager = new Mock(IssueTypeSchemeManager.class);
        mockIssueTypeSchemeManager.expectAndReturn("getIssueTypesForDefaultScheme", issueTypes);
        mockIssueTypeSchemeManager.setStrict(true);

        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper(null, (ProjectManager) mockProjectManager.proxy(), (IssueTypeSchemeManager) mockIssueTypeSchemeManager.proxy());
        assertTrue(issueTypeImportHelper.isIssueTypeValidForProject("TST", "2"));

        mockProjectManager.verify();
        mockIssueTypeSchemeManager.verify();
    }

    @Test
    public void testIsIssueTypeValidForProjectFromDefaultSchemeAngryPath()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        final MockProject mockProject = new MockProject(1, "TST");
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq("TST")), mockProject);

        Set issueTypes = new HashSet();
        issueTypes.add(new MockIssueType("1", "Bug"));
        issueTypes.add(new MockIssueType("2", "FUBAR"));

        Mock mockIssueTypeSchemeManager = new Mock(IssueTypeSchemeManager.class);
        mockIssueTypeSchemeManager.expectAndReturn("getIssueTypesForProject", P.args(P.eq(mockProject)), issueTypes);
        mockIssueTypeSchemeManager.setStrict(true);

        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper(null, (ProjectManager) mockProjectManager.proxy(), (IssueTypeSchemeManager) mockIssueTypeSchemeManager.proxy());
        assertFalse(issueTypeImportHelper.isIssueTypeValidForProject("TST", "3"));

        mockProjectManager.verify();
        mockIssueTypeSchemeManager.verify();
    }

    @Test
    public void testIsMappingValidNotGoodForProject()
    {
        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper(null, null, null)
        {
            public boolean isIssueTypeValidForProject(final String projectKey, final String newIssueTypeId)
            {
                return false;
            }
        };
        assertFalse(issueTypeImportHelper.isMappingValid(new MockIssueType("1", "asdf"), "TST", false));
    }

    @Test
    public void testIsMappingValidSubTasknessDoesntMatch()
    {
        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper(null, null, null)
        {
            public boolean isIssueTypeValidForProject(final String projectKey, final String newIssueTypeId)
            {
                return true;
            }
        };
        assertFalse(issueTypeImportHelper.isMappingValid(new MockIssueType("1", "asdf"), "TST", true));
    }

    @Test
    public void testIsMappingValidHappyPath()
    {
        IssueTypeImportHelper issueTypeImportHelper = new IssueTypeImportHelper(null, null, null)
        {
            public boolean isIssueTypeValidForProject(final String projectKey, final String newIssueTypeId)
            {
                return true;
            }
        };
        assertTrue(issueTypeImportHelper.isMappingValid(new MockIssueType("1", "asdf", true), "TST", true));
    }


}
