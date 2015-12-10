package com.atlassian.jira.propertyset;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test of {@link DefaultJiraCachingPropertySetManager}.
 */
public class TestDefaultJiraCachingPropertySetManager
{
    // Fixture
    private DefaultJiraCachingPropertySetManager propertySetManager;

    @Before
    public void setUp() throws Exception
    {
        propertySetManager = new DefaultJiraCachingPropertySetManager();
    }

    @Test
    public void managerShouldNotRegisterNullPropertySet()
    {
        // Invoke
        propertySetManager.register(null);

        // Check
        assertEquals(0, propertySetManager.getManagedPropertySets().size());
    }

    @Test
    public void managerShouldKeepWeakReferencesToPropertySets()
    {
        // Invoke
        propertySetManager.register(mock(JiraCachingPropertySet.class));

        // Check
        System.gc();
        assertEquals(0, propertySetManager.getManagedPropertySets().size());
    }

    @Test
    public void managerShouldRetainReferenceToStronglyReferencedPropertySet()
    {
        // Set up
        final JiraCachingPropertySet mockPropertySet = mock(JiraCachingPropertySet.class);

        // Invoke
        propertySetManager.register(mockPropertySet);

        // Check
        System.gc();
        assertEquals(1, propertySetManager.getManagedPropertySets().size());
    }

    @Test
    public void managerShouldInvalidateAllPropertySetsUponClearCacheEvent()
    {
        // Set up
        final JiraCachingPropertySet mockPropertySet = mock(JiraCachingPropertySet.class);
        propertySetManager.register(mockPropertySet);

        // Invoke
        propertySetManager.onClearCache(null);

        // Test
        verify(mockPropertySet).clearCache();
    }
}
