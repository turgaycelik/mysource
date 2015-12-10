package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.IssueTableAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.dump.TestCaseDumpKit;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.log.LogOnBothSides;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.jira.util.ProgressPageControl;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

import java.net.URISyntaxException;
import java.util.Date;

/**
 * The base class for JIRA "next generation" functional test cases.  This lightweight implementation of {@link
 * junit.framework.TestCase} provides for the ability to "record" exceptions that happening during the running of tests
 * and produce "test artifacts" of these exceptions.
 * <p/>
 * Also much of the standard JIRA setUp() and tearDown() is done via this class.  It is the intended base class for JIRA
 * web functional test cases.
 * <p/>
 * NOTE: {@link #setUp()} and {@link #tearDown()} are final, please use the template methods {@link #setUpTest()}  and
 * {@link #tearDownTest()} instead
 *
 * @since v3.13
 */
public class FuncTestCase extends TestCase implements EnvironmentAware, FuncTestLogger, FunctTestConstants
{
    /**
     * Turns a simple key into a complete key for a built in custom field type
     * @param type simple key
     * @return complete key for the given built-in custom field type
     */
    public static String builtInCustomFieldKey(String type)
    {
        return BUILT_IN_CUSTOM_FIELD_KEY + ":" + type;
    }

    private FuncTestCaseJiraSetup jiraTestJiraSetup;
    private FuncTestLoggerImpl logger = new FuncTestLoggerImpl(1);

    /**
     * Use this field to access the {@link net.sourceforge.jwebunit.WebTester} in play
     */
    protected WebTester tester;

    /**
     * Use this field to access the {@link com.atlassian.jira.webtests.util.JIRAEnvironmentData} in play
     */
    protected JIRAEnvironmentData environmentData;

    /**
     * Use this field to access the {@link Navigation} helper in play
     */
    protected Navigation navigation;

    /**
     * Used to set form values in tests.
     */
    protected Form form;

    /**
     * Used to find out about the current HTML page that the test is on.
     */
    protected HtmlPage page;

    /**
     * Use this field to access the {@link Parser} helper in play
     */
    protected Parser parse;

    /**
     * Use this field to access the {@link Administration} helper in play
     */
    protected Administration administration;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.backdoor.Backdoor} helper in play, which can make sly RPCs to the server.
     */
    protected Backdoor backdoor;

    /**
     * Use this field to access the {@link Assertions} helper in play
     */
    protected Assertions assertions;

    /**
     * Use this field to access the {@link TextAssertions} helper in play
     */
    protected TextAssertions text;

    /**
     * Use this field to access the {@link IssueTableAssertions} helper in play
     */
    protected IssueTableAssertions issueTableAssertions;

    /**
     * Use this field to access the {@link com.atlassian.jira.testkit.client.log.FuncTestLogger} in play
     */
    protected FuncTestLogger log;

    /**
     * Use this field to access the {@link com.atlassian.jira.functest.framework.LocatorFactory} in play
     */
    protected LocatorFactory locator;

    /**
     * Use this field to access the {@link com.atlassian.jira.webtests.JIRAWebTest} test case in play.  Access is
     * provided to the old way of writing functiona; test cases but its use is not encouraged.  Please consider porting
     * the required function into the new framework.
     *
     * @deprecated think about why you need use the old ways
     */
    protected JIRAWebTest oldway_consider_porting;

    /**
     * Factory for getting access to useful Func Test objects
     */
    protected FuncTestHelperFactory funcTestHelperFactory;

    private FuncTestWebClientListener webClientListener;

    private long startTime;

    /**
     * Setup for an actual test
     */
    protected void setUpTest()
    {}

    /**
     * Override this to set up any {@link com.meterware.httpunit.HttpUnitOptions} that must be set before the {@link
     * net.sourceforge.jwebunit.WebTester} is instantiated.
     */
    protected void setUpHttpUnitOptions()
    {}

    /**
     * <p>The default and final setUp() method will connect to JIRA and make sure its initialised and ready to go.<p/>
     * For individual test setUp, override {@link #setUpTest()} instead.
     */
    protected final void setUp()
    {
        DefaultFuncTestHttpUnitOptions.setDefaultOptions();
        // allow people to override these options
        setUpHttpUnitOptions();

        JIRAEnvironmentData envData = getEnvironmentData();

        // keep track of HTTP traffic
        webClientListener = new FuncTestWebClientListener();

        try
        {
            funcTestHelperFactory = new FuncTestHelperFactory(this, envData, webClientListener, shouldSkipSetup());
        }
        catch (Throwable throwable)
        {
            // An error has occurred before the WebTester has been set up
            WebTester tester = WebTesterFactory.createNewWebTester(envData);
            tester.beginAt("/");
            dumpFailureInformation(throwable, this, tester);
            throw new RuntimeException("Unable to setup JIRA", throwable);
        }

        try
        {
            tester = funcTestHelperFactory.getTester();
            navigation = funcTestHelperFactory.getNavigation();
            form = funcTestHelperFactory.getForm();
            page = new HtmlPage(tester);
            administration = funcTestHelperFactory.getAdministration();
            backdoor = funcTestHelperFactory.getBackdoor();
            assertions = funcTestHelperFactory.getAssertions();
            text = funcTestHelperFactory.getTextAssertions();
            issueTableAssertions = funcTestHelperFactory.getIssueTableAssertions();
            parse = funcTestHelperFactory.getParser();
            log = new FuncTestLoggerImpl(3);
            locator = funcTestHelperFactory.getLocator();

            jiraTestJiraSetup = funcTestHelperFactory.getFuncTestCaseJiraSetup();

            // a concrete implementation of the old way of doing things.
            // We allow acccess to the old functions but we dont encourage it.  We do it last so it doesnt try to re-setup JIRA
            class ConcreteJIRAWebTest extends JIRAWebTest
            {

                public ConcreteJIRAWebTest(String name, WebTester tester, JIRAEnvironmentData envirData)
                {
                    super(name, envirData);
                    this.tester = tester;
                }
            }
            oldway_consider_porting = new ConcreteJIRAWebTest(getName(), tester, envData);

            setUpTest();
        }
        catch (Throwable throwable)
        {
            dumpFailureInformation(throwable, this);
            throw new RuntimeException("Unable to setup the TestCase itself", throwable); 
        }

    }

    protected boolean shouldSkipSetup()
    {
        return false;
    }

    /**
     * This will logout of JIRA and then produce test artifacts for any test that throws exceptions.
     * <p/>
     * For individual test tearDown, override {@link #tearDownTest()} instead.
     */
    protected final void tearDown()
    {
        try
        {
            tearDownTest();
        }
        finally
        {
            jiraTestJiraSetup.tearDown(this);
        }
    }

    /**
     * individual test tear down
     */
    protected void tearDownTest()
    {}

    public JIRAEnvironmentData getEnvironmentData()
    {
        if (environmentData == null)
        {
            environmentData = new LocalTestEnvironmentData();
        }
        return environmentData;
    }

    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        this.environmentData = environmentData;
    }

    public WebTester getTester()
    {
        return tester;
    }

    public Assertions getAssertions()
    {
        return assertions;
    }

    public FuncTestWebClientListener getWebClientListener()
    {
        return webClientListener;
    }

    public void log(Object logData)
    {
        logger.log(logData);
    }

    public void log(Throwable t)
    {
        logger.log(t);
    }

    /**
     * A shortcut method to allow quick creation of {@link com.atlassian.jira.functest.framework.locator.XPathLocator}s
     *
     * @param xpathExpression the xpath expression
     * @return an XPathLocator
     */
    protected XPathLocator xpath(final String xpathExpression)
    {
        return locator.xpath(xpathExpression);
    }

    /**
     * Overridden so we can "record" exceptions that may happen during test execution.  This is the method that all
     * JUnit run methods end up calling.
     *
     * @see junit.framework.TestCase#runTest()
     */
    protected void runTest() throws Throwable
    {
        try
        {
            super.runTest();
        }
        catch (Throwable throwable)
        {
            dumpFailureInformation(throwable, this);
            throw throwable;
        }
    }

    private void dumpFailureInformation(Throwable throwable, final TestCase testCase)
    {
        try
        {
            TestCaseDumpKit.dumpTestInformation(testCase, new Date(), throwable, isDumpHTML());
        }
        catch (RuntimeException ignored)
        {
            ignored.printStackTrace();
        }
    }

    private void dumpFailureInformation(Throwable throwable, final TestCase testCase, final WebTester tester)
    {
        try
        {
            TestCaseDumpKit.dumpTestInformation(testCase, new Date(), throwable, isDumpHTML(), tester);
        }
        catch (RuntimeException ignored)
        {
            ignored.printStackTrace();
        }
    }

    /**
     * @return a boolean indicating whether to dump the HTML response to System.out (useful to disable in REST tests)
     */
    protected boolean isDumpHTML()
    {
        return true;
    }

    /**
     * The outer most edge of a JUnit Test.  All things start and end here.
     *
     * @see junit.framework.TestCase#runBare()
     */
    public void runBare() throws Throwable
    {
        startTime = System.currentTimeMillis();
        //We need to get this through the method because an environment may not have been set.
        JIRAEnvironmentData data = getEnvironmentData();
        final com.atlassian.jira.testkit.client.Backdoor testkit = new com.atlassian.jira.testkit.client.Backdoor(data);
        LogOnBothSides.log(testkit, TestInformationKit.getStartMsg(this, data.getTenant()));
        try
        {
            super.runBare();
            LogOnBothSides.log(testkit, TestInformationKit.getEndMsg(this, data.getTenant(), System.currentTimeMillis() - startTime, webClientListener));
        }
        catch (Throwable throwable)
        {
            LogOnBothSides.log(testkit, TestInformationKit.getEndMsg(this, data.getTenant(), System.currentTimeMillis() - startTime, webClientListener, throwable));
            throw throwable;
        }
        finally
        {
            clearTestCaseVariables();
        }
    }

    /**
     * JUnit keeps each Testcase in memory for reporting reasons and hence if we dont clear the internal variables we
     * will use  alot of memory and eventually run out!
     */
    private void clearTestCaseVariables()
    {
        tester = null;
        funcTestHelperFactory = null;
        environmentData = null;
        navigation = null;
        form = null;
        page = null;
        parse = null;
        administration = null;
        backdoor = null;
        assertions = null;
        jiraTestJiraSetup = null;
        text = null;
        log = null;
        oldway_consider_porting = null;
        webClientListener = null;
        locator = null;
    }

    protected JiraRestClient createRestClient(final String user, final String password)
    {
        final AsynchronousJiraRestClientFactory restClientFactory = new AsynchronousJiraRestClientFactory();
        try
        {
            return restClientFactory.createWithBasicHttpAuthentication(getEnvironmentData().getBaseUrl().toURI(), user, password);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected JiraRestClient createRestClient()
    {
        return createRestClient(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void waitAndReloadBulkOperationProgressPage()
    {
        waitAndReloadBulkOperationProgressPage(tester);
    }

    public void waitAndReloadBulkOperationProgressPage(WebTester webTester)
    {
        webTester.assertTextPresent("Bulk Operation Progress");
        ProgressPageControl.waitAndReload(webTester, "bulkoperationprogressform", "Refresh", "Acknowledge");
        log("waitAndReloadBulkOperationProgressPage");
    }
}
