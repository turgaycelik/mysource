package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.api.client.AbstractClientListener;
import com.atlassian.buildeng.hallelujah.api.client.ClientListener;
import com.atlassian.buildeng.hallelujah.api.client.ClientTestCaseProvider;
import com.atlassian.buildeng.hallelujah.api.client.ClientTestCaseResultCollector;
import com.atlassian.buildeng.hallelujah.api.client.ClientTestCaseRunner;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseName;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseResult;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import static com.atlassian.jira.functest.framework.suite.Category.fromAnnotation;
import static com.atlassian.jira.hallelujah.WebTestListenerToClientListenerAdapter.WebTestClientDescription.fakeSuite;
import static com.atlassian.jira.hallelujah.WebTestListenerToClientListenerAdapter.WebTestClientDescription.fromTestCaseName;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Arrays.asList;

public class WebTestListenerToClientListenerAdapter extends AbstractClientListener implements ClientListener
{


    private WebTestListener listener;

    public WebTestListenerToClientListenerAdapter(final WebTestListener listener)
    {
        this.listener = notNull(listener);
    }

    @Override
    public boolean onPass(final ClientTestCaseRunner clientTestCaseRunner, final ClientTestCaseProvider clientTestCaseProvider,
            final ClientTestCaseResultCollector clientTestCaseResultCollector, final TestCaseResult result)
    {
        listener.testFinished(fromTestCaseName(result.testCaseName));
        return true;
    }

    @Override
    public boolean onFailure(ClientTestCaseRunner clientTestCaseRunner, ClientTestCaseProvider clientTestCaseProvider, ClientTestCaseResultCollector clientTestCaseResultCollector, TestCaseResult result)
    {
        listener.testFailure(fromTestCaseName(result.testCaseName), new HallelujahTestFailureException(result.failure.message, result.failure));
        listener.testFinished(fromTestCaseName(result.testCaseName));
        return true;
    }

    @Override
    public boolean onError(ClientTestCaseRunner clientTestCaseRunner, ClientTestCaseProvider clientTestCaseProvider, ClientTestCaseResultCollector clientTestCaseResultCollector, TestCaseResult result)
    {
        listener.testError(fromTestCaseName(result.testCaseName), new HallelujahTestFailureException(result.error.message, result.failure));
        listener.testFinished(fromTestCaseName(result.testCaseName));
        return true;
    }

    @Override
    public void onFinish(ClientTestCaseRunner clientTestCaseRunner, ClientTestCaseProvider clientTestCaseProvider, ClientTestCaseResultCollector clientTestCaseResultCollector)
    {
        listener.suiteFinished(fakeSuite());
    }

    @Override
    public void onStart(ClientTestCaseRunner clientTestCaseRunner, ClientTestCaseProvider clientTestCaseProvider, ClientTestCaseResultCollector clientTestCaseResultCollector)
    {
        listener.suiteStarted(fakeSuite());
    }

    @Override
    public boolean onTestStart(ClientTestCaseRunner clientTestCaseRunner, ClientTestCaseProvider clientTestCaseProvider, ClientTestCaseResultCollector clientTestCaseResultCollector,
            TestCaseName testCaseName)
    {
        listener.testStarted(fromTestCaseName(testCaseName));
        return true;
    }

    static class WebTestClientDescription implements WebTestDescription
    {
        private final Class<?> testClass;
        private final TestCaseName testCaseName;

        public static WebTestClientDescription fromTestCaseName(TestCaseName name)
        {
            return new WebTestClientDescription(name);
        }

        public static WebTestDescription fakeSuite()
        {
            return new WebTestClientDescription();
        }

        public WebTestClientDescription(TestCaseName testCaseName)
        {
            try
            {
                this.testCaseName = testCaseName;
                this.testClass = Class.forName(testCaseName.className);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }

        public WebTestClientDescription()
        {
            this.testClass = getClass();
            this.testCaseName = new TestCaseName(testClass().getName(), "");
        }


        @Override
        public String name()
        {
            return testCaseName.className + "." + testCaseName.methodName;
        }

        @Override
        public String className()
        {
            return testCaseName.className;
        }

        @Override
        public String methodName()
        {
            return testCaseName.methodName;
        }

        @Override
        public Class<?> testClass()
        {
            return testClass;
        }

        @Override
        public Iterable<Annotation> annotations()
        {
            return asList(testClass.getAnnotations());
        }

        @Override
        public Set<Category> categories()
        {
            return fromAnnotation(testClass.getAnnotation(WebTest.class));
        }

        @Override
        public boolean isTest()
        {
            return true;
        }

        @Override
        public boolean isSuite()
        {
            return false;
        }

        @Override
        public int testCount()
        {
            return 1;
        }

        @Override
        public Iterable<WebTestDescription> children()
        {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            WebTestClientDescription that = (WebTestClientDescription) o;

            if (testCaseName != null ? !testCaseName.equals(that.testCaseName) : that.testCaseName != null)
            {
                return false;
            }
            if (testClass != null ? !testClass.equals(that.testClass) : that.testClass != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = testClass != null ? testClass.hashCode() : 0;
            result = 31 * result + (testCaseName != null ? testCaseName.hashCode() : 0);
            return result;
        }
    }
}
