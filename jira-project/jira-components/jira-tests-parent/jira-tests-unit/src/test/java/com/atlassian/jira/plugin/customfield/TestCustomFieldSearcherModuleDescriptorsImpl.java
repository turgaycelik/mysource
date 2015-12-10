package com.atlassian.jira.plugin.customfield;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldSearcherModuleDescriptorsImpl
{
    @Mock
    private PluginAccessor pluginAccessor;

    private CustomFieldSearcherModuleDescriptorsImpl descriptors;

    @Before
    public void setUp()
    {
        descriptors = new CustomFieldSearcherModuleDescriptorsImpl(pluginAccessor);
    }

    @Test
    public void getCustomFieldSearcherReturnsUndefinedOptionWhenTheGivenKeyIsNull()
    {
        Option<CustomFieldSearcher> searcher = descriptors.getCustomFieldSearcher(null);

        assertThat(searcher.isDefined(), is(false));
    }

    @Test
    public void getCustomFieldSearcherReturnsUndefinedOptionWhenTheGivenKeyIsTheEmptyString()
    {
        Option<CustomFieldSearcher> searcher = descriptors.getCustomFieldSearcher("");

        assertThat(searcher.isDefined(), is(false));
    }

    @Test
    @SuppressWarnings ("unchecked")
    public void getCustomFieldSearcherReturnsUndefinedOptionWhenThePluginModuleForTheGivenKeyIsNotAnInstanceOfCustomFieldSearcherModuleDescriptor()
    {
        String key = "pluginKey";
        when(pluginAccessor.getEnabledPluginModule(key)).thenReturn(mock(ModuleDescriptor.class));

        Option<CustomFieldSearcher> searcher = descriptors.getCustomFieldSearcher("");

        assertThat(searcher.isDefined(), is(false));
    }

    @Test
    @SuppressWarnings ("unchecked")
    public void getCustomFieldSearcherReturnsTheInitializedSearcherWhenThePluginModuleForTheGivenKeyIsAnInstanceOfCustomFieldSearcherModuleDescriptor()
    {
        String key = "pluginKey";
        CustomFieldSearcherModuleDescriptor module = mock(CustomFieldSearcherModuleDescriptor.class);
        when(pluginAccessor.getEnabledPluginModule(key)).thenReturn((ModuleDescriptor) module);

        CustomFieldSearcher expectedSearcher = mock(CustomFieldSearcher.class);
        when(module.getModule()).thenReturn(expectedSearcher);

        Option<CustomFieldSearcher> actualSearcher = descriptors.getCustomFieldSearcher(key);

        assertThat(actualSearcher.isDefined(), is(true));
        assertThat(actualSearcher.get(), is(expectedSearcher));
    }
}
