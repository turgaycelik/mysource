package com.atlassian.jira.jql;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.search.BooleanQuery;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestNoOpClauseHandler extends MockControllerTestCase
{
    private ClauseNames names;
    private NoOpClauseHandler noOpClauseHandler;
    private ClausePermissionHandler clausePermissionHandler;

    @Before
    public void setUp() throws Exception
    {
        this.clausePermissionHandler = EasyMock.createMock(ClausePermissionHandler.class);

        names = new ClauseNames("primaryName", "secondaryName");
        this.noOpClauseHandler = new NoOpClauseHandler(clausePermissionHandler, "fieldId", names, "jira.jql.validation.no.such.field")
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
    }

    @Test
    public void testReturnsFalseQueries() throws Exception
    {
        assertEquals(new QueryFactoryResult(new BooleanQuery()), noOpClauseHandler.getFactory().getQuery(null, null));
    }

    @Test
    public void testReturnsCorrectValidationMessage() throws Exception
    {
        final MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("Field 'dude' does not exist or you do not have permission to view it.");
        assertEquals(messageSet, noOpClauseHandler.getValidator().validate(null, new TerminalClauseImpl("dude", Operator.EQUALS, "sweet")));
    }

    @Test
    public void testReturnsCorrectContext() throws Exception
    {
        assertEquals(ClauseContextImpl.createGlobalClauseContext(),
                noOpClauseHandler.getClauseContextFactory().getClauseContext(null, new TerminalClauseImpl("dude", Operator.EQUALS, "sweet")));
    }

    @Test
    public void testReturnsCorrectPermission() throws Exception
    {
        EasyMock.expect(clausePermissionHandler.hasPermissionToUseClause(null)).andReturn(true);
        EasyMock.replay(clausePermissionHandler);
        assertEquals(true, noOpClauseHandler.getPermissionHandler().hasPermissionToUseClause(null));
    }

    @Test
    public void testClauseInformationIsCorrect() throws Exception
    {
        assertEquals("fieldId", noOpClauseHandler.getInformation().getFieldId());
        assertEquals("fieldId", noOpClauseHandler.getInformation().getIndexField());
        assertEquals(names, noOpClauseHandler.getInformation().getJqlClauseNames());
        assertEquals(Collections.<Operator>emptySet(), noOpClauseHandler.getInformation().getSupportedOperators());
        assertEquals(JiraDataTypes.ALL, noOpClauseHandler.getInformation().getDataType());
    }
    
}
