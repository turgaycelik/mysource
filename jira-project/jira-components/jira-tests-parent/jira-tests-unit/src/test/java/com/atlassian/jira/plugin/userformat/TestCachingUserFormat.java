package com.atlassian.jira.plugin.userformat;

import java.util.Map;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingUserFormat
{

    @Mock
    private UserFormat userFormat1;

    @Mock
    private UserFormat userFormat2;

    @After
    public void tearDown()
    {
        JiraAuthenticationContextImpl.getRequestCache().clear();
    }

    @Test
    public void testDifferentUserFormatsInSameRequest()
    {
        when(userFormat1.format(any(String.class), any(String.class))).thenReturn("userFormat1");
        when(userFormat2.format(any(String.class), any(String.class))).thenReturn("userFormat2");

        final String formatted1 = (new CachingUserFormat(userFormat1)).format("userkey", "id");
        final String formatted2 = (new CachingUserFormat(userFormat2)).format("userkey", "id");

        assertEquals("userFormat1", formatted1);
        assertEquals("userFormat2", formatted2);
    }

    @Test
    public void testValuesGetCached()
    {
        when(userFormat1.format(any(String.class), any(String.class))).thenReturn("userFormat1");

        (new CachingUserFormat(userFormat1)).format("userkey", "id");
        (new CachingUserFormat(userFormat1)).format("userkey", "id");
        (new CachingUserFormat(userFormat1)).format("userkey", "id");

        verify(userFormat1, times(1)).format(eq("userkey"), eq("id"));
    }

    @Test
    public void testValuesGetCachedForTheSameInstance()
    {
        when(userFormat1.format(any(String.class), any(String.class))).thenReturn("userFormat1");

        final CachingUserFormat cachingUserFormat = new CachingUserFormat(userFormat1);
        cachingUserFormat.format("userkey", "id");
        cachingUserFormat.format("userkey", "id");
        cachingUserFormat.format("userkey", "id");

        verify(userFormat1, times(1)).format(eq("userkey"), eq("id"));
    }

    @Test
    public void testValuesGetCachesMoreComplicatedScenario()
    {
        when(userFormat1.format(any(String.class), any(String.class))).thenAnswer(new Answer<String>()
        {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[0].toString() + invocation.getArguments()[1].toString();
            }
        });

        final CachingUserFormat cachingUserFormat = new CachingUserFormat(userFormat1);

        final String format1 = cachingUserFormat.format("userkey", "id");
        final String format2 = cachingUserFormat.format("userkey", "id");
        final String format3 = cachingUserFormat.format("userkey1", "id");
        final String format4 = cachingUserFormat.format("userkey1", "id");
        final String format5 = cachingUserFormat.format("userkey", "id1");
        final String format6 = cachingUserFormat.format("userkey", "id1");
        final String format7 = cachingUserFormat.format("userkey1", "id1");
        final String format8 = cachingUserFormat.format("userkey1", "id1");

        verify(userFormat1, times(4)).format(any(String.class), any(String.class));

        assertEquals("userkeyid", format1);
        assertEquals("userkeyid", format2);
        assertEquals("userkey1id", format3);
        assertEquals("userkey1id", format4);
        assertEquals("userkeyid1", format5);
        assertEquals("userkeyid1", format6);
        assertEquals("userkey1id1", format7);
        assertEquals("userkey1id1", format8);
    }

    @Test
    public void testVariousContextParams()
    {
        final Map<String, Object> params1 = ImmutableMap.<String, Object>of("key1", "value1", "key2", "value2");
        final Map<String, Object> params2 = ImmutableMap.<String, Object>of("key1", "value1", "key2", "value2");
        final Map<String, Object> params3 = ImmutableMap.<String, Object>of("key3", "value3", "key4", "value4");

        when(userFormat1.format(any(String.class), any(String.class), eq(params1))).thenReturn("result1");
        when(userFormat1.format(any(String.class), any(String.class), eq(params3))).thenReturn("result3");

        final CachingUserFormat cachingUserFormat = new CachingUserFormat(userFormat1);

        final String result1 = cachingUserFormat.format("", "", params1);
        final String result2 = cachingUserFormat.format("", "", params2);
        final String result3 = cachingUserFormat.format("", "", params3);

        verify(userFormat1, times(1)).format(any(String.class), any(String.class), eq(params1));
        verify(userFormat1, times(1)).format(any(String.class), any(String.class), eq(params3));

        assertEquals("result1", result1);
        assertEquals("result1", result2);
        assertEquals("result3", result3);
    }

    @Test
    public void testNullValuesInCache()
    {
        when(userFormat1.format(any(String.class), any(String.class))).thenReturn(null);

        final CachingUserFormat cachingUserFormat = new CachingUserFormat(userFormat1);
        final String result1 = cachingUserFormat.format("userkey", "id");
        final String result2 = cachingUserFormat.format("userkey", "id");

        verify(userFormat1, times(1)).format(eq("userkey"), eq("id"));
        assertEquals(null, result1);
        assertEquals(null, result2);
    }
}