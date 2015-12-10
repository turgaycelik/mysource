package com.atlassian.jira.web.util;

import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.HelpUrlsParser;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.help.MockHelpUrlsParser;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class TestHelpUtil
{
    private static final String EXTERNAL = "com/atlassian/jira/web/util/help-external.properties";
    private static final String INTERNAL = "com/atlassian/jira/web/util/help-internal.properties";

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer (interfaceClass = HelpUrls.class)
    private MockHelpUrls urls = new MockHelpUrls();

    @AvailableInContainer(interfaceClass = HelpUrlsParser.class)
    private MockHelpUrlsParser parser = new MockHelpUrlsParser();

    private HelpUrl defaultUrl = urls.createDefault();
    private HelpUrl one = urls.createSimpleUrl("one");
    private HelpUrl two = urls.createSimpleUrl("two");

    private HelpUrl three = parser.createUrl("three", "three.com");
    private HelpUrl four = parser.createUrl("four", "four.com");

    @Test
    public void getHelpPathReturnsCorrectHelpPathWithCreatedWithNullProperties()
    {
        parser.defaultUrl("url", "title");

        final HelpUtil helpUtil = new HelpUtil(null);

        Matcher<HelpUtil.HelpPath> matcher = matcher(parser.getGeneratedDefault(null));
        assertThat(helpUtil.getHelpPath(one.getKey()), matcher);
        assertThat(helpUtil.getHelpPath(two.getKey()), matcher);
        assertThat(helpUtil.getHelpPath("bad"), matcher);
    }

    @Test
    public void getHelpPathReturnsCorrectHelpPathWhenCreatedWithProperties()
    {
        parser.defaultUrl("url", "title");

        Properties properties = new Properties();
        properties.put(three.getKey(), "something");
        properties.put(four.getKey(), "four");

        final HelpUtil helpUtil = new HelpUtil(properties);

        assertThat(helpUtil.getHelpPath(four.getKey()), matcher(parser.getGeneratedUrl(four, "four")));
        assertThat(helpUtil.getHelpPath(three.getKey()), matcher(parser.getGeneratedUrl(three, "something")));
        assertThat(helpUtil.getHelpPath("bad"), matcher(parser.getGeneratedDefault(null)));
    }

    @Test
    public void getHelpPathReturnsCorrectHelpPathForGlobalJiraHelp()
    {
        final HelpUtil helpUtil = new HelpUtil();

        assertThat(helpUtil.getHelpPath(one.getKey()), matcher(one));
        assertThat(helpUtil.getHelpPath(two.getKey()), matcher(two));
        assertThat(helpUtil.getHelpPath("bad"), matcher(defaultUrl));
    }

    @Test
    public void getHelpPathReturnsCorrectHelpPathWhenCreatedWithPropertiesFile()
    {
        final HelpUtil helpUtil = new HelpUtil(EXTERNAL, INTERNAL);

        assertThat(helpUtil.getHelpPath(three.getKey()), matcher(parser.getGeneratedUrl(three, "Three")));
        assertThat(helpUtil.getHelpPath(four.getKey()), matcher(parser.getGeneratedUrl(four, "Four")));
        assertThat(helpUtil.getHelpPath("bad"), matcher(parser.getGeneratedDefault(null)));
    }

    @Test
    public void getHelpPathReturnsCorrectHelpPathWhenCreatedWithBadExternalPropertiesFile()
    {
        final HelpUtil helpUtil = new HelpUtil("com/atlassian/jira/web/util/external.bad.properties", INTERNAL);

        assertThat(helpUtil.getHelpPath(three.getKey()), matcher(parser.getGeneratedUrl(three, "Three")));
        Matcher<HelpUtil.HelpPath> defaultMatcher = matcher(parser.getGeneratedDefault(null));
        assertThat(helpUtil.getHelpPath(four.getKey()), defaultMatcher);
        assertThat(helpUtil.getHelpPath("bad"), defaultMatcher);
    }

    @Test
    public void getHelpPathReturnsCorrectHelpPathWhenCreatedWithBadInternalPropertiesFile()
    {
        final HelpUtil helpUtil = new HelpUtil(EXTERNAL, null);

        assertThat(helpUtil.getHelpPath(four.getKey()), matcher(parser.getGeneratedUrl(four, "Four")));
        Matcher<HelpUtil.HelpPath> defaultMatcher = matcher(parser.getGeneratedDefault(null));
        assertThat(helpUtil.getHelpPath(three.getKey()), defaultMatcher);
        assertThat(helpUtil.getHelpPath("bad"), defaultMatcher);
    }

    @Test
    public void getKeysReturnsAllTheKeys()
    {
        HelpUtil helpUtil = new HelpUtil();
        assertThat(helpUtil.keySet(), containsInAnyOrder(one.getKey(), two.getKey(), defaultUrl.getKey()));
    }

    @Test
    public void toJsonOnHelpPath()
    {
        ObjectMapper mapper = new ObjectMapper();
        HelpUtil helpUtil = new HelpUtil();
        HelpUtil.HelpPath helpPath = helpUtil.getHelpPath(one.getKey());
        try
        {
            JsonBean bean = mapper.reader(JsonBean.class).readValue(helpPath.toJson());
            assertThat(bean, equalTo(new JsonBean(one)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Matcher<HelpUtil.HelpPath> matcher(HelpUrl url)
    {
        return new HelpUtilPathMatcher(url);
    }

    private static class HelpUtilPathMatcher extends TypeSafeDiagnosingMatcher<HelpUtil.HelpPath>
    {
        private String url;
        private String simpleUrl;
        private String alt;
        private String key;
        private boolean local;
        private String title;

        public HelpUtilPathMatcher()
        {
        }

        public HelpUtilPathMatcher(HelpUrl url)
        {
            this.url = url.getUrl();
            this.simpleUrl = url.getUrl();
            this.alt = url.getAlt();
            this.title = url.getTitle();
            this.local = url.isLocal();
            this.key = url.getKey();
        }

        public HelpUtilPathMatcher url(String url)
        {
            this.url = url;
            return this;
        }

        public HelpUtilPathMatcher key(String key)
        {
            this.key = key;
            return this;
        }

        public HelpUtilPathMatcher title(String title)
        {
            this.title = title;
            return this;
        }

        public HelpUtilPathMatcher local(boolean local)
        {
            this.local = local;
            return this;
        }

        public HelpUtilPathMatcher alt(String alt)
        {
            this.alt = alt;
            return this;
        }

        public HelpUtilPathMatcher simpleUrl(String simpleUrl)
        {
            this.simpleUrl = simpleUrl;
            return this;
        }

        @Override
        protected boolean matchesSafely(final HelpUtil.HelpPath item, final Description mismatchDescription)
        {
            if (Objects.equal(item.getUrl(), url)
                    && Objects.equal(item.getAlt(), alt)
                    && Objects.equal(item.getKey(), key)
                    && Objects.equal(item.getTitle(), title)
                    && Objects.equal(item.getSimpleUrl(), simpleUrl)
                    && Objects.equal(item.isLocal(), local))
            {
                return true;
            }
            else
            {
                mismatchDescription.appendText(asString(item));
                return false;
            }
        }

        private String asString(HelpUtil.HelpPath item)
        {
            return asString(item.getUrl(), item.getAlt(), item.getKey(), item.isLocal(), item.getTitle(), item.getSimpleUrl());
        }

        private String asString(final String url, final String alt, final String key, final boolean locale, final String title, final String simpleUrl)
        {
            return String.format("URL[url=%s,alt=%s,key=%s,local=%s,title=%s,simpleUrl=%s]", url,
                    alt, key, locale, title, simpleUrl);
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(asString(url, alt, key, local, title, simpleUrl));
        }
    }

    public static class JsonBean
    {
        private String title;
        private String url;
        private String alt;
        private String key;
        private boolean local;

        public JsonBean() {}

        public JsonBean(HelpUrl path)
        {
            this.title = path.getTitle();
            this.url = path.getUrl();
            this.alt = path.getAlt();
            this.key = path.getKey();
            this.local = path.isLocal();
        }

        @JsonProperty
        public String getTitle()
        {
            return title;
        }

        public void setTitle(final String title)
        {
            this.title = title;
        }

        @JsonProperty
        public String getUrl()
        {
            return url;
        }

        public void setUrl(final String url)
        {
            this.url = url;
        }

        @JsonProperty
        public String getAlt()
        {
            return alt;
        }

        public void setAlt(final String alt)
        {
            this.alt = alt;
        }

        @JsonProperty
        public String getKey()
        {
            return key;
        }

        public void setKey(final String key)
        {
            this.key = key;
        }

        @JsonProperty
        public boolean getLocal()
        {
            return local;
        }

        public void setLocal(final boolean local)
        {
            this.local = local;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final JsonBean jsonBean = (JsonBean) o;

            if (local != jsonBean.local) { return false; }
            if (alt != null ? !alt.equals(jsonBean.alt) : jsonBean.alt != null) { return false; }
            if (key != null ? !key.equals(jsonBean.key) : jsonBean.key != null) { return false; }
            if (title != null ? !title.equals(jsonBean.title) : jsonBean.title != null) { return false; }
            if (url != null ? !url.equals(jsonBean.url) : jsonBean.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (alt != null ? alt.hashCode() : 0);
            result = 31 * result + (key != null ? key.hashCode() : 0);
            result = 31 * result + (local ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("title", title).
                    append("url", url).
                    append("alt", alt).
                    append("key", key).
                    append("local", local).
                    toString();
        }
    }
}
