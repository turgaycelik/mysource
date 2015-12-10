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
import com.atlassian.jira.util.collect.CollectionBuilder;
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAllStandardIssueTypesFunction
{
    @Mock ConstantsManager constantsManager;
    @Mock SubTaskManager subTaskManager;

    private JqlFunctionModuleDescriptor moduleDescriptor;
    private TerminalClause terminalClause = null;

    @Before
    public void setUp()
    {
        moduleDescriptor = MockJqlFunctionModuleDescriptor.create("standardIssueTypes", true);
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
        AllStandardIssueTypesFunction handler = new Fixture();
        assertEquals(JiraDataTypes.ISSUE_TYPE, handler.getDataType());
    }

    @Test
    public void testBadConstructor() throws Exception
    {
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        final SubTaskManager subTaskManager = mock(SubTaskManager.class);

        try
        {
            new AllStandardIssueTypesFunction(null, subTaskManager);
            fail("Exception expected");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new AllStandardIssueTypesFunction(constantsManager, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testGetValues()
    {
        when(constantsManager.getRegularIssueTypeObjects()).thenReturn(ImmutableList.<IssueType>of(
                new MockIssueType("1", null),
                new MockIssueType("2", null),
                new MockIssueType("nlah", null)));

        AllStandardIssueTypesFunction handler = new Fixture();
        final FunctionOperand operand = new FunctionOperand("blarg!");
        final List<QueryLiteral> queryLiteralList = handler.getValues(null, operand, terminalClause);

        assertEquals("1", queryLiteralList.get(0).getStringValue());
        assertEquals("2", queryLiteralList.get(1).getStringValue());
        assertEquals("nlah", queryLiteralList.get(2).getStringValue());
        assertEquals(3, queryLiteralList.size());

        assertEquals(operand, queryLiteralList.get(0).getSourceOperand());
        assertEquals(operand, queryLiteralList.get(1).getSourceOperand());
        assertEquals(operand, queryLiteralList.get(2).getSourceOperand());
    }

    @Test
    public void testValidateWithArgs()
    {
        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        FunctionOperand operand = new FunctionOperand("testfunc", CollectionBuilder.newBuilder("arg").asList());
        MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertThat(messageSet.hasAnyErrors(), is(true));
    }

    @Test
    public void testValidateWithNoArgsSubTasksDisabled()
    {
        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        handler.init(moduleDescriptor);

        FunctionOperand operand = new FunctionOperand("testfunc", Collections.<String>emptyList());
        MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertThat(messageSet.hasAnyErrors(), is(false));
    }

    @Test
    public void testValidateWithNoArgsSubTasksEnabled()
    {
        AllStandardIssueTypesFunction handler = new Fixture()
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };

        FunctionOperand operand = new FunctionOperand("testfunc", Collections.<String>emptyList());
        MessageSet messageSet = handler.validate(null, operand, terminalClause);
        assertThat(messageSet.hasAnyErrors(), is(false));
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments()
    {
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        final SubTaskManager subTaskManager = mock(SubTaskManager.class);

        AllStandardIssueTypesFunction handler = new AllStandardIssueTypesFunction(constantsManager, subTaskManager);

        assertEquals(0, handler.getMinimumNumberOfExpectedArguments());
    }

    class Fixture extends AllStandardIssueTypesFunction
    {
        Fixture()
        {
            super(constantsManager, TestAllStandardIssueTypesFunction.this.subTaskManager);
        }
    }
}
