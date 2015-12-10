package com.atlassian.jira.bc.admin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.event.config.ApplicationPropertyChangeEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.validation.Success;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ApplicationPropertiesServiceImpl}.
 *
 * @since v4.4
 */
public class TestApplicationPropertiesServiceImpl
{
    private static final String NEW_VALUE = "newval";
    private static final String ORIGINAL_VALUE = "original value";
    private static final String KEY = "foo";

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private FeatureManager featureManager;

    @Test
    public void testSetApplicationProperty() {
        final ApplicationPropertyMetadata fooMeta = Mockito.mock(ApplicationPropertyMetadata.class);
        when(fooMeta.validate(NEW_VALUE)).thenReturn(new Success(NEW_VALUE));
        ApplicationPropertiesStore store = Mockito.mock(ApplicationPropertiesStore.class);

        ApplicationProperty fooValue = new ApplicationProperty(fooMeta, ORIGINAL_VALUE);
        when(store.getApplicationPropertyFromKey(KEY)).thenReturn(fooValue);
        ApplicationProperty newFooValue = new ApplicationProperty(fooMeta, NEW_VALUE);
        when(store.setApplicationProperty(KEY, NEW_VALUE)).thenReturn(newFooValue);

        EventPublisher publisher = Mockito.mock(EventPublisher.class);
        final ApplicationPropertyChangeEvent event = Mockito.mock(ApplicationPropertyChangeEvent.class);


        ApplicationPropertiesServiceImpl service = new ApplicationPropertiesServiceImpl(store, publisher, permissionManager, authenticationContext, featureManager)
        {
            protected ApplicationPropertyChangeEvent createEvent(ApplicationPropertyMetadata metadata, String oldValue, String newValue)
            {
                Assert.assertEquals(fooMeta,metadata);
                Assert.assertEquals(ORIGINAL_VALUE,oldValue);
                Assert.assertEquals(NEW_VALUE,newValue);
                return event;
            }
        };

        // production call
        service.setApplicationProperty(KEY, NEW_VALUE);
        verify(publisher).publish(event);
    }

}
