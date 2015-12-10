package com.atlassian.jira.web.action.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;

import com.opensymphony.module.propertyset.PropertySet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestEditService
{
    private final List<String> defaultList = new ArrayList<String>();
    private final List<String> valueList = new ArrayList<String>();
    private final List<String> arrayList = new ArrayList<String>();
    private final List<String> integerList = new ArrayList<String>();

    private final ObjectConfiguration ocNull = new MockObjectConfiguration(null)
    {
        public String getFieldDefault(String key) throws ObjectConfigurationException
        {
            return null;
        }
    };

    private final ObjectConfiguration ocValue = new MockObjectConfiguration(null)
    {
        public String getFieldDefault(String key) throws ObjectConfigurationException
        {
            return "value 123";
        }
    };

    private final ObjectConfiguration ocException = new MockObjectConfiguration(null)
    {
        public String getFieldDefault(String key) throws ObjectConfigurationException
        {
            throw new ObjectConfigurationException("some message");
        }
    };

    @Mock ServiceManager serviceManager;
    @Mock JiraServiceContainer container;
    @Mock ObjectConfiguration configuration;
    @Mock PropertySet properties;

    public TestEditService()
    {
        defaultList.add("value 123");
        valueList.add("v1");
        arrayList.add("123");
        arrayList.add("abc");
        integerList.add("789");
    }

    @Test
    public void testGetParamValues() throws Exception
    {
        when(serviceManager.containsServiceWithId(null)).thenReturn(false);

        EditService es1 = new EditService(serviceManager)
        {
            public ObjectConfiguration getObjectConfiguration() throws Exception
            {
                return ocNull;
            }
        };
        EditService es2 = new EditService(serviceManager)
        {
            public ObjectConfiguration getObjectConfiguration() throws Exception
            {
                return ocValue;
            }
        };
        EditService es3 = new EditService(serviceManager)
        {
            public ObjectConfiguration getObjectConfiguration() throws Exception
            {
                return ocException;
            }
        };

        assertEquals(Collections.EMPTY_LIST, es1.getParamValues(null));
        assertEquals(defaultList, es2.getParamValues(null));
        assertEquals(Collections.EMPTY_LIST, es3.getParamValues(null));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("k1", "v1");
        params.put("k3", new String[]{"123", "abc"});
        params.put("k4", 789);
        es1.setParameters(params);
        es2.setParameters(params);
        es3.setParameters(params);

        assertEquals(Collections.EMPTY_LIST, es1.getParamValues(null));
        assertEquals(defaultList, es2.getParamValues(null));
        assertEquals(Collections.EMPTY_LIST, es3.getParamValues(null));

        assertEquals(valueList, es1.getParamValues("k1"));
        assertEquals(valueList, es2.getParamValues("k1"));
        assertEquals(valueList, es3.getParamValues("k1"));

        assertEquals(Collections.EMPTY_LIST, es1.getParamValues("k2"));
        assertEquals(defaultList, es2.getParamValues("k2"));
        assertEquals(Collections.EMPTY_LIST, es3.getParamValues("k2"));

        assertEquals(arrayList, es1.getParamValues("k3"));
        assertEquals(arrayList, es2.getParamValues("k3"));
        assertEquals(arrayList, es3.getParamValues("k3"));

        assertEquals(integerList, es1.getParamValues("k4"));
        assertEquals(integerList, es2.getParamValues("k4"));
        assertEquals(integerList, es3.getParamValues("k4"));
    }

    @Test
    public void testUseDefaultStays() throws Exception
    {
        // Doesn't get stripped because we're not the ExportService
        verifyKeys("something.awesome", new String[] { "USE_DEFAULT_DIRECTORY", "two" }, null);
    }

    @Test
    public void testUseDefaultGoes() throws Exception
    {
        // Does get stripped because we are the ExportService
        verifyKeys(ExportService.class.getCanonicalName(), new String[] { "two" }, null);
    }

    @Test
    public void testUseDefaultStaysWithPath() throws Exception
    {
        // Stays because "/old/path" is still set.
        verifyKeys("something.awesome", new String[] { "USE_DEFAULT_DIRECTORY", "two" }, "/old/path");
    }

    @Test
    public void testUseDefaultStaysWithPathAndExportService() throws Exception
    {
        // Stays because "/old/path" is still set, even though we're on the ExportService
        verifyKeys(ExportService.class.getCanonicalName(), new String[] { "USE_DEFAULT_DIRECTORY", "two" }, "/old/path");
    }

    private void verifyKeys(String className, String[] expecteds, String dirName) throws Exception
    {
        when(container.getObjectConfiguration()).thenReturn(configuration);
        when(container.getProperties()).thenReturn(properties);
        when(container.getProperty("DIR_NAME")).thenReturn(dirName);
        when(container.getServiceClass()).thenReturn(className);

        when(configuration.getFieldKeys()).thenReturn(new String[] { "DIR_NAME", "USE_DEFAULT_DIRECTORY", "two" });

        final EditService service = new EditService(serviceManager)
        {
            @Override
            public JiraServiceContainer getService() throws Exception
            {
                return container;
            }

            @Override
            public boolean isUnsafeService(final long serviceId)
            {
                return true;
            }
        };

        service.setId(12345L);
        final String[] keys = service.getObjectConfigurationKeys();
        assertArrayEquals(expecteds, keys);
    }
}
