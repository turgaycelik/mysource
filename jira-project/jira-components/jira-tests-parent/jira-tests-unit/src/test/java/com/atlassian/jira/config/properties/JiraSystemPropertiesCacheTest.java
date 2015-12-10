package com.atlassian.jira.config.properties;


import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Test for JiraSystemProperties
 *
 */
public class JiraSystemPropertiesCacheTest
{
    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    @Mock
    private PropertiesAccessor accessor;

    private JiraSystemPropertiesCache cache;
    private long tickerTime;

    @Before
    public void setUp() throws Exception
    {
        cache = new JiraSystemPropertiesCache(accessor) {
            @Override
            protected CacheBuilder<Object, Object> newBuilder()
            {
                return super.newBuilder().ticker(new Ticker() {
                    @Override
                    public long read()
                    {
                        return tickerTime;
                    }
                });
            }
        };
        tickerTime = System.nanoTime();
    }

    private void pushTime(final long seconds)
    {
        tickerTime += TimeUnit.NANOSECONDS.convert(seconds, TimeUnit.SECONDS);
    }

    @Test
    public void shouldCacheSystemGetPropertyCalls() {

        final String propertyKey = "testProperty";
        final String value = "value";
        final String value2 = "value2";
        
        //having
        when(accessor.getProperty(propertyKey)).thenReturn(value).thenReturn(value2);

        //when
        final String testProperty1 = cache.getProperty(propertyKey);
        final String testProperty2 = cache.getProperty(propertyKey);

        //then
        assertThat("Should return proper value", testProperty1, Matchers.sameInstance(value));
        assertThat("Should return proper value", testProperty2, Matchers.sameInstance(value));
        Mockito.verify(accessor).getProperty(propertyKey);
    }

    @Test
    public void shouldCacheBooleanGetBooleanCalls(){
        
        final String propertyKey = "testProperty";
        final Boolean value = Boolean.TRUE;
        final Boolean value2 = Boolean.FALSE;

        //having
        when(accessor.getBoolean(propertyKey)).thenReturn(value).thenReturn(value2);

        //when
        final Boolean testProperty1 = cache.getBoolean(propertyKey);
        final Boolean testProperty2 = cache.getBoolean(propertyKey);

        //then
        assertThat("Should return proper value", testProperty1, Matchers.sameInstance(value));
        assertThat("Should return proper value", testProperty2, Matchers.sameInstance(value));
        Mockito.verify(accessor).getBoolean(propertyKey);
    }

    @Test
    public void shouldCacheIntegerGetIntegerCalls(){
        
        final String propertyKey = "testProperty";
        final Integer value = 123;
        final Integer value2 = 321;

        //having
        when(accessor.getInteger(propertyKey)).thenReturn(value).thenReturn(value2);

        //when
        final Integer testProperty1 = cache.getInteger(propertyKey);
        final Integer testProperty2 = cache.getInteger(propertyKey);

        //then
        assertThat("Should return proper value", testProperty1, Matchers.sameInstance(value));
        assertThat("Should return proper value", testProperty2, Matchers.sameInstance(value));
        Mockito.verify(accessor).getInteger(propertyKey);
    }
    
    @Test
    public void shouldCacheLongIntegerGetLongCalls(){
        final String propertyKey = "testProperty";
        final Long value = 987654321L;
        final Long value2 = 123456789L;

        //having
        when(accessor.getLong(propertyKey)).thenReturn(value).thenReturn(value2);

        //when
        final Long testProperty1 = cache.getLong(propertyKey);
        final Long testProperty2 = cache.getLong(propertyKey);

        //then
        assertThat("Should return proper value", testProperty1, Matchers.sameInstance(value));
        assertThat("Should return proper value", testProperty2, Matchers.sameInstance(value));
        Mockito.verify(accessor).getLong(propertyKey);
    }


    @Test
    public void shouldClearForStringKeyIfSetPropertyIsUsed(){

        //having
        final String mutatingPropertyKey = "mutatingProperty";
        final String staticPropertyKey = "staticProperty";
        when(accessor.getProperty(mutatingPropertyKey)).thenReturn("mutate");
        when(accessor.getProperty(staticPropertyKey)).thenReturn("state");

        //when
        cache.getProperty(mutatingPropertyKey);
        cache.getProperty(staticPropertyKey);

        cache.setProperty(mutatingPropertyKey, "newValue");

        cache.getProperty(mutatingPropertyKey);
        cache.getProperty(staticPropertyKey);

        //then
        Mockito.verify(accessor, Mockito.times(2)).getProperty(mutatingPropertyKey);
        Mockito.verify(accessor).getProperty(staticPropertyKey);
    }

    @Test
    public void shouldClearCacheOnClearCacheEvent(){
        //having
        final String propertyKey = "testProperty";
        final String value = "value";
        when(accessor.getProperty(propertyKey)).thenReturn(value);

        //when
        final String testProperty1 = cache.getProperty(propertyKey);
        cache.clear();
        final String testProperty2 = cache.getProperty(propertyKey);
        //then
        assertThat("Should return proper value",testProperty1,Matchers.sameInstance(value));
        assertThat("Two consecutive calls should return the same value",testProperty1,Matchers.sameInstance(testProperty2));
        Mockito.verify(accessor,Mockito.times(2)).getProperty(propertyKey);
    }

    @Test
    public void shouldCacheGetPropertiesCalls()
    {
        //having
        final Properties value = buildProperties("key1", "value1", "key2", "value2");
        when(accessor.getProperties()).thenReturn(value);

        //when
        final Properties testProperty1 = cache.getProperties();
        final Properties testProperty2 = cache.getProperties();

        //then
        assertThat("Should return proper value", testProperty1, Matchers.is(value));
        assertThat("Two consecutive calls should return the same value", testProperty1, Matchers.is(testProperty2));
        Mockito.verify(accessor).getProperties();
    }

    @Test
    public void shouldReturnCopiesOfPropertyMaps(){

        final String propertyKey = "testProperty";

        //having
        final Properties value = new Properties();
        when(accessor.getProperties()).thenReturn(value);
        final Properties testProperty1 = cache.getProperties();
        final Properties testProperty2 = cache.getProperties();

        //when
        testProperty2.setProperty(propertyKey, "testValue");

        //then
        assertThat("Should not affect other returned maps", testProperty1, Matchers.not(Matchers.hasKey(propertyKey)));
        assertThat("Should not give out the same instance of property map", testProperty1, Matchers.not(Matchers.sameInstance(value)));
        assertThat("Should not give out the same instance of property map", testProperty2, Matchers.not(Matchers.sameInstance(value)));
        Mockito.verify(accessor).getProperties();
    }

    @Test
    public void shouldClearClearAllCachesOnSetPropertiesCalls(){

        final String stringPropertyKey = "testPropertyString";
        final String booleanPropertyKey = "testPropertyBoolean";
        final String longPropertyKey = "testPropertyLong";
        final String integerPropertyKey = "testPropertyInteger";

        // having
        when(accessor.getProperties()).thenReturn(new Properties());
        when(accessor.getProperty(stringPropertyKey)).thenReturn("test");
        when(accessor.getLong(longPropertyKey)).thenReturn(1L);
        when(accessor.getInteger(integerPropertyKey)).thenReturn(1);
        when(accessor.getBoolean(booleanPropertyKey)).thenReturn(Boolean.TRUE);

        // when
        cache.getProperties();
        cache.getProperty(stringPropertyKey);
        cache.getLong(longPropertyKey);
        cache.getInteger(integerPropertyKey);
        cache.getBoolean(booleanPropertyKey);

        final Properties newValues = buildProperties("key", "val");
        cache.setProperties(newValues);

        cache.getProperty(stringPropertyKey);
        cache.getLong(longPropertyKey);
        cache.getInteger(integerPropertyKey);
        cache.getBoolean(booleanPropertyKey);
        cache.getProperties();

        // then
        Mockito.verify(accessor, times(2)).getProperties();
        Mockito.verify(accessor, times(2)).getProperty(stringPropertyKey);
        Mockito.verify(accessor, times(2)).getLong(longPropertyKey);
        Mockito.verify(accessor, times(2)).getInteger(integerPropertyKey);
        Mockito.verify(accessor, times(2)).getBoolean(booleanPropertyKey);
    }

    @Test
    public void shouldNotCacheNullValues(){

        //having
        final String propertyKey = "testProperty";

        //when
        cache.getProperty(propertyKey);
        cache.getProperty(propertyKey);

        //then
        Mockito.verify(accessor, Mockito.times(2)).getProperty(propertyKey);
    }

    @Test
    public void shouldHaveExpiryTimeoutSetToReasonableTime()
    {
        final String propertyKey = "testProperty";
        final String value = "value";
        final String value2 = "value2";

        //having
        when(accessor.getProperty(propertyKey)).thenReturn(value).thenReturn(value2);

        //when
        final String testProperty1 = cache.getProperty(propertyKey);
        pushTime(JiraSystemPropertiesCache.CACHE_WRITE_EXPIRY_SECONDS + 5L);
        final String testProperty2 = cache.getProperty(propertyKey);

        //then
        assertThat("Should return proper value", testProperty1, Matchers.sameInstance(value));
        assertThat("Should return proper value", testProperty2, Matchers.sameInstance(value2));
        Mockito.verify(accessor, times(2)).getProperty(propertyKey);

    }

    @Ignore("Turning off because it's flaky in google implementation.")
    @Test
    public void shouldUseLRUForEvictionIfOverflowHappens(){
        //having
        when(accessor.getProperty(anyString())).thenReturn("someValue");
        final String oldestKey = Integer.toBinaryString(0);
        final String freshKey = Integer.toBinaryString(JiraSystemPropertiesCache.CACHE_CAPACITY);


        //when
        // This should work, but I've seen this flake with exceeding limit by 1. So it _might_ be flaky with 15 too!
        for (int i = 0; i < JiraSystemPropertiesCache.CACHE_CAPACITY + 15; i++)
        {
            cache.getProperty(Integer.toBinaryString(i));
        }
        // because this is flaky let's make the cache think it didn't do maintenance for a long time,
        // but not too long for timed evictions:
        pushTime(JiraSystemPropertiesCache.CACHE_WRITE_EXPIRY_SECONDS / 2);
        // then test size eviction:
        cache.getProperty(oldestKey);
        cache.getProperty(freshKey);

        //then
        Mockito.verify(accessor, times(2)).getProperty(oldestKey);
        Mockito.verify(accessor).getProperty(freshKey);
    }

    private Properties buildProperties(final String key1, final String value1) {
        final Properties properties = new Properties();
        properties.put(key1, value1);
        return properties;
    }

    private Properties buildProperties(final String key1, final String value1, final String key2, final String value2) {
        final Properties properties = new Properties();
        properties.putAll(MapBuilder.build(key1, value1, key2, value2));
        return properties;
    }
}
