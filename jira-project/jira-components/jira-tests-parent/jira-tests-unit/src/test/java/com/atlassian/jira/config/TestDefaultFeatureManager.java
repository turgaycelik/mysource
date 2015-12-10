package com.atlassian.jira.config;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.tenancy.TenantImpl;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.event.events.PluginDisabledEvent;

import com.atlassian.tenancy.api.Tenant;
import com.atlassian.tenancy.api.TenantAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;

import static com.atlassian.jira.mock.plugin.elements.MockResourceDescriptorBuilder.feature;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.config.DefaultFeatureManager}.
 *
 * @since v4.4
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultFeatureManager
{
    private static final String CORE_ENABLED_FEATURE_KEY = "enabled-feature";
    private static final String CORE_DISABLED_FEATURE_KEY = "disabled-feature";
    private final MemoryCacheManager cacheManager = new MemoryCacheManager();

    @Mock
    @AvailableInContainer
    private InstrumentRegistry mockInstrumentRegistry;

    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);


    @AvailableInContainer @Mock private PluginAccessor mockAccessor;
    @AvailableInContainer @Mock private EventPublisher mockEventPublisher;
    @Mock private ApplicationProperties applicationProperties;
    @AvailableInContainer @Mock private UserPreferencesManager preferencesManager;
    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private PermissionManager permissionManager;
    @Mock private Preferences preferences;
    @Mock private FeatureStore featureStore;
    @Mock private TenantAccessor tenantAccessor;
    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());

    @Before
    public void setUp()
    {
        when(preferencesManager.getPreferences(Matchers.<User>any())).thenReturn(preferences);
        when(tenantAccessor.getAvailableTenants()).thenReturn(ImmutableList. <Tenant>of(new TenantImpl("BaseTenant")));
    }

    @Test
    public void shouldHandleExistingAndNotExistingFeatureKeys()
    {
        DefaultFeatureManager.PropertiesContainer propertiesContainer = fromMap(ImmutableMap.of(
                "key1", "true",
                "key2", "value2"
        ));
        final FeatureManager tested = makeFeatureManager(propertiesContainer);
        assertTrue(tested.isEnabled("key1"));
        assertFalse("Value different than 'true' should be treated as false", tested.isEnabled("key2"));
        assertFalse("Not existing value should be treated as false", tested.isEnabled("key3"));
    }

    @Test
    public void shouldHandleNonStringsAsFalse()
    {
        final Properties props = new Properties();
        props.put("key1", new Object());
        props.put("key2", Boolean.TRUE); // yes, that as well
        final FeatureManager tested = makeFeatureManager(asContainer(props));
        assertFalse("Non strings should be treated as false", tested.isEnabled("key1"));
        assertFalse("Non strings should be treated as false", tested.isEnabled("key2"));
    }

    @Test
    public void shouldHandleCoreFatures()
    {
        final FeatureManager tested = makeFeatureManager(fromMap(ImmutableMap.of(
                "com.atlassian.jira.config.CoreFeatures.ON_DEMAND", "true"
        )));
        assertTrue(tested.isEnabled(CoreFeatures.ON_DEMAND));
        assertTrue(tested.isOnDemand());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowUsersToEnableOrDisableOnDemand()
    {
        final FeatureManager tested = makeFeatureManager();
        tested.enableUserDarkFeature(new MockUser("fred"), CoreFeatures.ON_DEMAND.featureKey());
    }

    public void shouldAllowUsersToEnableFeatureThatIsNotCore() throws Exception
    {
        final FeatureManager tested = makeFeatureManager();
        tested.enableUserDarkFeature(new MockUser("fred"), "x");
        verify(preferences).setString("user.features.enabled", "");
    }

    public void shouldAllowUsersToEnableFeatureThatLooksLikeACoreFeatureButDoesNotExist() throws Exception
    {
        final FeatureManager tested = makeFeatureManager();
        tested.enableUserDarkFeature(new MockUser("fred"), CoreFeatures.ON_DEMAND.featureKey() + 'x');
        verify(preferences).setString("user.features.enabled", "");
    }

    @Test
    public void shouldPickUpFeaturesFromPlugins() throws Exception
    {
        final Map<String,String> props = ImmutableMap.of(
                "key1", "true",
                "key2", "false"
        );
        final Plugin plugin = new MockPlugin("Test", "test-plugin", new PluginInformation())
                .addResourceDescriptor(feature("some-features", "/features.properties"), serialize(props));
        when(mockAccessor.getEnabledPlugins()).thenReturn(ImmutableList.of(plugin));
        final DefaultFeatureManager tested = makeFeatureManager();
        tested.start();
        assertTrue(tested.isEnabled("key1"));
        assertFalse(tested.isEnabled("key2"));
        assertFalse(tested.isEnabled("key3"));
    }

    @Test
    public void pluginFeaturesShouldOverrideCore() throws Exception
    {
        final Map<String,String> props = ImmutableMap.of(
                CORE_ENABLED_FEATURE_KEY, "false",
                CORE_DISABLED_FEATURE_KEY, "true"
        );
        final Plugin plugin = new MockPlugin("Test", "test-plugin", new PluginInformation())
                .addResourceDescriptor(feature("some-features", "/features.properties"), serialize(props));
        when(mockAccessor.getEnabledPlugins()).thenReturn(ImmutableList.of(plugin));
        final DefaultFeatureManager tested = makeFeatureManager();
        tested.start();
        assertFalse(tested.isEnabled(CORE_ENABLED_FEATURE_KEY));
        assertTrue(tested.isEnabled(CORE_DISABLED_FEATURE_KEY));
    }

    // TODO - JRADEV-7117 - disabled until the listener changed.
//    @Test
    public void pluginEventsShouldResetTheFeatureManagerState()
    {
        FeatureManager tested = makeFeatureManager();

        final String PLUGIN_FEAT = "my.plugin.feature";

        final Plugin pluginWithFeature = new MockPlugin("Test", "test-plugin", new PluginInformation())
                .addResourceDescriptor(feature("some-features", "/features.properties"), serialize(Collections.singletonMap(PLUGIN_FEAT, "true")));

        Capture<Object> featuresMapHolder = new Capture<Object>();
        mockEventPublisher.register(EasyMock.capture(featuresMapHolder));

        when(mockAccessor.getEnabledPlugins()).thenReturn(ImmutableList.of(pluginWithFeature));
        EasyMockAnnotations.replayMocks(this);

        assertThat(tested.getEnabledFeatureKeys(), hasItem(PLUGIN_FEAT));

        // now disable the plugin and try again
        ((DefaultFeatureManager.FeaturesMapHolder) featuresMapHolder.getValue()).onPluginDisabled(new PluginDisabledEvent(pluginWithFeature));
        when(mockAccessor.getEnabledPlugins()).thenReturn(Collections.<Plugin>emptyList());
        assertThat(tested.getEnabledFeatureKeys(), IsNot.not(hasItem(PLUGIN_FEAT)));
    }


    /*
        We need to override the methods of DefaultFeatureManager that use the ComponentAccessor to get components, using
        our mock implementations instead.
     */
    private DefaultFeatureManager makeFeatureManager(DefaultFeatureManager.PropertiesContainer propertiesContainer)
    {
        return new DefaultFeatureManager(propertiesContainer, authenticationContext, featureStore, jiraProperties, cacheManager, tenantAccessor);
    }

    private DefaultFeatureManager makeFeatureManager()
    {
        return new DefaultFeatureManager(authenticationContext, featureStore, jiraProperties, cacheManager, tenantAccessor);
    }

    private DefaultFeatureManager.PropertiesContainer asContainer(Properties props)
    {
        return new DefaultFeatureManager.PropertiesContainer(ImmutableList.of(props));
    }

    private DefaultFeatureManager.PropertiesContainer fromMap(Map<String,String> values)
    {
        final Properties props = new Properties();
        props.putAll(values);
        return asContainer(props);
    }

    private static String serialize(Map<String, String> props)
    {
        final StringBuilder result = new StringBuilder();
        for (Map.Entry<String,String> entry : props.entrySet())
        {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return result.delete(result.length()-1, result.length()).toString();
    }

}
