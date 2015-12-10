package com.atlassian.jira.help;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;

import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A parser that a creates {@link com.atlassian.jira.help.HelpUrls} from name-value pairs.
 *
 * Consider the following properties file:
 * <pre>
 *   url-prefix=https:///DOCS/docs-${docs.version}/
 *   url-prefix.ondemand=https://CAC/display/AOD/
 *
 *   jira101.url=JIRA+101
 *   jira101.url.ondemand=JIRA+OnDemand
 *   jira101.title=JIRA 101
 *   jira101.title.ondemand=JIRA OnDemand
 *
 *   default.url = Index
 *   default.title = JIRA Help
 * </pre>
 *
 * It OnDemand it produces a {@code HelpUrls} where:
 * <ul>
 *     <li>{@code urls.getDefault() == HelpUrl{url: https://CAC/display/AOD/Index, title: "JIRA Help"}}</li>
 *     <li>{@code urls.get(default) == HelpUrl{url: https://CAC/display/AOD/Index, title: "JIRA Help"}}</li>
 *     <li>{@code urls.get(jira101) == HelpUrl{url: https://CAC/display/AOD/JIRA+OnDemand, title: "JIRA OnDemand"}}</li>
 *     <li>{@code urls.get(anyOtherKey) == HelpUrl{url: https://CAC/display/AOD/Index, title: "JIRA Help"}}</li>
 * </ul>
 *
 * In a non-OnDemand 6.2.x instance produces a HelpUrls where:
 * <ul>
 *     <li>{@code urls.getDefault() == HelpUrl{url: https:///DOCS/docs-062/Index, title: "JIRA Help"}}</li>
 *     <li>{@code urls.get(default) == HelpUrl{url: https:///DOCS/docs-062/Index, title: "JIRA Help"}}</li>
 *     <li>{@code urls.get(jira101) == HelpUrl{url: https:///DOCS/docs-062/JIRA+101, title: "JIRA 101"}}</li>
 *     <li>{@code urls.get(anyOtherKey) == HelpUrl{url: https:///DOCS/docs-062/Index, title: "JIRA Help"}}</li>
 * </ul>
 *
 * The default URL specified via {@link #defaultUrl(String, String)} is used when a default URL is not configured
 * in the passed properties.
 *
 * An injected instance is configured with the default url being null (i.e. {@code defaultUrl(null, null)}) and
 * with the correct OnDemand status (i.e. {@code onDemand(inOnDemand)}). The object is immutable, however, you can
 * easily create new instances of {@code HelpUrlParser} with different settings using {@link #onDemand(boolean)} or
 * {@link #defaultUrl(String, String)}.
 *
 * @since v6.2.4
 */
@Immutable
@ExperimentalApi
public interface HelpUrlsParser
{
    /**
     * Returns a new parser that will either use or ignore {@code .ondemand} properties.
     *
     * @param onDemand {@code true} if the parser should use {@code .ondemand} properties, or {@code false} if they
     * should be ignored.
     *
     * @return a new parser configured to either use or ignore {@code .ondemand} properties
     */
    @Nonnull
    HelpUrlsParser onDemand(boolean onDemand);

    /***
     * Returns a new parser that will use the passed URL (title) in URLs that don't have a URL (title) set. It is
     * possible for the parser to ignore these values if the input name-value pairs during a parse
     * have their own configured default.
     *
     * @param url the default URL.
     * @param title the default title of the URL.
     *
     * @return a new parser with the URL and title of the default {@code HelpUrl} configured.
     */
    @Nonnull
    HelpUrlsParser defaultUrl(String url, String title);

    /**
     * Parse the passed properties and return an equivalent {@code HelpUrls} instance.
     *
     * @param properties the properties to parse.
     * @return the {@code HelpUrls} representation of the passed properties.
     */
    @Nonnull
    HelpUrls parse(@Nonnull Properties properties);

    /**
     * Parse the passed properties and return an equivalent {@code HelpUrls} instance.
     *
     * <p>This method should not be called by plugins as the format of {@code internalProperties} is not documented
     * and is subject to change.
     *
     * @param externalProperties the properties to parse.
     * @param internalProperties the internal properties to parse. These properties follow a different format to the
     *  that documented on this interface and should only be used by JIRA internally.
     * @return the {@code HelpUrls} representation of the passed properties.
     */
    @Internal
    @Nonnull
    HelpUrls parse(@Nonnull Properties externalProperties, @Nonnull Properties internalProperties);

    /**
     * Parse the passed properties and return an equivalent {@code HelpUrls} instance.
     *
     * @param properties the properties to parse.
     * @return the {@code HelpUrls} representation of the passed properties.
     */
    @Nonnull
    HelpUrls parse(@Nonnull Map<String, String> properties);
}
