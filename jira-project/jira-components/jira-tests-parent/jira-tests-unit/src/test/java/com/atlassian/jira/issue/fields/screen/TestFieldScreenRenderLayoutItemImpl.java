package com.atlassian.jira.issue.fields.screen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutItem;
import com.atlassian.jira.mock.issue.MockIssue;

import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenRenderLayoutItemImpl
{
    @Test
    public void testGetOrderableField() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field);

        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, item);

        assertSame(field, testItem.getOrderableField());
    }

    @Test
    public void testIsShownFieldLayoutItemHidden() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(true);

        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, item);

        assertFalse(testItem.isShow(new MockIssue()));
    }

    @Test
    public void testIsShownHiddenOnScreen() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final MockIssue mockIssue = new MockIssue();

        final IMocksControl iMocksControl = createControl();
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);
        expect(screenLayoutItem.isShown(mockIssue)).andReturn(false);

        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item);
        assertFalse(testItem.isShow(mockIssue));
        iMocksControl.verify();
    }

    @Test
    public void testIsShownNoScreen() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final MockIssue mockIssue = new MockIssue();

        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, item);
        assertFalse(testItem.isShow(mockIssue));
    }

    @Test
    public void testIsShownShown() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final MockIssue mockIssue = new MockIssue();

        final IMocksControl iMocksControl = createControl();
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);
        expect(screenLayoutItem.isShown(mockIssue)).andReturn(true);

        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item);
        assertTrue(testItem.isShow(mockIssue));
        iMocksControl.verify();
    }

    @Test
    public void testIsRequiredFalse() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setRequired(false);

        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, item);
        assertFalse(testItem.isRequired());
    }

    @Test
    public void testIsRequiredTrue() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setRequired(true);

        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, item);
        assertTrue(testItem.isRequired());
    }

    @Test
    public void testGetRendererType() throws Exception
    {
        MockOrderableField field = new MockOrderableField("455");
        MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setRendererType("blarg");

        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, item);
        assertEquals(item.getRendererType(), testItem.getRendererType());
    }

    @Test
    public void testGetEditHtmlShown() throws Exception
    {
        final String html = "good";

        final IMocksControl iMocksControl = createControl();
        final MockIssue issue = new MockIssue(23);
        final MockOrderableField field = new MockOrderableField("455");
        final MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);
        expect(screenLayoutItem.getEditHtml(item, null, null, issue, Collections.<String, Object>emptyMap())).andReturn(html);
        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return true;
            }
        };
        assertEquals(html, testItem.getEditHtml(null, null, issue));
        iMocksControl.verify();
    }

    @Test
    public void testGetEditHtmlNotShown() throws Exception
    {
        final String html = "";

        final IMocksControl iMocksControl = createControl();
        final MockIssue issue = new MockIssue(23);
        final MockOrderableField field = new MockOrderableField("455");
        final MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);

        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return false;
            }
        };
        assertEquals(html, testItem.getEditHtml(null, null, issue));
        iMocksControl.verify();
    }

    @Test
    public void testGetCreateHtmlShown() throws Exception
    {
        final String html = "good";

        final IMocksControl iMocksControl = createControl();
        final MockIssue issue = new MockIssue(23);
        final MockOrderableField field = new MockOrderableField("455");
        final MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);
        expect(screenLayoutItem.getCreateHtml(item, null, null, issue, Collections.<String, Object>emptyMap())).andReturn(html);
        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return true;
            }
        };
        assertEquals(html, testItem.getCreateHtml(null, null, issue));
        iMocksControl.verify();
    }

    @Test
    public void testGetCreateHtmlNotShown() throws Exception
    {
        final String html = "";

        final IMocksControl iMocksControl = createControl();
        final MockIssue issue = new MockIssue(23);
        final MockOrderableField field = new MockOrderableField("455");
        final MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);

        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return false;
            }
        };
        assertEquals(html, testItem.getCreateHtml(null, null, issue));
        iMocksControl.verify();
    }

    @Test
    public void testGetViewHtmlShown() throws Exception
    {
        final String html = "good";

        final IMocksControl iMocksControl = createControl();
        final MockIssue issue = new MockIssue(23);
        final MockOrderableField field = new MockOrderableField("455");
        final MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);
        expect(screenLayoutItem.getViewHtml(item, null, null, issue, Collections.<String, Object>emptyMap())).andReturn(html);
        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return true;
            }
        };
        assertEquals(html, testItem.getViewHtml(null, null, issue));
        iMocksControl.verify();
    }

    @Test
    public void testGetViewHtmlNotShown() throws Exception
    {
        final String html = "";

        final IMocksControl iMocksControl = createControl();
        final MockIssue issue = new MockIssue(23);
        final MockOrderableField field = new MockOrderableField("455");
        final MockFieldLayoutItem item = new MockFieldLayoutItem().setOrderableField(field).setHidden(false);
        final FieldScreenLayoutItem screenLayoutItem = iMocksControl.createMock(FieldScreenLayoutItem.class);

        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(screenLayoutItem, item)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return false;
            }
        };
        assertEquals(html, testItem.getViewHtml(null, null, issue));
        iMocksControl.verify();
    }

    @Test
    public void testPopulateDefaults() throws Exception
    {
        final IMocksControl iMocksControl = createControl();
        final OrderableField field = iMocksControl.createMock(OrderableField.class);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", 333L);
        final MockIssue mockIssue = new MockIssue(4334);

        field.populateDefaults(map, mockIssue);


        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, null)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return true;
            }

            @Override
            public OrderableField getOrderableField()
            {
                return field;
            }
        };

        testItem.populateDefaults(map, mockIssue);
        iMocksControl.verify();
    }

    @Test
    public void testPopulateDefaultsNoShown() throws Exception
    {
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, null)
        {
            @Override
            public boolean isShow(final Issue issue)
            {
                return false;
            }
        };

        testItem.populateDefaults(new HashMap<Object, Object>(), new MockIssue());
    }

    @Test
    public void testPopulateFromIssue() throws Exception
    {
        final IMocksControl iMocksControl = createControl();
        final OrderableField field = iMocksControl.createMock(OrderableField.class);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", 333L);
        final MockIssue mockIssue = new MockIssue(4334);

        field.populateFromIssue(map, mockIssue);


        iMocksControl.replay();
        final FieldScreenRenderLayoutItemImpl testItem = new FieldScreenRenderLayoutItemImpl(null, null)
        {
            @Override
            public OrderableField getOrderableField()
            {
                return field;
            }
        };

        testItem.populateFromIssue(map, mockIssue);
        iMocksControl.verify();
    }
}
