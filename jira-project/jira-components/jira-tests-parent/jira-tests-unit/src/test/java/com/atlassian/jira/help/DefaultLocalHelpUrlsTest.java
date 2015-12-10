package com.atlassian.jira.help;

import com.atlassian.core.util.PropertyUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.4
 */
public class DefaultLocalHelpUrlsTest
{
    public static final String GOOD_RESOURCE = "com/atlassian/jira/help/internal-test.properties";

    @Test
    public void loadNoResource()
    {
        DefaultLocalHelpUrls loader = new DefaultLocalHelpUrls("bad.properties");
        final Iterable<HelpUrl> load = loader.load();
        assertThat(Iterables.isEmpty(load), equalTo(true));
    }

    @Test
    public void parseNoProperties()
    {
        DefaultLocalHelpUrls loader = new DefaultLocalHelpUrls(GOOD_RESOURCE);
        final Iterable<HelpUrl> load = loader.parse(new Properties());
        assertThat(Iterables.isEmpty(load), equalTo(true));
    }

    @Test
    public void loadReadsFromPropertiesFile()
    {
        DefaultLocalHelpUrls loader = new DefaultLocalHelpUrls(GOOD_RESOURCE);
        assertGoodResources(loader.load());
    }

    @Test
    public void parseParsesInternalUrlsCorrectly()
    {
        DefaultLocalHelpUrls loader = new DefaultLocalHelpUrls("bad.resource.banna.coffee.texst.qwerty");
        loader.parse(loadResource(GOOD_RESOURCE));
        assertGoodResources(loader.parse(loadResource(GOOD_RESOURCE)));
    }

    private void assertGoodResources(final Iterable<HelpUrl> load)
    {
        List<Matcher<? super HelpUrl>> matchers = Lists.newArrayList();
        matchers.add(new HelpUrlMatcher().key("good").url("good").title("title").local(true));
        matchers.add(new HelpUrlMatcher().key("good.spaces").url("good").title("title").local(true));
        assertThat(load, containsInAnyOrder(matchers));
    }

    private Properties loadResource(String resource)
    {
        return PropertyUtils.getProperties(resource, this.getClass());
    }
}
