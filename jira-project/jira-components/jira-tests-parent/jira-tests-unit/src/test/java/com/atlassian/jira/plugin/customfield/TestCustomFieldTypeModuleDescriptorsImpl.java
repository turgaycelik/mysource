package com.atlassian.jira.plugin.customfield;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldTypeModuleDescriptorsImpl
{
    @Mock
    private PluginAccessor pluginAccessor;

    private CustomFieldTypeModuleDescriptorsImpl descriptors;

    @Before
    public void setUp()
    {
        descriptors = new CustomFieldTypeModuleDescriptorsImpl(pluginAccessor);
    }

    @Test
    public void getCustomFieldTypesReturnsAnEmptyListIfThereAreNoCustomFieldTypeModulesDefined()
    {
        when(pluginAccessor.getEnabledModulesByClass(CustomFieldType.class)).thenReturn(Collections.<CustomFieldType>emptyList());

        List<CustomFieldType<?, ?>> customFieldTypes = descriptors.getCustomFieldTypes();

        assertThat(customFieldTypes.isEmpty(), is(true));
    }

    @Test
    public void getCustomFieldTypesReturnsTheListOfCustomFieldTypeModulesDefined()
    {
        List<CustomFieldType> customFieldTypes = Arrays.asList(
                mock(CustomFieldType.class),
                mock(CustomFieldType.class)
        );
        when(pluginAccessor.getEnabledModulesByClass(CustomFieldType.class)).thenReturn(customFieldTypes);

        List<CustomFieldType<?, ?>> actualTypes = descriptors.getCustomFieldTypes();

        assertThat(actualTypes.size(), is(customFieldTypes.size()));
        assertThat(actualTypes.get(0), is(customFieldTypes.get(0)));
        assertThat(actualTypes.get(1), is(customFieldTypes.get(1)));
    }

    @Test
    public void getCustomFieldTypesOrdersTheListOfCustomFieldTypesInNaturalOrderByName()
    {
        CustomFieldType firstCustomFieldByName = customFieldTypeWithName("a name that should go first");
        CustomFieldType lastCustomFieldByName = customFieldTypeWithName("a name that should go last");

        List<CustomFieldType> unorderedListByName = Arrays.asList(lastCustomFieldByName, firstCustomFieldByName);
        when(pluginAccessor.getEnabledModulesByClass(CustomFieldType.class)).thenReturn(unorderedListByName);

        List<CustomFieldType<?, ?>> actualTypes = descriptors.getCustomFieldTypes();

        assertThat(actualTypes.get(0), is(firstCustomFieldByName));
        assertThat(actualTypes.get(1), is(lastCustomFieldByName));
    }
    
    @Test
    public void getCustomFieldTypeReturnsUndefinedOptionIfThereIsNoEnabledPluginModuleForTheGivenKey()
    {
        String key = "someKey";
        when(pluginAccessor.getEnabledPluginModule(key)).thenReturn(null);

        Option<CustomFieldType> customFieldType = descriptors.getCustomFieldType(key);

        assertThat(customFieldType.isDefined(), is(false));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void getCustomFieldTypeReturnsUndefinedOptionIfTheEnabledPluginModuleForTheGivenKeyIsNotAnInstanceOfCustomFieldTypeModuleDescriptor()
    {
        String key = "someKey";
        when(pluginAccessor.getEnabledPluginModule(key)).thenReturn(mock(ModuleDescriptor.class));

        Option<CustomFieldType> customFieldType = descriptors.getCustomFieldType(key);

        assertThat(customFieldType.isDefined(), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCustomFieldTypeReturnsTheModuleIfTheEnabledPluginModuleForTheGivenKeyExistsAndIsAnInstanceOfCustomFieldTypeModuleDescriptor()
    {
        String key = "someKey";
        CustomFieldType expectedCustomFieldType = mock(CustomFieldType.class);

        CustomFieldTypeModuleDescriptor descriptorForKey = descriptorWithType(expectedCustomFieldType);
        when(pluginAccessor.getEnabledPluginModule(key)).thenReturn((ModuleDescriptor) descriptorForKey);

        Option<CustomFieldType> actualFieldType = descriptors.getCustomFieldType(key);

        assertThat(actualFieldType.isDefined(), is(true));
        assertThat(actualFieldType.get(), is(expectedCustomFieldType));
    }

    private CustomFieldType customFieldTypeWithName(final String name)
    {
        CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(customFieldType.getName()).thenReturn(name);
        return customFieldType;
    }

    private CustomFieldTypeModuleDescriptor descriptorWithType(final CustomFieldType customFieldType)
    {
        CustomFieldTypeModuleDescriptor moduleDescriptor = mock(CustomFieldTypeModuleDescriptor.class);
        when(moduleDescriptor.getModule()).thenReturn(customFieldType);
        return moduleDescriptor;
    }
}
