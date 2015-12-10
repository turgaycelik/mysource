package com.atlassian.jira.functest.config;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.junit.Assert.fail;

/**
 * Asserts that there are not tests in the suite with missing reason for @Ignore.
 *
 * @since v4.3
 */
public final class BlankIgnoresFinder
{
    private static final Logger log = Logger.getLogger(BlankIgnoresFinder.class);

    private final String suiteName;
    private final Iterable<Class<? extends TestCase>> testsInSuite;
    private final AnnotationFinder<Ignore> annotationFinder;

    public BlankIgnoresFinder(String suiteName, Iterable<Class<? extends TestCase>> testsInSuite)
    {
        this.suiteName = notNull("suiteName", suiteName);
        this.testsInSuite = notNull("testsInSuite", testsInSuite);
        this.annotationFinder = AnnotationFinder.newFinder(allTests(), Ignore.class);
    }

    public void assertNoIgnoresWithoutReason()
    {
        List<String> withReason = new ArrayList<String>();
        List<String> blankReasons = new ArrayList<String>();
        for (Map.Entry<AnnotatedElement,Ignore> ignoredTest : annotationFinder.findAll().entrySet())
        {
            if (StringUtils.isBlank(ignoredTest.getValue().value()))
            {
                blankReasons.add(ignoredTest.getKey().toString());
            }
            else
            {
                withReason.add(ignoredTest.getKey().toString() + " REASON: " + ignoredTest.getValue().value());
            }
        }
        log(withReason);
        if (!blankReasons.isEmpty())
        {
            fail(errorMsg(blankReasons));
        }
    }

    private void log(List<String> withReasons)
    {
        log.info("** Ignored tests in suite '" + suiteName + "':");
        for (String withReason : withReasons)
        {
            log.info(withReason);
        }
    }

    private String errorMsg(List<String> blankReasons)
    {
        StringBuilder answer = new StringBuilder("No reasons for @Ignore:\n");
        for (String blankReason : blankReasons)
        {
            answer.append(blankReason).append("\n");
        }
        return answer.toString();
    }

    @SuppressWarnings ({ "unchecked" })
    private Set<Class<?>> allTests()
    {
        return (Set) testsInSuite;
    }



}
