package com.atlassian.jira.help;

import com.atlassian.jira.mock.MockFeatureManager;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @since v6.2.4
 */
public class SwitchingHelpUrlBuilderFactoryTest
{
    private static final String SUFFIX = "suffix";
    private static final String PREFIX = "prefix";

    private final MockFeatureManager featureManager = new MockFeatureManager();
    private final HelpUrlBuilder.Factory simpleFactory = Mockito.spy(MockHelpUrlBuilder.factory());
    private final HelpUrlBuilder.Factory analyticsFactory = Mockito.spy(MockHelpUrlBuilder.factory());
    private final SwitchingHelpUrlBuilderFactory factory = new SwitchingHelpUrlBuilderFactory(featureManager, analyticsFactory, simpleFactory);

    @Test
    public void testAnalyticsEnabledInOd()
    {
        featureManager.setOnDemand(true);
        assertThat(factory.get(PREFIX, SUFFIX), notNullValue(HelpUrlBuilder.class));
        verify(analyticsFactory, only()).get(PREFIX, SUFFIX);
        verifyNoMoreInteractions(simpleFactory);
    }

    @Test
    public void testAnalyticsDisabledBtf()
    {
        assertThat(factory.get(PREFIX, SUFFIX), notNullValue(HelpUrlBuilder.class));
        verify(simpleFactory, only()).get(PREFIX, SUFFIX);
        verifyNoMoreInteractions(analyticsFactory);
    }
}
