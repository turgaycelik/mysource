/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.customfields.persistence;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestEagerLoadingOfBizCustomFieldPersister
{

    @After
    public void tearDown() throws Exception
    {
        JiraAuthenticationContextImpl.getRequestCache().clear();
    }

    @Test
    public void testCacheKeyEquivalence()
    {
        final EagerLoadingOfBizCustomFieldPersister.CacheKey key1 = new EagerLoadingOfBizCustomFieldPersister.CacheKey(new Long(100));
        final EagerLoadingOfBizCustomFieldPersister.CacheKey key2 = new EagerLoadingOfBizCustomFieldPersister.CacheKey(new Long(100));

        assertEquals(key1, key2);

        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testCacheKeyNonEquivalence()
    {
        final EagerLoadingOfBizCustomFieldPersister.CacheKey key1 = new EagerLoadingOfBizCustomFieldPersister.CacheKey(new Long(100));
        final EagerLoadingOfBizCustomFieldPersister.CacheKey key2 = new EagerLoadingOfBizCustomFieldPersister.CacheKey(new Long(101));

        assertFalse(key1.equals(key2));
        assertFalse(key2.equals(key1));

        assertFalse(key1.hashCode() == key2.hashCode());
    }

    @Test
    public void testCacheKeyNullNonEquivalence()
    {
        final EagerLoadingOfBizCustomFieldPersister.CacheKey key1 = new EagerLoadingOfBizCustomFieldPersister.CacheKey(new Long(100));
        final EagerLoadingOfBizCustomFieldPersister.CacheKey key2 = new EagerLoadingOfBizCustomFieldPersister.CacheKey(null);

        assertFalse(key1.equals(key2));
        assertFalse(key2.equals(key1));

        assertFalse(key1.hashCode() == key2.hashCode());
    }

    @Test
    public void testCacheHits()
    {
        final AtomicInteger count = new AtomicInteger(0);
        final Object delegate = new Object()
        {
            public List findByAnd(final String string, final Map params)
            {
                count.getAndIncrement();
                return Collections.EMPTY_LIST;
            }
        };
        final OfBizDelegator delegator = (OfBizDelegator) DuckTypeProxy.getProxy(OfBizDelegator.class, delegate);
        final EagerLoadingOfBizCustomFieldPersister persister = new EagerLoadingOfBizCustomFieldPersister(delegator);

        final Object customFieldDelegate = new Object()
        {
            public String getId()
            {
                return "1";
            }
        };
        final CustomField customField = (CustomField) DuckTypeProxy.getProxy(CustomField.class, customFieldDelegate);
        assertEquals(0, count.get());
        persister.getValuesForType(customField, new Long(1));
        assertEquals(1, count.get());
        persister.getValuesForType(customField, new Long(1));
        assertEquals(1, count.get());
        persister.getValuesForType(customField, new Long(1));
        assertEquals(1, count.get());
        persister.getValuesForType(customField, new Long(1));
        assertEquals(1, count.get());

        // we are really tied to the impl here
        JiraAuthenticationContextImpl.getRequestCache().clear();
        persister.getValuesForType(customField, new Long(1));
        assertEquals(2, count.get());
        persister.getValuesForType(customField, new Long(1));
        assertEquals(2, count.get());
        persister.getValuesForType(customField, new Long(1));
        assertEquals(2, count.get());
    }
}
