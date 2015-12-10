package com.atlassian.jira.plugin.jql.function;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAllSubIssueTypeFunction
{
    @Mock ConstantsManager constantsManager;
    @Mock SubTaskManager subTaskManager;

    private JqlFunctionModuleDescriptor moduleDescriptor;
    private TerminalClause terminalClause;

    @Before
    public void setUp()
    {
        moduleDescriptor = MockJqlFunctionModuleDescriptor.create("subIssueTypes", true);
    }

    @After
    public void tearDown()
    {
        moduleDescriptor = null;
        terminalClause = null;
    }

    @Test
    public void testDataType() throws Exception
    {
        final AllSubIssueTypesFunction handler = new Fixture();
        assertEquals(JiraDataTypes.ISSUE_TYPE, handler.getDataType());        
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testBadConstructorNullConstantsManager() throws Exception
    {
        new AllSubIssueTypesFunction(constantsManager, null);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testBadConstructorNullSubTaskManager() throws Exception
    {
        new AllSubIssueTypesFunction(null, subTaskManager);
    }

    @Test
    public void testGetValues()
    {
        final IssueType issueType1 = new MockIssueType("1", "testsub");
        when(constantsManager.getSubTaskIssueTypeObjects()).thenReturn(ImmutableList.of(issueType1));

        final AllSubIssueTypesFunction handler = new Fixture();
        final List<QueryLiteral> queryLiteralList = handler.getValues(null, new FunctionOperand("blarg!"), terminalClause);
        assertThat(queryLiteralList, hasSize(1));
        assertThat(queryLiteralList.get(0).getStringValue(), equalTo("1"));
    }

    @Test
    public void testValidateWithNoArgsSubTasksDisabled()
    {
        final AllSubIssueTypesFunction handler = new Fixture();

        handler.init(moduleDescriptor);

        final FunctionOperand operand = new FunctionOperand("testfunc", ImmutableList.<String>of());
        final MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'subIssueTypes' is invalid as sub-tasks are currently disabled.");
    }

    @Test
    public void testValidateWithNoArgsSubTasksEnabled()
    {
        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);
        final AllSubIssueTypesFunction handler = new Fixture();

        final FunctionOperand operand = new FunctionOperand("testfunc", Collections.<String>emptyList());
        final MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertNoMessages(messageSet);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments()
    {
        final AllSubIssueTypesFunction handler = new Fixture();
        assertEquals(0, handler.getMinimumNumberOfExpectedArguments());
    }



    class Fixture extends AllSubIssueTypesFunction
    {
        Fixture()
        {
            super(constantsManager, TestAllSubIssueTypeFunction.this.subTaskManager);
        }

        @Override
        protected I18nHelper getI18n()
        {
            return new MockI18nBean();
        }
    }
}
