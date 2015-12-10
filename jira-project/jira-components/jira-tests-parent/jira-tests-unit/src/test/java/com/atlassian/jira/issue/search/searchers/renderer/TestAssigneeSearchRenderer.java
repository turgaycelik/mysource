package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import junit.framework.Assert;

/**
 * @since v4.0
 */
public class TestAssigneeSearchRenderer
{
    @Rule
    public TestRule mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Test
    public void testIsShown() throws Exception
    {
        final FieldVisibilityManager fVB = Mockito.mock(FieldVisibilityManager.class);
        final SearchContext searchContext = new MockSearchContext();

        Mockito.when(fVB.isFieldHiddenInAllSchemes(SystemSearchConstants.forAssignee().getFieldId(), searchContext, null)).thenReturn(true, false);

        final AssigneeSearchRenderer assigneeSearchRenderer = new AssigneeSearchRenderer("nameKey", null, null, null, null, fVB, null, null);

        Assert.assertFalse(assigneeSearchRenderer.isShown(null, searchContext));
        Assert.assertTrue(assigneeSearchRenderer.isShown(null, searchContext));
    }
}
