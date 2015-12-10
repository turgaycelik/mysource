package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.FieldScreenImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenImpl
{
    private static final String FIELD_ID = "id";
    private static final String DEFAULT_DESCRIPTION = "description";
    private static final String FIELD_DESCRIPTION = DEFAULT_DESCRIPTION;
    private static final String DEFAULT_NAME = "name";
    private static final String FIELD_NAME = DEFAULT_NAME;
    private static final int DEFAULT_ID = 10;

    @Test
    public void testCotrNullGenericValue() throws Exception
    {
        FieldScreenImpl fieldScreen = new FieldScreenImpl(null);
        assertNull(fieldScreen.getDescription());
        assertNull(fieldScreen.getId());
        assertNull(fieldScreen.getName());

        fieldScreen = new FieldScreenImpl(null, null);
        assertNull(fieldScreen.getDescription());
        assertNull(fieldScreen.getId());
        assertNull(fieldScreen.getName());
    }

    @Test
    public void testCotrGenericValue() throws Exception
    {
        final long id = 100L;
        final String name = DEFAULT_NAME;
        final String description = "mine";

        MockGenericValue mgv = createGv(id, name, description);

        FieldScreenImpl fieldScreen = new FieldScreenImpl(null, mgv);
        assertEquals(Long.valueOf(id), fieldScreen.getId());
        assertEquals(name, fieldScreen.getName());
        assertEquals(description, fieldScreen.getDescription());
        assertFalse(fieldScreen.isModified());
    }

    @Test
    public void testSetIdNoGV() throws Exception
    {
        FieldScreenImpl fieldScreen = new FieldScreenImpl(null);
        assertNull(fieldScreen.getId());
        fieldScreen.setId(100L);
        assertEquals(100L, (long) fieldScreen.getId());
        fieldScreen.setId(101L);
        assertEquals(101L, (long) fieldScreen.getId());
    }

    @Test
    public void testSetIdWithGV() throws Exception
    {
        FieldScreenImpl fieldScreen = new FieldScreenImpl(null, new MockGenericValue("testSetIdWithGV"));
        assertNull(fieldScreen.getId());
        try
        {
            fieldScreen.setId(100L);
            fail("Should not be able to set id when GV is present.");
        }
        catch (IllegalStateException expected)
        {
            //good.
        }
    }

    @Test
    public void testSetNameNoGv() throws Exception
    {
        FieldScreenImpl fieldScreen = new FieldScreenImpl(null);
        assertNull(fieldScreen.getName());
        assertFalse(fieldScreen.isModified());
        fieldScreen.setName(DEFAULT_NAME);
        assertEquals(DEFAULT_NAME, fieldScreen.getName());
        assertTrue(fieldScreen.isModified());
    }

    @Test
    public void testSetNameGv() throws Exception
    {
        FieldScreenImpl fieldScreen = new FieldScreenImpl(null, createGv());
        assertEquals(DEFAULT_NAME, fieldScreen.getName());
        assertFalse(fieldScreen.isModified());
        fieldScreen.setName(DEFAULT_NAME);
        assertEquals(DEFAULT_NAME, fieldScreen.getName());
        assertFalse(fieldScreen.isModified());
        assertEquals(DEFAULT_NAME, fieldScreen.getGenericValue().get(FIELD_NAME));

        final String newName = "newName";
        fieldScreen.setName(newName);
        assertEquals(newName, fieldScreen.getName());
        assertTrue(fieldScreen.isModified());
        assertEquals(newName, fieldScreen.getGenericValue().get(FIELD_NAME));

        fieldScreen.setName(null);
        assertNull(fieldScreen.getName());
        assertTrue(fieldScreen.isModified());
        assertNull(fieldScreen.getGenericValue().get(FIELD_NAME));
    }

    @Test
    public void testSetDescriptionNoGv() throws Exception
    {
        final String desc = "desc";

        FieldScreenImpl fieldScreen = new FieldScreenImpl(null);
        assertNull(fieldScreen.getDescription());
        assertFalse(fieldScreen.isModified());
        fieldScreen.setDescription(desc);

        assertEquals(desc, fieldScreen.getDescription());
        assertTrue(fieldScreen.isModified());
    }

    @Test
    public void testSetDescriptopnGv() throws Exception
    {

        FieldScreenImpl fieldScreen = new FieldScreenImpl(null, createGv());
        assertEquals(DEFAULT_DESCRIPTION, fieldScreen.getDescription());
        assertFalse(fieldScreen.isModified());
        fieldScreen.setDescription(DEFAULT_DESCRIPTION);
        assertEquals(DEFAULT_DESCRIPTION, fieldScreen.getDescription());
        assertEquals(DEFAULT_DESCRIPTION, fieldScreen.getGenericValue().get(FIELD_DESCRIPTION));
        assertFalse(fieldScreen.isModified());

        final String newdesc = "newdescription";
        fieldScreen.setDescription(newdesc);
        assertEquals(newdesc, fieldScreen.getDescription());
        assertEquals(newdesc, fieldScreen.getGenericValue().get(FIELD_DESCRIPTION));
        assertTrue(fieldScreen.isModified());

        fieldScreen.setDescription(null);
        assertNull(fieldScreen.getDescription());
        assertNull(fieldScreen.getGenericValue().get(FIELD_DESCRIPTION));
        assertTrue(fieldScreen.isModified());
    }

    @Test
    public void testGetTabs() throws Exception
    {
        List<FieldScreenTab> tabs = Arrays.<FieldScreenTab>asList(new MockFieldScreenTab(), new MockFieldScreenTab());

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);

        replay(mgr);

        assertEquals(tabs, fieldScreen.getTabs());

        verify(mgr);
    }

    @Test
    public void testGetTabsPosition() throws Exception
    {
        List<FieldScreenTab> tabs = Arrays.<FieldScreenTab>asList(new MockFieldScreenTab(), new MockFieldScreenTab());

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);

        replay(mgr);


        int count = 0;
        for (FieldScreenTab tab : tabs)
        {
            assertSame(tab, fieldScreen.getTab(count++));
        }

        try
        {
            fieldScreen.getTab(count);
            fail("Should not be able to get tabs past the end.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //good.
        }

        verify(mgr);
    }

    @Test
    public void testAddTab() throws Exception
    {
        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(new MockFieldScreenTab(), new MockFieldScreenTab()));

        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final AtomicBoolean resequenceCalled = new AtomicBoolean(false);
        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.set(true);
            }

            @Override
            public void resequence()
            {
                resequenceCalled.set(true);
            }
        };
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);

        replay(mgr);

        final FieldScreenTab tab = fieldScreen.addTab("testTab");
        assertEquals("testTab", tab.getName());
        assertEquals(2, tab.getPosition());
        assertSame(fieldScreen, tab.getFieldScreen());
        assertTrue(storeCalled.get());
        assertTrue(resequenceCalled.get());

        verify(mgr);
    }

    @Test
    public void testRemoveTab() throws Exception
    {
        final StorableTab remainingTab = new StorableTab();
        final StorableTab removedTab = new StorableTab();
        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(removedTab, remainingTab));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final AtomicBoolean resequenceCalled = new AtomicBoolean(false);


        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv())
        {
            @Override
            public void store()
            {
                storeCalled.set(true);
            }

            @Override
            public void resequence()
            {
                resequenceCalled.set(true);
            }
        };
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);

        replay(mgr);

        fieldScreen.removeTab(0);
        assertTrue(storeCalled.get());
        assertTrue(resequenceCalled.get());

        assertEquals(1, removedTab.removeCount);
        assertEquals(1, fieldScreen.getTabs().size());
        assertSame(remainingTab, fieldScreen.getTab(0));

        try
        {
            fieldScreen.removeTab(1);
            fail("Should not be able to remove tabs past the end.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //good.
        }

        verify(mgr);
    }

    @Test
    public void testMoveFieldScreenTabLeft()
    {
        final StorableTab firstTab = new StorableTab();
        final StorableTab secondTab = new StorableTab();
        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(firstTab, secondTab));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final AtomicBoolean resequenceCalled = new AtomicBoolean(false);

        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv())
        {
            @Override
            public void resequence()
            {
                resequenceCalled.set(true);
            }
        };
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);
        replay(mgr);

        fieldScreen.moveFieldScreenTabLeft(1);
        assertTrue(resequenceCalled.get());

        assertEquals(Arrays.<FieldScreenTab>asList(secondTab, firstTab), fieldScreen.getTabs());

        try
        {
            fieldScreen.moveFieldScreenTabLeft(2);
            fail("Should not be able to move tabs past the end.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //good.
        }

        verify(mgr);

    }

    @Test
    public void testMoveFieldScreenTabRight()
    {
        final StorableTab firstTab = new StorableTab();
        final StorableTab secondTab = new StorableTab();
        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(firstTab, secondTab));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final AtomicBoolean resequenceCalled = new AtomicBoolean(false);

        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv())
        {
            @Override
            public void resequence()
            {
                resequenceCalled.set(true);
            }
        };
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);
        replay(mgr);

        fieldScreen.moveFieldScreenTabRight(0);
        assertTrue(resequenceCalled.get());

        assertEquals(Arrays.<FieldScreenTab>asList(secondTab, firstTab), fieldScreen.getTabs());

        try
        {
            fieldScreen.moveFieldScreenTabRight(2);
            fail("Should not be able to move tabs past the end.");
        }
        catch (IndexOutOfBoundsException expected)
        {
            //good.
        }

        verify(mgr);
    }

    @Test
    public void testResequence()
    {
        final MockFieldScreenTab firstTab = new MockFieldScreenTab();
        final MockFieldScreenTab secondTab = new MockFieldScreenTab();
        firstTab.setPosition(102020);
        secondTab.setPosition(3985903);

        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(firstTab, secondTab));
        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);
        replay(mgr);

        fieldScreen.resequence();

        int count = 0;
        for (FieldScreenTab tab : tabs)
        {
            assertEquals(count++, tab.getPosition());
        }

        verify(mgr);
    }

    @Test
    public void testStoreNotModified() throws Exception
    {
        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        replay(mgr);

        fieldScreen.store();

        verify(mgr);
    }

    @Test
    public void testStoreNewWithOutTabs() throws Exception
    {
        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr);
        mgr.createFieldScreen(EasyMock.<FieldScreen>same(fieldScreen));
        replay(mgr);

        fieldScreen.setModified(true);
        fieldScreen.store();

        verify(mgr);

    }

    @Test
    public void testStoreUpdateWithOutTabs() throws Exception
    {
        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        mgr.updateFieldScreen(EasyMock.<FieldScreen>same(fieldScreen));
        replay(mgr);

        fieldScreen.setModified(true);
        fieldScreen.store();

        verify(mgr);
    }

    @Test
    public void testStoreWithTabs() throws Exception
    {
        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(new StorableTab(), new StorableTab()));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);
        replay(mgr);

        fieldScreen.getTabs();
        fieldScreen.store();

        for (FieldScreenTab tab : tabs)
        {
            final StorableTab storableTab = (StorableTab) tab;
            assertEquals(1, storableTab.storeCount);
            assertEquals(0, storableTab.removeCount);
        }

        verify(mgr);
    }

    @Test
    public void testRemoveTabs() throws Exception
    {
        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Arrays.asList(new StorableTab(), new StorableTab()));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);

        for (FieldScreenTab tab : tabs)
        {
            mgr.removeFieldScreenLayoutItems(EasyMock.<FieldScreenTab>same(tab));
        }

        mgr.removeFieldScreenTabs(same(fieldScreen));
        mgr.removeFieldScreen(10L);

        replay(mgr);

        fieldScreen.remove();

        verify(mgr);

    }

    @Test
    public void testContainsField() throws Exception
    {
        MockFieldScreenTab tab = new MockFieldScreenTab();
        tab.addFieldScreenLayoutItem("one");
        tab.addFieldScreenLayoutItem("two");

        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Collections.singletonList(tab));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);
        replay(mgr);

        assertFalse(fieldScreen.containsField("rjeke"));
        assertTrue(fieldScreen.containsField("one"));

        verify(mgr);
    }

    @Test
    public void testRemoveScreenLayoutItem() throws Exception
    {
        MockFieldScreenTab tab = new MockFieldScreenTab();
        tab.addFieldScreenLayoutItem("one");
        tab.addFieldScreenLayoutItem("two");

        List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>(Collections.singletonList(tab));

        final FieldScreenManager mgr = createMock(FieldScreenManager.class);
        final FieldScreenImpl fieldScreen = new FieldScreenImpl(mgr, createGv());
        expect(mgr.getFieldScreenTabs(EasyMock.<FieldScreen>same(fieldScreen))).andReturn(tabs);
        replay(mgr);

        fieldScreen.removeFieldScreenLayoutItem("two");
        assertNotNull(tab.getFieldScreenLayoutItem("one"));
        assertNull(tab.getFieldScreenLayoutItem("two"));

        try
        {
            fieldScreen.removeFieldScreenLayoutItem("sdkdlsa");
        }
        catch (IllegalArgumentException expected)
        {
            //good
        }

        verify(mgr);
    }

    private MockGenericValue createGv(final long id, final String name, final String description)
    {
        MockGenericValue mgv = new MockGenericValue("dontCare");
        mgv.set(FIELD_ID, id);
        mgv.set(FIELD_NAME, name);
        mgv.set(FIELD_DESCRIPTION, description);
        return mgv;
    }

    private MockGenericValue createGv()
    {
        return createGv(DEFAULT_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION);
    }

    private static class StorableTab extends MockFieldScreenTab
    {
        private int storeCount = 0;
        private int removeCount = 0;

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
