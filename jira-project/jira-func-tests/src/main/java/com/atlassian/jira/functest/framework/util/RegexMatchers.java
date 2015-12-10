package com.atlassian.jira.functest.framework.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Set of convenient matchers for checking regular expressions
 *
 * @since v6.1
 */
public class RegexMatchers
{
    public static RegexMatches regexMatches(String regex)
    {
        return new RegexMatches(regex);
    }

    public static RegexMatchesNot regexMatchesNot(String regex)
    {
        return new RegexMatchesNot(regex);
    }

    public static RegexMatchesPattern regexMatchesPattern(Pattern pattern)
    {
        return new RegexMatchesPattern(pattern);
    }

    public static RegexMatchesPatternNot regexMatchesPatternNot(Pattern pattern)
    {
        return new RegexMatchesPatternNot(pattern);
    }

    public static class RegexMatches extends TypeSafeMatcher<String>
    {
        private final String regex;

        public RegexMatches(final String regex)
        {
            this.regex = regex;
        }

        @Override
        protected boolean matchesSafely(final String text)
        {
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher match = pattern.matcher(text);

            return match.find();
        }

        @Override
        public void describeTo(final Description description)
        {
            description
                    .appendText("Text should match regular expression ")
                    .appendValue(regex);
        }
    }

    public static class RegexMatchesNot extends TypeSafeMatcher<String>
    {
        private final String regex;

        public RegexMatchesNot(final String regex)
        {
            this.regex = regex;
        }

        @Override
        protected boolean matchesSafely(final String text)
        {
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher match = pattern.matcher(text);

            return !match.find();
        }

        @Override
        public void describeTo(final Description description)
        {
            description
                    .appendText("Text should not match regular expression ")
                    .appendValue(regex);
        }
    }

    public static class RegexMatchesPattern extends TypeSafeMatcher<String>
    {
        private final Pattern pattern;

        public RegexMatchesPattern(final Pattern pattern)
        {
            this.pattern = pattern;
        }

        @Override
        protected boolean matchesSafely(final String text)
        {
            Matcher match = pattern.matcher(text);

            return pattern.matcher(text).find();
        }

        @Override
        public void describeTo(final Description description)
        {
            description
                    .appendText("Text should match regular expression ")
                    .appendValue(pattern.pattern());
        }
    }

    public static class RegexMatchesPatternNot extends TypeSafeMatcher<String>
    {
        private final Pattern pattern;

        public RegexMatchesPatternNot(final Pattern pattern)
        {
            this.pattern = pattern;
        }

        @Override
        protected boolean matchesSafely(final String text)
        {
            Matcher match = pattern.matcher(text);

            return !pattern.matcher(text).find();
        }

        @Override
        public void describeTo(final Description description)
        {
            description
                    .appendText("Text should not match regular expression ")
                    .appendValue(pattern.pattern());
        }
    }
}
