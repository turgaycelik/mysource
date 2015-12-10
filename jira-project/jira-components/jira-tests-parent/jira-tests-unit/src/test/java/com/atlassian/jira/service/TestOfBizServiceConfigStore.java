package com.atlassian.jira.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.ofbiz.MockOfBizPropertyEntryStore;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;
import com.atlassian.jira.service.services.DebugService;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestOfBizServiceConfigStore
{
    private MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @Before
    public void setUp()
    {
        new MockComponentWorker().init()
                .addMock(OfBizDelegator.class, ofBizDelegator)
                .addMock(OfBizPropertyEntryStore.class, new MockOfBizPropertyEntryStore());
    }

    @Test
    public void testRemoveServiceConfigCleansProperties() throws Exception
    {
        // mock serviceConfigGV
        final GenericValue serviceConfigGV = new MockGenericValue("ServiceConfig", FieldMap.build("id", 12L));

        final AtomicBoolean removePropertySetCalled = new AtomicBoolean(false);
        final OfBizServiceConfigStore store = new OfBizServiceConfigStore(ofBizDelegator, null)
        {
            @Override
            GenericValue getGenericValueForConfig(final JiraServiceContainer config)
            {
                return serviceConfigGV;
            }

            @Override
            void removePropertySet(final GenericValue gv)
            {
                removePropertySetCalled.set(true);
                assertSame(serviceConfigGV, gv);
            }
        };

        ofBizDelegator.createValue(serviceConfigGV);
        assertNotNull(ofBizDelegator.findById("ServiceConfig", 12L));
        final JiraServiceContainer config = new JiraServiceContainerImpl(null, null);
        store.removeServiceConfig(config);

        // assertions
        assertNull(ofBizDelegator.findById("ServiceConfig", 12L));
        assertTrue(removePropertySetCalled.get());
    }

    //JRA-20419
    @Test
    public void testRuntimeExceptionDuringInitialisation() throws Exception
    {
        ComponentClassManager mockComponentClassManager = mock(ComponentClassManager.class);
        when(mockComponentClassManager.<JiraService>newInstance("badClass")).thenThrow(new RuntimeException("Some random exception"));
        when(mockComponentClassManager.<JiraService>newInstance("goodClass")).thenReturn(new DebugService());

        OfBizServiceConfigStore cs = createStore(mockComponentClassManager);

        createServiceConfig(10001, 10, "badClass", "Broken");
        createServiceConfig(10002, 11, "goodClass", "Good");

        final Collection<JiraServiceContainer> configs = new ArrayList<JiraServiceContainer>(cs.getAllServiceConfigs());
        assertEquals(2, configs.size());

        JiraServiceContainer currentService = findServiceById(configs, 10001);
        assertNotNull(currentService);
        assertTrue(currentService instanceof UnloadableJiraServiceContainer);
        assertEquals("Broken", currentService.getName());
        assertEquals(10, currentService.getDelay());

        currentService = findServiceById(configs, 10002);
        assertNotNull(currentService);
        assertEquals("Good", currentService.getName());
        assertEquals(11, currentService.getDelay());
        assertTrue(currentService instanceof JiraServiceContainerImpl);
    }

    private JiraServiceContainer findServiceById(Collection<JiraServiceContainer> services, long id)
    {
        for (Iterator<JiraServiceContainer> iterator = services.iterator(); iterator.hasNext();)
        {
            JiraServiceContainer jiraService = iterator.next();
            if (jiraService.getId() == id)
            {
                iterator.remove();
                return jiraService;
            }
        }
        return null;
    }

    private OfBizServiceConfigStore createStore(ComponentClassManager classMgr)
    {
        return new OfBizServiceConfigStore(ofBizDelegator, classMgr);
    }

    private GenericValue createServiceConfig(long id, long delayTime, String klazz, String name)
    {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("time", delayTime);
        map.put("clazz", klazz);
        map.put("name", name);

        return ofBizDelegator.createValue("ServiceConfig", map);
    }
}
