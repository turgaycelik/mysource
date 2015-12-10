package com.atlassian.jira.help;

import com.google.common.base.Objects;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static java.lang.String.format;

/**
* @since v6.2.4.
*/
public class HelpUrlMatcher extends TypeSafeDiagnosingMatcher<HelpUrl>
{
    private String url;
    private String alt;
    private String key;
    private boolean local;
    private String title;

    public HelpUrlMatcher(HelpUrl url)
    {
        this.url = url.getUrl();
        this.alt = url.getAlt();
        this.key = url.getKey();
        this.local = url.isLocal();
        this.title = url.getTitle();
    }

    public HelpUrlMatcher()
    {
    }

    public HelpUrlMatcher(HelpUrlMatcher matcher)
    {
        this.url = matcher.url;
        this.alt = matcher.alt;
        this.key = matcher.key;
        this.local = matcher.local;
        this.title = matcher.title;
    }

    public HelpUrlMatcher url(String url)
    {
        this.url = url;
        return this;
    }

    public HelpUrlMatcher key(String key)
    {
        this.key = key;
        return this;
    }

    public HelpUrlMatcher title(String title)
    {
        this.title = title;
        return this;
    }

    public HelpUrlMatcher local(boolean local)
    {
        this.local = local;
        return this;
    }

    public HelpUrlMatcher alt(String alt)
    {
        this.alt = alt;
        return this;
    }

    public HelpUrlMatcher copy()
    {
        return new HelpUrlMatcher(this);
    }

    @Override
    protected boolean matchesSafely(final HelpUrl item, final Description mismatchDescription)
    {
        if (Objects.equal(item.getUrl(), url)
                && Objects.equal(item.getAlt(), alt)
                && Objects.equal(item.getKey(), key)
                && Objects.equal(item.getTitle(), title)
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

    private String asString(HelpUrl item)
    {
        return asString(item.getUrl(), item.getAlt(), item.getKey(), item.isLocal(), item.getTitle());
    }

    private String asString(final String url, final String alt, final String key, final boolean locale, final String title)
    {
        return format("URL[url=%s,alt=%s,key=%s,local=%s,title=%s]", url, alt, key, locale, title);
    }

    private static String nullAsEmpty(final String prefix)
    {
        return prefix == null ? "" : prefix;
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(asString(url, alt, key, local, title));
    }
}
