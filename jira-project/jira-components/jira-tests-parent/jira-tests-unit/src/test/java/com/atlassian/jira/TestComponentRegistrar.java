package com.atlassian.jira;

import com.atlassian.jira.appconsistency.db.LockedDatabaseOfBizDelegator;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.WrappingOfBizDelegator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picocontainer.ComponentAdapter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestComponentRegistrar
{

    @Mock
    private ComponentContainer mockComponentContainer;

    @Mock
    private ComponentAdapter  mockComponentAdapter;

    @Mock
    private ApplicationProperties mockApplicationProperties;

    @Mock
    private FeatureManager mockFeatureManager;

    @Mock
    private ClusterNodeProperties mockClusterNodeProperties;

    @Before
    public void setupMocks()
    {
        MockitoAnnotations.initMocks(this);
        when(mockComponentContainer.getComponentAdapter(any(Class.class))).thenReturn(mockComponentAdapter);
        when(mockComponentContainer.getComponentInstance(ApplicationProperties.class)).thenReturn(mockApplicationProperties);
        when(mockComponentContainer.getComponentInstance(FeatureManager.class)).thenReturn(mockFeatureManager);
        when(mockComponentContainer.getComponentInstance(ClusterNodeProperties.class)).thenReturn(mockClusterNodeProperties);

    }

    @Test
    public void testRegistrationStartupOK() throws Exception
    {
        new ContainerRegistrar().registerComponents(mockComponentContainer, true);
        verify(mockComponentContainer).implementation(ComponentContainer.Scope.PROVIDED,
                OfBizDelegator.class, WrappingOfBizDelegator.class);
    }

    @Test
    public void testRegistrationStartupBad() throws Exception
    {   
        new ContainerRegistrar().registerComponents(mockComponentContainer, false);
        verify(mockComponentContainer).implementation(ComponentContainer.Scope.PROVIDED,
                OfBizDelegator.class, LockedDatabaseOfBizDelegator.class);
    }
}
