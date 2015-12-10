package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.MapBuilder;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.TestFieldScreenTabImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenTabImpl
{
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SCREEN = "fieldscreen";
    private static final long DEFAULT_ID = 3743283L;
    private static final String DEFAULT_NAME = "fieldNameThatILike";
    private static final long DEFAULT_SEQUENCE = 303L;
    private static final long DEFAULT_SCREEN = 322L;

    @Test
    public void testCotrNullGenericValue() throws Exception
    {
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, null);
        assertNull(tab.getId());
        assertEquals(0, tab.getPosition());
        assertFalse(tab.isModified());
    }

    @Test
    public void testCotrWithGenericValue() throws Exception
    {
        final long id = 178L;
        final String name = "field";
        final long sequence = 20L;

        final MockGenericValue value = createGv(id, name, sequence, 22);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, value);
        assertEquals(id, (long) tab.getId());
        assertEquals(name, tab.getName());
        assertEquals((int) sequence, tab.getPosition());
    }

    @Test
    public void testCotrWithGenericValueNoPosition() throws Exception
    {
        final long id = 178L;
        final String name = "field";

        final MockGenericValue value = new MockGenericValue("dontCare");
        value.set(FIELD_ID, id);
        value.set(FIELD_NAME, name);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, value);
        assertEquals(id, (long) tab.getId());
        assertEquals(name, tab.getName());
        assertEquals(0, tab.getPosition());
    }

    @Test
    public void testSetPositionWithNoGv() throws Exception
    {
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, null);

        assertEquals(0, tab.getPosition());
        assertFalse(tab.isModified());
        tab.setPosition(5);

        assertEquals(5, tab.getPosition());
        assertTrue(tab.isModified());
    }

    @Test
    public void testSetPositionWithGv() throws Exception
    {
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, createGv());

        assertEquals(DEFAULT_SEQUENCE, tab.getPosition());
        assertFalse(tab.isModified());
        tab.setPosition((int) DEFAULT_SEQUENCE);
        assertEquals(DEFAULT_SEQUENCE, tab.getPosition());
        assertEquals(DEFAULT_SEQUENCE, tab.getGenericValue().get(FIELD_SEQUENCE));
        assertFalse(tab.isModified());

        final int newPos = 5;
        tab.setPosition(newPos);
        assertEquals(newPos, tab.getPosition());
        assertEquals((long) newPos, tab.getGenericValue().get(FIELD_SEQUENCE));
        assertTrue(tab.isModified());
    }

    @Test
    public void testSetNameNoGv() throws Exception
    {
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, null);
        final String nameValue = "nameksjldksa";

        assertNull(tab.getName());
        assertFalse(tab.isModified());
        tab.setName(nameValue);

        assertEquals(nameValue, tab.getName());
        assertTrue(tab.isModified());
    }

    @Test
    public void testSetFieldIdWithGv() throws Exception
    {
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, createGv());

        assertEquals(DEFAULT_NAME, tab.getName());
        assertFalse(tab.isModified());
        tab.setName(DEFAULT_NAME);
        assertEquals(DEFAULT_NAME, tab.getName());
        assertEquals(DEFAULT_NAME, tab.getGenericValue().get(FIELD_NAME));
        assertFalse(tab.isModified());

        final String newName = "5";
        tab.setName(newName);
        assertEquals(newName, tab.getName());
        assertEquals(newName, tab.getGenericValue().get(FIELD_NAME));
        assertTrue(tab.isModified());
    }

    @Test
    public void testFieldScreen() throws Exception
    {
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(null, createGv());

        MockFieldScreen screen = new MockFieldScreen();
        screen.setId(DEFAULT_SCREEN);

        assertNull(tab.getFieldScreen());
        assertFalse(tab.isModified());

        tab.setFieldScreen(screen);
        assertSame(screen, tab.getFieldScreen());
        assertFalse(tab.isModified());

        screen = new MockFieldScreen();
        screen.setId(DEFAULT_SCREEN + 1);
        tab.setFieldScreen(screen);
        assertSame(screen, tab.getFieldScreen());
        assertTrue(tab.isModified());
        assertEquals(screen.getId(), tab.getGenericValue().get(FIELD_SCREEN));
        tab.setModified(false);

        assertFalse(tab.isModified());
        tab.setFieldScreen(null);
        assertNull(tab.getFieldScreen());
        assertTrue(tab.isModified());
        assertNull(tab.getGenericValue().get(FIELD_SCREEN));
    }

    @Test
    public void testStoreNotModified() throws Exception
    {
        final FieldScreenImpl tab = new FieldScreenImpl(null, createGv());
        tab.store();
    }

    @Test
    public void testStoreCreated() throws Exception
    {
        final MockGenericValue value = new MockGenericValue("dontCare");

        value.set(FIELD_NAME, DEFAULT_NAME);
        value.set(FIELD_SEQUENCE, DEFAULT_SEQUENCE);

        final FieldScreenManager smgr = createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, value);

        smgr.createFieldScreenTab(same(tab));

        replay(smgr);

        tab.rename("sdsdaslda");

        verify(smgr);
    }

    @Test
    public void testStoreUpdate() throws Exception
    {
        final FieldScreenManager smgr = createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        smgr.updateFieldScreenTab(same(tab));

        replay(smgr);

        tab.setName("sdsdaslda");
        tab.store();

        verify(smgr);
    }

    @Test
    public void testStoreItems() throws Exception
    {
        final IMocksControl control = createControl();

        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        final List<FieldScreenLayoutItem> layoutItems = createItems(4);

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(layoutItems);

        control.replay();

        tab.getFieldScreenLayoutItems();
        tab.store();

        for (FieldScreenLayoutItem layoutItem : layoutItems)
        {
            StorableItem sItem = (StorableItem) layoutItem;
            assertEquals(1, sItem.storeCount);
        }

        control.verify();
    }

    @Test
    public void testGetFieldScreenLayoutItems() throws Exception
    {
        final IMocksControl control = createControl();

        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        final List<FieldScreenLayoutItem> layoutItems = createItems(4);

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(layoutItems);

        control.replay();

        assertEquals(layoutItems, tab.getFieldScreenLayoutItems());

        control.verify();
    }

    @Test
    public void testGetFieldScreenLayoutItemsPosition() throws Exception
    {
        final IMocksControl control = createControl();

        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        final List<FieldScreenLayoutItem> layoutItems = createItems(4);

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(layoutItems);

        control.replay();

        int count = 0;
        for (FieldScreenLayoutItem item : layoutItems)
        {
            assertSame(item, tab.getFieldScreenLayoutItem(count++));
        }

        try
        {
            tab.getFieldScreenLayoutItem(count);
            fail("Should not be able to ask for tab outside of range.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //good.
        }

        control.verify();
    }

    @Test
    public void testGetFieldScreenLayoutItem() throws Exception
    {
        final IMocksControl control = createControl();

        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        final List<FieldScreenLayoutItem> layoutItems = createItems(4);

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(layoutItems);

        control.replay();

        int count = 0;
        for (FieldScreenLayoutItem item : layoutItems)
        {
            assertSame(item, tab.getFieldScreenLayoutItem(String.valueOf(count++)));
        }

        assertNull(tab.getFieldScreenLayoutItem("bdbsss"));

        control.verify();
    }

    @Test
    public void testIsConstainsField() throws Exception
    {
        final IMocksControl control = createControl();

        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        final List<FieldScreenLayoutItem> layoutItems = createItems(4);

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(layoutItems);

        control.replay();

        for (FieldScreenLayoutItem item : layoutItems)
        {
            assertTrue(tab.isContainsField(item.getFieldId()));
        }

        assertFalse(tab.isContainsField("bdbsss"));

        control.verify();
    }

    @Test
    public void testAddFieldScreenLayoutItem() throws Exception
    {
        final List<FieldScreenLayoutItem> layoutItems = createItems(4);
        final String newField = "newField";
        final int newPos = layoutItems.size();
        final AtomicBoolean called = new AtomicBoolean(false);

        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void addFieldScreenLayoutItem(final String orderableFieldId, final int position)
            {
                assertEquals(newField, orderableFieldId);
                assertEquals(newPos, position);
                called.set(true);
            }
        };

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(layoutItems);

        control.replay();

        tab.addFieldScreenLayoutItem(newField);
        assertTrue(called.get());

        control.verify();
    }

    @Test
    public void testAddFieldScreenLayoutItemPosition() throws Exception
    {
        final String newField = "newField";

        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final FieldScreenLayoutItem newItem = new MockFieldScreenLayoutItem();
        newItem.setFieldId(newField);
        final AtomicBoolean storeCalled = new AtomicBoolean(false);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.set(true);
            }
        };

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(createItems(4));
        expect(smgr.buildNewFieldScreenLayoutItem(newField)).andReturn(newItem).atLeastOnce();
        control.replay();

        tab.addFieldScreenLayoutItem(newField, 1);

        assertTabOrder(tab, "0", newField, "1", "2", "3");
        assertTrue(storeCalled.get());

        try
        {
            tab.addFieldScreenLayoutItem(newField, 50);
            fail("Should not be able to add an time beyond the end.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //expected.
        }

        control.verify();
    }

    @Test
    public void testMoveFieldScreenLayoutItemFirst() throws Exception
    {
        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final AtomicInteger storeCalled = new AtomicInteger(0);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.incrementAndGet();
            }
        };

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(createItems(4));
        control.replay();

        tab.moveFieldScreenLayoutItemFirst(3);
        assertTabOrder(tab, "3", "0", "1", "2");
        assertEquals(1, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemFirst(0);
        assertTabOrder(tab, "3", "0", "1", "2");
        assertEquals(2, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemFirst(2);
        assertTabOrder(tab, "1", "3", "0", "2");
        assertEquals(3, storeCalled.intValue());

        control.verify();
    }

    @Test
    public void testMoveFieldScreenLayoutItemLast() throws Exception
    {
        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final AtomicInteger storeCalled = new AtomicInteger(0);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.incrementAndGet();
            }
        };

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(createItems(4));
        control.replay();

        tab.moveFieldScreenLayoutItemLast(3);
        assertTabOrder(tab, "0", "1", "2", "3");
        assertEquals(1, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemLast(0);
        assertTabOrder(tab, "1", "2", "3", "0");
        assertEquals(2, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemLast(2);
        assertTabOrder(tab, "1", "2", "0", "3");
        assertEquals(3, storeCalled.intValue());

        control.verify();
    }

    @Test
    public void testMoveFieldScreenLayoutItemDown() throws Exception
    {
        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final AtomicInteger storeCalled = new AtomicInteger(0);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.incrementAndGet();
            }
        };

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(createItems(4));
        control.replay();

        tab.moveFieldScreenLayoutItemDown(0);
        assertTabOrder(tab, "1", "0", "2", "3");
        assertEquals(1, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemDown(1);
        assertTabOrder(tab, "1", "2", "0", "3");
        assertEquals(2, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemDown(2);
        assertTabOrder(tab, "1", "2", "3", "0");
        assertEquals(3, storeCalled.intValue());

        control.verify();
    }

    @Test
    public void testMoveFieldScreenLayoutItemUp() throws Exception
    {
        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final AtomicInteger storeCalled = new AtomicInteger(0);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.incrementAndGet();
            }
        };

        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(createItems(4));
        control.replay();

        tab.moveFieldScreenLayoutItemUp(3);
        assertTabOrder(tab, "0", "1", "3", "2");
        assertEquals(1, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemUp(2);
        assertTabOrder(tab, "0", "3", "1", "2");
        assertEquals(2, storeCalled.intValue());
        tab.moveFieldScreenLayoutItemUp(1);
        assertTabOrder(tab, "3", "0", "1", "2");
        assertEquals(3, storeCalled.intValue());

        control.verify();
    }

    @Test
    public void testRemoveFieldScreenLayoutItem() throws Exception
    {
        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final AtomicInteger storeCalled = new AtomicInteger(0);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.incrementAndGet();
            }
        };

        final List<FieldScreenLayoutItem> layoutItems = createItems(4);
        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(new ArrayList<FieldScreenLayoutItem>(layoutItems));
        control.replay();

        assertSame(layoutItems.get(1), tab.removeFieldScreenLayoutItem(1));
        assertEquals(1, ((StorableItem)layoutItems.get(1)).removeCount);
        assertNull(tab.getFieldScreenLayoutItem(String.valueOf(1)));
        assertTabOrder(tab, "0", "2", "3");

        try
        {
            tab.removeFieldScreenLayoutItem(494);
            fail("Should not be able to remove tab past the end.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //expected.
        }

        control.verify();
    }

    @Test
    public void testMoveFieldScreenLayoutItemToPosition()
    {
        final IMocksControl control = createControl();
        final FieldScreenManager smgr = control.createMock(FieldScreenManager.class);
        final AtomicInteger storeCalled = new AtomicInteger(0);

        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.incrementAndGet();
            }
        };


        final List<FieldScreenLayoutItem> layoutItems = createItems(4);
        expect(smgr.getFieldScreenLayoutItems(tab)).andReturn(new ArrayList<FieldScreenLayoutItem>(layoutItems));
        control.replay();

        tab.moveFieldScreenLayoutItemToPosition(MapBuilder.newBuilder(3, layoutItems.get(0)).toListOrderedMap());
        assertTabOrder(tab, "1", "2", "3", "0");
        assertEquals(1, storeCalled.intValue());

        Map<Integer, FieldScreenLayoutItem> order = MapBuilder.newBuilder(0, layoutItems.get(2))
                .add(1, layoutItems.get(0))
                .add(2, layoutItems.get(3))
                .add(3, layoutItems.get(1)).toListOrderedMap();

        tab.moveFieldScreenLayoutItemToPosition(order);
        assertTabOrder(tab, "2", "0", "3", "1");
        assertEquals(2, storeCalled.intValue());

        control.verify();
    }

    @Test
    public void testRemove() throws Exception
    {
        final FieldScreenManager smgr = createMock(FieldScreenManager.class);
        final FieldScreenTabImpl tab = new FieldScreenTabImpl(smgr, createGv());

        smgr.removeFieldScreenLayoutItems(EasyMock.<FieldScreenTab>same(tab));
        smgr.removeFieldScreenTab(DEFAULT_ID);

        replay(smgr);

        tab.remove();

        verify(smgr);
    }

    private static void assertTabOrder(final FieldScreenTabImpl tab, final String... expectedIds)
    {
        for (int i = 0; i < expectedIds.length; i++)
        {
            String expectedId = expectedIds[i];
            final FieldScreenLayoutItem actualItem = tab.getFieldScreenLayoutItem(i);
            assertEquals(expectedId, actualItem.getFieldId());
            assertEquals(i, actualItem.getPosition());
        }
    }

    private List<FieldScreenLayoutItem> createItems(int count)
    {
        List<FieldScreenLayoutItem> items = new ArrayList<FieldScreenLayoutItem>();
        for (int i = 0; i < count; i++)
        {
            final StorableItem itemOne = new StorableItem();
            itemOne.setFieldId(String.valueOf(i));
            itemOne.setPosition(i);
            items.add(itemOne);
        }

        return items;
    }

    private static MockGenericValue createGv(final long id, final String name, final long sequence, final long screen)
    {
        final MockGenericValue value = new MockGenericValue("dontCare");
        value.set(FIELD_ID, id);
        value.set(FIELD_NAME, name);
        value.set(FIELD_SEQUENCE, sequence);
        value.set(FIELD_SCREEN, screen);
        return value;
    }

    private static MockGenericValue createGv()
    {
        return createGv(DEFAULT_ID, DEFAULT_NAME, DEFAULT_SEQUENCE, DEFAULT_SCREEN);
    }

    private static class StorableItem extends MockFieldScreenLayoutItem
    {
        private int storeCount = 0;
        private int removeCount = 0;

        public StorableItem()
        {
            super();
        }

        @Override
        public void store()
        {
            storeCount++;
        }

        @Override
        public void remove()
        {
            removeCount++;
        }
    }
}
