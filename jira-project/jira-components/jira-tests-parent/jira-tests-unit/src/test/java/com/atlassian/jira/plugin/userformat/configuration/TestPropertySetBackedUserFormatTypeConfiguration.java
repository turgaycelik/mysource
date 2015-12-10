package com.atlassian.jira.plugin.userformat.configuration;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.mock.propertyset.MockPropertySet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.plugin.userformat.configuration.PropertySetBackedUserFormatTypeConfiguration.UserFormatMappingSupplier.USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
/**
 * @since v3.13
 */
public class TestPropertySetBackedUserFormatTypeConfiguration
{
    @Mock private JiraPropertySetFactory mockJiraPropertySetFactory;
    private MockPropertySet mockPropertySet;
    private PropertySetBackedUserFormatTypeConfiguration userFormatTypeConfiguration;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
        mockPropertySet = new MockPropertySet();
        when(mockJiraPropertySetFactory.buildCachingDefaultPropertySet(USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY, true))
                .thenReturn(mockPropertySet);
        userFormatTypeConfiguration =
                new PropertySetBackedUserFormatTypeConfiguration(mockJiraPropertySetFactory, new MemoryCacheManager());
    }

    @Test
    public void setUserFormatForTypeShouldSetAnEntryToTheUnderlyingPropertySet()
    {
        // Set up
        final String formatType = "myType";
        final String moduleKey = "some.plugin:profileLink.module";

        // Invoke
        userFormatTypeConfiguration.setUserFormatKeyForType(formatType, moduleKey);

        // Check
        assertEquals(moduleKey, mockPropertySet.getMap().get(formatType));
    }
}
