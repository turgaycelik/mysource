package com.atlassian.jira;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.AsynchronousAbleEventDispatcher;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.EventExecutorFactory;
import com.atlassian.instrumentation.DefaultInstrumentRegistry;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.instrumentation.RegistryConfiguration;
import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.instrumentation.operations.ThreadLocalOpTimerFactory;
import com.atlassian.jira.config.component.SimpleSwitchingComponentAdaptor;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.DatabaseConfigurationManagerImpl;
import com.atlassian.jira.config.database.SystemDatabaseConfigurationLoader;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.BackingPropertySetManager;
import com.atlassian.jira.config.properties.DbBackedPropertiesManager;
import com.atlassian.jira.config.properties.MemorySwitchToDatabaseBackedPropertiesManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.DefaultJiraHome;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.event.JiraEventExecutorFactory;
import com.atlassian.jira.event.JiraListenerHandlerConfigurationImpl;
import com.atlassian.jira.instrumentation.InstrumentationConfiguration;
import com.atlassian.jira.plugin.JiraFailedPluginTracker;
import com.atlassian.jira.propertyset.BootstrapOfBizPropertyEntryStore;
import com.atlassian.jira.propertyset.DefaultJiraCachingPropertySetManager;
import com.atlassian.jira.propertyset.DefaultJiraPropertySetFactory;
import com.atlassian.jira.propertyset.JiraCachingPropertySetManager;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;
import com.atlassian.jira.startup.DatabaseInitialImporter;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.JiraComponentLocator;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;

import static com.atlassian.jira.ComponentContainer.Scope.INTERNAL;
import static com.atlassian.jira.ComponentContainer.Scope.PROVIDED;

/**
 * Register the components in the {@link ComponentContainer} that allow JIRA to come up untenanted.
 * .
 *
 * The components in here at meant to be the ABSOLUTE MINIMUM set of components that JIRA needs.  Don't add components here unless
 * you are positive that untenanted code in JIRA needs it.
 *
 */
@SuppressWarnings ("deprecation")
class BootstrapContainerRegistrar
{
    public void registerComponents(final ComponentContainer register)
    {
        //instrumentation components
        //TF-212: they need to be registered so early as they might be used through statically-accessible
        //Instrumentation's class methods and might be potentially used in any of initialization process
        register.implementation(INTERNAL, RegistryConfiguration.class, InstrumentationConfiguration.class);
        register.implementation(INTERNAL, OpTimerFactory.class, ThreadLocalOpTimerFactory.class);
        register.implementation(PROVIDED, InstrumentRegistry.class, DefaultInstrumentRegistry.class);

        // this allows us to determine if the database is setup or not
        register.implementation(INTERNAL, DatabaseConfigurationManager.class, DatabaseConfigurationManagerImpl.class);
        register.implementation(INTERNAL, DatabaseConfigurationLoader.class, SystemDatabaseConfigurationLoader.class);
        register.implementation(INTERNAL, ComponentLocator.class, JiraComponentLocator.class);

        // this allows us access to JIRA home
        register.implementation(PROVIDED, JiraHome.class, DefaultJiraHome.class);

        // this allows us to send events
        register.implementation(INTERNAL, EventDispatcher.class, AsynchronousAbleEventDispatcher.class);
        register.implementation(INTERNAL, EventExecutorFactory.class, JiraEventExecutorFactory.class);
        register.implementation(INTERNAL, ListenerHandlersConfiguration.class, JiraListenerHandlerConfigurationImpl.class);
        register.implementation(PROVIDED, EventPublisher.class, EventPublisherImpl.class);
        register.implementation(PROVIDED, PluginEventManager.class, DefaultPluginEventManager.class, EventPublisher.class);

        // track failed plugins,
        register.implementation(INTERNAL, JiraFailedPluginTracker.class);

        // ApplicationProperties
        register.implementation(INTERNAL, JiraCachingPropertySetManager.class, DefaultJiraCachingPropertySetManager.class);
        register.implementation(PROVIDED, JiraPropertySetFactory.class, DefaultJiraPropertySetFactory.class);

        register.implementation(INTERNAL, OfBizPropertyEntryStore.class, BootstrapOfBizPropertyEntryStore.class);
        register.implementation(INTERNAL, MemorySwitchToDatabaseBackedPropertiesManager.class);
        register.implementation(INTERNAL, DbBackedPropertiesManager.class);
        register.implementation(INTERNAL, DatabaseInitialImporter.class);
        register.component(INTERNAL, new SimpleSwitchingComponentAdaptor<BackingPropertySetManager>(BackingPropertySetManager.class)
        {
            @Override
            public Class<? extends BackingPropertySetManager> getComponentImplementation()
            {
                return register.getComponentInstance(DatabaseConfigurationManager.class).isDatabaseSetup() ?
                        DbBackedPropertiesManager.class : MemorySwitchToDatabaseBackedPropertiesManager.class;
            }
        });
        register.implementation(INTERNAL, PropertiesManager.class);
        register.implementation(INTERNAL, ApplicationPropertiesStore.class);

        register.implementation(PROVIDED, ApplicationProperties.class, ApplicationPropertiesImpl.class);
        register.implementation(PROVIDED, BuildUtilsInfo.class, BuildUtilsInfoImpl.class);
    }
}
