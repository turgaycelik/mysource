package com.atlassian.jira.util.index;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;

import com.opensymphony.module.propertyset.PropertySet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.index.IndexingCounterManagerImpl.COUNTER_LOCK_NAME;
import static com.atlassian.jira.util.index.IndexingCounterManagerImpl.PROPERTY_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith (MockitoJUnitRunner.class)
public class IndexingCounterManagerImplTest
{
    private static final long START_VALUE = 800L;

    @Mock private ClusterLock mockCounterLock;
    @Mock private ClusterLockService mockClusterLockService;
    @Mock private PropertySet mockPropertySet;

    private IndexingCounterManagerImpl counterManager;

    @Before
    public void setUp() throws Exception
    {
        when(mockClusterLockService.getLockForName(COUNTER_LOCK_NAME)).thenReturn(mockCounterLock);
        setUpProperty(START_VALUE, true);
        counterManager = new IndexingCounterManagerImpl(null, mockClusterLockService) {
            @Override
            PropertySet getPropertySet()
            {
                return mockPropertySet;
            }
        };
    }

    private void setUpProperty(final long value, final boolean exists)
    {
        when(mockPropertySet.exists(PROPERTY_KEY)).thenReturn(exists);
        when(mockPropertySet.getLong(PROPERTY_KEY)).thenReturn(value);
    }

    @Test
    public void testGetCurrentValueWhenStartingFromExistingValue()
    {
        // Set up
        setUpProperty(START_VALUE, true);
        counterManager.start();

        // Invoke and check
        assertEquals(START_VALUE, counterManager.getCurrentValue());
        verify(mockPropertySet).exists(PROPERTY_KEY);
        verify(mockPropertySet).getLong(PROPERTY_KEY);
        verifyNoMoreInteractions(mockPropertySet);
    }

    @Test
    public void testIncrementValueAfterConstruction()
    {
        // Set up
        counterManager.start();

        // Invoke
        final long incrementedValue = counterManager.incrementValue();

        // Check
        assertEquals(START_VALUE + 1, incrementedValue);
        verify(mockPropertySet).setLong(PROPERTY_KEY, incrementedValue);
    }

    @Test
    public void testIncrementValueWhenStartingFromExistingValue()
    {
        // Set up
        final long existingLong = 10;
        setUpProperty(existingLong, true);
        counterManager.start();

        // Invoke
        final long incrementedValue = counterManager.incrementValue();

        // Check
        assertEquals(existingLong + 1, incrementedValue);
        verify(mockPropertySet).setLong(PROPERTY_KEY, incrementedValue);
    }

    @Test
    public void testGetCurrentValueWhenStartingFromNewValue()
    {
        // Set up
        setUpProperty(0, false);
        counterManager.start();

        // Invoke and check
        assertEquals(0, counterManager.getCurrentValue());
    }

    @Test
    public void testIncrementValueWhenStartingFromNewValue()
    {
        // Set up
        setUpProperty(0, false);
        counterManager.start();

        // Invoke and check
        assertEquals(1, counterManager.incrementValue());
    }

    @Test
    public void testGetSeesResultOfIncrement()
    {
        // Set up
        counterManager.start();

        // Invoke
        counterManager.incrementValue();

        // Check
        assertEquals(START_VALUE + 1, counterManager.getCurrentValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testIncrementBeforeStartThrowsException()
    {
        // Invoke
        counterManager.incrementValue();
    }
}
