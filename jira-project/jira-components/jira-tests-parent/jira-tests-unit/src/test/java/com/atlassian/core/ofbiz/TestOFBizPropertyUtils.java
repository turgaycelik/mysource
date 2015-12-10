package com.atlassian.core.ofbiz;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.propertyset.CachingOfBizPropertyEntryStore;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;

import com.opensymphony.module.propertyset.PropertySet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.core.ofbiz.util.OFBizPropertyUtils.getCachingPropertySet;
import static com.atlassian.core.ofbiz.util.OFBizPropertyUtils.getPropertySet;
import static com.atlassian.core.ofbiz.util.OFBizPropertyUtils.removePropertySet;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

public class TestOFBizPropertyUtils extends AbstractOFBizTestCase
{
    @Rule public ClearStatics clearStatics = new ClearStatics();

    DelegatorInterface genericDelegator;
    OfBizPropertyEntryStore ofBizPropertyEntryStore;
    GenericValue entity;
    PropertySet ps;

    @Override
    @Before
    public void setUp()
    {
        genericDelegator = GenericDelegator.getGenericDelegator("default");
        ofBizPropertyEntryStore = new CachingOfBizPropertyEntryStore(genericDelegator, new MemoryCacheManager());

        new MockComponentWorker()
                .addMock(DelegatorInterface.class, genericDelegator)
                .addMock(OfBizPropertyEntryStore.class, ofBizPropertyEntryStore)
                .init();
        entity = new MockGenericValue("Project", new FieldMap("id", 1L));
        removePropertySet(entity);
    }

    @After
    public void tearDown()
    {
        genericDelegator = null;
        ofBizPropertyEntryStore = null;
        entity = null;
        ps = null;
    }

    @Test
    public void testCreateRemovePropertySet()
    {
        final PropertySet stale = getCachingPropertySet(entity);
        final PropertySet ps = getPropertySet(entity);
        assertNotSame("Property set factory should have returned different types", ps.getClass(), stale.getClass());
        assertCreateAndRemoveWork(ps);

        assertThat("Stale cache should still have the cached miss in it", stale.getText("foo"), nullValue());
        stale.setInt("foo", 42);
        assertThat("Updates should work correctly even if the cache is stale", ps.getInt("foo"), equalTo(42));
    }

    @Test
    public void testCreateRemoveCachingPropertySet() throws Exception
    {
        final PropertySet stale = getCachingPropertySet(entity);
        final PropertySet ps = getCachingPropertySet(entity);
        assertSame("Property set factory should have returned the same type", ps.getClass(), stale.getClass());
        assertNotSame("Property set factory should have returned different instances", ps, stale);

        assertCreateAndRemoveWork(ps);

        assertThat("Stale cache should get new value because the other cache should have invalidated the miss", stale.getText("foo"), equalTo("xyzzy"));
        stale.setInt("foo", 42);
        assertThat("Updates should work correctly even if the cache is stale", ps.getInt("foo"), equalTo(42));
    }

    private void assertCreateAndRemoveWork(final PropertySet ps)
    {
        assertThat("Entry should initially not exist", ps.getString("foo"), nullValue());
        ps.setString("foo", "bar");
        assertThat("Set followed by get should work, right?!", ps.getString("foo"), equalTo("bar"));

        // make sure it's set where expected
        assertThat("Entry should have been added to the original", ps.getString("foo"), equalTo("bar"));
        assertThat("Entry should have been added to a new uncached property set", getPropertySet(entity).getString("foo"), equalTo("bar"));
        assertThat("Entry should have been added to a new cached property set", getCachingPropertySet(entity).getString("foo"), equalTo("bar"));

        // remove
        removePropertySet(entity);

        // now make sure it's blank everywhere.  Since we use cached ones to do the remove, this should always work for everyone.
        assertThat("Entry should have been removed from the original", ps.getString("foo"), nullValue());
        assertThat("Entry should have been removed from a new uncached property set", getPropertySet(entity).getString("foo"), nullValue());
        assertThat("Entry should have been removed from a new cached property set", getCachingPropertySet(entity).getString("foo"), nullValue());

        ps.setText("foo", "xyzzy");
        assertThat("Original property set should see the new value", ps.getText("foo"), equalTo("xyzzy"));
    }
}
