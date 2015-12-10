package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestPriorityClauseValuesGenerator extends MockControllerTestCase
{
    private ConstantsManager constantsManager;
    private PriorityClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        this.constantsManager = mockController.getMock(ConstantsManager.class);
        this.valuesGenerator = new PriorityClauseValuesGenerator(constantsManager);
    }

    @Test
    public void testGetAllConstants() throws Exception
    {
        final MockPriority priority1 = new MockPriority("1", "Aa it");
        final MockPriority priority2 = new MockPriority("2", "A it");
        final MockPriority priority3 = new MockPriority("3", "B it");
        final MockPriority priority4 = new MockPriority("4", "C it");

        constantsManager.getPriorityObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(priority4, priority3, priority2, priority1).asList());
        mockController.replay();

        valuesGenerator.getAllConstants();
        mockController.verify();
    }
}
