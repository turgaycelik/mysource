package com.atlassian.jira.issue.fields.screen;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.FieldScreenRendererImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenRendererImpl
{
    @Test
    public void testGetFieldScreenRenderTabs() throws Exception
    {
        final MockFieldScreenRendererTab fieldScreenRendererTab = new MockFieldScreenRendererTab();
        final List<FieldScreenRenderTab> expectedList = Collections.<FieldScreenRenderTab>singletonList(fieldScreenRendererTab);
        FieldScreenRendererImpl renderer = new FieldScreenRendererImpl(expectedList, null);
        assertEquals(expectedList, renderer.getFieldScreenRenderTabs());
    }

    @Test
    public void testGetFieldLayout() throws Exception
    {
        final MockFieldScreenRendererTab fieldScreenRendererTab = new MockFieldScreenRendererTab();
        final List<FieldScreenRenderTab> expectedList = Collections.<FieldScreenRenderTab>singletonList(fieldScreenRendererTab);
        final MockFieldLayout fieldLayout = new MockFieldLayout();

        FieldScreenRendererImpl renderer = new FieldScreenRendererImpl(expectedList, fieldLayout);
        assertSame(fieldLayout, renderer.getFieldLayout());
    }
}
