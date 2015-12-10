package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;
import junit.framework.Test;
import junit.framework.TestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some utilities for working with test names.
 *
 * @since v4.2
 */
final class TestNameUtils
{
    /**
     * Regular expression used to split CamelCase into individual words. Not 100% correct but good enough. It looks for
     * the change between lowercase and UPPERCASE (?<=\p{javaLowerCase})(?=[\p{javaUpperCase}\p{Digit}]) or an
     * underscore or a dollar sign ([_$])
     */
    private static final Pattern SPLIT = Pattern.compile("(?:(?<=\\p{javaLowerCase})(?=[\\p{javaUpperCase}\\p{Digit}]))|[_$]", Pattern.UNICODE_CASE);

    private TestNameUtils()
    {
    }

    static String getSuiteName(Test test)
    {
        return afterLastDot(test.getClass().getName()).trim();
    }

    static String getSuiteName(WebTestDescription test)
    {
        return afterLastDot(test.className()).trim();
    }

    static String getTestName(Test test)
    {
        String name;
        if (test instanceof TestCase)
        {
            name = ((TestCase) test).getName();
            name = afterLastDot(name);
        }
        else
        {
            name = test.toString();
        }
        return name.trim();
    }

    static String getTestName(WebTestDescription test)
    {
        return test.methodName();
    }

    static String getSubtitleForTest(Test test)
    {
        final String suiteName = makeSubtitlePart(getSuiteName(test));
        final String testName = makeSubtitlePart(getTestName(test));

        return suiteName + " - " + testName;
    }

    static String getSubtitleForTest(WebTestDescription test)
    {
        final String suiteName = makeSubtitlePart(getSuiteName(test));
        final String testName = makeSubtitlePart(getTestName(test));

        return suiteName + " - " + testName;
    }

    private static String makeSubtitlePart(String part)
    {
        return replaceCamelName(removePrefixIgnoreCase(part, "TEST"), " ");
    }

    private static String removePrefixIgnoreCase(final String name, final String prefix)
    {
        if (prefix.length() <= name.length())
        {
            final String actualPrefix = name.substring(0, prefix.length());
            if (actualPrefix.equalsIgnoreCase(prefix))
            {
                return name.substring(prefix.length());
            }
        }
        return name;
    }

    private static String replaceCamelName(String name, final String replacement)
    {
        return SPLIT.matcher(name).replaceAll(Matcher.quoteReplacement(replacement));
    }

    private static String afterLastDot(String name)
    {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0)
        {
            name = name.substring(lastDot + 1);
        }
        return name;
    }
}
