package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.MockIssueConstant;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.operand.SingleValueOperand;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestIssueConstantIndexedInputHelper extends MockControllerTestCase
{
    private NameResolver<IssueConstant> nameResolver;
    private IssueConstantIndexedInputHelper helper;

    @Before
    public void setUp() throws Exception
    {
        nameResolver = getMock(NameResolver.class);
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsntNumber() throws Exception
    {
        helper = instantiate(IssueConstantIndexedInputHelper.class);

        assertEquals(new SingleValueOperand("test"), helper.createSingleValueOperandFromId("test"));
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsNotAComponent() throws Exception
    {
        expect(nameResolver.get(123l))
                .andReturn(null);

        helper = instantiate(IssueConstantIndexedInputHelper.class);

        assertEquals(new SingleValueOperand(123l), helper.createSingleValueOperandFromId("123"));
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsAVersion() throws Exception
    {
        expect(nameResolver.get(123l))
                .andReturn(new MockIssueConstant("123","Component 1"));

        helper = instantiate(IssueConstantIndexedInputHelper.class);

        assertEquals(new SingleValueOperand("Component 1"), helper.createSingleValueOperandFromId("123"));
    }
}
