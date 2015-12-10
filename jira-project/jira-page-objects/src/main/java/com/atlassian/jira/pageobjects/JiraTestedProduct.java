package com.atlassian.jira.pageobjects;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.config.AdaptingConfigProvider;
import com.atlassian.jira.pageobjects.config.EnvironmentBasedProductInstance;
import com.atlassian.jira.pageobjects.config.FuncTestPluginDetector;
import com.atlassian.jira.pageobjects.config.JiraConfigProvider;
import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.pageobjects.config.RestoreJiraData;
import com.atlassian.jira.pageobjects.config.SimpleJiraSetup;
import com.atlassian.jira.pageobjects.config.SmartRestoreJiraData;
import com.atlassian.jira.pageobjects.config.TestEnvironment;
import com.atlassian.jira.pageobjects.config.TestkitPluginDetector;
import com.atlassian.jira.pageobjects.framework.MessageBoxPostProcessor;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.LogoutPage;
import com.atlassian.jira.pageobjects.pages.QuickLoginPage;
import com.atlassian.jira.pageobjects.pages.btf.JiraBtfLoginPage;
import com.atlassian.jira.pageobjects.pages.ondemand.JiraOdLoginPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.JavascriptRunner;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoBanner;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.binder.InjectPageBinder;
import com.atlassian.pageobjects.binder.StandardModule;
import com.atlassian.pageobjects.component.Header;
import com.atlassian.pageobjects.component.WebSudoBanner;
import com.atlassian.pageobjects.elements.ElementModule;
import com.atlassian.pageobjects.elements.timeout.PropertiesBasedTimeouts;
import com.atlassian.pageobjects.elements.timeout.TimeoutsModule;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.pageobjects.page.WebSudoPage;
import com.atlassian.selenium.visualcomparison.VisualComparableClient;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.AtlassianWebDriverModule;
import com.atlassian.webdriver.pageobjects.DefaultWebDriverTester;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.visualcomparison.WebDriverVisualComparableClient;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;

import org.openqa.selenium.WebDriver;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * JIRA implementation of {@link com.atlassian.pageobjects.TestedProduct}.
 *
 * @since 4.4
 */
@Defaults (instanceId = "jira", contextPath = "/jira", httpPort = 2990)
public class JiraTestedProduct implements TestedProduct<WebDriverTester>
{
    private static final String TIMEOUTS_PATH = "com/atlassian/jira/pageobjects/pageobjects-timeouts.properties";
    public static final String TEST_ONDEMAND_PROPERTY = "test.ondemand";
    public static final String TEST_SKIP_INSTANCE_SETUP_PROPERTY = "test.jira.setup.skip";

    private final WebDriverTester webDriverTester;
    private final ProductInstance productInstance;
    private final JIRAEnvironmentData environmentData;
    private final InjectPageBinder pageBinder;

    private final WindowSession windowSession;
    private final Backdoor backdoor;

    private final boolean isOnDemand;
    private final boolean skipSetup;
    private final UserCredentials adminCredentials;
    private final UserCredentials sysadminCredentials;

    public JiraTestedProduct(final TestedProductFactory.TesterFactory<WebDriverTester> testerFactory, final ProductInstance productInstance)
    {
        this.webDriverTester = testerFactory != null ? testerFactory.create() : new DefaultWebDriverTester();
        this.productInstance = notNull(productInstance);
        this.environmentData = buildEnvironmentData(productInstance);
        this.isOnDemand = Boolean.parseBoolean(getEnvironmentProperty(TEST_ONDEMAND_PROPERTY, "false"));
        this.skipSetup = Boolean.parseBoolean(getEnvironmentProperty(TEST_SKIP_INSTANCE_SETUP_PROPERTY, "false"));
        this.adminCredentials = getCredentialsFromProperty("admin", "admin");
        this.sysadminCredentials = getCredentialsFromProperty("sysadmin", isOnDemand ? "sysadmin" : "admin");
        this.backdoor = new Backdoor(environmentData);
        this.pageBinder = createFlavorDependentBinder();
        this.windowSession = injector().getProvider(WindowSession.class).get();

        // Set login credentials inside backdoor
        setCredentialsInBackdoorControls(backdoor);
        setCredentialsInBackdoorControls(backdoor.getTestkit());
    }

    /**
     * Sets username and password for all backdoor controls, using nasty tricks.
     */
    private void setCredentialsInBackdoorControls(final Object targetBackdoor)
    {
        final UserCredentials sysadmin = getSysadminCredentials();
        final Method[] methods = targetBackdoor.getClass().getMethods();
        for (final Method m : methods)
        {
            final Class<?> returnType = m.getReturnType();
            if (RestApiClient.class.isAssignableFrom(returnType) && (m.getParameterTypes().length == 0))
            {
                try
                {
                    final RestApiClient restApiClient = (RestApiClient)m.invoke(targetBackdoor);
                    restApiClient.loginAs(sysadmin.getUsername(), sysadmin.getPassword());
                }
                catch (Exception e)
                {
                    // Somethings is wrong :/
                    // throw new RuntimeException(e);
                }
            }
        }

    }

    private UserCredentials getCredentialsFromProperty(final String user, final String defaultUsernameAndPassword)
    {
        final String username = getEnvironmentProperty("jira." + user + ".username", defaultUsernameAndPassword);
        final String password = getEnvironmentProperty("jira." + user + ".password", defaultUsernameAndPassword);
        return UserCredentials.credentialsFor(username, password);
    }

    protected String getEnvironmentProperty(final String key, final String defaultValue)
    {
        return getEnvironmentProperty(key, defaultValue, false);
    }

    protected String getEnvironmentProperty(final String key, final String defaultValue, boolean allowEmpty)
    {
        final String systemProperty = System.getProperty(key);
        if(systemProperty == null || (!allowEmpty && systemProperty.isEmpty()))
        {
            final String envProperty = environmentData.getProperty(key);
            return envProperty == null ? defaultValue : envProperty;
        }
        return systemProperty;
    }

    private InjectPageBinder createFlavorDependentBinder()
    {
        final AtlassianWebDriver driver = webDriverTester.getDriver();

        final InjectPageBinder injectPageBinder = new InjectPageBinder(productInstance, webDriverTester,
                new StandardModule(this),
                new AtlassianWebDriverModule(this),
                new ElementModule(),
                new TimeoutsModule(PropertiesBasedTimeouts.fromClassPath(TIMEOUTS_PATH)),
                new EnvironmentDataModule(),
                new AliasModule(driver),
                new JiraUtilsModule(driver),
                new JiraInjectionPostProcessors());

        injectPageBinder.override(Header.class, JiraHeader.class);
        injectPageBinder.override(HomePage.class, DashboardPage.class);
        injectPageBinder.override(AdminHomePage.class, JiraAdminHomePage.class);
        injectPageBinder.override(WebSudoBanner.class, JiraWebSudoBanner.class);
        injectPageBinder.override(WebSudoPage.class, JiraWebSudoPage.class);

        if (isOnDemand())
        {
            injectPageBinder.override(JiraLoginPage.class, JiraOdLoginPage.class);
            injectPageBinder.override(LoginPage.class, JiraOdLoginPage.class);
        }
        else
        {
            injectPageBinder.override(JiraLoginPage.class, JiraBtfLoginPage.class);
            injectPageBinder.override(LoginPage.class, JiraBtfLoginPage.class);
        }

        return injectPageBinder;
    }

    public JiraTestedProduct(final ProductInstance productInstance)
    {
        this(null, productInstance);
    }

    public DashboardPage gotoHomePage()
    {
        return pageBinder.navigateToAndBind(DashboardPage.class);
    }

    public JiraAdminHomePage goToAdminHomePage()
    {
        return pageBinder.navigateToAndBind(JiraAdminHomePage.class);
    }

    public void quickLogin(final String username, final String password)
    {
        pageBinder.navigateToAndBind(QuickLoginPage.class, username, password);
    }

    public <P extends Page> P quickLogin(final String username, final String password, final Class<P> pageClass, final Object... args)
    {
        pageBinder.navigateToAndBind(QuickLoginPage.class, username, password);
        return pageBinder.navigateToAndBind(pageClass, args);
    }

    public void quickLoginAsSysadmin()
    {
        final UserCredentials sysadminCredentials = getSysadminCredentials();
        pageBinder.navigateToAndBind(QuickLoginPage.class, sysadminCredentials.getUsername(), sysadminCredentials.getPassword());
    }

    public <P extends Page> P quickLoginAsSysadmin(final Class<P> pageClass, final Object... args)
    {
        final UserCredentials sysadminCredentials = getSysadminCredentials();
        pageBinder.navigateToAndBind(QuickLoginPage.class, sysadminCredentials.getUsername(), sysadminCredentials.getPassword());
        return pageBinder.navigateToAndBind(pageClass, args);
    }

    public void quickLoginAsAdmin()
    {
        final UserCredentials adminCredentials = getAdminCredentials();
        pageBinder.navigateToAndBind(QuickLoginPage.class, adminCredentials.getUsername(), adminCredentials.getPassword());
    }

    public <P extends Page> P quickLoginAsAdmin(final Class<P> pageClass, final Object... args)
    {
        final UserCredentials adminCredentials = getAdminCredentials();
        pageBinder.navigateToAndBind(QuickLoginPage.class, adminCredentials.getUsername(), adminCredentials.getPassword());
        return pageBinder.navigateToAndBind(pageClass, args);
    }

    public JiraLoginPage gotoLoginPage()
    {
        return pageBinder.navigateToAndBind(JiraLoginPage.class);
    }

    public ViewIssuePage goToViewIssue(final String issueKey)
    {
        return pageBinder.navigateToAndBind(ViewIssuePage.class, issueKey);
    }

    public AdvancedSearch goToIssueNavigator()
    {
        return pageBinder.navigateToAndBind(AdvancedSearch.class);
    }

    /**
     * Synonyme to {@link #visit(Class, Object...)}.
     *
     * @param pageClass page class
     * @param params params
     * @param <P> page type
     * @return page instance
     */
    public <P extends Page> P goTo(final Class<P> pageClass, final Object... params)
    {
        return visit(pageClass, params);
    }

    public JiraTestedProduct logout()
    {
        pageBinder.navigateToAndBind(LogoutPage.class).logout();
        return this;
    }

    public <P extends Page> P visit(final Class<P> pageClass, final Object... args)
    {
        return pageBinder.navigateToAndBind(pageClass, args);
    }

    public <P extends Page> DelayedBinder<P> visitDelayed(final Class<P> pageClass, final Object... args)
    {
        final DelayedBinder<P> binder = pageBinder.delayedBind(pageClass, args);
        webDriverTester.gotoUrl(productInstance.getBaseUrl() + binder.get().getUrl());
        return binder;
    }

    public boolean isAt(final Class<? extends Page> page)
    {
        return pageBinder.delayedBind(page).canBind();
    }

    public PageBinder getPageBinder()
    {
        return pageBinder;
    }

    public ProductInstance getProductInstance()
    {
        return productInstance;
    }

    public WebDriverTester getTester()
    {
        return webDriverTester;
    }

    public JIRAEnvironmentData environmentData()
    {
        return environmentData;
    }

    @Nonnull
    private JIRAEnvironmentData buildEnvironmentData(@Nonnull final ProductInstance productInstance)
    {
        if (productInstance instanceof EnvironmentBasedProductInstance)
        {
            return ((EnvironmentBasedProductInstance) productInstance).environmentData();
        }
        else
        {
            return new ProductInstanceBasedEnvironmentData(productInstance);
        }
    }

    public Backdoor backdoor()
    {
        return backdoor;
    }

    public Injector injector()
    {
        return pageBinder.injector();
    }

    public WindowSession windowSession()
    {
        return windowSession;
    }

    public boolean shouldSkipSetup()
    {
        return skipSetup;
    }

    private class EnvironmentDataModule implements Module
    {
        @Override
        public void configure(final Binder binder)
        {
            binder.bind(JIRAEnvironmentData.class).toInstance(environmentData);
            binder.bind(Backdoor.class).toInstance(backdoor);
            binder.bind(com.atlassian.jira.testkit.client.Backdoor.class).toInstance(backdoor.getTestkit());
            binder.bind(TestEnvironment.class).toInstance(new TestEnvironment());
        }

    }

    private static class AliasModule implements Module
    {
        private final AtlassianWebDriver driver;

        private AliasModule(final AtlassianWebDriver driver) {this.driver = driver;}

        @Override
        public void configure(final Binder binder)
        {
            binder.bind(org.openqa.selenium.JavascriptExecutor.class).toInstance(driver);
            binder.bind(WebDriver.class).toInstance(driver);
        }
    }

    private static class JiraUtilsModule implements Module
    {
        private final AtlassianWebDriver driver;

        public JiraUtilsModule(final AtlassianWebDriver driver)
        {
            this.driver = driver;
        }
        @Override
        public void configure(final Binder binder)
        {
            binder.bind(FuncTestPluginDetector.class).in(Scopes.SINGLETON);
            binder.bind(TestkitPluginDetector.class).in(Scopes.SINGLETON);
            binder.bind(RestoreJiraData.class).to(SmartRestoreJiraData.class);
            binder.bind(JiraConfigProvider.class).to(AdaptingConfigProvider.class).in(Scopes.SINGLETON);
            binder.bind(SimpleJiraSetup.class);
            binder.bind(VisualComparableClient.class).toInstance(new WebDriverVisualComparableClient(driver));
            binder.bind(JavascriptRunner.class).in(Scopes.SINGLETON);
            binder.bind(UserSessionHelper.class).in(Scopes.SINGLETON);
        }
    }

    private static class JiraInjectionPostProcessors implements Module
    {
        @Override
        public void configure(final Binder binder)
        {
            binder.bind(MessageBoxPostProcessor.class);
        }
    }

    public boolean isOnDemand()
    {
        return isOnDemand;
    }

    public UserCredentials getAdminCredentials()
    {
        return adminCredentials;
    }

    public UserCredentials getSysadminCredentials()
    {
        return sysadminCredentials;
    }
}
