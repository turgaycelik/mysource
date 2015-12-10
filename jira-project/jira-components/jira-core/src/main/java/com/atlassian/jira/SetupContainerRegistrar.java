package com.atlassian.jira;

import com.atlassian.aui.spi.AuiIntegration;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.jira.appconsistency.db.LockedDatabaseOfBizDelegator;
import com.atlassian.jira.bc.license.BootstrapJiraServerIdProvider;
import com.atlassian.jira.bc.license.JiraServerIdProvider;
import com.atlassian.jira.config.BootstrapFeatureManager;
import com.atlassian.jira.config.DefaultLocaleManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.component.DateTimeFormatterProvider;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.config.webwork.WebworkConfigurator;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.crowd.embedded.JiraPasswordEncoderFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryImpl;
import com.atlassian.jira.help.CachingHelpUrls;
import com.atlassian.jira.help.DefaultHelpUrlsLoader;
import com.atlassian.jira.help.DefaultLocalHelpUrls;
import com.atlassian.jira.help.HelpUrlBuilder;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.HelpUrlsLoader;
import com.atlassian.jira.help.HelpUrlsParser;
import com.atlassian.jira.help.InitialHelpUrlsParser;
import com.atlassian.jira.help.LocalHelpUrls;
import com.atlassian.jira.help.SimpleHelpUrlBuilder;
import com.atlassian.jira.i18n.BackingI18nFactory;
import com.atlassian.jira.i18n.BackingI18nFactoryImpl;
import com.atlassian.jira.i18n.CachingI18nFactory;
import com.atlassian.jira.i18n.DelegateI18nFactory;
import com.atlassian.jira.i18n.JiraI18nResolver;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.BootstrapPluginLoaderFactory;
import com.atlassian.jira.plugin.BootstrapPluginVersionStore;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.DefaultComponentClassManager;
import com.atlassian.jira.plugin.DefaultPackageScannerConfiguration;
import com.atlassian.jira.plugin.JiraBootstrapStateStore;
import com.atlassian.jira.plugin.JiraCacheResetter;
import com.atlassian.jira.plugin.JiraContentTypeResolver;
import com.atlassian.jira.plugin.JiraHostContainer;
import com.atlassian.jira.plugin.JiraModuleDescriptorFactory;
import com.atlassian.jira.plugin.JiraModuleFactory;
import com.atlassian.jira.plugin.JiraOsgiContainerManager;
import com.atlassian.jira.plugin.JiraPluginManager;
import com.atlassian.jira.plugin.JiraPluginResourceDownload;
import com.atlassian.jira.plugin.JiraServletContextFactory;
import com.atlassian.jira.plugin.PluginLoaderFactory;
import com.atlassian.jira.plugin.PluginPath;
import com.atlassian.jira.plugin.PluginVersionStore;
import com.atlassian.jira.plugin.aui.JiraAuiIntegration;
import com.atlassian.jira.plugin.navigation.HeaderFooterRendering;
import com.atlassian.jira.plugin.util.PluginModuleTrackerFactory;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkFactoryModuleDescriptors;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.JiraWebFragmentHelper;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactoryModuleDescriptors;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webresource.JiraWebResourceBatchingConfiguration;
import com.atlassian.jira.plugin.webresource.JiraWebResourceIntegration;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManagerImpl;
import com.atlassian.jira.plugin.webresource.JiraWebResourceUrlProvider;
import com.atlassian.jira.plugin.webwork.AutowireCapableWebworkActionRegistry;
import com.atlassian.jira.plugin.webwork.DefaultAutowireCapableWebworkActionRegistry;
import com.atlassian.jira.sal.JiraApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.login.BootstrapLoginManagerImpl;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManagerImpl;
import com.atlassian.jira.security.xsrf.DefaultXsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.SimpleXsrfTokenGenerator;
import com.atlassian.jira.security.xsrf.XsrfDefaults;
import com.atlassian.jira.security.xsrf.XsrfDefaultsImpl;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.setting.GzipCompression;
import com.atlassian.jira.startup.JiraStartupPluginSystemListener;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.template.soy.SoyTemplateRendererProviderImpl;
import com.atlassian.jira.template.velocity.DefaultVelocityTemplatingEngine;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;
import com.atlassian.jira.tenancy.JiraTenantAccessor;
import com.atlassian.jira.tenancy.PluginKeyPredicateLoader;
import com.atlassian.jira.tenancy.TenantContext;
import com.atlassian.jira.tenancy.TenantContextImpl;
import com.atlassian.jira.timezone.NoDatabaseTimeZoneResolver;
import com.atlassian.jira.user.BootstrapUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.FileSystemFileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraComponentFactory;
import com.atlassian.jira.util.UnsupportedBrowserManager;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.i18n.I18nTranslationModeImpl;
import com.atlassian.jira.util.resourcebundle.InitialResourceBundleLoader;
import com.atlassian.jira.util.resourcebundle.ResourceBundleLoader;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.HttpServletVariablesImpl;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.action.setup.IndexLanguageToLocaleMapper;
import com.atlassian.jira.web.action.setup.IndexLanguageToLocaleMapperImpl;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarLanguageUtilImpl;
import com.atlassian.jira.web.bean.i18n.DefaultTranslationStoreFactory;
import com.atlassian.jira.web.bean.i18n.TranslationStoreFactory;
import com.atlassian.jira.web.dispatcher.PluginsAwareViewMapping;
import com.atlassian.jira.web.pagebuilder.DefaultJiraPageBuilderService;
import com.atlassian.jira.web.pagebuilder.JiraPageBuilderService;
import com.atlassian.jira.web.pagebuilder.PageBuilderServiceSpi;
import com.atlassian.jira.web.session.currentusers.JiraUserSessionTracker;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.jira.web.util.ProductVersionDataBeanProvider;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.manager.PluginPersistentStateStore;
import com.atlassian.plugin.metadata.DefaultPluginMetadataManager;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;
import com.atlassian.plugin.servlet.ContentTypeResolver;
import com.atlassian.plugin.servlet.DefaultServletModuleManager;
import com.atlassian.plugin.servlet.DownloadStrategy;
import com.atlassian.plugin.servlet.ServletContextFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.web.DefaultWebInterfaceManager;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.PluginResourceLocator;
import com.atlassian.plugin.webresource.PluginResourceLocatorImpl;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.plugin.webresource.assembler.DefaultWebResourceAssemblerFactory;
import com.atlassian.plugin.webresource.transformer.DefaultStaticTransformers;
import com.atlassian.plugin.webresource.transformer.DefaultStaticTransformersSupplier;
import com.atlassian.plugin.webresource.transformer.StaticTransformers;
import com.atlassian.plugin.webresource.transformer.StaticTransformersSupplier;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.spi.HostContextAccessor;
import com.atlassian.seraph.auth.AuthenticationContext;
import com.atlassian.seraph.auth.AuthenticationContextImpl;
import com.atlassian.tenancy.api.TenantAccessor;
import com.atlassian.velocity.JiraVelocityManager;
import com.atlassian.velocity.VelocityManager;
import com.atlassian.velocity.htmlsafe.event.referenceinsertion.EnableHtmlEscapingDirectiveHandler;
import com.atlassian.webresource.api.assembler.WebResourceAssemblerFactory;
import org.picocontainer.parameters.ConstantParameter;

import static com.atlassian.jira.ComponentContainer.Scope.INTERNAL;
import static com.atlassian.jira.ComponentContainer.Scope.PROVIDED;

/**
 * Register the components in the {@link com.atlassian.jira.ComponentContainer} that allow JIRA to be setup
 * .
 *
 * The components in here at meant to be the MINIMUM set of components that JIRA needs during setup.  Don't add components here unless
 * you are positive that that setup code in JIRA needs it.
 *
 */
@SuppressWarnings ("deprecation")
class SetupContainerRegistrar
{
    public void registerComponents(final ComponentContainer register)
    {


        // MT bootstrap stuff

        register.implementation(INTERNAL, JiraStartupPluginSystemListener.class);
        register.implementation(INTERNAL, HostContainer.class, JiraHostContainer.class);

        MultipleKeyRegistrant.registrantFor(JiraModuleDescriptorFactory.class)
                .implementing(ModuleDescriptorFactory.class)
                .implementing(ListableModuleDescriptorFactory.class)
                .implementing(DescribedModuleDescriptorFactory.class)
                .registerWith(PROVIDED, register);

        register.implementation(PROVIDED, PluginLoaderFactory.class, BootstrapPluginLoaderFactory.class);
        register.implementation(PROVIDED, ModuleFactory.class, JiraModuleFactory.class);

        register.implementation(PROVIDED, ServletModuleManager.class, DefaultServletModuleManager.class,
                new ConstantParameter(ServletContextProvider.getServletContext()),
                PluginEventManager.class);

        register.implementation(PROVIDED, PluginMetadataManager.class, DefaultPluginMetadataManager.class);

        register.implementation(INTERNAL, PluginKeyPredicateLoader.class);
        MultipleKeyRegistrant.registrantFor(JiraPluginManager.class)
                .implementing(PluginSystemLifecycle.class)
                .implementing(PluginAccessor.class)
                .implementing(PluginController.class)
                .registerWith(PROVIDED, register);

        register.implementation(INTERNAL, JiraCacheResetter.class);




        register.implementation(PROVIDED, PluginPath.class, PluginPath.JiraHomeAdapter.class);
        register.implementation(INTERNAL, OsgiContainerManager.class, JiraOsgiContainerManager.class);
        register.instance(INTERNAL, HostComponentProvider.class, register.getHostComponentProvider());
        register.implementation(PROVIDED, PackageScannerConfiguration.class, DefaultPackageScannerConfiguration.class);

        register.implementation(PROVIDED, DownloadStrategy.class, JiraPluginResourceDownload.class);
        register.implementation(PROVIDED, ContentTypeResolver.class, JiraContentTypeResolver.class);

        register.implementation(PROVIDED, PluginResourceLocator.class, PluginResourceLocatorImpl.class);
        register.implementation(PROVIDED, ServletContextFactory.class, JiraServletContextFactory.class);
        register.implementation(INTERNAL, ComponentClassManager.class, DefaultComponentClassManager.class);

        register.implementation(PROVIDED, HostContextAccessor.class, DefaultHostContextAccessor.class);

        register.implementation(INTERNAL, PluginPersistentStateStore.class, JiraBootstrapStateStore.class);
        register.implementation(INTERNAL, PluginVersionStore.class, BootstrapPluginVersionStore.class);
        register.implementation(INTERNAL, PluginModuleTrackerFactory.class);

        register.implementation(PROVIDED, WebInterfaceManager.class, DefaultWebInterfaceManager.class);
        register.implementation(PROVIDED, WebFragmentHelper.class, JiraWebFragmentHelper.class);
        register.implementation(PROVIDED, WebResourceManager.class, JiraWebResourceManagerImpl.class);
        register.implementation(PROVIDED, WebResourceIntegration.class, JiraWebResourceIntegration.class);
        register.implementation(PROVIDED, WebResourceUrlProvider.class, JiraWebResourceUrlProvider.class);
        register.implementation(PROVIDED, StaticTransformersSupplier.class, DefaultStaticTransformersSupplier.class);
        register.implementation(PROVIDED, StaticTransformers.class, DefaultStaticTransformers.class);
        register.implementation(PROVIDED, ResourceBatchingConfiguration.class, JiraWebResourceBatchingConfiguration.class);
        register.implementation(PROVIDED, SimpleLinkManager.class, DefaultSimpleLinkManager.class);
        register.implementation(PROVIDED, SimpleLinkFactoryModuleDescriptors.class, DefaultSimpleLinkFactoryModuleDescriptors.class);
        register.implementation(PROVIDED, WebResourceAssemblerFactory.class, DefaultWebResourceAssemblerFactory.class);
        register.implementation(INTERNAL, JiraWebInterfaceManager.class);
        register.implementation(PROVIDED, EncodingConfiguration.class, EncodingConfiguration.PropertiesAdaptor.class);

        // Velocity components
        register.implementation(INTERNAL, VelocityEngineFactory.class, VelocityEngineFactory.Default.class);
        register.implementation(INTERNAL, EnableHtmlEscapingDirectiveHandler.class);
        register.implementation(PROVIDED, VelocityTemplatingEngine.class, DefaultVelocityTemplatingEngine.class);
        register.implementation(PROVIDED, VelocityManager.class, JiraVelocityManager.class);
        register.implementation(PROVIDED, VelocityRequestContextFactory.class, DefaultVelocityRequestContextFactory.class);

        //Soy components
        register.implementation(PROVIDED, SoyTemplateRendererProvider.class, SoyTemplateRendererProviderImpl.class);

        // this will prevent ANY database access via OfBiz.  This is belts and braces on top of the bootstrapping
        register.implementation(PROVIDED, OfBizDelegator.class, LockedDatabaseOfBizDelegator.class);

        register.implementation(INTERNAL, WebworkConfigurator.class);
        register.implementation(INTERNAL, PluginsAwareViewMapping.Component.class);
        register.implementation(INTERNAL, AutowireCapableWebworkActionRegistry.class, DefaultAutowireCapableWebworkActionRegistry.class);
        register.implementation(INTERNAL, ActionConfiguration.class, ActionConfiguration.FromWebWorkConfiguration.class);

        register.implementation(INTERNAL, LoginManager.class, BootstrapLoginManagerImpl.class);
        register.implementation(PROVIDED, AuthenticationContext.class, AuthenticationContextImpl.class);
        register.implementation(PROVIDED, JiraAuthenticationContext.class, JiraAuthenticationContextImpl.class);
        register.implementation(INTERNAL, JiraUserSessionTracker.class);

        register.implementation(PROVIDED, com.atlassian.cache.CacheManager.class, MemoryCacheManager.class);

        register.implementation(INTERNAL, UserLocaleStore.class, BootstrapUserLocaleStore.class);
        register.implementation(INTERNAL, ResourceBundleLoader.class, InitialResourceBundleLoader.class);
        register.implementation(INTERNAL, BackingI18nFactory.class, BackingI18nFactoryImpl.class);
        register.implementation(INTERNAL, CachingI18nFactory.class);
        register.implementation(PROVIDED, I18nHelper.BeanFactory.class, DelegateI18nFactory.class);
        register.implementation(INTERNAL, JiraLocaleUtils.class);
        register.implementation(PROVIDED, I18nTranslationMode.class, I18nTranslationModeImpl.class);
        register.implementation(PROVIDED, TranslationStoreFactory.class, DefaultTranslationStoreFactory.class);
        //
        //
        // By rights, the I18nResolver and ApplicationApplication should be defined in jira-sal-plugin, but it is need by AUI during bootstrap
        // So instead of creating a separate jira-sal-plugin-micro-kernel plugin, we just whack it in the SetupContainer.
        //
        // We need to repeat this pattern whenever we have a plugin that uses SAL code but it being called inside the bootstrap
        // phase.  We can only have plugins that have light weight SAL usage.  To bring in a full SAL requires
        // much more than the bootstrap phase can provide.
        //
        //
        register.implementation(PROVIDED, I18nResolver.class, JiraI18nResolver.class);
        register.implementation(PROVIDED, com.atlassian.sal.api.ApplicationProperties.class, JiraApplicationProperties.class);
        register.implementation(PROVIDED, com.atlassian.sal.api.web.context.HttpContext.class, HttpServletVariablesImpl.class);

        register.implementation(PROVIDED, AuiIntegration.class, JiraAuiIntegration.class);

        register.implementation(PROVIDED, LocaleManager.class, DefaultLocaleManager.class);
        register.implementation(PROVIDED, IndexLanguageToLocaleMapper.class, IndexLanguageToLocaleMapperImpl.class);
        register.implementation(PROVIDED, CalendarLanguageUtil.class, CalendarLanguageUtilImpl.class);
        register.implementation(INTERNAL, UnsupportedBrowserManager.class);

        register.implementation(INTERNAL, FileFactory.class, FileSystemFileFactory.class);

        register.implementation(PROVIDED, PasswordEncoderFactory.class, JiraPasswordEncoderFactory.class);
        register.instance(INTERNAL, ComponentFactory.class, JiraComponentFactory.getInstance());

        register.implementation(INTERNAL, XsrfDefaults.class, XsrfDefaultsImpl.class);
        register.implementation(PROVIDED, XsrfTokenGenerator.class, SimpleXsrfTokenGenerator.class);
        register.implementation(PROVIDED, XsrfInvocationChecker.class, DefaultXsrfInvocationChecker.class);
        register.implementation(PROVIDED, InternalWebSudoManager.class, InternalWebSudoManagerImpl.class);
        register.implementation(PROVIDED, JiraServerIdProvider.class, BootstrapJiraServerIdProvider.class);

        register.implementation(INTERNAL, Instrumentation.class);

        register.implementation(ComponentContainer.Scope.INTERNAL, NoDatabaseTimeZoneResolver.class);
        register.implementation(PROVIDED, DateTimeFormatterFactory.class, DateTimeFormatterFactoryImpl.class);
        register.component(PROVIDED, new DateTimeFormatterProvider());

        register.implementation(PROVIDED, FeatureManager.class, BootstrapFeatureManager.class);
        register.implementation(INTERNAL, GzipCompression.class);

        register.implementation(PROVIDED, MailSettings.class, MailSettings.DefaultMailSettings.class);
        register.implementation(INTERNAL, ProductVersionDataBeanProvider.class);
        register.implementation(INTERNAL, HeaderFooterRendering.class);

        MultipleKeyRegistrant.registrantFor(DefaultJiraPageBuilderService.class)
                .implementing(JiraPageBuilderService.class)
                .implementing(com.atlassian.webresource.api.assembler.PageBuilderService.class)
                .implementing(PageBuilderServiceSpi.class)
                .registerWith(PROVIDED, register);

        register.instance(PROVIDED, JiraProperties.class, JiraSystemProperties.getInstance());
        register.implementation(PROVIDED, TenantContext.class, TenantContextImpl.class);
        register.implementation(PROVIDED, TenantAccessor.class, JiraTenantAccessor.class);

        register.implementation(INTERNAL, HelpUrlBuilder.Factory.class, SimpleHelpUrlBuilder.Factory.class);
        register.implementation(PROVIDED, HelpUrlsParser.class, InitialHelpUrlsParser.class);
        register.implementation(INTERNAL, LocalHelpUrls.class, DefaultLocalHelpUrls.class);
        register.implementation(INTERNAL, HelpUrlsLoader.class, DefaultHelpUrlsLoader.class);
        register.implementation(PROVIDED, HelpUrls.class, CachingHelpUrls.class);
    }

}
