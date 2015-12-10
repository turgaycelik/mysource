package com.atlassian.jira.help;

import java.util.Map;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * Template for implementing the {@link com.atlassian.jira.help.HelpUrlBuilder}. Implementers must implement the
 * two template methods {@link #newInstance()}} and {@link #getExtraParameters()}.
 *
 * @since v6.2.4
 */
abstract class HelpUrlBuilderTemplate implements HelpUrlBuilder
{
    private final String prefix;
    private final String suffix;

    private String url;
    private String alt;
    private String title;
    private String key;
    private boolean local;

    HelpUrlBuilderTemplate(String prefix, String suffix)
    {
        this.suffix = suffix;
        this.prefix = prefix;
    }

    @Override
    public final HelpUrlBuilder key(final String key)
    {
        this.key = key;
        return this;
    }

    @Override
    public final HelpUrlBuilder alt(final String alt)
    {
        this.alt = alt;
        return this;
    }

    @Override
    public final HelpUrlBuilder title(final String title)
    {
        this.title = title;
        return this;
    }

    @Override
    public final HelpUrlBuilder url(final String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public final HelpUrlBuilder local(final boolean local)
    {
        this.local = local;
        return this;
    }

    @Override
    public final HelpUrlBuilder copy()
    {
        return newInstance()
                .title(title)
                .alt(alt)
                .url(url)
                .key(key)
                .local(local);
    }

    @Override
    public final HelpUrl build()
    {
        checkState(key != null, "key must be specified");

        return new ImmutableHelpUrl(key, isExternalLink(url) ? url : generateUrl(), title, alt, local);
    }

    private String generateUrl()
    {
        if (prefix == null && url == null && suffix == null)
        {
            return null;
        }

        final String targetAnchor;
        final String targetUrl;

        //Split out the anchor off the URL.
        final int hashIndex = indexOf(url, '#');
        if (hashIndex != -1)
        {
            targetUrl = url.substring(0, hashIndex);
            targetAnchor = url.substring(hashIndex);
        }
        else
        {
            targetUrl = url;
            targetAnchor = null;
        }

        StringBuilder builder = new StringBuilder();
        if (prefix != null)
        {
            builder.append(prefix);
        }
        if (targetUrl != null)
        {
            builder.append(targetUrl);
        }
        if (suffix != null)
        {
            builder.append(suffix);
        }

        final Map<String, String> parameters = getExtraParameters();
        if (!parameters.isEmpty())
        {
            char sep = builder.indexOf("?") == -1 ? '?' : '&';
            for (Map.Entry<String, String> parameter : parameters.entrySet())
            {
                builder.append(sep).append(parameter.getKey()).append('=').append(parameter.getValue());
                sep = '&';
            }
        }
        if (targetAnchor != null)
        {
            builder.append(targetAnchor);
        }
        return builder.toString();
    }

    private static boolean isExternalLink(String url)
    {
        return startsWithIgnoreCase(url, "http://") || startsWithIgnoreCase(url, "https://");
    }

    String getPrefix()
    {
        return prefix;
    }

    String getSuffix()
    {
        return suffix;
    }

    /**
     * Return a map containing any extra parameters to add to the generated URL.
     *
     * @return a map contains any extra parameters to add to the URL.
     */
    @Nonnull
    abstract Map<String, String> getExtraParameters();

    /**
     * Create a new blank concrete instance of {@link HelpUrlBuilder}.
     *
     * @return a new blank concrete instance of {@link HelpUrlBuilder}.
     */
    abstract HelpUrlBuilder newInstance();
}
