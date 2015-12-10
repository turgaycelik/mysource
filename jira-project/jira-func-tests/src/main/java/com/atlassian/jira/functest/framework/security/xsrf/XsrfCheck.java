package com.atlassian.jira.functest.framework.security.xsrf;

import com.atlassian.jira.functest.framework.Form;
import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.junit.Assert.fail;

/**
 * Class that makes xsrf related testing easier. Provide it with repeatable steps and it'll perform a test for both
 * valid and invalid tokens
 *
 * @since v4.1
 */
public class XsrfCheck
{
    public static final String ATL_TOKEN = "atl_token";
    public static final String XSRF_DEFAULT_ERROR = "SecurityTokenMissing";

    private String description;
    private Setup setup;
    private Submission submission;
    private FuncTestHelperFactory funcTestHelperFactory;
    private FuncTestLogger logger;
    private boolean initCalled = false;

    /**
     * @param description Description of testcase
     * @param setup The steps to perform before sending a request with a valid / invalid token
     * @param submission The submission implementation - eg Link or Form based submission
     */
    public XsrfCheck(String description, Setup setup, Submission submission)
    {
        this.description = description;
        this.setup = setup;
        this.submission = submission;
        this.logger = new FuncTestLoggerImpl(2);
    }

    protected void init(final FuncTestHelperFactory funcTestHelperFactory)
    {
        this.funcTestHelperFactory = funcTestHelperFactory;
        this.submission.init(funcTestHelperFactory);
        this.initCalled = true;
    }

    public void run() throws Exception
    {
        run(XSRF_DEFAULT_ERROR);
    }

    public void run(String xsrfError)  throws Exception
    {
        if (!initCalled)
        {
            throw new IllegalStateException("init() must be called before running the check!");
        }

        logger.log("STARTING: " + description);
        logger.log("\t SETUP");

        getTester().gotoPage(""); // start at the top of JIRA
        setup.setup();

        logger.log("\t SUBMITTING DODGY TOKEN");
        submission.removeToken();
        submission.submitRequest();
        getTester().assertTextPresent(xsrfError);

        logger.log("\t SETUP");
        getNavigation().gotoDashboard();
        setup.setup();

        logger.log("\t SUBMITTING VALID TOKEN");
        submission.submitRequest();
        getTester().assertTextNotPresent(xsrfError);

        logger.log("COMPLETED: " + description);

    }

    private WebTester getTester()
    {
        return funcTestHelperFactory.getTester();
    }

    private Navigation getNavigation()
    {
        return funcTestHelperFactory.getNavigation();
    }

    private Form getForm()
    {
        return funcTestHelperFactory.getForm();
    }

    /**
     * Given a url string, if the {@link com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck#ATL_TOKEN} is
     * present as a parameter, this method will make its value invalid.
     *
     * @param original the url string
     * @return the replacement string if token was found; the original otherwise.
     */
    public static String invalidTokenInUrl(String original)
    {
        int startIndex = original.indexOf(ATL_TOKEN);
        if (startIndex != -1)
        {
            StringBuilder sb = new StringBuilder(original);
            sb.insert(startIndex + ATL_TOKEN.length() + 1, "invalidToken");
            return sb.toString();
        }
        return original;
    }

    /**
     * Used to define the steps to perform before sending a request with a valid / invalid token
     */
    public static interface Setup
    {
        void setup();

        /**
         * Used to define no setup routine
         */
        public static final class None implements Setup
        {

            public void setup()
            {
            }
        }

    }


    /**
     * Used to define the implementation of removing a token from a request and sending a request
     */
    public static interface Submission
    {
        /**
         * Initialise the submission with state from the checker.
         *
         * @param funcTestHelperFactory factory
         */
        void init(FuncTestHelperFactory funcTestHelperFactory);

        /**
         * Removes the token from the state so that we can test what happens when a "dodgy token" is submitted to an
         * action that is expecting a good token.
         *
         * @throws Exception e
         */
        void removeToken() throws Exception;

        /**
         * Submits a request to an action that presumably is XSRF-protected and requires a token to be present.
         *
         * @throws Exception e
         */
        void submitRequest() throws Exception;
    }

    abstract static class BaseSubmission implements Submission
    {
        protected FuncTestHelperFactory funcTestHelperFactory;

        public void init(final FuncTestHelperFactory funcTestHelperFactory)
        {
            this.funcTestHelperFactory = funcTestHelperFactory;
        }

        WebTester getTester()
        {
            return funcTestHelperFactory.getTester();
        }

        Navigation getNavigation()
        {
            return funcTestHelperFactory.getNavigation();
        }

        Form getForm()
        {
            return funcTestHelperFactory.getForm();
        }
    }

    /**
     * Base class for form-based submission
     */
    public abstract static class AbstractFormSubmission extends BaseSubmission implements Submission
    {
        public void removeToken()
        {
            for (WebForm webForm : getForm().getForms())
            {
                if (webForm.hasParameterNamed(ATL_TOKEN))
                {
                    webForm.getScriptableObject().setParameterValue(ATL_TOKEN, "invalidToken");
                }

                webForm.getScriptableObject().setAction(invalidTokenInUrl(webForm.getAction()));
            }
        }
    }

    /**
     * Form-based submission by name
     */
    public static class FormSubmission extends AbstractFormSubmission
    {
        private String submitName;

        public FormSubmission(String submitName)
        {
            this.submitName = submitName;
        }

        public void submitRequest()
        {
            getTester().submit(submitName);
        }
    }

    /**
     * Form-based submission by id
     */
    public static class FormSubmissionWithId extends AbstractFormSubmission
    {
        private String submitId;

        public FormSubmissionWithId(String submitId)
        {
            this.submitId = submitId;
        }

        public void submitRequest()
        {
            getTester().clickButton(submitId);
        }
    }

    /**
     * TODO: Document this class / interface here
     */
    public static abstract class AsynchFormSubmission extends FormSubmission
    {
        private boolean removedTokenCalled;
        private final long timeoutMS;

        public AsynchFormSubmission(final String submitId, long timeoutMS)
        {
            super(submitId);
            this.timeoutMS = timeoutMS;
            removedTokenCalled = false;
        }

        @Override
        public void removeToken()
        {
            removedTokenCalled = true;
            super.removeToken();
        }

        @Override
        public void submitRequest()
        {
            super.submitRequest();
            if (!removedTokenCalled)
            {
                boolean timedout = true;
                long then = System.currentTimeMillis();
                while (then - System.currentTimeMillis() < timeoutMS)
                {
                    sleep();
                    if (isOperationFinished())
                    {
                        timedout = false;
                        break;
                    }
                }
                if (timedout)
                {
                    throw new RuntimeException("The Asynch Form Submission never became true");
                }
            }
            removedTokenCalled = false;
        }

        private void sleep()
        {
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return true if the operation has finished running
         */
        public abstract boolean isOperationFinished();

    }

    /**
     * Abstract implementation of Submission for mutative actions accessed by links.
     */
    public static abstract class AbstractLinkSubmission extends BaseSubmission implements Submission
    {
        private String urlToSubmit;

        public abstract String getLink() throws Exception;

        public void removeToken() throws Exception
        {
            urlToSubmit = invalidTokenInUrl(getLink());
        }

        public void submitRequest() throws Exception
        {
            getTester().gotoPage(urlToSubmit);
            urlToSubmit = getLink();
        }
    }

    /**
     * Id-based link submission
     */
    public static class LinkWithIdSubmission extends AbstractLinkSubmission
    {
        private String linkId;
        private String originalUrl;

        public LinkWithIdSubmission(final String linkId)
        {
            this.linkId = linkId;
        }

        public String getLink() throws Exception
        {
            if (originalUrl == null)
            {
                WebLink link = getTester().getDialog().getResponse().getLinkWithID(linkId);
                notNull("link", link);
                originalUrl = link.getURLString();
            }
            return originalUrl;
        }
    }

    /**
     * Name-based link submission
     */
    public static class LinkWithTextSubmission extends AbstractLinkSubmission
    {
        private String linkText;
        private String originalUrl;

        public LinkWithTextSubmission(final String linkText)
        {
            this.linkText = linkText;
        }

        public String getLink() throws Exception
        {
            if (originalUrl == null)
            {
                WebLink link = getTester().getDialog().getResponse().getLinkWith(linkText);
                notNull("link", link);
                originalUrl = link.getURLString();
            }
            return originalUrl;
        }
    }

    /**
     * XPath based link submission. Used when a page has more than 1 link with the same link title
     */
    public static class XPathLinkSubmission extends AbstractLinkSubmission
    {
        private String xPathExpression;
        private String originalUrl;

        public XPathLinkSubmission(final String xPathExpression)
        {
            this.xPathExpression = xPathExpression;
        }

        public String getLink() throws Exception
        {
            if (originalUrl == null)
            {
                XPathLocator locator = new XPathLocator(getTester(), xPathExpression);
                final Node node = locator.getNode();
                notNull("node not found for xPathExpression [" + xPathExpression + "]", node);
                originalUrl = ((Attr) node.getAttributes().getNamedItem("href")).getValue();
            }
            return originalUrl;
        }
    }

    /**
     * XPath based link submission. Used when a page has more than 1 link with the same link title
     */
    public static class CssLocatorLinkSubmission extends AbstractLinkSubmission
    {
        private final int index;
        private String cssExpression;
        private String originalUrl;

        public CssLocatorLinkSubmission(final String cssExpression, int index)
        {
            this.cssExpression = cssExpression;
            this.index = index;
        }

        public CssLocatorLinkSubmission(final String cssExpression)
        {
            this(cssExpression, 0);
        }

        public String getLink() throws Exception
        {
            if (originalUrl == null)
            {
                CssLocator locator = new CssLocator(getTester(), cssExpression);
                Node[] nodes = locator.getNodes();
                if (index >= nodes.length)
                {
                    fail(String.format("Unable to find link %d at '%s'.", index, cssExpression));
                }
                originalUrl = ((Attr) nodes[index].getAttributes().getNamedItem("href")).getValue();
            }
            return originalUrl;
        }
    }
}
