/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.security.auth.trustedapps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultTrustedApplicationManager
{
    @Test
    public void testNullInCtorThrows()
    {
        try
        {
            new DefaultTrustedApplicationManager(null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testGetAllForMultiples()
    {
        Collection<TrustedApplicationData> datas = Lists.newArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));
        datas.add(new MockTrustedApplicationData(2, "anotherApplicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        final Set<TrustedApplicationInfo> allInfos = manager.getAll();
        assertEquals(2, allInfos.size());
        for (TrustedApplicationInfo info : allInfos)
        {
            assertNotNull(info);
            assertNotNull(info.getPublicKey());
            assertNotNull(info.getID());
        }
    }

    @Test
    public void testGetBy()
    {
        Collection<TrustedApplicationData> datas = Lists.newArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        assertEquals(1, manager.getAll().size());
        TrustedApplicationInfo info = manager.get(1);
        assertNotNull(info);
        assertNotNull(info.getPublicKey());
        assertEquals("name", info.getName());
        assertEquals("applicationId", info.getID());
        assertEquals("192.168.0.*", info.getIpMatch());
        assertEquals("urlMatch", info.getUrlMatch());
        assertEquals(0, info.getTimeout());

        info = manager.get("applicationId");
        assertNotNull(info);
        assertNotNull(info.getPublicKey());
        assertEquals("name", info.getName());
        assertEquals("applicationId", info.getID());
        assertEquals("192.168.0.*", info.getIpMatch());
        assertEquals("urlMatch", info.getUrlMatch());
        assertEquals(0, info.getTimeout());
    }

    @Test
    public void testGetNotExists()
    {
        Collection<TrustedApplicationData> datas = Lists.newArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        assertEquals(1, manager.getAll().size());
        TrustedApplicationInfo info = manager.get(2);
        assertNull(info);

        info = manager.get("someApplicationId");
        assertNull(info);
    }

    @Test
    public void testDelete()
    {
        Collection<TrustedApplicationData> datas = Lists.newArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        assertEquals(1, manager.getAll().size());
        assertTrue(manager.delete(new MockUser("test"), 1));
        assertEquals(0, manager.getAll().size());
    }

    @Test
    public void testStore()
    {
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(Collections.<TrustedApplicationData>emptyList());
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store);
        Date testStart = new Date();

        TrustedApplicationInfo info = manager.store("createUser", new TrustedApplicationBuilder().set(new MockTrustedApplicationData(0, "appId", "name", 1000)).toInfo());
        assertNotNull(info);
        assertEquals(1, manager.getAll().size());
        assertEquals(1, store.getAll().size());

        AuditLog created = store.getByApplicationId("appId").getCreated();
        assertNotNull(created);
        assertEquals("createUser", created.getWho());
        assertNotNull(created.getWhen());
        assertTrue(testStart.getTime() <= created.getWhen().getTime());

        AuditLog updated = store.getByApplicationId("appId").getUpdated();
        assertNotNull(updated);
        assertEquals("createUser", updated.getWho());
        assertNotNull(updated.getWhen());
        assertEquals(created.getWhen().getTime(), updated.getWhen().getTime());

        sleep(16);

        TrustedApplicationInfo newInfo = manager.store("updateUser", new TrustedApplicationBuilder().set(new MockTrustedApplicationData(info.getNumericId(), "appId", "name", 1000)).toInfo());
        assertNotNull(newInfo);
        assertEquals(1, manager.getAll().size());
        assertEquals(1, store.getAll().size());

        created = store.getByApplicationId("appId").getCreated();
        assertNotNull(created);
        assertEquals("createUser", created.getWho());
        assertNotNull(created.getWhen());
        assertTrue(testStart.getTime() <= created.getWhen().getTime());

        updated = store.getByApplicationId("appId").getUpdated();
        assertNotNull(updated);
        assertEquals("updateUser", updated.getWho());
        assertNotNull(updated.getWhen());
        assertTrue(created.getWhen().getTime() < updated.getWhen().getTime());
    }

    @Test
    public void testStoreUser()
    {
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(Collections.<TrustedApplicationData>emptyList());
        final AtomicReference<String> userRef = new AtomicReference<String>();
        final AtomicReference<TrustedApplicationInfo> infoRef  = new AtomicReference<TrustedApplicationInfo>();
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store)
        {
            @Override
            public TrustedApplicationInfo store(String user, TrustedApplicationInfo info)
            {
                assertTrue(userRef.compareAndSet(null, user));
                assertTrue(infoRef.compareAndSet(null, info));

                return info;
            }
        };
        TrustedApplicationInfo info = new TrustedApplicationBuilder().set(new MockTrustedApplicationData(0, "appId", "name", 1000)).toInfo();
        MockUser user = new MockUser("IamAraNDOMtestUser");

        assertSame(info, manager.store(user, info));
        assertEquals(user.getName(), userRef.get());
        assertSame(info, infoRef.get());
    }

    @Test
    public void testStoreNonExistentApp()
    {
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(Collections.<TrustedApplicationData>emptyList());
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store);
        User createUser = new MockUser("createUser");
        try
        {
            manager.store(createUser, new TrustedApplicationBuilder().set(new MockTrustedApplicationData(1, "appId", "name", 1000)).toInfo());
            fail("Can't update a nonexistent entity, should have thrown IllegalArg");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testStoreExistingAppWithDifferentAppId()
    {
        List<TrustedApplicationData> datas = new ArrayList<TrustedApplicationData>();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(datas);
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store);
        User createUser = new MockUser("createUser");

        try
        {
            manager.store(createUser, new TrustedApplicationBuilder().set(new MockTrustedApplicationData(1, "appId", "name", 1000)).toInfo());
            fail("Can't change an existing entity's applicationId, should have thrown IllegalArg");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    static void sleep(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
