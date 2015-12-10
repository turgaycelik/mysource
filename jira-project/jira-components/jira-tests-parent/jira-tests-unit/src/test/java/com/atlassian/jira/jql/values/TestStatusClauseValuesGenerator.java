package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestStatusClauseValuesGenerator extends MockControllerTestCase
{
    private ConstantsManager constantsManager;
    private StatusClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        this.constantsManager = mockController.getMock(ConstantsManager.class);
        this.valuesGenerator = new StatusClauseValuesGenerator(constantsManager);
    }

    @Test
    public void testGetAllConstants() throws Exception
    {
        final MockStatus type1 = new MockStatus("1", "Aa it");
        final MockStatus type2 = new MockStatus("2", "A it");
        final MockStatus type3 = new MockStatus("3", "B it");
        final MockStatus type4 = new MockStatus("4", "C it");

        constantsManager.getStatusObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type4, type3, type2, type1).asList());

        mockController.replay();

        valuesGenerator.getAllConstants();
        mockController.verify();
    }
    
}
