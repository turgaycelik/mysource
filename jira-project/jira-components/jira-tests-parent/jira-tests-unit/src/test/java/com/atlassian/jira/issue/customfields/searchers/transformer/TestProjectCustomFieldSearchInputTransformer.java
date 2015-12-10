package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestProjectCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private String id;
    private CustomField customField;
    private JqlOperandResolver jqlOperandResolver;
    private ProjectIndexInfoResolver projectIndexInfoResolver;
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private IndexedInputHelper indexedInputHelper;
    private NameResolver<Project> projectResolver;
    private User searcher = null;
    private CustomFieldInputHelper customFieldInputHelper;
    private ClauseNames clauseNames;

    @Before
    public void setUp() throws Exception
    {
        id = "cf[100]";
        customField = mockController.getMock(CustomField.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        projectIndexInfoResolver = mockController.getMock(ProjectIndexInfoResolver.class);
        fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        indexedInputHelper = mockController.getMock(IndexedInputHelper.class);
        projectResolver = mockController.getMock(NameResolver.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        clauseNames = new ClauseNames(id);
    }

    @Test
    public void testGetParamsFromSearchRequest() throws Exception
    {
        final Query query = mockController.getMock(Query.class);
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final ClauseNames names = clauseNames;
        indexedInputHelper.getAllNavigatorValuesForMatchingClauses(null, names, query);
        mockController.setReturnValue(null);

        mockController.replay();
        final ProjectCustomFieldSearchInputTransformer transformer = new ProjectCustomFieldSearchInputTransformer(id, names, customField, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper)
        {
            @Override
            IndexedInputHelper createIndexedInputHelper()
            {
                return indexedInputHelper;
            }
        };
        transformer.getParamsFromSearchRequest(null, query, searchContext);

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNullQuery() throws Exception
    {
        final Query query = null;
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        mockController.replay();
        final ProjectCustomFieldSearchInputTransformer transformer = new ProjectCustomFieldSearchInputTransformer(id, clauseNames, customField, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper)
        {
            @Override
            IndexedInputHelper createIndexedInputHelper()
            {
                return indexedInputHelper;
            }
        };
        transformer.getParamsFromSearchRequest(null, query, searchContext);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseAllFlag() throws Exception
    {
        mockController.replay();
        final ProjectCustomFieldSearchInputTransformer transformer = new ProjectCustomFieldSearchInputTransformer(id, clauseNames, customField, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper);
        assertNull(transformer.createSearchClause(searcher, "-1"));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseString() throws Exception
    {
        EasyMock.expect(customField.getUntranslatedName())
                .andReturn(id);

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(searcher, id, id))
                .andReturn(id);

        replay();

        final ProjectCustomFieldSearchInputTransformer transformer = new ProjectCustomFieldSearchInputTransformer(id, clauseNames, customField, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper);
        final Clause result = transformer.createSearchClause(searcher, "blurble");
        final Clause exptectedResult = new TerminalClauseImpl(id, Operator.EQUALS, "blurble");
        assertEquals(exptectedResult, result);
    }

    @Test
    public void testGetSearchClauseLongIsProject() throws Exception
    {
        EasyMock.expect(projectResolver.get(789L))
                .andReturn(new MockProject(789L, "MKY"));

        EasyMock.expect(customField.getUntranslatedName())
                .andReturn(id);

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(searcher, id, id))
                .andReturn(id);

        replay();

        final ProjectCustomFieldSearchInputTransformer transformer = new ProjectCustomFieldSearchInputTransformer(id, clauseNames, customField, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper);
        final Clause result = transformer.createSearchClause(searcher, "789");
        final Clause exptectedResult = new TerminalClauseImpl(id, Operator.EQUALS, "MKY");
        assertEquals(exptectedResult, result);
    }

    @Test
    public void testGetSearchClauseLongIsNotProject() throws Exception
    {
        EasyMock.expect(projectResolver.get(789L))
                .andReturn(null);

        EasyMock.expect(customField.getUntranslatedName())
                .andReturn(id);

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(searcher, id, id))
                .andReturn(id);

        replay();

        final ProjectCustomFieldSearchInputTransformer transformer = new ProjectCustomFieldSearchInputTransformer(id, clauseNames, customField, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper);
        final Clause result = transformer.createSearchClause(searcher, "789");
        final Clause exptectedResult = new TerminalClauseImpl(id, Operator.EQUALS, 789L);
        assertEquals(exptectedResult, result);
    }
}
