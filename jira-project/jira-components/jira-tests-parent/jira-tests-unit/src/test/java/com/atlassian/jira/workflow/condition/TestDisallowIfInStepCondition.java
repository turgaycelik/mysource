/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;

import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class TestDisallowIfInStepCondition
{
    private Map transientVars;
    private Mock wfe;
    private Mock wfc;
    private DisallowIfInStepCondition condition;
    private Mock wfs;

    @Before
    public void setUp() throws Exception
    {
        wfc = new Mock(WorkflowContext.class);
        wfe = new Mock(WorkflowEntry.class);

        List<SimpleStep> steps = new ArrayList<SimpleStep>();
        for (int i = 10; i < 16; i++)
        {
            steps.add(new SimpleStep(i, i, i, i, null, null, null, null, null, null, null));
        }

        wfs = new Mock(WorkflowStore.class);
        wfs.setupResult("findCurrentSteps", steps);

        final Object contextProxy = wfc.proxy();
        transientVars = EasyMap.build("context", contextProxy, "entry", wfe.proxy(), "store", wfs.proxy());

        condition = new DisallowIfInStepCondition();
    }

    @Test
    public void testFilterConditionOk1()
    {
        Map args = EasyMap.build("stepIds", "1, 2, 3, 4, 5");
        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void testFilterConditionOk2()
    {
        Map args = EasyMap.build("stepIds", "");
        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void testFilterConditioOk3()
    {
        Map args = EasyMap.build("stepIds", "10, 11, 12, 13, 14, 15");
        wfs.setupResult("findCurrentSteps", Collections.EMPTY_LIST);

        assertTrue(condition.passesCondition(transientVars, args, null));
    }

    @Test
    public void testFilterConditonFail()
    {
        Map args = EasyMap.build("stepIds", "6, 7, 8, 9, 10");
        assertTrue(!condition.passesCondition(transientVars, args, null));
    }
}
