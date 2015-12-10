/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.util.workflow;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;

import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.RestrictionDescriptor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestWorkflowEditorTransitionConditionUtil
{
    private WorkflowEditorTransitionConditionUtil wetcu;
    private ActionDescriptor actionDescriptor;
    private ConditionsDescriptor conditionsDescriptor;

    @Before
    public void setUp() throws Exception
    {
        wetcu = new WorkflowEditorTransitionConditionUtil();
        actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();
        RestrictionDescriptor restriction = new RestrictionDescriptor();
        actionDescriptor.setRestriction(restriction);
        conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
        restriction.setConditionsDescriptor(conditionsDescriptor);

    }

    @After
    public void tearDown() throws Exception
    {
        actionDescriptor = null;
        conditionsDescriptor = null;
        wetcu = null;
    }

    @Test
    public void testDeleteCondition()
    {
        ConditionDescriptor condition1 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(condition1, DescriptorFactory.getFactory().createConditionDescriptor()));


        wetcu.deleteCondition(actionDescriptor, "2");

        RestrictionDescriptor restriction = actionDescriptor.getRestriction();
        assertNotNull(restriction);
        ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
        assertNotNull(conditionsDescriptor);
        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(1, conditions.size());
        assertTrue(condition1 == conditions.iterator().next());
    }

    @Test
    public void testDeleteLastCondition()
    {
        conditionsDescriptor.setConditions(Lists.newArrayList(DescriptorFactory.getFactory().createConditionDescriptor()));

        wetcu.deleteCondition(actionDescriptor, "1");

        assertNull(actionDescriptor.getRestriction());
    }

    @Test
    public void testDeleteLastNestedCondition()
    {
        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(DescriptorFactory.getFactory().createConditionDescriptor()));
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2));

        wetcu.deleteCondition(actionDescriptor, "1.1");

        assertNull(actionDescriptor.getRestriction());
    }

    @Test
    public void testDeleteLastDoublyNestedCondition()
    {
        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionsDescriptor conditionsDescriptor3 = DescriptorFactory.getFactory().createConditionsDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionsDescriptor3));
        conditionsDescriptor3.setConditions(Lists.newArrayList(DescriptorFactory.getFactory().createConditionDescriptor()));
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2));

        wetcu.deleteCondition(actionDescriptor, "1.1.1");

        assertNull(actionDescriptor.getRestriction());
    }

    @Test
    public void testDeleteNestedCondition()
    {
        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(DescriptorFactory.getFactory().createConditionDescriptor()));
        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2, conditionDescriptor));

        wetcu.deleteCondition(actionDescriptor, "1.1");

        assertNotNull(actionDescriptor.getRestriction());
        assertNotNull(actionDescriptor.getRestriction().getConditionsDescriptor());
        Collection conditions = actionDescriptor.getRestriction().getConditionsDescriptor().getConditions();
        assertNotNull(conditions);
        assertEquals(1, conditions.size());
        assertTrue(conditionDescriptor == conditions.iterator().next());
    }

    @Test
    public void testDeleteNestedCondition2()
    {
        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor3 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionDescriptor, conditionDescriptor2, conditionDescriptor3));

        ConditionDescriptor conditionDescriptor4 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2, conditionDescriptor4));

        wetcu.deleteCondition(actionDescriptor, "1.2");

        assertNotNull(actionDescriptor.getRestriction());
        assertNotNull(actionDescriptor.getRestriction().getConditionsDescriptor());
        Collection conditions = actionDescriptor.getRestriction().getConditionsDescriptor().getConditions();
        assertNotNull(conditions);
        assertEquals(2, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionsDescriptor2 == iterator.next());
        assertTrue(conditionDescriptor4 == iterator.next());

        Collection conditions2 = conditionsDescriptor2.getConditions();
        assertNotNull(conditions2);
        assertEquals(2, conditions2.size());

        Iterator iterator2 = conditions2.iterator();
        assertTrue(conditionDescriptor == iterator2.next());
        assertTrue(conditionDescriptor3 == iterator2.next());
    }

    @Test
    public void testDeleteSecondLastCondition()
    {
        // Test if the second last condition is deleted from a nested ConditionsBLock that the block
        // is 'removed' - flattened out.

        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(DescriptorFactory.getFactory().createConditionDescriptor(), DescriptorFactory.getFactory().createConditionDescriptor()));


        ConditionsDescriptor conditionsDescriptor3 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor3.setConditions(Lists.newArrayList(conditionDescriptor, conditionDescriptor2));

        ConditionDescriptor conditionDescriptor4 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2, conditionsDescriptor3, conditionDescriptor4));

        wetcu.deleteCondition(actionDescriptor, "2.1");

        assertNotNull(actionDescriptor.getRestriction());
        assertNotNull(actionDescriptor.getRestriction().getConditionsDescriptor());
        Collection conditions = actionDescriptor.getRestriction().getConditionsDescriptor().getConditions();
        assertNotNull(conditions);
        assertEquals(3, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionsDescriptor2 == iterator.next());
        assertTrue(conditionDescriptor2 == iterator.next());
        assertTrue(conditionDescriptor4 == iterator.next());
    }

    @Test
    public void testDeleteLastConditionInBlock()
    {
        // Test if the last condition is deleted from the ConditionsBlock when it has only one
        // nested ConditionsBlock left that the nested block becomes the conditions block

        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();

        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionDescriptor, conditionDescriptor2));

        ConditionDescriptor conditionDescriptor3 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2, conditionDescriptor3));

        wetcu.deleteCondition(actionDescriptor, "2");

        assertNotNull(actionDescriptor.getRestriction());
        assertNotNull(actionDescriptor.getRestriction().getConditionsDescriptor());
        Collection conditions = actionDescriptor.getRestriction().getConditionsDescriptor().getConditions();
        assertNotNull(conditions);
        assertEquals(2, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionDescriptor == iterator.next());
        assertTrue(conditionDescriptor2 == iterator.next());
    }

    @Test
    public void testDeleteFirstConditionWithMultipleBlocks()
    {
        // Test that when a condition is removed from the first nested block and the block is flattened, its
        // remaining condition is moved after the last nested block in the parent. Remember that OSWorkflow
        // DTD forces us to have all conditions blocks before conditions.

        ConditionsDescriptor conditionsDescriptor1= DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor11 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor12 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor1.setConditions(Lists.newArrayList(conditionDescriptor11, conditionDescriptor12));

        ConditionsDescriptor conditionsDescriptor2= DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor21 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor22 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionDescriptor21, conditionDescriptor22));


        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor1, conditionsDescriptor2));

        wetcu.deleteCondition(actionDescriptor, "1.2");

        assertNotNull(actionDescriptor.getRestriction());
        assertNotNull(actionDescriptor.getRestriction().getConditionsDescriptor());
        Collection conditions = actionDescriptor.getRestriction().getConditionsDescriptor().getConditions();
        assertNotNull(conditions);
        assertEquals(2, conditions.size());
        Iterator iterator = conditions.iterator();
        ConditionsDescriptor cd = (ConditionsDescriptor) iterator.next();
        // Ensure the conditionsDescriptor2 has been moved up so it appears above conditionDescriptor21
        assertTrue(conditionsDescriptor2 == cd);
        assertTrue(conditionDescriptor11 == iterator.next());

        // Ensure the contents of conditionsDescriptor2 have not hcnaged
        assertNotNull(cd.getConditions());
        assertEquals(2, cd.getConditions().size());
        Iterator iterator2 = cd.getConditions().iterator();
        assertTrue(conditionDescriptor21 == iterator2.next());
        assertTrue(conditionDescriptor22 == iterator2.next());
    }

    @Test
    public void testAddFirstCondition()
    {
        actionDescriptor.setRestriction(null);

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addCondition(actionDescriptor, "", conditionToAdd);

        assertEquals("1", index);

        assertNotNull(actionDescriptor.getRestriction());

        ConditionsDescriptor conditionsDescriptor = actionDescriptor.getRestriction().getConditionsDescriptor();
        assertNotNull(conditionsDescriptor);
        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(1, conditions.size());
        assertTrue(conditionToAdd == conditions.iterator().next());
    }

    @Test
    public void testAddConditionToExistingBlock()
    {
        ConditionDescriptor conditionDescriptor1 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor2, conditionDescriptor1, conditionDescriptor2));

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addCondition(actionDescriptor, "", conditionToAdd);

        assertEquals("4", index);

        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(4, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionsDescriptor2 == iterator.next());
        assertTrue(conditionDescriptor1 == iterator.next());
        assertTrue(conditionDescriptor2 == iterator.next());
        assertTrue(conditionToAdd == iterator.next());
    }

    @Test
    public void testAddConditionToExistingNestedBlock()
    {
        ConditionsDescriptor conditionsDescriptor1 = DescriptorFactory.getFactory().createConditionsDescriptor();

        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor21 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor22 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionDescriptor21, conditionDescriptor22));

        ConditionDescriptor conditionDescriptor1 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor1, conditionsDescriptor2, conditionDescriptor1, conditionDescriptor2));

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addCondition(actionDescriptor, "2.1", conditionToAdd);

        assertEquals("2.3", index);

        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(4, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionsDescriptor1 == iterator.next());
        ConditionsDescriptor cd = (ConditionsDescriptor) iterator.next();
        assertTrue(conditionsDescriptor2 == cd);
        assertTrue(conditionDescriptor1 == iterator.next());
        assertTrue(conditionDescriptor2 == iterator.next());

        List nestedConditions = cd.getConditions();
        assertNotNull(nestedConditions);
        assertEquals(3, nestedConditions.size());
        Iterator iterator2 = nestedConditions.iterator();
        assertTrue(conditionDescriptor21 == iterator2.next());
        assertTrue(conditionDescriptor22 == iterator2.next());
        assertTrue(conditionToAdd == iterator2.next());
    }

    @Test
    public void testAddConditionToDoublyExistingNestedBlock()
    {
        ConditionsDescriptor conditionsDescriptor1 = DescriptorFactory.getFactory().createConditionsDescriptor();

        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();

        ConditionsDescriptor conditionsDescriptor21 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor211 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor212 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor21.setConditions(Lists.newArrayList(conditionDescriptor211, conditionDescriptor212));

        ConditionDescriptor conditionDescriptor22 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionsDescriptor21, conditionDescriptor22));

        ConditionDescriptor conditionDescriptor1 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor1, conditionsDescriptor2, conditionDescriptor1, conditionDescriptor2));

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addCondition(actionDescriptor, "2.1.1", conditionToAdd);

        assertEquals("2.1.3", index);

        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(4, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionsDescriptor1 == iterator.next());
        ConditionsDescriptor cd = (ConditionsDescriptor) iterator.next();
        assertTrue(conditionsDescriptor2 == cd);
        assertTrue(conditionDescriptor1 == iterator.next());
        assertTrue(conditionDescriptor2 == iterator.next());

        Collection nestedConditions = cd.getConditions();
        assertNotNull(nestedConditions);
        assertEquals(2, nestedConditions.size());
        Iterator iterator2 = nestedConditions.iterator();
        ConditionsDescriptor nestedDesciptor = (ConditionsDescriptor) iterator2.next();
        assertTrue(conditionsDescriptor21 == nestedDesciptor);
        assertTrue(conditionDescriptor22 == iterator2.next());

        Collection nestedConditions2 = nestedDesciptor.getConditions();
        assertNotNull(nestedConditions2);
        assertEquals(3, nestedConditions2.size());

        Iterator iterator3 = nestedConditions2.iterator();
        assertTrue(conditionDescriptor211 == iterator3.next());
        assertTrue(conditionDescriptor212 == iterator3.next());
        assertTrue(conditionToAdd == iterator3.next());
    }

    @Test
    public void testAddFirstConditionAsNested()
    {
        actionDescriptor.setRestriction(null);

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addNestedCondition(actionDescriptor, "", conditionToAdd);

        assertEquals("1", index);

        assertNotNull(actionDescriptor.getRestriction());

        ConditionsDescriptor conditionsDescriptor = actionDescriptor.getRestriction().getConditionsDescriptor();
        assertNotNull(conditionsDescriptor);
        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(1, conditions.size());
        assertTrue(conditionToAdd == conditions.iterator().next());
    }

    @Test
    public void testAddNestedConditionsDescriptor()
    {
        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionDescriptor, conditionDescriptor2));

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addNestedCondition(actionDescriptor, "2", conditionToAdd);

        assertEquals("1.2", index);

        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(2, conditions.size());
        Iterator iterator = conditions.iterator();
        ConditionsDescriptor addedConditionsDescriptor = (ConditionsDescriptor) iterator.next();
        assertTrue(addedConditionsDescriptor instanceof ConditionsDescriptor);
        assertTrue(conditionDescriptor == iterator.next());
        Collection addedConditions = addedConditionsDescriptor.getConditions();
        assertNotNull(addedConditions);
        assertEquals(2, addedConditions.size());
        Iterator iterator2 = addedConditions.iterator();
        assertTrue(conditionDescriptor2 == iterator2.next());
        assertTrue(conditionToAdd == iterator2.next());
    }

    @Test
    public void testAddDoublyNestedConditionsDescriptor()
    {
        // Need to check that the created conditions descriptor was moved to the top if the condition that was
        // grouped was not. All ConditionsDescriptors must appear before ConditionDescriptors as this is mandated by
        // OSWorkflow's DTD

        ConditionsDescriptor conditionsDescriptor1 = DescriptorFactory.getFactory().createConditionsDescriptor();

        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor21 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor22 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor2.setConditions(Lists.newArrayList(conditionDescriptor21, conditionDescriptor22));

        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor2 = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(conditionsDescriptor1, conditionsDescriptor2, conditionDescriptor, conditionDescriptor2));

        ConditionDescriptor conditionToAdd = DescriptorFactory.getFactory().createConditionDescriptor();

        String index = wetcu.addNestedCondition(actionDescriptor, "2.2", conditionToAdd);

        assertEquals("2.1.2", index);

        Collection conditions = conditionsDescriptor.getConditions();
        assertNotNull(conditions);
        assertEquals(4, conditions.size());
        Iterator iterator = conditions.iterator();
        assertTrue(conditionsDescriptor1 == iterator.next());
        ConditionsDescriptor cd = (ConditionsDescriptor) iterator.next();
        assertTrue(conditionsDescriptor2 == cd);
        assertTrue(conditionDescriptor == iterator.next());
        assertTrue(conditionDescriptor2 == iterator.next());

        assertNotNull(cd.getConditions());
        assertEquals(2, cd.getConditions().size());

        Iterator iterator2 = cd.getConditions().iterator();
        Object o = iterator2.next();
        assertTrue(o instanceof ConditionsDescriptor);
        assertTrue(conditionDescriptor21 == iterator2.next());

        ConditionsDescriptor addedDescriptor = (ConditionsDescriptor) o;
        List addedConditions = addedDescriptor.getConditions();
        assertNotNull(addedConditions);
        assertEquals(2, addedConditions.size());
        Iterator iterator3 = addedConditions.iterator();
        assertTrue(conditionDescriptor22 == iterator3.next());
        assertTrue(conditionToAdd == iterator3.next());
    }


    @Test
    public void testChangeOperatorWhenNullResrtiction()
    {
        actionDescriptor.setRestriction(null);

        wetcu.changeLogicOperator(actionDescriptor, "");

        assertNull(actionDescriptor.getRestriction());
    }

    @Test
    public void testChangeOperatorToAnd()
    {
        _testChangeOperator(WorkflowEditorTransitionConditionUtil.OPERATOR_OR, WorkflowEditorTransitionConditionUtil.OPERATOR_AND);
    }

    @Test
    public void testChangeOperatorToOR()
    {
        _testChangeOperator(WorkflowEditorTransitionConditionUtil.OPERATOR_AND, WorkflowEditorTransitionConditionUtil.OPERATOR_OR);
    }

    private void _testChangeOperator(String oldOperator, String newOperator)
    {
        conditionsDescriptor.setType(oldOperator);

        wetcu.changeLogicOperator(actionDescriptor, "2");
        assertEquals(newOperator, conditionsDescriptor.getType());
    }

    @Test
    public void testChangeOperatorOfNestedBlockToAND()
    {
        _testChangeOperatorOfNestedBlock(WorkflowEditorTransitionConditionUtil.OPERATOR_AND, WorkflowEditorTransitionConditionUtil.OPERATOR_OR);
    }

    @Test
    public void testChangeOperatorOfNestedBlockToOR()
    {
        _testChangeOperatorOfNestedBlock(WorkflowEditorTransitionConditionUtil.OPERATOR_OR, WorkflowEditorTransitionConditionUtil.OPERATOR_AND);
    }

    private void _testChangeOperatorOfNestedBlock(String oldOperator, String newOperator)
    {
        ConditionsDescriptor nestedDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor11 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor12 = DescriptorFactory.getFactory().createConditionDescriptor();
        nestedDescriptor.setConditions(Lists.newArrayList(conditionDescriptor11, conditionDescriptor12));

        ConditionsDescriptor nestedDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor21 = DescriptorFactory.getFactory().createConditionDescriptor();
        ConditionDescriptor conditionDescriptor22 = DescriptorFactory.getFactory().createConditionDescriptor();
        nestedDescriptor2.setConditions(Lists.newArrayList(conditionDescriptor21, conditionDescriptor22));

        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionsDescriptor.setConditions(Lists.newArrayList(nestedDescriptor, nestedDescriptor2, conditionDescriptor));


        conditionsDescriptor.setType(oldOperator);
        nestedDescriptor.setType(oldOperator);
        nestedDescriptor2.setType(oldOperator);

        wetcu.changeLogicOperator(actionDescriptor, "2.2");

        assertEquals(oldOperator, conditionsDescriptor.getType());
        assertEquals(oldOperator, nestedDescriptor.getType());
        assertEquals(newOperator, nestedDescriptor2.getType());
    }


}
