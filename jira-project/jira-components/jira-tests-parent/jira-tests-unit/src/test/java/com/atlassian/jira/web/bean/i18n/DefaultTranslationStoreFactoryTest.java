package com.atlassian.jira.web.bean.i18n;

import com.atlassian.jira.config.properties.JiraProperties;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.config.CoreFeatures.ON_DEMAND;
import static com.atlassian.jira.web.bean.i18n.DefaultTranslationStoreFactory.COMPRESS_I18N_PROPERTY;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultTranslationStoreFactoryTest
{
    @Mock
    JiraProperties properties;

    @InjectMocks
    DefaultTranslationStoreFactory translationStoreFactory;

    ImmutableMap<String, String> bundles = ImmutableMap.of();

    @Test
    public void defaultInBtfIsAStringBackedStore() throws Exception
    {
        when(properties.getBoolean(ON_DEMAND.systemPropertyKey())).thenReturn(false);

        assertThat(translationStoreFactory.createTranslationStore(bundles), instanceOf(StringBackedStore.class));
    }

    @Test
    public void defaultInOnDemandIsAStringBackedStore() throws Exception
    {
        when(properties.getBoolean(ON_DEMAND.systemPropertyKey())).thenReturn(true);

        assertThat(translationStoreFactory.createTranslationStore(bundles), instanceOf(StringBackedStore.class));
    }

    @Test
    public void systemPropertyOverridesDefault() throws Exception
    {
        when(properties.getBoolean(ON_DEMAND.systemPropertyKey())).thenReturn(true);
        when(properties.getProperty(COMPRESS_I18N_PROPERTY)).thenReturn("false");

        assertThat(translationStoreFactory.createTranslationStore(bundles), instanceOf(StringBackedStore.class));
    }
}
