package com.atlassian.jira.config.webwork;

import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.local.MockControllerTestCase;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import webwork.config.ConfigurationInterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 */
public class TestCachingWebworkConfiguration
{
    @Mock
    private ConfigurationInterface configurationInterface;

    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);

    private CachingWebworkConfiguration cachingWebworkConfiguration;

    @Before
    public void setUp(){
        cachingWebworkConfiguration = new CachingWebworkConfiguration(configurationInterface);
    }

    @Test
    public void testConstruction()
    {
        try
        {
            new CachingWebworkConfiguration(null);
            fail("This should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testWriteNotSupported()
    {
        try
        {
            cachingWebworkConfiguration.setImpl("should", "go bang!");
            fail("This should have barfed");
        }
        catch (UnsupportedOperationException expected)
        {
        }
    }

    @Test
    public void testReadButNotCached()
    {
        when(configurationInterface.getImpl("key1")).thenReturn("val1", "val2");

        Object actualVal = cachingWebworkConfiguration.getImpl("key1");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("key1");
        assertEquals("val2", actualVal);

    }

    @Test
    public void testReadAndCached()
    {
        when(configurationInterface.getImpl("webwork.key1")).thenReturn("val1", null, null);
        when(configurationInterface.getImpl("not.webwork.key1")).thenReturn("not.val1");

        Object actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("not.webwork.key1");
        assertEquals("not.val1", actualVal);
    }

    @Test
    public void testUncacheableExceptions()
    {

        when(configurationInterface.getImpl("webwork.multipart.maxSize")).thenReturn("val1").thenReturn("val2");
        when(configurationInterface.getImpl("webwork.i18n.encoding")).thenReturn("val3", "val4");

        Object actualVal = cachingWebworkConfiguration.getImpl("webwork.multipart.maxSize");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.multipart.maxSize");
        assertEquals("val2", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.i18n.encoding");
        assertEquals("val3", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.i18n.encoding");
        assertEquals("val4", actualVal);
    }

    @Test
    public void testWeOnlyCacheNonNullValues()
    {
        when(configurationInterface.getImpl("webwork.key1")).thenReturn(null);

        Object actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertNull(actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertNull(actualVal);
    }
}
