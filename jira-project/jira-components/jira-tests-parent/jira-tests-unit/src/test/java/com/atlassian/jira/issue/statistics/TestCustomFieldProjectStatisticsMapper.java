package com.atlassian.jira.issue.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.GroupPickerStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.ProjectSelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.TextStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.UserPickerStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCustomFieldProjectStatisticsMapper extends MockControllerTestCase
{
    private ProjectManager projectManager;
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;
    private JiraAuthenticationContext authenticationContext;

    @Before
    public void setUp() throws Exception
    {
        projectManager = getMock(ProjectManager.class);
        customField = EasyMock.createMock(CustomField.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        authenticationContext = getMock(JiraAuthenticationContext.class);
    }

    @Test
    public void testEquals() throws Exception
    {
        final ClauseNames clauseNames = new ClauseNames("cf[10001]");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);

        final CustomField customField1 = EasyMock.createMock(CustomField.class);
        final ClauseNames clauseNames1 = new ClauseNames("cf[10002]");
        EasyMock.expect(customField1.getClauseNames()).andReturn(clauseNames1);
        EasyMock.expect(customField1.getId()).andReturn("customfield_10002");

        replay();
        EasyMock.replay(customField, customField1);

        CustomFieldProjectStatisticsMapper projectStatisticsMapper = new CustomFieldProjectStatisticsMapper(projectManager, customField, customFieldInputHelper, authenticationContext);

        assertTrue(projectStatisticsMapper.equals(projectStatisticsMapper));
        assertEquals(projectStatisticsMapper.hashCode(), projectStatisticsMapper.hashCode());

        CustomFieldProjectStatisticsMapper projectStatisticsMapper1 = new CustomFieldProjectStatisticsMapper(projectManager, customField, customFieldInputHelper, authenticationContext);
        // As the mappers are using the same custom field they should be equal

        assertTrue(projectStatisticsMapper.equals(projectStatisticsMapper1));
        assertEquals(projectStatisticsMapper.hashCode(), projectStatisticsMapper1.hashCode());

        CustomFieldProjectStatisticsMapper projectStatisticsMapper2 = new CustomFieldProjectStatisticsMapper(projectManager, customField1, customFieldInputHelper, authenticationContext);

        // As the mappers are using different custom field they should *not* be equal
        assertFalse(projectStatisticsMapper.equals(projectStatisticsMapper2));
        assertFalse(projectStatisticsMapper.hashCode() == projectStatisticsMapper2.hashCode());

        assertFalse(projectStatisticsMapper.equals(null));
        assertFalse(projectStatisticsMapper.equals(null));
        assertFalse(projectStatisticsMapper.equals(new Object()));
        assertFalse(projectStatisticsMapper.equals(new IssueKeyStatisticsMapper()));
        assertFalse(projectStatisticsMapper.equals(new IssueTypeStatisticsMapper(null)));
        assertFalse(projectStatisticsMapper.equals(new UserPickerStatisticsMapper(null, null, null)));
        assertFalse(projectStatisticsMapper.equals(new TextStatisticsMapper(null)));
        assertFalse(projectStatisticsMapper.equals(new ProjectSelectStatisticsMapper(null, null)));
        assertFalse(projectStatisticsMapper.equals(new GroupPickerStatisticsMapper(customField, null, authenticationContext, customFieldInputHelper)));

        verify();
        EasyMock.verify(customField, customField1);
    }

    @Test
    public void testGetUrlSuffixForUniqueCustomFieldName() throws Exception
    {
        GenericValue value = new MockGenericValue("component");
        value.set("id", 123l);
        value.set("name", "fixed");
        final SearchRequest searchRequest = getMock(SearchRequest.class);
        expect(searchRequest.getQuery()).andReturn(new QueryImpl());

        final MockProject project = new MockProject(123, "PR");
        expect(projectManager.getProjectObj(123l)).andReturn(project);

        final User user = new MockUser("fred");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        final ClauseNames clauseNames = new ClauseNames("cf[10001]");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getName()).andReturn("My Custom CF");

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(user, "cf[10001]", "My Custom CF")).andReturn("My Custom CF");

        replay();
        EasyMock.replay(customField);
        CustomFieldProjectStatisticsMapper projectStatisticsMapper = new CustomFieldProjectStatisticsMapper(projectManager, customField, customFieldInputHelper, authenticationContext);

        final SearchRequest modifiedSearchRequest = projectStatisticsMapper.getSearchUrlSuffix(value, searchRequest);

        final Query query = modifiedSearchRequest.getQuery();

        assertEquals(new QueryImpl(new TerminalClauseImpl("My Custom CF", Operator.EQUALS, "PR")), query);

        verify();
        EasyMock.verify(customField);
    }

    @Test
    public void testGetUrlSuffixForNonUniqueCustomFieldName() throws Exception
    {
        GenericValue value = new MockGenericValue("component");
        value.set("id", 123l);
        value.set("name", "fixed");
        final SearchRequest searchRequest = getMock(SearchRequest.class);
        expect(searchRequest.getQuery()).andReturn(new QueryImpl());

        final MockProject project = new MockProject(123, "PR");
        expect(projectManager.getProjectObj(123l)).andReturn(project);

        final User user = new MockUser("fred");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        final ClauseNames clauseNames = new ClauseNames("cf[10001]");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getName()).andReturn("My Custom CF");

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(user, "cf[10001]", "My Custom CF")).andReturn("cf[10001]");

        replay();
        EasyMock.replay(customField);
        CustomFieldProjectStatisticsMapper projectStatisticsMapper = new CustomFieldProjectStatisticsMapper(projectManager, customField, customFieldInputHelper, authenticationContext);

        final SearchRequest modifiedSearchRequest = projectStatisticsMapper.getSearchUrlSuffix(value, searchRequest);

        final Query query = modifiedSearchRequest.getQuery();

        assertEquals(new QueryImpl(new TerminalClauseImpl("cf[10001]", Operator.EQUALS, "PR")), query);

        verify();
        EasyMock.verify(customField);
    }

}
