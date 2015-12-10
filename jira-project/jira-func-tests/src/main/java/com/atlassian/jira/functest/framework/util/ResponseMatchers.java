package com.atlassian.jira.functest.framework.util;

import java.io.IOException;

import com.meterware.httpunit.WebResponse;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Set of convenient matchers for checking response content
 *
 * @since v6.1
 */
public class ResponseMatchers
{
    public static ResponseContainsString responseContains(String string)
    {
        return new ResponseContainsString(string);
    }

    public static ResponseCodeIs responseCodeIs(int code)
    {
        return new ResponseCodeIs(code);
    }


    public static class ResponseContainsString extends TypeSafeMatcher<WebResponse>
    {

        private final String containedString;

        public ResponseContainsString(final String containedString) {this.containedString = containedString;}

        @Override
        protected boolean matchesSafely(final WebResponse item)
        {
            try
            {
                return item.getText().contains(containedString);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("WebResponse text should contain ").appendValue(containedString);
        }
    }

    public static class ResponseCodeIs extends TypeSafeMatcher<WebResponse>
    {

        private final int expectedResponseCode;

        public ResponseCodeIs(final int expectedResponseCode) {this.expectedResponseCode = expectedResponseCode;}


        @Override
        protected boolean matchesSafely(final WebResponse item)
        {
            return item.getResponseCode() == expectedResponseCode;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("WebResponse code should be ").appendValue(expectedResponseCode);
        }
    }

}
