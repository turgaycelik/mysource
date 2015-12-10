package com.atlassian.jira.functest.framework.page;

import java.io.IOException;

import com.meterware.httpunit.WebResponse;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.functest.framework.util.ResponseMatchers.responseCodeIs;
import static com.atlassian.jira.functest.framework.util.ResponseMatchers.responseContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Extracts logic from 404 page
 *
 * @since v6.1
 */
public class Error404
{
    private final WebTester webTester;

    public Error404(final WebTester webTester) {this.webTester = webTester;}

    public Error404 visit(String url)
    {
        webTester.beginAt(url);
        return this;
    }

    static final Matcher<WebResponse> responseMatcher = Matchers.allOf(
            responseCodeIs(404),
            responseContains("Oops, you&#39;ve found a dead link.")
    );

    public static Matcher<Error404> isOn404Page()
    {
        return new TypeSafeMatcher<Error404>()
        {
            @Override
            protected boolean matchesSafely(final Error404 item)
            {
                return responseMatcher.matches(item.webTester.getDialog().getResponse());
            }

            @Override
            public void describeTo(final Description description)
            {
                responseMatcher.describeTo(description);
            }

        };
    }




}
