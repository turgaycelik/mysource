package com.atlassian.jira.issue.fields.screen;

import java.util.Collections;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.mock.issue.MockIssue;

import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createControl;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.AbstractFieldScreenLayoutItem}.
 *
 * @since v4.1
 */
public class TestAbstractFieldScreenLayoutItem
{
    @Test
    public void testGetOrderableField() throws Exception
    {
        final String fieldId = "5";
        final OrderableField field = mock(OrderableField.class);
        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);
        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);

        assertThat(testItem.getOrderableField(), sameInstance(field));
    }

    @Test
    public void testGetEditHtmlShown() throws Exception
    {
        final Issue issue = new MockIssue();
        final String html = "Pass";
        final String fieldId = "5";
        final OrderableField field = mock(OrderableField.class);
        when(field.isShown(issue)).thenReturn(true);
        when(field.getEditHtml(null, null, null, issue, Collections.emptyMap())).thenReturn(html);
        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);
        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);

        assertThat(testItem.getEditHtml(null, null, null, issue), is(html));
    }

    @Test
    public void testGetEditHtmlNotShown() throws Exception
    {
        final Issue issue = new MockIssue();
        final String fieldId = "5";
        final OrderableField field = mock(OrderableField.class);
        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);
        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);

        assertThat(testItem.getEditHtml(null, null, null, issue), is(""));
    }

    @Test
    public void testGetCreateHtmlShown() throws Exception
    {
        final Issue issue = new MockIssue();
        final String html = "Pass";
        final String fieldId = "5";

        final OrderableField field = mock(OrderableField.class);
        when(field.isShown(issue)).thenReturn(true);
        when(field.getCreateHtml(null, null, null, issue, Collections.emptyMap())).thenReturn(html);

        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals(html, testItem.getCreateHtml(null, null, null, issue));
    }

    @Test
    public void testGetCreateHtmlNotShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String fieldId = "5";

        final OrderableField field = mock(OrderableField.class);
        when(field.isShown(issue)).thenReturn(false);

        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals("", testItem.getCreateHtml(null, null, null, issue));
    }

    @Test
    public void testGetViewHtmlShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String html = "Pass";
        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = mock(OrderableField.class);
        when(field.isShown(issue)).thenReturn(true);
        when(field.getViewHtml(null, null, issue, Collections.emptyMap())).thenReturn(html);

        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals(html, testItem.getViewHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testGetViewHtmlNotShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String fieldId = "5";

        final OrderableField field = mock(OrderableField.class);
        when(field.isShown(issue)).thenReturn(false);

        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals("", testItem.getViewHtml(null, null, null, issue));
    }

    @Test
    public void testIsShown() throws Exception
    {
        final Issue issue = new MockIssue();
        final String fieldId = "5";
        final OrderableField field = mock(OrderableField.class);
        when(field.isShown(issue)).thenReturn(true, false);
        final FieldManager fieldManager = mock(FieldManager.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);
        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);

        assertThat("Shown when field says it is", testItem.isShown(issue), is(true));
        assertThat("Hidden when field says it is", testItem.isShown(issue), is(false));
    }



    static class TestItem extends AbstractFieldScreenLayoutItem
    {
        TestItem(FieldManager fieldManager)
        {
            super(null, fieldManager);
        }

        public Long getId()
        {
            return null;
        }

        public void setPosition(final int position)
        {
        }

        public void setFieldId(final String fieldId)
        {
            this.fieldId = fieldId;
        }

        public void setFieldScreenTab(final FieldScreenTab fieldScreenTab)
        {
        }

        public void store()
        {
        }

        public void remove()
        {
        }

        @Override
        protected void init()
        {
        }
    }
}
