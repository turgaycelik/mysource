package com.atlassian.jira.issue.fields.screen;

import java.util.Collection;

import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.AbstractFieldScreenRenderer}.
 *
 * @since v4.1
 */
public class TestAbstractFieldScreenRenderer
{
    /*
     * What happens when no tabs exist.
     */

    @Test
    public void testGetRequiredFieldScreenRenderItemsNullItems()
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        final Collection<FieldScreenRenderLayoutItem> items = screenRenderer.getRequiredFieldScreenRenderItems();
        assertTrue(items.isEmpty());
    }

    /*
     * Make sure the method only returns required layout items.
     */

    @Test
    public void testGetRequiredFieldScreenRenderItems() throws Exception
    {
        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(false);
        final FieldScreenRenderLayoutItem item1 = rendererTab.addLayoutItem().setRequired(true);

        rendererTab = renderer.addFieldScreenRendererTab();
        final FieldScreenRenderLayoutItem item2 = rendererTab.addLayoutItem().setRequired(true);

        assertThat(renderer.getRequiredFieldScreenRenderItems(), contains(item1, item2));

    }

    /*
     * Test what happens when no tabs exist.
     */

    @Test
    public void testGetFieldScreenRenderTabPositionNullItems()
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        assertThat(screenRenderer.getFieldScreenRenderTabPosition("blargs"), nullValue());
    }

    /*
     * Test what happens when the field cannot be matched.
     */

    @Test
    public void testGetFieldScreenRenderTabPositionNoMatch()
    {
        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(false).setOrderableField(new MockOrderableField("i"));
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        assertThat(renderer.getFieldScreenRenderTabPosition("6"), nullValue());
        assertThat(renderer.getFieldScreenRenderTabPosition(null), nullValue());
    }

    /*
     * Test when the field can be matched.
     */

    @Test
    public void testGetFieldScreenRenderTabPositionMatch()
    {
        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(false).setOrderableField(new MockOrderableField("i"));
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        assertSame(rendererTab, renderer.getFieldScreenRenderTabPosition("5"));
    }

    /*
     * Test when the field cannot be matched.
     */

    @Test
    public void testGetFieldScreenRenderLayoutItemNoMatch()
    {
        final MockOrderableField field = new MockOrderableField("6");
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final FieldLayout fieldLayout = mock(FieldLayout.class);
        when(fieldLayout.getFieldLayoutItem(field)).thenReturn(fieldLayoutItem);

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer().setFieldLayout(fieldLayout);
        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        final FieldScreenRenderLayoutItem actualItem = renderer.getFieldScreenRenderLayoutItem(field);
        assertSame(fieldLayoutItem, actualItem.getFieldLayoutItem());
        assertNull(actualItem.getFieldScreenLayoutItem());
    }

    /*
     * Test when the field cannot be matched.
     */

    @Test
    public void testGetFieldScreenRenderLayoutItemNoTabs()
    {
        final MockOrderableField field = new MockOrderableField("6");
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final FieldLayout fieldLayout = mock(FieldLayout.class);
        when(fieldLayout.getFieldLayoutItem(field)).thenReturn(fieldLayoutItem);

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer().setFieldLayout(fieldLayout);

        final FieldScreenRenderLayoutItem actualItem = renderer.getFieldScreenRenderLayoutItem(field);
        assertSame(fieldLayoutItem, actualItem.getFieldLayoutItem());
        assertNull(actualItem.getFieldScreenLayoutItem());
    }

    /*
     * Test when the field can be matched.
     */

    @Test
    public void testGetFieldScreenRenderLayoutItemMatch()
    {
        final MockOrderableField field = new MockOrderableField("i");

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();
        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        final FieldScreenRenderLayoutItem expectedItem = rendererTab.addLayoutItem().setRequired(false).setOrderableField(field);
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        assertSame(expectedItem, renderer.getFieldScreenRenderLayoutItem(field));
    }

    @Test
    public void testGetAllScreenRenderItemsNoItems() throws Exception
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        final Collection<FieldScreenRenderLayoutItem> items = screenRenderer.getAllScreenRenderItems();
        assertThat(items, Matchers.<FieldScreenRenderLayoutItem>emptyIterable());
    }

    @Test
    public void testGetAllScreenRenderItems() throws Exception
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        MockFieldScreenRendererTab tab = screenRenderer.addFieldScreenRendererTab();

        final FieldScreenRenderLayoutItem item1 = tab.addLayoutItem();
        final FieldScreenRenderLayoutItem item2 = tab.addLayoutItem();
        final FieldScreenRenderLayoutItem item3 = tab.addLayoutItem();

        tab = screenRenderer.addFieldScreenRendererTab();
        final FieldScreenRenderLayoutItem item4 = tab.addLayoutItem();

        final Collection<FieldScreenRenderLayoutItem> items = screenRenderer.getAllScreenRenderItems();
        assertThat(items, contains(item1, item2, item3, item4));
    }
}

