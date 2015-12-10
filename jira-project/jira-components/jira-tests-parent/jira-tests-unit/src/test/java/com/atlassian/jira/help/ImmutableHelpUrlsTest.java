package com.atlassian.jira.help;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * @since v6.2.4
 */
public class ImmutableHelpUrlsTest
{
    @Test
    public void getUrlReturnsMappedUrls()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");
        HelpUrl two = MockHelpUrl.simpleUrl("two");

        HelpUrls urls = urls(defaultUrl, one, two);
        assertThat(urls.getUrl(defaultUrl.getKey()), equalTo(defaultUrl));
        assertThat(urls.getUrl(one.getKey()), equalTo(one));
        assertThat(urls.getUrl(two.getKey()), equalTo(two));
    }

    @Test
    public void getUrlReturnsDefaultUrlForUnmapped()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");

        HelpUrls urls = urls(defaultUrl, one);
        assertThat(urls.getUrl(defaultUrl.getKey()), equalTo(defaultUrl));
        assertThat(urls.getUrl(one.getKey()), equalTo(one));
        assertThat(urls.getUrl("two"), equalTo(defaultUrl));
    }

    @Test
    public void getDefaultReturnsDefault()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");

        HelpUrls urls = urls(defaultUrl, one);
        assertThat(urls.getDefaultUrl(), equalTo(defaultUrl));
    }

    @Test
    public void getKeysReturnKeys()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");

        HelpUrls urls = urls(defaultUrl, one);
        assertThat(urls.getUrlKeys(), containsInAnyOrder(one.getKey(), defaultUrl.getKey()));
    }

    @Test
    public void getKeysIsImmutable()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");

        HelpUrls urls = urls(defaultUrl, one);
        Set<String> urlKeys = urls.getUrlKeys();
        try
        {
            urlKeys.remove(one.getKey());
            fail("Should not be able to remove key.");
        }
        catch (RuntimeException expected)
        {
            //good
        }
        assertThat(urlKeys, containsInAnyOrder(one.getKey(), defaultUrl.getKey()));
    }

    @Test
    public void putsDefaultOverwritesEntryWithSameKey()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");
        HelpUrl two = MockHelpUrl.simpleUrl("default").setTitle("NonDefault");

        HelpUrls urls = urls(defaultUrl, one, two);
        assertThat(urls.getDefaultUrl(), equalTo(defaultUrl));
        assertThat(urls.getUrl(defaultUrl.getKey()), equalTo(defaultUrl));
        assertThat(urls.getUrl(one.getKey()), equalTo(one));
    }

    @Test
    public void getUrlsReturnsAllUrls()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");
        HelpUrl two = MockHelpUrl.simpleUrl("two");

        HelpUrls urls = urls(defaultUrl, one, two);
        assertThat(urls, containsInAnyOrder(defaultUrl, one, two));
    }

    @Test
    public void getUrlsReturnsAllUrlsWithDefaultTakingPrecedence()
    {
        HelpUrl defaultUrl = MockHelpUrl.simpleUrl("default");
        HelpUrl one = MockHelpUrl.simpleUrl("one");
        HelpUrl two = MockHelpUrl.simpleUrl("two");
        MockHelpUrl fakeDefault = MockHelpUrl.simpleUrl("default").setTitle("Some");

        HelpUrls urls = urls(defaultUrl, one, two, fakeDefault);
        assertThat(urls, containsInAnyOrder(defaultUrl, one, two));
    }

    private HelpUrls urls(HelpUrl defaultUrl, HelpUrl... urls)
    {
        return new ImmutableHelpUrls(defaultUrl, Arrays.asList(urls));
    }

    private Map<String, HelpUrl> asMap(HelpUrl defaultUrl, HelpUrl... urls)
    {
        Map<String, HelpUrl> result = Maps.newHashMap();
        for (HelpUrl url : urls)
        {
            result.put(url.getKey(), url);
        }
        result.put(defaultUrl.getKey(), defaultUrl);
        return result;
    }

}
