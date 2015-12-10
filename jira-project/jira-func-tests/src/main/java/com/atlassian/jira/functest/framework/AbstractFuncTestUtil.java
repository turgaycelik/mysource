package com.atlassian.jira.functest.framework;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * A base class for func test utlity classes.  Put commmon code in here.
 * <p/>
 * Provides the abiltiy for the helpers to log and dump.
 *
 * @since v3.13
 */
public class AbstractFuncTestUtil implements FuncTestLogger
{
    protected static final String FS = System.getProperty("file.separator");

    protected JIRAEnvironmentData environmentData;
    protected final WebTester tester;
    protected final FuncTestLogger logger;
    protected final LocatorFactory locators;
    protected final int logIndentLevel;

    private final FuncTestHelperFactory funcTestHelperFactory;

    public AbstractFuncTestUtil(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        this.environmentData = environmentData;
        this.tester = tester;
        this.logger = new FuncTestLoggerImpl(logIndentLevel);
        this.logIndentLevel = logIndentLevel;
        this.funcTestHelperFactory = new FuncTestHelperFactory(tester, environmentData);
        this.locators = funcTestHelperFactory.getLocator();
    }

    protected final int childLogIndentLevel()
    {
        return logIndentLevel + 1;
    }

    protected final Navigation navigation()
    {
        return funcTestHelperFactory.getNavigation();
    }

    protected final Assertions getAssertions()
    {
        return funcTestHelperFactory.getAssertions();
    }

    public JIRAEnvironmentData getEnvironmentData()
    {
        return environmentData;
    }

    public FuncTestHelperFactory getFuncTestHelperFactory()
    {
        return funcTestHelperFactory;
    }

    public FuncTestLogger getLogger()
    {
        return logger;
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
     * Goes to the given URL, submits the given button or logs the given message if the given button doesn't exist.
     *
     * @param url url to go to to submit the button
     * @param button label on the button to submit at url
     * @param logOnFail null or a message to log if button isn't found
     */
    protected void submitAtPage(String url, String button, String logOnFail)
    {
        tester.gotoPage(url);
        if (tester.getDialog().hasSubmitButton(button))
        {
            tester.submit(button);
        }
        else if (logOnFail != null)
        {
            log(logOnFail);
        }
    }
}
