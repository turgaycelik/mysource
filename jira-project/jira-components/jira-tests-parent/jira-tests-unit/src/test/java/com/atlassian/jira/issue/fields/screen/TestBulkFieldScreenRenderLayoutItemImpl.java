package com.atlassian.jira.issue.fields.screen;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.BulkFieldScreenRenderLayoutItemImpl}.
 *
 * @since v4.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBulkFieldScreenRenderLayoutItemImpl
{
    @AvailableInContainer
    @Mock
    private HackyFieldRendererRegistry mockHackyFieldRendererRegistry;
    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Test
    public void testIsShownTrue() throws Exception
    {
        final FieldScreenLayoutItem screenLayoutItem = Mockito.mock(FieldScreenLayoutItem.class);
        final FieldLayoutItem itemOne = Mockito.mock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = Mockito.mock(FieldLayoutItem.class);
        Issue mockIssue = new MockIssue(67L);

        when(screenLayoutItem.isShown(mockIssue)).thenReturn(true);
        when(itemOne.isHidden()).thenReturn(false);
        when(itemTwo.isHidden()).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, screenLayoutItem, Arrays.asList(itemOne, itemTwo));
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isShow(mockIssue), is(Boolean.TRUE));
    }

    @Test
    public void testIsShownTrueNoFieldScreenLayoutItem() throws Exception
    {
        final FieldLayoutItem itemOne = Mockito.mock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = Mockito.mock(FieldLayoutItem.class);
        Issue mockIssue = new MockIssue(67L);

        when(itemOne.isHidden()).thenReturn(false);
        when(itemTwo.isHidden()).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Arrays.asList(itemOne, itemTwo));
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isShow(mockIssue), is(Boolean.TRUE));
    }

    @Test
    public void testIsShownFalseScreenLayoutItem() throws Exception
    {
        final FieldScreenLayoutItem screenLayoutItem = Mockito.mock(FieldScreenLayoutItem.class);
        final FieldLayoutItem itemOne = Mockito.mock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = Mockito.mock(FieldLayoutItem.class);
        Issue mockIssue = new MockIssue(67L);

        when(screenLayoutItem.isShown(mockIssue)).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, screenLayoutItem, Arrays.asList(itemOne, itemTwo));
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isShow(mockIssue), is(Boolean.FALSE));
    }

    @Test
    public void testIsShownFalseFieldLayoutItem() throws Exception
    {
        final FieldScreenLayoutItem screenLayoutItem = Mockito.mock(FieldScreenLayoutItem.class);
        final FieldLayoutItem itemOne = Mockito.mock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = Mockito.mock(FieldLayoutItem.class);
        final FieldLayoutItem itemThree = Mockito.mock(FieldLayoutItem.class);
        Issue mockIssue = new MockIssue(67L);

        when(screenLayoutItem.isShown(mockIssue)).thenReturn(true);
        when(itemOne.isHidden()).thenReturn(false);
        when(itemTwo.isHidden()).thenReturn(true);

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, screenLayoutItem, Arrays.asList(itemOne, itemTwo, itemThree));
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isShow(mockIssue), is(Boolean.FALSE));
    }

    @Test
    public void testIsRequiredFalse() throws Exception
    {
        final FieldLayoutItem itemOne = Mockito.mock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = Mockito.mock(FieldLayoutItem.class);

        when(itemOne.isRequired()).thenReturn(false);
        when(itemTwo.isRequired()).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Arrays.asList(itemOne, itemTwo));
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isRequired(), is(Boolean.FALSE));
    }

    @Test
    public void testIsRequiredTrue() throws Exception
    {
        final FieldLayoutItem itemOne = Mockito.mock(FieldLayoutItem.class, "itemOne");
        final FieldLayoutItem itemTwo = Mockito.mock(FieldLayoutItem.class, "itemTwo");

        when(itemOne.isRequired()).thenReturn(false);
        when(itemTwo.isRequired()).thenReturn(true);

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Arrays.asList(itemOne, itemTwo));
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isRequired(), is(true));
    }

    @Test
    public void testIsRequiredNoItems() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        Assert.assertThat(bulkFieldScreenRenderLayoutItem.isRequired(), is(Boolean.FALSE));
    }

    @Test
    public void testGetEditHtmlNoIssues() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = Mockito.mock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(false);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);

        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(of)
                .setFieldDescription(null)
                .setHidden(false)
                .setRequired(false)
                .setFieldManager(mockFieldManager)
                .build();
        when(of.getEditHtml(expectedItem, null, null, null, null)).thenReturn(expectedHtml);
        when(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        Assert.assertThat(bulk.getEditHtml(null, null, Collections.<Issue>emptyList(), null), is(expectedHtml));
    }

    @Test
    public void testGetEditHtmlShown() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = Mockito.mock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(true);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);
        final MockIssue lastIssue = new MockIssue(6);
        final Collection<Issue> issues = Arrays.<Issue>asList(new MockIssue(5), lastIssue);

        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(of)
                .setFieldDescription("the environment field")
                .setHidden(false)
                .setRequired(true)
                .setFieldManager(mockFieldManager)
                .build();
        when(of.isShown(any(Issue.class))).thenReturn(true);
        when(of.getEditHtml(expectedItem, null, null, lastIssue, null)).thenReturn(expectedHtml);
        when(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        Assert.assertThat(bulk.getEditHtml(null, null, issues, null), is(expectedHtml));
    }

    @Test
    public void testGetEditHtmlNotShown() throws Exception
    {
        final String expectedHtml = "";

        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockOrderableField of = mockFieldManager.addMockOrderableField(5);
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(true).setRequired(false);
        final Collection<Issue> issues = Collections.<Issue>singletonList(new MockIssue());

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>singletonList(layoutItem));
        Assert.assertThat(bulk.getEditHtml(null, null, issues, null), is(expectedHtml));
    }

    @Test
    public void testGetViewHtmlNoIssues() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = Mockito.mock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(false);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);
        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder(layoutItem)
                .setOrderableField(of)
                .setFieldDescription(null)
                .setHidden(false)
                .setRequired(false)
                .setFieldManager(mockFieldManager)
                .build();
        when(of.getViewHtml(expectedItem, null, null, null)).thenReturn(expectedHtml);
        when(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        Assert.assertThat(bulk.getViewHtml(null, null, Collections.<Issue>emptyList(), null), is(expectedHtml));
    }

    @Test
    public void testGetViewHtmlShown() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = Mockito.mock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(true);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);
        final MockIssue lastIssue = new MockIssue(6);
        final Collection<Issue> issues = Arrays.<Issue>asList(new MockIssue(5), lastIssue);

        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder(layoutItem)
                .setOrderableField(of)
                .setFieldDescription(null)
                .setHidden(false)
                .setRequired(true)
                .setFieldManager(mockFieldManager)
                .build();
        when(of.isShown(any(Issue.class))).thenReturn(true);
        when(of.getViewHtml(expectedItem, null, lastIssue, null)).thenReturn(expectedHtml);
        when(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).thenReturn(false);

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        Assert.assertThat(bulk.getViewHtml(null, null, issues, null), is(expectedHtml));
    }

    @Test
    public void testGetViewHtmlNotShown() throws Exception
    {
        final String expectedHtml = "";

        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockOrderableField of = mockFieldManager.addMockOrderableField(5);
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(true).setRequired(false);
        final Collection<Issue> issues = Collections.<Issue>singletonList(new MockIssue());

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>singletonList(layoutItem));
        Assert.assertThat(bulk.getViewHtml(null, null, issues, null), is(expectedHtml));
    }

    @Test
    public void testGetOrderableField() throws Exception
    {
        final MockFieldManager mfm = new MockFieldManager();
        final MockOrderableField mockOrderableField = mfm.addMockOrderableField(5);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(mockOrderableField);

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mfm, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>emptyList());
        Assert.assertSame(mockOrderableField, bulk.getOrderableField());
    }

    @Test
    public void testPopulateDefaults() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.populateDefaults(null, null);
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    @Test
    public void testPopuldateFromIssue() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.populateFromIssue(null, null);
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    @Test
    public void testGetRendererType() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getRendererType();
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    @Test
    public void testGetFieldLayoutItem() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getFieldLayoutItem();
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    @Test
    public void testGetEditHtmlInterface() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getEditHtml(null, null, null);
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    @Test
    public void testGetCreateHtmlInterface() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getCreateHtml(null, null, null);
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    @Test
    public void testGetViewHtmlInterface() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getViewHtml(null, null, null);
            Assert.fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }
}
