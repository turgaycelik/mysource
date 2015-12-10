package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldSearcherModuleDescriptorImpl
{
    @Mock
    private ModuleFactory moduleFactory;

    private CustomFieldSearcherModuleDescriptorImpl descriptor;

    @Before
    public void setUp()
    {
        descriptor = new CustomFieldSearcherModuleDescriptorImpl(
                mock(JiraAuthenticationContext.class),
                moduleFactory,
                mock(CustomFieldDefaultVelocityParams.class)
        );
    }

    @Test
    public void getModuleDoesNotCacheTheModuleInstance()
    {
        when(moduleFactory.createModule(anyString(), any(ModuleDescriptor.class))).thenReturn(mock(CustomFieldSearcher.class));

        descriptor.getModule();
        descriptor.getModule();

        verify(moduleFactory, times(2)).createModule(anyString(), any(ModuleDescriptor.class));
    }
}
