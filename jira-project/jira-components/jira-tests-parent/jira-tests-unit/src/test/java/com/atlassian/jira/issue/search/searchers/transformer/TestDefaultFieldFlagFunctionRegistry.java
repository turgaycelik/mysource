package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Set;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction;
import com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllSubIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @since v4.0
 */
public class TestDefaultFieldFlagFunctionRegistry
{
    @Test
    public void testGetAllReleasedVersionsFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Set<String> flags = defaultFieldFlagOperandRegistry.getFlagForOperand(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), new FunctionOperand(AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS));
        assertEquals(1, flags.size());
        assertTrue(flags.contains(VersionManager.ALL_RELEASED_VERSIONS));
    }

    @Test
    public void testGetAllUnreleasedVersionsFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Set<String> flags = defaultFieldFlagOperandRegistry.getFlagForOperand(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), new FunctionOperand(AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS));
        assertEquals(1, flags.size());
        assertTrue(flags.contains(VersionManager.ALL_UNRELEASED_VERSIONS));
    }

    @Test
    public void testGetAllReleasedVersionsOperand() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Operand operand = defaultFieldFlagOperandRegistry.getOperandForFlag(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), VersionManager.ALL_RELEASED_VERSIONS);
        assertEquals(AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS, operand.getName());
    }

    @Test
    public void testGetAllUnreleasedVersionsOperand() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Operand operand = defaultFieldFlagOperandRegistry.getOperandForFlag(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), VersionManager.ALL_UNRELEASED_VERSIONS);
        assertEquals(AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS, operand.getName());
    }

    @Test
    public void testGetVersionEmptyFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Set<String> flags = defaultFieldFlagOperandRegistry.getFlagForOperand(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), EmptyOperand.EMPTY);
        assertEquals(1, flags.size());
        assertTrue(flags.contains(VersionManager.NO_VERSIONS));
    }

    @Test
    public void testGetVersionEmptyOperand() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Operand operand = defaultFieldFlagOperandRegistry.getOperandForFlag(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), VersionManager.NO_VERSIONS);
        assertEquals(EmptyOperand.EMPTY, operand);
    }

    @Test
    public void testGetIssueTypeStandardsFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Set<String> flags = defaultFieldFlagOperandRegistry.getFlagForOperand(IssueFieldConstants.ISSUE_TYPE, new FunctionOperand(AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES));
        assertEquals(1, flags.size());
        assertTrue(flags.contains(ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
    }

    @Test
    public void testGetIssueTypeSubsFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Set<String> flags = defaultFieldFlagOperandRegistry.getFlagForOperand(IssueFieldConstants.ISSUE_TYPE, new FunctionOperand(AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES));
        assertEquals(1, flags.size());
        assertTrue(flags.contains(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES));
    }

    @Test
    public void testGetIssueTypeStandardsOperand() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Operand operand = defaultFieldFlagOperandRegistry.getOperandForFlag(IssueFieldConstants.ISSUE_TYPE, ConstantsManager.ALL_STANDARD_ISSUE_TYPES);
        assertEquals(AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES, operand.getName());
    }

    @Test
    public void testGetIssueTypeSubsOperand() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        Operand operand = defaultFieldFlagOperandRegistry.getOperandForFlag(IssueFieldConstants.ISSUE_TYPE, ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES);
        assertEquals(AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES, operand.getName());
    }

    @Test
    public void testGetFieldWithNoFlagsFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        assertNull(defaultFieldFlagOperandRegistry.getFlagForOperand("blah",  new FunctionOperand("blah")));
    }
    
    @Test
    public void testGetFieldWithNoFlagsForFunction() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        assertNull(defaultFieldFlagOperandRegistry.getFlagForOperand(IssueFieldConstants.ISSUE_TYPE,  new FunctionOperand("blah")));
    }

    @Test
    public void testGetFieldWithNoFlagsFunction() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        assertNull(defaultFieldFlagOperandRegistry.getOperandForFlag("blah",  "blah"));
    }
    
    @Test
    public void testGetFieldWithNoFunctionsForFlag() throws Exception
    {
        FieldFlagOperandRegistry defaultFieldFlagOperandRegistry = new DefaultFieldFlagOperandRegistry();
        assertNull(defaultFieldFlagOperandRegistry.getOperandForFlag(IssueFieldConstants.ISSUE_TYPE,  "blah"));
    }
}
