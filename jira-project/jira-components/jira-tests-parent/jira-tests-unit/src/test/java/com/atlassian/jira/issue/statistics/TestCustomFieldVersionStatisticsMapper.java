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
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCustomFieldVersionStatisticsMapper extends MockControllerTestCase
{
    private VersionManager versionManager;
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;
    private JiraAuthenticationContext authenticationContext;

    @Before
    public void setUp() throws Exception
    {
        versionManager = getMock(VersionManager.class);
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

        CustomFieldVersionStatisticsMapper userStatisticsMapper = new CustomFieldVersionStatisticsMapper(customField, versionManager, authenticationContext, customFieldInputHelper, false);

        assertTrue(userStatisticsMapper.equals(userStatisticsMapper));
        assertEquals(userStatisticsMapper.hashCode(), userStatisticsMapper.hashCode());

        CustomFieldVersionStatisticsMapper userStatisticsMapper1 = new CustomFieldVersionStatisticsMapper(customField, versionManager, authenticationContext, customFieldInputHelper, false);
        // As the mappers are using the same custom field they should be equal

        assertTrue(userStatisticsMapper.equals(userStatisticsMapper1));
        assertEquals(userStatisticsMapper.hashCode(), userStatisticsMapper1.hashCode());

        CustomFieldVersionStatisticsMapper userStatisticsMapper2 = new CustomFieldVersionStatisticsMapper(customField1, versionManager, authenticationContext, customFieldInputHelper, false);

        // As the mappers are using different custom field they should *not* be equal
        assertFalse(userStatisticsMapper.equals(userStatisticsMapper2));
        assertFalse(userStatisticsMapper.hashCode() == userStatisticsMapper2.hashCode());

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
        CustomFieldVersionStatisticsMapper versionStatisticsMapper = new CustomFieldVersionStatisticsMapper(customField, versionManager, authenticationContext, customFieldInputHelper, false);

        Version version = new MockVersion(123l,"Version 1.0", new MockProject(1000l,"DUMPTY"));

        final SearchRequest modifiedSearchRequest = versionStatisticsMapper.getSearchUrlSuffix(version, searchRequest);

        final Query query = modifiedSearchRequest.getQuery();

        final TerminalClauseImpl clause1 = new TerminalClauseImpl("project", Operator.EQUALS, "DUMPTY");
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("My Custom CF", Operator.EQUALS, "Version 1.0");
        assertEquals(new QueryImpl(new AndClause(clause1, clause2)), query);

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
        CustomFieldVersionStatisticsMapper versionStatisticsMapper = new CustomFieldVersionStatisticsMapper(customField, versionManager, authenticationContext, customFieldInputHelper, false);

        Version version = new MockVersion(123l,"Version 1.0", new MockProject(1000l,"DUMPTY"));
        final SearchRequest modifiedSearchRequest = versionStatisticsMapper.getSearchUrlSuffix(version, searchRequest);

        final Query query = modifiedSearchRequest.getQuery();

        final TerminalClauseImpl clause1 = new TerminalClauseImpl("project", Operator.EQUALS, "DUMPTY");
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("cf[10001]", Operator.EQUALS, "Version 1.0");
        assertEquals(new QueryImpl(new AndClause(clause1, clause2)), query);

        verify();
        EasyMock.verify(customField);
    }

    @Test
    public void testGetValueFromLuceneField()
    {
        Version version = new MockVersion(10020L, "Version 1.0", new MockProject(1000l, "DUMPTY"));
        expect(versionManager.getVersion(10020L)).andReturn(version);
        EasyMock.expect(customField.getId()).andReturn("customfield_10001");
        EasyMock.expect(customField.getIdAsLong()).andReturn(10001l);
        replay();
        EasyMock.replay(customField);
        final CustomFieldVersionStatisticsMapper versionStatisticsMapper = new CustomFieldVersionStatisticsMapper(customField, versionManager, null, null, false);

        final Object nullReturn = versionStatisticsMapper.getValueFromLuceneField(null);
        assertNull(nullReturn);
        final Object anotherNull = versionStatisticsMapper.getValueFromLuceneField("-1");
        assertNull(anotherNull);
        final Object returnedVersion = versionStatisticsMapper.getValueFromLuceneField("10020");
        assertEquals(version, returnedVersion);

        verify();
        EasyMock.verify(customField);
    }

}
