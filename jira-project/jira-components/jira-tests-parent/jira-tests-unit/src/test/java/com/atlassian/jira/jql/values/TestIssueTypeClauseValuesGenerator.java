package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestIssueTypeClauseValuesGenerator extends MockControllerTestCase
{
    private ConstantsManager constantsManager;
    private IssueTypeClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        this.constantsManager = mockController.getMock(ConstantsManager.class);
        this.valuesGenerator = new IssueTypeClauseValuesGenerator(constantsManager);
    }

    @Test
    public void testGetAllConstants() throws Exception
    {
        final MockIssueType type1 = new MockIssueType("1", "Aa it");
        final MockIssueType type2 = new MockIssueType("2", "A it");
        final MockIssueType type3 = new MockIssueType("3", "B it");
        final MockIssueType type4 = new MockIssueType("4", "C it");

        constantsManager.getAllIssueTypeObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        valuesGenerator.getAllConstants();
        mockController.verify();
    }
    
}
