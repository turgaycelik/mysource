package com.atlassian.jira.help;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.4
 */
public class DefaultHelpUrlsParserTest
{
    private static final String DEFAULT_KEY = "default";

    private final MockLocalHelpUrls localUrls = new MockLocalHelpUrls();
    private final HelpUrlsParser parser = new DefaultHelpUrlsParser(MockHelpUrlBuilder.factory(), localUrls,
            Suppliers.ofInstance(false), "", "");

    @Test
    public void parsesExternalUrlsCorrectly()
    {
        HelpUrlSerializer helpUrlSerializer = new HelpUrlSerializer();
        helpUrlSerializer.prefix("http://test.com/").suffix(".html");

        HelpUrl defaultUrl = helpUrlSerializer.setDefaultUrl("Help+Text", "General Help", "Alt");

        List<HelpUrl> urls = Lists.newArrayList();
        //Simple URL with everything configured.
        urls.add(helpUrlSerializer.addHelpUrl("workflow", "Workflows", "Workflow Help", "Workflow Alt"));

        //Simple URL with Alt from default
        urls.add(helpUrlSerializer.addHelpUrl("issuetype", "IssueType", "Issue Type Title", null));

        //Simple URL with Title from default
        urls.add(helpUrlSerializer.addHelpUrl("issueTypeScheme", "IssueType+Scheme", null, "Issue Type Scheme Alt"));

        //Has "#" within link.
        urls.add(helpUrlSerializer.addHelpUrl("screens", "Screens#something", "Screens Title", "Screens Alt"));

        //Has absolute URL.
        urls.add(helpUrlSerializer.addHelpUrl("outside", "http://outside.com", "", ""));

        //Has absolute SSL URL.
        urls.add(helpUrlSerializer.addHelpUrl("outsideSsl", "https://outside.com", "Outside Secure", null));

        //Ignore this OD URL.
        helpUrlSerializer.addHelpUrlOd("outsideSsl", "OnDemand", "Ondemand Ignored", "Really, I am meant to be ignored.");

        //With "" as key.
        urls.add(helpUrlSerializer.addHelpUrl("", "", null, " "));

        assertParser(helpUrlSerializer, defaultUrl, urls, parser);
    }

    @Test
    public void parsesExternalUrlsCorrectlyOd()
    {
        HelpUrlSerializer helpUrlSerializer = new HelpUrlSerializer();
        helpUrlSerializer.prefix("http://test.com/").suffix(".html").odPrefix("http://test.od.com");

        HelpUrl defaultUrl = helpUrlSerializer.setDefaultUrl("Help+Text", "General Help", "Alt");

        List<HelpUrl> urls = Lists.newArrayList();
        //Simple URL with everything configured.
        urls.add(helpUrlSerializer.addHelpUrlOd("workflow", "Workflows", "Workflow Help", "Workflow Alt"));

        //Simple URL with Alt from default
        urls.add(helpUrlSerializer.addHelpUrlOd("issuetype", "IssueType", "Issue Type Title", null));

        //Simple URL with Title from default
        urls.add(helpUrlSerializer.addHelpUrl("issueTypeScheme", "IssueType+Scheme", null, "Issue Type Scheme Alt"));

        //Has "#" within link.
        urls.add(helpUrlSerializer.addHelpUrlOd("screens", "Screens#something", "Screens Title", "Screens Alt"));

        //Has absolute URL.
        urls.add(helpUrlSerializer.addHelpUrl("outside", "http://outside.com", "", ""));

        //Has absolute SSL URL for OD and BTF, but will use the OD one.
        urls.add(helpUrlSerializer.addHelpUrlOd("outsideSsl", "https://od.outside.com", "AOD Outside Secure", null));
        helpUrlSerializer.addHelpUrl("outsideSsl", "https://outside.com", "Outside Secure", null);

        //With "" as key.
        urls.add(helpUrlSerializer.addHelpUrl("", "", null, "  "));

        HelpUrlsParser parser = this.parser.onDemand(true);
        assertParser(helpUrlSerializer, defaultUrl, urls, parser);
    }

    @Test
    public void parsesDefaultUrlCorrectly()
    {
        HelpUrlSerializer helpUrlSerializer = new HelpUrlSerializer();
        helpUrlSerializer.odPrefix("ignore").prefix("http://test.com/").suffix(".html");

        //Check data with no default in properties.
        HelpUrlsParser parser = this.parser.defaultUrl("url", "title");
        List<HelpUrl> objects = Collections.emptyList();
        MockHelpUrl defaultUrl = new MockHelpUrl().setKey(DEFAULT_KEY).setTitle("title").setUrl("http://test.com/url.html");
        assertParser(helpUrlSerializer, defaultUrl, objects, parser);

        //Get URL from combination of properties and defaults in parser.
        helpUrlSerializer.setDefaultUrl("url2", null, null);
        defaultUrl.setUrl("http://test.com/url2.html").setTitle("title");
        assertParser(helpUrlSerializer, defaultUrl, objects, parser);

        //Get complete default URL from properties.
        helpUrlSerializer.odPrefix("https://od.test.com/").setDefaultUrlOd("url3", "title2", "alt");
        defaultUrl.setUrl("https://od.test.com/url3.html").setTitle("title2").setAlt("alt");
        assertParser(helpUrlSerializer, defaultUrl, objects, parser.onDemand(true));
    }

    @Test
    public void parsesInternalAndExternalUrlsCorrectly()
    {
        HelpUrlSerializer helpUrlSerializer = new HelpUrlSerializer();
        helpUrlSerializer.prefix("http://test.com/").suffix(".html");

        HelpUrl defaultUrl = helpUrlSerializer.setDefaultUrl("Help+Text", "General Help", "Alt");
        List<HelpUrl> expected = Lists.newArrayList();

        //Simple URL with everything configured.
        expected.add(helpUrlSerializer.addHelpUrl("workflow", "Workflows", "Workflow Help", "Workflow Alt"));

        //This URL is not used because we overwrite it with an just above.
        localUrls.add(new MockHelpUrl().setKey("workflow").setTitle("WTF?").setLocal(true));

        MockHelpUrl local = new MockHelpUrl().setKey("local").setLocal(true);
        localUrls.add(local);
        expected.add(local);

        HelpUrls parse = parser.parse(helpUrlSerializer.toProperties(), new Properties());
        assertHelpUrls(parse, defaultUrl, expected);
    }

    @Test
    public void serviceDeskConfigurationWorks()
    {
        HelpUrls urls = parser.parse(readProperties("sd-help-paths.properties"));

        MockHelpUrl defaultUrl = new MockHelpUrl()
                .setKey(DEFAULT_KEY)
                .setUrl("https://confluence.atlassian.com/display/SERVICEDESK/JIRA+Service+Desk+Documentation");

        MockHelpUrl helpUrl = defaultUrl.copy().setKey("help");

        assertHelpUrls(urls, defaultUrl, helpUrl);
    }

    @Test
    public void serviceDeskStaticConfigurationWorks()
    {
        HelpUrls urls = parser.parse(readProperties("sd-static-links.properties"));

        MockHelpUrl defaultUrl = new MockHelpUrl()
                .setKey(DEFAULT_KEY);

        MockHelpUrl wac = defaultUrl.copy()
                .setUrl("http://www.atlassian.com/software/jira/service-desk")
                .setKey("wac");

        assertHelpUrls(urls, defaultUrl, wac);
    }

    private Properties readProperties(final String name)
    {
        InputStream resourceAsStream = this.getClass().getResourceAsStream(name);
        Properties props = new Properties();
        try
        {
            props.load(resourceAsStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return props;
    }


    private static void assertParser(final HelpUrlSerializer helpUrlSerializer, final HelpUrl defaultUrl,
            final Iterable<? extends HelpUrl> urls, final HelpUrlsParser parser)
    {
        assertHelpUrls(parser.parse(helpUrlSerializer.toMap()), defaultUrl, urls);
        assertHelpUrls(parser.parse(helpUrlSerializer.toProperties()), defaultUrl, urls);
    }

    private static void assertHelpUrls(HelpUrls urls, HelpUrl defaultUrl, HelpUrl... others)
    {
        assertHelpUrls(urls, defaultUrl, Arrays.asList(others));
    }

    private static void assertHelpUrls(HelpUrls urls, HelpUrl defaultUrl, Iterable<? extends HelpUrl> others)
    {
        List<Matcher<? super String>> keys = Lists.newArrayList();
        keys.add(equalTo(defaultUrl.getKey()));
        for (HelpUrl other : others)
        {
            keys.add(equalTo(other.getKey()));
        }

        assertThat(urls.getUrlKeys(), containsInAnyOrder(keys));

        for (HelpUrl other : others)
        {
            assertThat(urls.getUrl(other.getKey()), new HelpUrlMatcher(other));
        }
        HelpUrlMatcher defaultMatcher = new HelpUrlMatcher(defaultUrl);
        assertThat(urls.getDefaultUrl(), defaultMatcher);
        assertThat(urls.getUrl(DEFAULT_KEY), defaultMatcher);
        assertThat(urls.getUrl("some.key.that.wont.exist.really.i.wont.unless.a.peanut.butter.sandwich"), defaultMatcher);
    }

    private static class HelpUrlSerializer
    {
        private Map<String, String> data = Maps.newLinkedHashMap();
        private HelpUrl defaultUrl;
        private String prefix = "";
        private String suffix = "";

        public HelpUrlSerializer prefix(String prefix)
        {
            this.prefix = prefix;
            data.put("url-prefix", prefix);

            return this;
        }

        public HelpUrlSerializer odPrefix(String prefix)
        {
            this.prefix = prefix;
            data.put("url-prefix.ondemand", prefix);

            return this;
        }

        public HelpUrlSerializer suffix(String suffix)
        {
            this.suffix = suffix;
            data.put("url-suffix", suffix);

            return this;
        }

        public HelpUrlSerializer odSuffix(String suffix)
        {
            this.suffix = suffix;
            data.put("url-suffix.ondemand", suffix);

            return this;
        }

        public HelpUrl setDefaultUrlOd(String url, String title, String alt)
        {
            return defaultUrl = addHelpUrlOd(DEFAULT_KEY, url, title, alt);
        }

        public HelpUrl setDefaultUrl(String url, String title, String alt)
        {
            return defaultUrl = addHelpUrl(DEFAULT_KEY, url, title, alt);
        }

        public HelpUrl addHelpUrl(String key, String url, String title, String alt)
        {
            return addHelpUrl(key, url, title, alt, false);
        }

        public HelpUrl addHelpUrlOd(String key, String url, String title, String alt)
        {
            return addHelpUrl(key, url, title, alt, true);
        }

        public HelpUrl addHelpUrl(String key, String url, String title, String alt, boolean onDemand)
        {
            final String extra = onDemand ? ".ondemand" : "";

            if (url != null)
            {
                data.put(String.format("%s.url%s", key, extra), url);
            }
            if (alt != null)
            {
                data.put(String.format("%s.alt%s", key, extra), alt);
            }
            if (title != null)
            {
                data.put(String.format("%s.title%s", key, extra), title);
            }

            MockHelpUrl mockHelpUrl = new MockHelpUrl().setKey(key);
            if (defaultUrl != null)
            {
                if (url == null)
                {
                    url = defaultUrl.getUrl();
                }
                else
                {
                    url = createUrl(url);
                }
                if (title == null)
                {
                    title = defaultUrl.getTitle();
                }
                if (alt == null)
                {
                    alt = defaultUrl.getAlt();
                }
            }
            else
            {
                url = createUrl(url);
            }

            return mockHelpUrl
                    .setUrl(url)
                    .setTitle(title)
                    .setAlt(alt);
        }

        private String createUrl(String url)
        {
            if (url == null)
            {
                return null;
            }
            else
            {
                return String.format("%s%s%s", prefix, url, suffix);
            }
        }

        private Map<String, String> toMap()
        {
            return ImmutableMap.copyOf(data);
        }

        private Properties toProperties()
        {
            Properties properties = new Properties();
            properties.putAll(data);
            return properties;
        }
    }
}
