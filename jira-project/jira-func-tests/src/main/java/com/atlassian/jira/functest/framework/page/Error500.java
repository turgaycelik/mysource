package com.atlassian.jira.functest.framework.page;

import com.meterware.httpunit.WebResponse;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.functest.framework.util.ResponseMatchers.responseCodeIs;
import static com.atlassian.jira.functest.framework.util.ResponseMatchers.responseContains;

/**
 *
 * @since v6.1
 */
public class Error500
{
    private final WebTester webTester;

    public Error500(final WebTester webTester) {this.webTester = webTester;}

    public Error500 visit(String url){
        webTester.beginAt(url);
        return this;
    }

    static final Matcher<WebResponse> short500PageMatcher = Matchers.allOf(
            responseCodeIs(500),
            responseContains("Sorry, we had some technical problems during your last operation."),
            responseContains("Copy the content below and send it to your JIRA Administrator")
    );

    public static Matcher<Error500> isShort500Page()
    {
        return new TypeSafeMatcher<Error500>()
        {
            @Override
            protected boolean matchesSafely(final Error500 item)
            {
                return short500PageMatcher.matches(item.webTester.getDialog().getResponse());
            }

            @Override
            public void describeTo(final Description description)
            {
                short500PageMatcher.describeTo(description);
            }
        };
    }
}
