package com.atlassian.jira.functest.config;

import com.atlassian.jira.webtests.util.TestClassUtils;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Kill JUnit4!
 *
 * @since v4.3
 */
public class JUnit4Suppressor
{
    private static final Logger log = Logger.getLogger(JUnit4Suppressor.class);


    private final List<Class<? extends TestCase>> testsInSuite;
    private List<Violation> violations;

    public JUnit4Suppressor(Collection<Class<? extends TestCase>> testsInSuite)
    {
        this.testsInSuite = new ArrayList<Class<? extends TestCase>>(testsInSuite);
    }

    public void killJUnit4()
    {
        violations = new ArrayList<Violation>();
        for (Class<? extends TestCase> testClass : testsInSuite)
        {
            checkNoJUnit4ConstructsInTest(testClass);
        }
        if (!violations.isEmpty())
        {
            fail(message());
        }
    }

    private void checkNoJUnit4ConstructsInTest(Class<? extends TestCase> testClass)
    {
        log.debug("Verifying class " + testClass.getName());
        checkNoJUnit4Constructs(testClass);
        for (Method testMethod : TestClassUtils.getTestMethods(testClass))
        {
            log.debug("    Veryfing method " + testClass.getSimpleName() + "." + testMethod.getName());
            checkNoJUnit4Constructs(testMethod);
        }
    }

    private void checkNoJUnit4Constructs(AnnotatedElement annotated)
    {
        checkViolation(annotated, org.junit.Test.class);
        checkViolation(annotated, org.junit.Before.class);
        checkViolation(annotated, org.junit.After.class);
        // TODO more?
    }

    private void checkViolation(AnnotatedElement offender, Class<? extends Annotation> offence)
    {
        if (offender.getAnnotation(offence) != null)
        {
            log.debug("FAIL");
            violations.add(new Violation(offender, offence));
        }
    }

    private String message()
    {
        StringBuilder builder = new StringBuilder(violations.size() * 20).append("Found following JUnit4 constructs:\n");
        for (Violation violation : violations)
        {
            builder.append(" -- ").append(violation).append('\n');
        }
        return builder.toString();
    }

    private static class Violation
    {
        private final AnnotatedElement offender;
        private final Class<? extends Annotation> offence;

        public Violation(AnnotatedElement offender, Class<? extends Annotation> offence)
        {
            this.offender = offender;
            this.offence = offence;
        }

        @Override
        public String toString()
        {
            return "Test <" + offender + "> is annotated with " + offence.getName();
        }
    }
}
