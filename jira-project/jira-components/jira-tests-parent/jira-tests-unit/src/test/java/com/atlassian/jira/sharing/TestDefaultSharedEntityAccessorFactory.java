package com.atlassian.jira.sharing;

import java.util.List;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.SharedEntityAccessor.Factory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestDefaultSharedEntityAccessorFactory
{
    private SharedEntityAccessor searchRequestManager;
    private SharedEntityAccessor portalPageManager;
    private Factory sharedEntityAccessorFactory;

    @Before
    public void setUp()
    {
        searchRequestManager = createSharedEntityAccessor(SearchRequest.ENTITY_TYPE);
        portalPageManager = createSharedEntityAccessor(PortalPage.ENTITY_TYPE);

        sharedEntityAccessorFactory = new DefaultSharedEntityAccessorFactory()
        {
            List /*<SharedEntityAccessor>*/ getAccessors()
            {
                return EasyList.build(searchRequestManager, portalPageManager);
            }
        };
    }

    @Test
    public void testSearchRequestType()
    {
        assertSame(searchRequestManager, sharedEntityAccessorFactory.getSharedEntityAccessor(SearchRequest.ENTITY_TYPE));
        assertSame(searchRequestManager, sharedEntityAccessorFactory.getSharedEntityAccessor(SearchRequest.ENTITY_TYPE.getName()));
    }

    @Test
    public void testPortalPage()
    {
        assertSame(portalPageManager, sharedEntityAccessorFactory.getSharedEntityAccessor(PortalPage.ENTITY_TYPE));
        assertSame(portalPageManager, sharedEntityAccessorFactory.getSharedEntityAccessor(PortalPage.ENTITY_TYPE.getName()));
    }

    @Test
    public void testNullEntityTpe()
    {
        assertNull(sharedEntityAccessorFactory.getSharedEntityAccessor((SharedEntity.TypeDescriptor) null));
        assertNull(sharedEntityAccessorFactory.getSharedEntityAccessor((String) null));
    }

    @Test
    public void testNullEntity()
    {
        assertNull(sharedEntityAccessorFactory.getSharedEntityAccessor((SharedEntity.TypeDescriptor) null));
        assertNull(sharedEntityAccessorFactory.getSharedEntityAccessor((String) null));
    }

    @Test
    public void testNonMapped()
    {
        assertNull(sharedEntityAccessorFactory.getSharedEntityAccessor(new SharedEntity.TypeDescriptor("NotMapped")));
        assertNull(sharedEntityAccessorFactory.getSharedEntityAccessor("NotMapped"));
    }

    private static SharedEntityAccessor createSharedEntityAccessor(final TypeDescriptor entityType)
    {
        return (SharedEntityAccessor) DuckTypeProxy.getProxy(SharedEntityAccessor.class, new Object()
        {
            public TypeDescriptor getType()
            {
                return entityType;
            }
        });
    }
}
