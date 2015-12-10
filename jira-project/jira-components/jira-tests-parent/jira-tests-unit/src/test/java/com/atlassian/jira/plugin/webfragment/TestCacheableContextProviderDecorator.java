package com.atlassian.jira.plugin.webfragment;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCacheableContextProviderDecorator
{
    @Mock private CacheableContextProvider contextProvider;

    private MockHttpServletRequest mockHttpServletRequest;

    @Before
    public void setUp()
    {
        mockHttpServletRequest = new MockHttpServletRequest();
    }

    @After
    public void tearDown()
    {
        contextProvider = null;
        mockHttpServletRequest = null;
    }

    @Test
    public void testGetContextMap()
    {
        final Map<String, Object> suppliedMap = ImmutableMap.<String,Object>of(
                "lala", "dontcare",
                "mtan", "bleah");

        final Map<String, Object> resultingMap = ImmutableMap.<String,Object>of(
                "lolo", "wecare",
                "lele", "wedontcare");

        when(contextProvider.getContextMap(suppliedMap)).thenReturn(resultingMap);
        when(contextProvider.getUniqueContextKey(suppliedMap)).thenReturn("aUniqueKey");

        CacheableContextProviderDecorator decorator = getDecorator(contextProvider);

        Map<String, Object> contextMap = decorator.getContextMap(suppliedMap);

        assertEquals(resultingMap, contextMap);

        final String expectedKey = CacheableContextProviderDecorator.REQUEST_ATTRIBUTE_PREFIX +
                contextProvider.getClass().getName() + ":aUniqueKey";

        assertEquals(resultingMap, mockHttpServletRequest.getAttribute(expectedKey));

        // Doing it again since it's cached
        contextMap = decorator.getContextMap(suppliedMap);

        assertEquals(resultingMap, contextMap);
    }


    private CacheableContextProviderDecorator getDecorator(final CacheableContextProvider contextProvider)
    {
        return new CacheableContextProviderDecorator(contextProvider)
        {
            @Override
            protected HttpServletRequest getRequest(Map<String, Object> context)
            {
                return mockHttpServletRequest;
            }
        };
    }
}
