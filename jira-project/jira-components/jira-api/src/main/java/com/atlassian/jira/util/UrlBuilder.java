package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;
import com.google.common.collect.Lists;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Builds a URL from parameters.
 *
 * <p>NOTE: This class does not HTML escape the URLs. Be very careful if using this class to output a URL on the UI.</p>
 *
 * @since v4.0
 */
@NotThreadSafe
@PublicApi
public final class UrlBuilder
{
    private static final char PATH_QUERY_SEPARATOR_CHAR = '?';
    private static final char PATH_SEPARATOR_CHAR = '/';
    private static final String PATH_SEPARATOR = String.valueOf(PATH_SEPARATOR_CHAR);

    private final StringBuilder pathBuilder;
    private final StringBuilder anchorBuilder;
    private final StringBuilder queryBuilder;
    private final String encoding;

    private boolean querySnippet;

    /**
     * Creates a builder with a blank URL.
     *
     * @param querySnippet whether or not this URL is complete or just a query snippet
     * @throws IllegalArgumentException if the base url is null.
     */
    public UrlBuilder(boolean querySnippet)
    {
        this("", querySnippet);
    }

    /**
     * Creates a copy of the passed builder. The state of the two builders will be independent after the copy.
     *
     * @param source the builder to copy, cannot be null.
     * @throws IllegalArgumentException if source is null.
     */
    public UrlBuilder(final UrlBuilder source)
    {
        notNull("source", source);
        this.pathBuilder = new StringBuilder(source.pathBuilder);
        this.queryBuilder = new StringBuilder(source.queryBuilder);
        this.anchorBuilder = new StringBuilder(source.anchorBuilder);
        this.encoding = source.encoding;
        this.querySnippet = source.querySnippet;
    }

    /**
     * Creates a builder with the specified URL fragment. The URL will be used as the start of the URL generated by this
     * builder.
     *
     * @param urlFragment the URL fragment for the builder. This parameter will not be escaped in the resulting URL.
     * @throws IllegalArgumentException if <code>urlFragment</code> is null.
     */
    public UrlBuilder(final String urlFragment)
    {
        this(urlFragment, null, false);
    }

    /**
     * Creates a builder with the specified URL fragment. The URL will be used as the start of the URL generated by this
     * builder.
     *
     * @param urlFragment the URL or query snippet for the builder. This parameter will not be escaped in the resulting URL.
     * @param querySnippet whether or not this URL is complete or just a query snippet
     * @throws IllegalArgumentException if the <code>urlFragment</code> is null
     */
    public UrlBuilder(final String urlFragment, final boolean querySnippet)
    {
        this(urlFragment, null, querySnippet);
    }

    /**
     * Creates a builder with the specified URL fragment. The URL will be used as the start of the URL generated by this
     * builder.
     *
     * @param urlFragment the URL fragment for the builder. This parameter will not be escaped in the resulting URL.
     * @param querySnippet whether or not this URL is complete or just a query snippet.
     * @param encoding the character encoding to use for parameter names and values. Can be left null (recommended) to indicate JIRA default encoding.
     * @throws IllegalArgumentException if <code>urlFragment</code> is null
     */
    public UrlBuilder(final String urlFragment, @Nullable String encoding, boolean querySnippet)
    {
        notNull("urlFragment", urlFragment);
        if (querySnippet)
        {
            this.pathBuilder = new StringBuilder();
            this.queryBuilder = new StringBuilder(urlFragment);
        }
        else
        {
            PathQueryHolder holder = parseBaseUrl(urlFragment);
            this.pathBuilder = new StringBuilder(holder.path);
            this.queryBuilder = new StringBuilder(holder.query);
        }
        this.anchorBuilder = new StringBuilder();
        this.encoding = encoding;
        this.querySnippet = querySnippet;
    }
    
    /**
     * Adds the given path to the URL. If needed, a '/' will be added between the existing URL and the given path. If
     * you use this method, you must ensure that the parameters are already safe. Otherwise, use {@link #addPath(String)}.
     *
     * @param path the path to add to the URL. This parameter name is not escaped before it is added to the URL.
     * @return builder so that calls may be chained
     */
    public UrlBuilder addPathUnsafe(String path)
    {
        notBlank("path", path);
        final boolean empty = pathBuilder.length() == 0;
        final boolean urlEndsWithSlash = !empty && pathBuilder.charAt(pathBuilder.length() - 1) == PATH_SEPARATOR_CHAR;
        final boolean pathStartsWithSlash = path.startsWith(PATH_SEPARATOR);

        if (!empty && !urlEndsWithSlash && !pathStartsWithSlash)
        {
            pathBuilder.append(PATH_SEPARATOR);
        }
        else if (urlEndsWithSlash && pathStartsWithSlash)
        {
            path = path.substring(1);
        }

        pathBuilder.append(path);
        return this;
    }

    /**
     * URL encodes and adds the given path to the URL. If needed a '/' will be added between the existing URL and the given
     * path.
     *
     * @param path path to be encoded and added to the URL. The path cannot be blank.
     * @return this builder so that calls may be chained
     * @throws IllegalArgumentException if path is blank
     */
    public UrlBuilder addPath(String path)
    {
        notBlank("path", path);

        String safePath = encode(StringUtils.strip(path, "/"));
        final boolean pathEndsWithSlash = path.charAt(path.length() - 1) == PATH_SEPARATOR_CHAR;
        if (pathEndsWithSlash)
        {
            safePath += PATH_SEPARATOR_CHAR;
        }
        return addPathUnsafe(safePath);
    }

    /**
     * URL encodes and adds the given paths to the URL. If needed a '/' will be added between the existing URL and the given
     * paths. The paths in between the path separator ('/') will be encoded.
     *
     * @param paths paths to be encoded and added to the URL. E.g. "/one/two/three". Cannot be blank.
     * @return this builder so that calls may be chained
     * @throws IllegalArgumentException if paths is blank
     */
    public UrlBuilder addPaths(String paths)
    {
        notBlank("paths", paths);

        String[] pathComponents = StringUtils.split(paths, PATH_SEPARATOR_CHAR);
        List<String> safePathComponents = Lists.newArrayListWithExpectedSize(pathComponents.length);
        for (String singlePath : pathComponents)
        {
            safePathComponents.add(encode(singlePath));
        }

        String safePaths = StringUtils.join(safePathComponents, '/');
        final boolean pathEndsWithSlash = paths.charAt(paths.length() - 1) == PATH_SEPARATOR_CHAR;
        if (pathEndsWithSlash)
        {
            safePaths += PATH_SEPARATOR_CHAR;
        }
        return addPathUnsafe(safePaths);
    }

    /**
     * Adds the parameter to the URL without URL encoding them. This is UNSAFE as it may allow XSS attacks. If you
     * use this method, you must ensure that the parameters are already safe.
     *
     * @param name the name of the parameter. This parameter name is not escaped before it is added to the URL. This
     * value cannot be blank.
     * @param value the value of the parameter. This value is not escaped before it is added to the URL.
     * @return this builder so that calls may be chained
     * @throws IllegalArgumentException if name is blank
     */
    public UrlBuilder addParameterUnsafe(final String name, final String value)
    {
        notBlank("name", name);

        addParameterSeparator();
        queryBuilder.append(name).append('=').append(value);

        return this;
    }

    /**
     * Adds the parameter to the URL while URL encoding them.
     *
     * @param name the name of the parameter. This value cannot be blank.
     * @param value the value of the parameter.
     * @return this builder so that calls may be changed
     * @throws IllegalArgumentException if name is blank
     */
    public UrlBuilder addParameter(final String name, final String value)
    {
        notBlank("name", name);

        final String safeName = encode(name);
        final String safeValue = value == null ? "" : encode(value);

        addParameterUnsafe(safeName, safeValue);

        return this;
    }

    /**
     * Adds the passed parameter to the URL while URL encoding them.
     *
     * @param name the name of the parameter. This value cannot be blank.
     * @param value the value of the parameter
     * @return this builder so that calls may be changed
     * @throws IllegalArgumentException if name is blank
     */
    public UrlBuilder addParameter(final String name, final Object value)
    {
        return addParameter(name, value == null ? null : value.toString());
    }

    /**
     * Add the passed anchor value to the URL while URL encoding it. The result will be something like <code>#myAnchor</code>.
     * Note that to be compliant with standards, you will want to call this only <em>after</em> adding all your parameters.
     *
     * @param value the value of the anchor
     * @return this builder so that calls may be changed
     * @throws IllegalArgumentException if name is blank
     */
    public UrlBuilder addAnchor(final String value)
    {
        notBlank("value", value);

        final String safeValue = encode(value);

        anchorBuilder.append("#").append(safeValue);

        return this;
    }

    /**
     * Add multiple parameters from a map safely. Any keys which are null or blank will be ignored.
     * The parameters will be added in order as given by the passed map's entrySet.
     *
     * @param params map containing parameters to add. Must not be null.
     * @return this builder
     */
    public UrlBuilder addParametersFromMap(final Map<?, ?> params)
    {
        notNull("params", params);
        for (Map.Entry<?, ?> entry : params.entrySet())
        {
            if (entry.getKey() == null || StringUtils.isBlank(entry.getKey().toString()))
            {
                continue;
            }
            addParameter(entry.getKey().toString(), entry.getValue());
        }
        return this;
    }

    /**
     * Returns the URL as a String.
     *
     * @return URL as a String
     */
    public String asUrlString()
    {
        if (querySnippet)
        {
            // done for backwards compatibility
            return queryBuilder.length() == 0 ? "" : "&" + queryBuilder.toString() + anchorBuilder.toString();
        }
        else
        {
            if (queryBuilder.length() == 0)
            {
                return pathBuilder.toString() + anchorBuilder.toString();
            }
            else
            {
                return pathBuilder.toString() + "?" + queryBuilder.toString() + anchorBuilder.toString();
            }
        }
    }

    /**
     * Returns the URL as a URI.
     *
     * @return URL as a URI
     */
    public URI asURI()
    {
        return URI.create(asUrlString());
    }

    /**
     * Returns the same thing as {@link #asUrlString()}, to avoid confusion.
     * @return the same thing as {@link #asUrlString()}
     */
    @Override
    public String toString()
    {
        return asUrlString();
    }

    private void addParameterSeparator()
    {
        if (queryBuilder.length() != 0)
        {
            queryBuilder.append("&");
        }
    }

    private String encode(final String str)
    {
        if (str != null)
        {
            if (encoding == null)
            {
                return JiraUrlCodec.encode(str);
            }
            else
            {
                return JiraUrlCodec.encode(str, encoding);
            }
        }
        else
        {
            return str;
        }
    }

    /**
     * Splits the URL into the path and query components.
     *
     * @param url URL to parse. Cannot be <tt>null</tt>.
     * @return holder object containing the path and query components
     */
    private static PathQueryHolder parseBaseUrl(String url)
    {
        notNull("url", url);
        String[] tokens = StringUtils.splitPreserveAllTokens(url, PATH_QUERY_SEPARATOR_CHAR);
        switch (tokens.length)
        {
            case 0:
                return new PathQueryHolder("", "");
            case 1:
                return new PathQueryHolder(tokens[0], "");
            case 2:
                return new PathQueryHolder(tokens[0], tokens[1]);
            default:
                return new PathQueryHolder(tokens[0], StringUtils.join(ArrayUtils.remove(tokens, 0), PATH_QUERY_SEPARATOR_CHAR));
        }
    }

    private static class PathQueryHolder
    {
        private final String path;
        private final String query;

        private PathQueryHolder(String path, String query)
        {
            this.path = path;
            this.query = query;
        }
    }
}
