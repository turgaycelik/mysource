package com.atlassian.jira.functest.framework;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.testcase.TestCaseKit;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

// import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * {@link com.atlassian.jira.functest.framework.WebTestDescription} based on the JUnit4
 * {@link org.junit.runner.Description}.
 *
 * @since v4.4
 */
public class JUnit3WebTestDescription implements WebTestDescription
{
    public static Function<Test,WebTestDescription> TRANSFORMER = new Function<Test,WebTestDescription>()
    {
        @Override
        public WebTestDescription apply(@Nullable Test input)
        {
            return new JUnit3WebTestDescription(input);
        }
    };

    private final Test test;
    private final Iterable<WebTestDescription> children;

    public JUnit3WebTestDescription(Test test)
    {
        this.test = notNull(test);
        this.children = collectChildren();
    }

    private Iterable<WebTestDescription> collectChildren()
    {
        if (isTest())
        {
            return Collections.emptyList();
        }
        else
        {
            checkState(TestSuite.class.isInstance(test), "Unknown JUnit3 suite: " + test);
            return transform(getTests(), TRANSFORMER);
        }
    }

    private Iterable<Test> getTests()
    {
        return ImmutableList.copyOf(Iterators.forEnumeration(((TestSuite) test).tests()));
    }

    @Override
    public String name()
    {
        return TestCaseKit.getFullName(test);
    }

    @Override
    public String className()
    {
        return test.getClass().getName();
    }

    @Override
    public String methodName()
    {
        if (test instanceof TestCase)
        {
            return ((TestCase)test).getName();
        }
        else
        {
            return null;
        }
    }

    @Override
    public Class<?> testClass()
    {
        return test.getClass();
    }

    @Override
    public Iterable<Annotation> annotations()
    {
        // TODO this should also look at method! (implement when we need it)
        return asList(testClass().getAnnotations());
    }

    @Override
    public Set<Category> categories()
    {
        WebTest webTest = testClass().getAnnotation(WebTest.class);
        return Category.fromAnnotation(webTest);
    }

    @Override
    public boolean isTest()
    {
        return test instanceof TestCase;
    }

    @Override
    public boolean isSuite()
    {
        return !isTest();
    }

    @Override
    public int testCount()
    {
        return test.countTestCases();
    }

    @Override
    public Iterable<WebTestDescription> children()
    {
        return ImmutableList.copyOf(children);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!JUnit3WebTestDescription.class.isInstance(obj))
        {
            return false;
        }
        JUnit3WebTestDescription that = (JUnit3WebTestDescription) obj;
        return this.test.equals(that.test);
    }

    @Override
    public int hashCode()
    {
        return test.hashCode();
    }
}
