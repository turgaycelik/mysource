package com.atlassian.jira.web.util;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.action.MockAction;

import org.junit.Assert;
import org.junit.Test;

public class TestWebActionUtil
{
    @Test
    public void testNoErrors()
    {
        MockAction action = new MockAction();
        WebActionUtil.addDependentVersionErrors(action, EasyList.build(4L, 5L), "field");
        Assert.assertEquals(0, action.getErrors().size());
    }

    @Test
    public void testErrors()
    {
        MockAction action = new MockAction();
        WebActionUtil.addDependentVersionErrors(action, EasyList.build(-2L), "field");
        Assert.assertEquals(1, action.getErrors().size());
        Assert.assertEquals("You cannot specify \"Unreleased\" or \"Released\".", action.getErrors().get("field"));
    }
}
