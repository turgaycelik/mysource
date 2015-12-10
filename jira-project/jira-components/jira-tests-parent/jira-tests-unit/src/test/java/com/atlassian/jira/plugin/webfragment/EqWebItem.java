package com.atlassian.jira.plugin.webfragment;

import com.atlassian.plugin.web.api.WebItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Deep matcher for {@link com.atlassian.plugin.web.api.WebItem}.
 */
public class EqWebItem extends TypeSafeMatcher<WebItem>
{
    private final WebItem expected;

    public EqWebItem(final WebItem expected)
    {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(WebItem item)
    {
        return StringUtils.equals(expected.getId(), item.getId()) &&
                StringUtils.equals(expected.getAccessKey(), item.getAccessKey()) &&
                StringUtils.equals(expected.getSection(), item.getSection()) &&
                StringUtils.equals(expected.getUrl(), item.getUrl()) &&
                StringUtils.equals(expected.getLabel(), item.getLabel()) &&
                StringUtils.equals(expected.getStyleClass(), item.getStyleClass()) &&
                StringUtils.equals(expected.getTitle(), item.getTitle()) &&
                expected.getWeight() == item.getWeight() &&
                expected.getParams().equals(item.getParams());
    }

    public void describeTo(Description description)
    {
        description.appendText("did not match " + ToStringBuilder.reflectionToString(expected, ToStringStyle.SHORT_PREFIX_STYLE));
    }

    @Factory
    public static Matcher<WebItem> eqWebItem(final WebItem expected)
    {
        return new EqWebItem(expected);
    }
}
