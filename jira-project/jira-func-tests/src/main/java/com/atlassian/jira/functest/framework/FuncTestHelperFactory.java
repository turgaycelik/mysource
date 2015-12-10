package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.AssertionsImpl;
import com.atlassian.jira.functest.framework.assertions.IssueTableAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.util.IssueTableClient;
import com.atlassian.jira.functest.framework.util.SearchersClient;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

/**
 * This class is responsible for correctly instantiating the various
 * helper frameworks such as {@link Assertions} etc
 */
public class FuncTestHelperFactory
{
    private final JIRAEnvironmentData environmentData;
    private final FuncTestCaseJiraSetup funcTestCaseJiraSetup;

    private WebTester tester;
    private NavigationImpl navigation = null;
    private AdministrationImpl administration = null;
    private Backdoor backdoor;
    private AssertionsImpl assertions = null;
    private IssueTableAssertions issueTableAssertions = null;
    private Parser parser = null;
    private Form form;
    private LocatorFactory locator;

    /**
     * Create func test helper objects to be used when running the passed tests.
     * 
     * @param funcTest the test to be run.
     * @param environmentData the JIRA environment for the test.
     */
    public FuncTestHelperFactory(TestCase funcTest, JIRAEnvironmentData environmentData)
    {
        this(funcTest,environmentData, new FuncTestWebClientListener(), false);
    }

    /**
     * Create func test helper objects for an already running test. This should only be used from old tests that
     * want access to the new helper objects.
     *
     * @param tester current state of the func test.
     * @param environmentData the JIRA environment under test.
     */
    public FuncTestHelperFactory(final WebTester tester, final JIRAEnvironmentData environmentData)
    {
        this.tester = tester;
        this.environmentData = environmentData;
        this.funcTestCaseJiraSetup = null;
    }

    /**
     * Create func test helper objects to be used when running the passed tests.
     *
     * @param funcTest the test to be run.
     * @param environmentData the JIRA environment for the test.
     * @param webClientListener
     */
    FuncTestHelperFactory(TestCase funcTest, JIRAEnvironmentData environmentData,
            final FuncTestWebClientListener webClientListener, final boolean skipSetup)
    {
        this.tester = new WebTester();
        this.environmentData = environmentData;

        initWebTester(environmentData);

        //now set JIRA properly.
        this.funcTestCaseJiraSetup = new FuncTestCaseJiraSetup(funcTest, getTester(), environmentData, getNavigation(), webClientListener, skipSetup);
    }

    private void initWebTester(JIRAEnvironmentData environmentData)
    {
        // setup things ready for testing
        WebTesterFactory.setupWebTester(tester, environmentData);
        tester.beginAt("/");
    }

    public WebTester getTester()
    {
        return tester;
    }

    public Navigation getNavigation()
    {
        if (navigation == null)
        {
            navigation = new NavigationImpl(getTester(), getEnvironmentData());
        }
        return navigation;
    }

    public Form getForm()
    {
        if (form == null)
        {
            form = new FormImpl(getTester());
        }
        return form;
    }

    public Administration getAdministration()
    {
        if (administration == null)
        {
            administration = new AdministrationImpl(getTester(), getEnvironmentData(), getNavigation(), getAssertions());
        }
        
        return administration;
    }

    public Assertions getAssertions()
    {
        if (assertions == null)
        {
            assertions = new AssertionsImpl(getTester(), getEnvironmentData(), getNavigation(), getLocator());
        }
        return assertions;
    }

    public LocatorFactory getLocator()
    {
        if (locator == null)
        {
            locator = new LocatorFactoryImpl(getTester());
        }
        return locator;
    }

    public TextAssertions getTextAssertions()
    {
        return getAssertions().getTextAssertions();
    }

    public IssueTableAssertions getIssueTableAssertions()
    {
        if (null == issueTableAssertions)
        {
            issueTableAssertions = new IssueTableAssertions(backdoor);
        }
        return issueTableAssertions;
    }

    public FuncTestCaseJiraSetup getFuncTestCaseJiraSetup()
    {
        return funcTestCaseJiraSetup;
    }

    public Parser getParser()
    {
        if (parser == null)
        {
            parser = new ParserImpl(tester, environmentData);
        }
        return parser;
    }

    public Backdoor getBackdoor()
    {
        if (backdoor == null)
        {
            backdoor = new Backdoor(environmentData);
        }
        return backdoor;
    }

    public JIRAEnvironmentData getEnvironmentData()
    {
        return environmentData;
    }
}
