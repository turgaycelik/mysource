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
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCustomFieldUserStatisticsMapper extends MockControllerTestCase
{
    private UserManager userManager;
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;
    private JiraAuthenticationContext authenticationContext;

    @Before
    public void setUp() throws Exception
    {
        userManager = EasyMock.createMock(UserManager.class);
        customField = EasyMock.createMock(CustomField.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        authenticationContext = getMock(JiraAuthenticationContext.class);
    }

    @Test
    public void testEquals() throws Exception
    {
        EasyMock.expect(customField.getIdAsLong()).andReturn(10001l);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        EasyMock.expect(customField.getIdAsLong()).andReturn(10001l);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        EasyMock.expect(customField.getClauseNames()).andReturn(new ClauseNames("test"));

        final CustomField customField1 = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customField1.getIdAsLong()).andReturn(10002l);
        EasyMock.expect(customField1.getId()).andReturn("customfield_10002");

        replay();
        EasyMock.replay(customField, customField1);

        CustomFieldUserStatisticsMapper userStatisticsMapper = new CustomFieldUserStatisticsMapper(customField, userManager, authenticationContext, customFieldInputHelper);

        assertTrue(userStatisticsMapper.equals(userStatisticsMapper));
        assertEquals(userStatisticsMapper.hashCode(), userStatisticsMapper.hashCode());

        CustomFieldUserStatisticsMapper projectStatisticsMapper1 = new CustomFieldUserStatisticsMapper(customField, userManager, authenticationContext, customFieldInputHelper);
        // As the mappers are using the same custom field they should be equal

        assertTrue(userStatisticsMapper.equals(projectStatisticsMapper1));
        assertEquals(userStatisticsMapper.hashCode(), projectStatisticsMapper1.hashCode());

        CustomFieldUserStatisticsMapper projectStatisticsMapper2 = new CustomFieldUserStatisticsMapper(customField1, userManager, authenticationContext, customFieldInputHelper);

        // As the mappers are using different custom field they should *not* be equal
        assertFalse(userStatisticsMapper.equals(projectStatisticsMapper2));
        assertFalse(userStatisticsMapper.hashCode() == projectStatisticsMapper2.hashCode());

        assertFalse(userStatisticsMapper.equals(null));
        assertFalse(userStatisticsMapper.equals(null));
        assertFalse(userStatisticsMapper.equals(new Object()));
        assertFalse(userStatisticsMapper.equals(new IssueKeyStatisticsMapper()));
        assertFalse(userStatisticsMapper.equals(new IssueTypeStatisticsMapper(null)));
        assertFalse(userStatisticsMapper.equals(new UserPickerStatisticsMapper(null, null, null)));
        assertFalse(userStatisticsMapper.equals(new TextStatisticsMapper(null)));
        assertFalse(userStatisticsMapper.equals(new ProjectSelectStatisticsMapper(null, null)));
        assertFalse(userStatisticsMapper.equals(new GroupPickerStatisticsMapper(customField, null, authenticationContext, customFieldInputHelper)));

        verify();
        EasyMock.verify(customField, customField1);
    }

    @Test
    public void testGetUrlSuffixForUniqueCustomFieldName() throws Exception
    {
        final SearchRequest searchRequest = getMock(SearchRequest.class);
        expect(searchRequest.getQuery()).andReturn(new QueryImpl());

        final User user = new MockUser("fred");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);

        EasyMock.expect(customField.getIdAsLong()).andReturn(10001l);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        final ClauseNames clauseNames = new ClauseNames("cf[10001]");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getName()).andReturn("My Custom CF");

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(user, "cf[10001]", "My Custom CF")).andReturn("My Custom CF");

        replay();
        EasyMock.replay(customField);
        CustomFieldUserStatisticsMapper userStatisticsMapper = new CustomFieldUserStatisticsMapper(customField, userManager, authenticationContext, customFieldInputHelper);

        User value = new MockUser("humptydumpty");

        final SearchRequest modifiedSearchRequest = userStatisticsMapper.getSearchUrlSuffix(value, searchRequest);

        final Query query = modifiedSearchRequest.getQuery();

        assertEquals(new QueryImpl(new TerminalClauseImpl("My Custom CF", Operator.EQUALS, "humptydumpty")), query);

        verify();
        EasyMock.verify(customField);
    }

    @Test
    public void testGetUrlSuffixForNonUniqueCustomFieldName() throws Exception
    {
        final SearchRequest searchRequest = getMock(SearchRequest.class);
        expect(searchRequest.getQuery()).andReturn(new QueryImpl());

        final User user = new MockUser("fred");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        EasyMock.expect(customField.getIdAsLong()).andReturn(10001l);
        final ClauseNames clauseNames = new ClauseNames("cf[10001]");
        EasyMock.expect(customField.getClauseNames()).andReturn(clauseNames);
        EasyMock.expect(customField.getName()).andReturn("My Custom CF");

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(user, "cf[10001]", "My Custom CF")).andReturn("cf[10001]");

        replay();
        EasyMock.replay(customField);
        CustomFieldUserStatisticsMapper userStatisticsMapper = new CustomFieldUserStatisticsMapper(customField, userManager, authenticationContext, customFieldInputHelper);

        final User value = new MockUser("Eggskymp");
        final SearchRequest modifiedSearchRequest = userStatisticsMapper.getSearchUrlSuffix(value, searchRequest);

        final Query query = modifiedSearchRequest.getQuery();

        assertEquals(new QueryImpl(new TerminalClauseImpl("cf[10001]", Operator.EQUALS, "Eggskymp")), query);

        verify();
        EasyMock.verify(customField);
    }

}
