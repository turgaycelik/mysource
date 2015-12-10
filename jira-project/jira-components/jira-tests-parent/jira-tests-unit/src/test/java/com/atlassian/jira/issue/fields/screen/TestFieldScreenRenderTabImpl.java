package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.MockOrderableField;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderTabImpl}
 *
 * @since v4.1
 */
public class TestFieldScreenRenderTabImpl
{
    @Test
    public void testGetFieldScreenRenderLayoutItemsForProcessing() throws Exception
    {
        final List<FieldScreenRenderLayoutItem> items = new ArrayList<FieldScreenRenderLayoutItem>();
        items.add(new MockFieldScreenRendererLayoutItem().setOrderableField(new MockOrderableField("383")));
        items.add(new MockFieldScreenRendererLayoutItem().setOrderableField(new MockOrderableField(IssueFieldConstants.ASSIGNEE)));
        items.add(new MockFieldScreenRendererLayoutItem().setOrderableField(new MockOrderableField("39393")));

        List<FieldScreenRenderLayoutItem> expectedItems = new ArrayList<FieldScreenRenderLayoutItem>(items);
        expectedItems.add(expectedItems.remove(1));

        final FieldScreenRenderTabImpl tab = new FieldScreenRenderTabImpl("name", 1, items);
        assertEquals(expectedItems, tab.getFieldScreenRenderLayoutItemsForProcessing());
    }
}
