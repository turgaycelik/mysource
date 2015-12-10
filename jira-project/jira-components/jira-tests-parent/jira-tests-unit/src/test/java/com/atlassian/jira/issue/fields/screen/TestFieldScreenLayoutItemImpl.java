package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItemImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenLayoutItemImpl
{
    private static final String FIELD_ID = "id";
    private static final String FIELD_FIELD = "fieldidentifier";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_FIELDSCREENTAB = "fieldscreentab";
    private static final long DEFAULT_ID = 3743283L;
    private static final String DEFAULT_FIELDID = "5210";
    private static final long DEFAULT_SEQUENCE = 303L;

    @Test
    public void testCotrNullGenericValue() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null);
        assertNull(screenLayoutItem.getId());
        assertEquals(0, screenLayoutItem.getPosition());
        assertFalse(screenLayoutItem.isModified());
    }

    @Test
    public void testCotrWithGenericValue() throws Exception
    {
        final long id = 178L;
        final String fieldId = "field";
        final long sequence = 20L;

        final MockGenericValue value = createGv(id, fieldId, sequence);

        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null, value);
        assertEquals(id, (long) screenLayoutItem.getId());
        assertEquals(fieldId, screenLayoutItem.getFieldId());
        assertEquals((int) sequence, screenLayoutItem.getPosition());
    }

    @Test
    public void testCotrWithGenericValueNoPosition() throws Exception
    {
        final long id = 178L;
        final String fieldId = "field";

        final MockGenericValue value = new MockGenericValue("dontCare");
        value.set(FIELD_ID, id);
        value.set(FIELD_FIELD, fieldId);

        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null, value);
        assertEquals(id, (long) screenLayoutItem.getId());
        assertEquals(fieldId, screenLayoutItem.getFieldId());
        assertEquals(0, screenLayoutItem.getPosition());
    }

    @Test
    public void testSetPositionWithNoGv() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null);

        assertEquals(0, screenLayoutItem.getPosition());
        assertFalse(screenLayoutItem.isModified());
        screenLayoutItem.setPosition(5);

        assertEquals(5, screenLayoutItem.getPosition());
        assertTrue(screenLayoutItem.isModified());
    }

    @Test
    public void testSetPositionWithGv() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null, createGv());

        assertEquals(DEFAULT_SEQUENCE, screenLayoutItem.getPosition());
        assertFalse(screenLayoutItem.isModified());
        screenLayoutItem.setPosition((int) DEFAULT_SEQUENCE);
        assertEquals(DEFAULT_SEQUENCE, screenLayoutItem.getPosition());
        assertEquals(DEFAULT_SEQUENCE, screenLayoutItem.getGenericValue().get(FIELD_SEQUENCE));
        assertFalse(screenLayoutItem.isModified());

        final int newPos = 5;
        screenLayoutItem.setPosition(newPos);
        assertEquals(newPos, screenLayoutItem.getPosition());
        assertEquals((long) newPos, screenLayoutItem.getGenericValue().get(FIELD_SEQUENCE));
        assertTrue(screenLayoutItem.isModified());
    }

    @Test
    public void testSetFieldIdNoGv() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null);
        final String fieldValue = "field";

        assertNull(screenLayoutItem.getFieldId());
        assertFalse(screenLayoutItem.isModified());
        screenLayoutItem.setFieldId(fieldValue);

        assertEquals(fieldValue, screenLayoutItem.getFieldId());
        assertTrue(screenLayoutItem.isModified());
    }

    @Test
    public void testSetFieldIdWithGv() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null, createGv());

        assertEquals(DEFAULT_FIELDID, screenLayoutItem.getFieldId());
        assertFalse(screenLayoutItem.isModified());
        screenLayoutItem.setFieldId(DEFAULT_FIELDID);
        assertEquals(DEFAULT_FIELDID, screenLayoutItem.getFieldId());
        assertEquals(DEFAULT_FIELDID, screenLayoutItem.getGenericValue().get(FIELD_FIELD));
        assertFalse(screenLayoutItem.isModified());

        final String newField = "5";
        screenLayoutItem.setFieldId(newField);
        assertEquals(newField, screenLayoutItem.getFieldId());
        assertEquals(newField, screenLayoutItem.getGenericValue().get(FIELD_FIELD));
        assertTrue(screenLayoutItem.isModified());
    }

    @Test
    public void testSetFieldScreenTabWithGv() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null, createGv());
        final MockFieldScreenTab tab = new MockFieldScreenTab().setId(5L);

        assertNull(screenLayoutItem.getFieldScreenTab());
        screenLayoutItem.setFieldScreenTab(tab);
        assertEquals(tab.getId(), screenLayoutItem.getGenericValue().get(FIELD_FIELDSCREENTAB));
        assertTrue(screenLayoutItem.isModified());
        assertSame(tab, screenLayoutItem.getFieldScreenTab());

        screenLayoutItem.setFieldScreenTab(null);
        assertNull(screenLayoutItem.getGenericValue().get(FIELD_FIELDSCREENTAB));
        assertTrue(screenLayoutItem.isModified());
        screenLayoutItem.setModified(false);
        screenLayoutItem.setFieldScreenTab(null);
        assertNull(screenLayoutItem.getGenericValue().get(FIELD_FIELDSCREENTAB));
        assertFalse(screenLayoutItem.isModified());

        final MockFieldScreenTab tab2 = new MockFieldScreenTab().setId(6L);
        screenLayoutItem.setFieldScreenTab(tab2);
        assertEquals(tab2.getId(), screenLayoutItem.getGenericValue().get(FIELD_FIELDSCREENTAB));
        assertTrue(screenLayoutItem.isModified());
        assertSame(tab2, screenLayoutItem.getFieldScreenTab());
    }

    @Test
    public void testStoreNotModified() throws Exception
    {
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(null, null, createGv());
        screenLayoutItem.store();
    }

    @Test
    public void testStoreCreatedNew() throws Exception
    {
        final MockGenericValue value = new MockGenericValue("dontCare");

        value.set(FIELD_FIELD, DEFAULT_FIELDID);
        value.set(FIELD_SEQUENCE, DEFAULT_SEQUENCE);

        final FieldScreenManager smgr = createMock(FieldScreenManager.class);
        final MockFieldManager fm = new MockFieldManager();
        final MockOrderableField field = fm.addMockOrderableField(50);
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(smgr, fm, value);

        smgr.createFieldScreenLayoutItem(same(screenLayoutItem));

        replay(smgr);

        screenLayoutItem.setFieldId(field.getId());
        screenLayoutItem.store();

        verify(smgr);
    }

    @Test
    public void testStoreCreatedUpdate() throws Exception
    {
        final FieldScreenManager smgr = createMock(FieldScreenManager.class);
        final MockFieldManager fm = new MockFieldManager();
        final MockOrderableField field = fm.addMockOrderableField(50);
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(smgr, fm, createGv());

        smgr.updateFieldScreenLayoutItem(same(screenLayoutItem));

        replay(smgr);

        screenLayoutItem.setFieldId(field.getId());
        screenLayoutItem.store();

        verify(smgr);
    }

    @Test
    public void testRemove() throws Exception
    {
        final FieldScreenManager smgr = createMock(FieldScreenManager.class);
        final MockFieldManager fm = new MockFieldManager();
        final MockOrderableField field = fm.addMockOrderableField(50);
        final FieldScreenLayoutItemImpl screenLayoutItem = new FieldScreenLayoutItemImpl(smgr, fm, createGv());

        smgr.removeFieldScreenLayoutItem(same(screenLayoutItem));

        replay(smgr);

        screenLayoutItem.setFieldId(field.getId());
        screenLayoutItem.remove();

        verify(smgr);
    }

    private static MockGenericValue createGv(final long id, final String fieldId, final long sequence)
    {
        final MockGenericValue value = new MockGenericValue("dontCare");
        value.set(FIELD_ID, id);
        value.set(FIELD_FIELD, fieldId);
        value.set(FIELD_SEQUENCE, sequence);
        return value;
    }

    private static MockGenericValue createGv()
    {
        return createGv(DEFAULT_ID, DEFAULT_FIELDID, DEFAULT_SEQUENCE);
    }
}
