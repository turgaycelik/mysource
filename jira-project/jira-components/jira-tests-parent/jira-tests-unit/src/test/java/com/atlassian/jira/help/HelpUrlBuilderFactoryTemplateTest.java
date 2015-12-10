package com.atlassian.jira.help;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.MockBuildUtilsInfo;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.4
 */
public class HelpUrlBuilderFactoryTemplateTest
{
    private final BuildUtilsInfo info = new MockBuildUtilsInfo().setDocVersion("567");

    @Test
    public void getCreatesBuilderWithRightPrefixAndSuffix()
    {
        final FactoryForTest factory = new FactoryForTest(info);
        final MockHelpUrlBuilder builder = (MockHelpUrlBuilder)factory.get("prefix", ".suffix");

        assertThat(builder.prefix(), equalTo("prefix"));
        assertThat(builder.suffix(), equalTo(".suffix"));
    }

    @Test
    public void getSubstitutesIntoPrefix()
    {
        final FactoryForTest factory = new FactoryForTest(info);
        MockHelpUrlBuilder builder = (MockHelpUrlBuilder)factory.get("http://localhost/${doc.version}", ".html");

        assertThat(builder.prefix(), equalTo("http://localhost/" + info.getDocVersion()));
        assertThat(builder.suffix(), equalTo(".html"));

        builder = (MockHelpUrlBuilder)factory.get("https://localhost/${docs.version}/jira", ".html");

        assertThat(builder.prefix(), equalTo("https://localhost/" + info.getDocVersion() + "/jira"));
        assertThat(builder.suffix(), equalTo(".html"));
    }

    private static class FactoryForTest extends HelpUrlBuilderFactoryTemplate
    {
        FactoryForTest(final BuildUtilsInfo buildNumbers)
        {
            super(buildNumbers);
        }

        @Override
        HelpUrlBuilder newUrlBuilder(final String prefix, final String suffix)
        {
            return new MockHelpUrlBuilder(prefix, suffix);
        }
    }
}
