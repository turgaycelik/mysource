package com.atlassian.jira.help;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @since v6.2.4
 */
public class HelpUrlBuilderTemplateTest
{
    @Test
    public void ableToCreateSimpleUrl()
    {
        HelpUrlBuilder builder = createBuilder().key("key").alt("alt").title("title").url("url");
        final HelpUrlMatcher matcher = new HelpUrlMatcher().url("url").key("key").alt("alt").title("title");

        assertThat(builder.build(), matcher);
        assertThat(builder.build(), matcher);

        builder.title("newtitle");
        matcher.title("newtitle");
        assertThat(builder.build(), matcher);

        builder.alt("newalt");
        matcher.alt("newalt");
        assertThat(builder.build(), matcher);

        builder.key("newKey");
        matcher.key("newKey");
        assertThat(builder.build(), matcher);

        builder.url("newUrl");
        matcher.url("newUrl");
        assertThat(builder.build(), matcher);

        builder.local(true);
        matcher.local(true);
        assertThat(builder.build(), matcher);
    }

    @Test
    public void ableToCreateUrlWithPrefixAndSuffix()
    {
        HelpUrlBuilder builder = createBuilder("prefix", "suffix").key("key").url("url");
        final HelpUrlMatcher matcher = new HelpUrlMatcher().key("key").url("prefixurlsuffix");

        assertThat(builder.build(), matcher);

        builder = createBuilder("    ", "suffix").key("key").url("url");
        matcher.url("    urlsuffix");

        assertThat(builder.build(), matcher);

        builder = createBuilder("start", null).key("key").url("url");
        matcher.url("starturl");

        assertThat(builder.build(), matcher);
    }

    @Test
    public void ableToCreateUrlWithAnchors()
    {
        HelpUrlBuilder builder = createBuilder("prefix", "suffix").key("key").url("url#anchor");
        final HelpUrlMatcher matcher = new HelpUrlMatcher().key("key").url("prefixurlsuffix#anchor");
        assertThat(builder.build(), matcher);

        builder.url("url#");
        matcher.url("prefixurlsuffix#");
        assertThat(builder.build(), matcher);

        builder = createBuilder("prefix", null).key("key").url("url#anchor");
        matcher.url("prefixurl#anchor");
        assertThat(builder.build(), matcher);

        builder = createBuilder("prefix", null).key("key").url("empty#");
        matcher.url("prefixempty#");
        assertThat(builder.build(), matcher);

        builder = createBuilder("prefix", "suffix").key("key").url("#");
        matcher.url("prefixsuffix#");
        assertThat(builder.build(), matcher);

        builder = createBuilder().key("key").url("#ignoreme");
        matcher.url("#ignoreme");
        assertThat(builder.build(), matcher);
    }

    @Test
    public void ableToCreateUrlWithExtraParameters()
    {
        final TestBuilder builder = createBuilder("prefix", null);
        builder.key("key").url("url#anchor");
        builder.addParameter("what", "string");

        final HelpUrlMatcher matcher = new HelpUrlMatcher().key("key").url("prefixurl?what=string#anchor");
        assertThat(builder.build(), matcher);

        builder.url("url?a=b");
        matcher.url("prefixurl?a=b&what=string");
        assertThat(builder.build(), matcher);

        builder.addParameter("what2", "string2");
        matcher.url("prefixurl?a=b&what=string&what2=string2");
        assertThat(builder.build(), matcher);
    }

    @Test
    public void ableToCreateAbsoluteUrls()
    {
        final TestBuilder builder = createBuilder("prefix", null);
        builder.key("key").url("https://atlassian.com");
        builder.addParameter("what", "string");

        final HelpUrlMatcher matcher = new HelpUrlMatcher().key("key").url("https://atlassian.com");
        assertThat(builder.build(), matcher);

        builder.url("httP://atlassian.com");
        matcher.url("httP://atlassian.com");
        assertThat(builder.build(), matcher);
    }

    //HIROL-62
    @Test
    public void ableToCreateUrlsWithBlankParameters()
    {
        List<String> emptys = Lists.newArrayList(null, "", "   ", "\t\r\n");
        for (String prefix : emptys)
        {
            for (String suffix : emptys)
            {
                final HelpUrlBuilder builder = createBuilder(prefix, suffix).key("key");
                HelpUrlMatcher matcher = new HelpUrlMatcher().key("key");

                if (prefix == null && suffix == null)
                {
                    assertThat(builder.build(), matcher);
                }
                else
                {
                    StringBuilder expectedUrl = new StringBuilder();
                    if (prefix != null)
                    {
                        expectedUrl.append(prefix);
                    }
                    if (suffix != null)
                    {
                        expectedUrl.append(suffix);
                    }
                    assertThat(builder.build(), matcher.url(expectedUrl.toString()));
                }
            }
        }
    }

    @Test
    public void ableToCopyBuilderClonesBuilderAndIsIndependent()
    {
        HelpUrlBuilder builder = createBuilder().key("key").alt("alt").title("title").url("url");
        final HelpUrlMatcher matcher = new HelpUrlMatcher().url("url").key("key").alt("alt").title("title");

        final HelpUrlBuilder builderCopy = builder.copy();
        final HelpUrlMatcher matcherCopy = matcher.copy();

        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcher);

        builder.title("newtitle");
        matcher.title("newtitle");
        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcherCopy);

        builder.alt("newalt");
        matcher.alt("newalt");
        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcherCopy);

        builder.key("newKey");
        matcher.key("newKey");
        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcherCopy);

        builder.url("newUrl");
        matcher.url("newUrl");
        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcherCopy);

        builder.local(true);
        matcher.local(true);
        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcherCopy);

        builderCopy.url("url2").title("title2").local(false);
        matcherCopy.url("url2").title("title2").local(false);

        assertThat(builder.build(), matcher);
        assertThat(builderCopy.build(), matcherCopy);
    }

    @Test(expected = IllegalStateException.class)
    public void notAbleToBuildWithKey()
    {
        createBuilder().key(null).url("url").build();
    }

    @Test
    public void blanksInterpretedAsBlanks()
    {
        HelpUrlBuilder builder = createBuilder().key("key");
        HelpUrlMatcher matcher = new HelpUrlMatcher().key("key");

        assertThat(builder.build(), matcher);

        List<String> emptys = Lists.newArrayList(null, "", "   ", "\t\r\n");

        for (String empty : emptys)
        {
            builder.title(empty);
            matcher.title(empty);
            assertThat(builder.build(), matcher);
        }

        for (String empty : emptys)
        {
            builder.alt(empty);
            matcher.alt(empty);
            assertThat(builder.build(), matcher);
        }

        for (String empty : emptys)
        {
            builder.url(empty);
            matcher.url(empty);
            assertThat(builder.build(), matcher);
        }
    }

    private TestBuilder createBuilder()
    {
        return createBuilder(null, null);
    }

    private TestBuilder createBuilder(String prefix, String suffix)
    {
        return new TestBuilder(prefix, suffix);
    }

    private static class TestBuilder extends HelpUrlBuilderTemplate
    {
        private Map<String, String> parameters = Maps.newLinkedHashMap();

        private TestBuilder(String prefix, String suffix)
        {
            this(prefix, suffix, Collections.<String, String>emptyMap());
        }

        private TestBuilder(String prefix, String suffix,  Map<String, String> parameters)
        {
            super(prefix, suffix);
            this.parameters.putAll(parameters);
        }

        private TestBuilder addParameter(String key, String value)
        {
            parameters.put(key, value);
            return this;
        }

        @Nonnull
        @Override
        Map<String, String> getExtraParameters()
        {
            return parameters;
        }

        @Override
        HelpUrlBuilder newInstance()
        {
            return new TestBuilder(getPrefix(), getSuffix(), parameters);
        }
    }
}
