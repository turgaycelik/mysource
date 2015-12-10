package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.WebTestDescription;
import com.google.common.base.Function;
import org.junit.runner.Description;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Set;

import static com.atlassian.jira.functest.framework.suite.Category.fromAnnotation;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

/**
 * {@link com.atlassian.jira.functest.framework.WebTestDescription} based on the JUnit4
 * {@link org.junit.runner.Description}.
 *
 * @since v4.4
 */
public class JUnit4WebTestDescription implements WebTestDescription
{
    public static Function<Description,WebTestDescription> TRANSFORMER = new Function<Description,WebTestDescription>()
    {
        @Override
        public WebTestDescription apply(@Nullable Description input)
        {
            return new JUnit4WebTestDescription(input);
        }
    };

    private final Description description;
    private final Iterable<WebTestDescription> children;

    public JUnit4WebTestDescription(Description description)
    {
        this.description = notNull(description);
        this.children = initChildren();
    }


    private Iterable<WebTestDescription> initChildren()
    {
        return transform(description.getChildren(), TRANSFORMER);
    }

    @Override
    public String name()
    {
        return description.getDisplayName();
    }

    @Override
    public String className()
    {
        return description.getClassName();
    }

    @Override
    public String methodName()
    {
        return description.getMethodName();
    }

    @Override
    public Class<?> testClass()
    {
        return description.getTestClass();
    }

    @Override
    public Iterable<Annotation> annotations()
    {
        return asList(testClass().getAnnotations());
    }

    @Override
    public Set<Category> categories()
    {
        return fromAnnotation(testClass().getAnnotation(WebTest.class));
    }

    @Override
    public boolean isTest()
    {
        return description.isTest();
    }

    @Override
    public boolean isSuite()
    {
        return description.isSuite();
    }

    @Override
    public int testCount()
    {
        return description.testCount();
    }

    @Override
    public Iterable<WebTestDescription> children()
    {
        return children;
    }

    @Override
    public String toString()
    {
        return description.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!JUnit4WebTestDescription.class.isInstance(obj))
        {
            return false;
        }
        JUnit4WebTestDescription that = (JUnit4WebTestDescription) obj;
        return this.description.equals(that.description);
    }

    @Override
    public int hashCode()
    {
        return description.hashCode();
    }
}
