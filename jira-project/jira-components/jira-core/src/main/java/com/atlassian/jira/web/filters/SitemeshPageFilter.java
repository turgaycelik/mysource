package com.atlassian.jira.web.filters;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opensymphony.module.sitemesh.filter.PageFilter;
import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.web.filters.InitParamSupport.optional;
import static com.atlassian.jira.web.filters.InitParamSupport.required;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * This Sitemesh page filter adds HTTP request header-driven exclusions to the standard PageFilter. Decoration can
 * be skipped based on the present of certain HTTP request headers, or based on the requested path.
 * </p>
 * This filter accepts two initialization parameters:
 * <ul>
 * <li><tt>exclude.paths</tt>: comma-separated list of excluded servlet paths (<b>REQUIRED</b>)
 *  <li><tt>exclude.headers</tt>: name of HTTP request headers
 * </ul>
 * <p>
 * The excluded paths patterns in the list may come in two forms:
 * <ul>
 * <li>exact pattern - exact servlet path that will be matched by means of the {@link String#equals(Object)} method
 * <li>wildcard pattern - part of the path followed by the '*' (wildcard) character, will match against all
 * requests, whose servlet path starts with the pattern (by means of {@link String#startsWith(String)})
 * </ul>
 * </p>
 *
 * @since v4.2
 */
public final class SitemeshPageFilter extends SiteMeshFilter
{
    private static final String SEPARATOR = ",";
    private static final String WILDCARD = "*";

    private static final String INIT_LOG_MSG = "PathExclusionFilter [%s] initialized, \n"
            + "Header names: %s\n"
            + "Exact exclude patterns: %s\n"
            + "Wildcard exclude patterns: %s";
    private static final String FILTER_LOG_MSG = "PathExclusionFilter[%s].filter decorated filter %s for servlet path [%s] ";

    private static final Logger log = LoggerFactory.getLogger(SitemeshPageFilter.class);

    public static enum InitParams
    {
        EXCLUDED_PATHS(required("exclude.paths")),
        EXCLUDED_HEADERS(optional("exclude.headers"));

        private final InitParamSupport support;

        private InitParams(InitParamSupport support)
        {
            this.support = support;
        }

        public String key()
        {
            return support.key();
        }

        String get(FilterConfig config)
        {
            return support.get(config);
        }
    }

    private String name;
    private final Set<String> excludedExactPatterns = new LinkedHashSet<String>();
    private final Set<String> excludedWildcardPatterns = new LinkedHashSet<String>();

    /**
     * Contains the lower-cased exclude headers.
     */
    private final AtomicReference<ImmutableSet<String>> excludedHeaderNames = new AtomicReference<ImmutableSet<String>>();

    @Override
    public void init(final FilterConfig filterConfig)
    {
        super.init(filterConfig);
        this.name = filterConfig.getFilterName();
        parseExcludedPaths(InitParams.EXCLUDED_PATHS.get(filterConfig));
        excludedHeaderNames.set(parseExcludeHeaderNames(InitParams.EXCLUDED_HEADERS.get(filterConfig)));
        logInit();
    }

    @Override
    public void doFilter(ServletRequest requestz, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) requestz;

        boolean isExcluded = hasExcludeHeader(httpRequest) || isExcluded(httpRequest.getServletPath());
        if (log.isDebugEnabled())
        {
            log.debug(String.format(FILTER_LOG_MSG, name, excludedMsg(isExcluded), httpRequest.getServletPath()));
        }

        if (isExcluded)
        {
            chain.doFilter(httpRequest, response);
        }
        else
        {
            // let the real sitemesh filter do its thing
            super.doFilter(httpRequest, response, chain);
        }
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }

    /*
     * Returns true if any of the exclusion headers is set on the request.
     */
    private boolean hasExcludeHeader(HttpServletRequest request)
    {
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements())
        {
            String headerName = (String) headerNames.nextElement();
            if (excludedHeaderNames.get().contains(headerName.toLowerCase(Locale.US)))
            {
                return true;
            }
        }

        return false;
    }

    private boolean isExcluded(String servletPath)
    {
        return matchesExact(servletPath) || matchesWildcard(servletPath);
    }

    private boolean matchesExact(final String servletPath)
    {
        return excludedExactPatterns.contains(servletPath);
    }

    private boolean matchesWildcard(final String servletPath)
    {
        for (String wildcardPattern : excludedWildcardPatterns)
        {
            if (servletPath.startsWith(wildcardPattern))
            {
                return true;
            }
        }
        return false;
    }

    private void parseExcludedPaths(String pathsParamValue)
    {
        if (StringUtils.isBlank(pathsParamValue))
        {
            log.warn("No excluded paths configured for filter '{}'", name);
            return;
        }
        for (String path : pathsParamValue.split(SEPARATOR))
        {
            addToPaths(path.trim());
        }
    }

    private ImmutableSet<String> parseExcludeHeaderNames(String headerNames)
    {
        if (isBlank(headerNames))
        {
            log.warn("No excluded paths configured for filter '{}'", name);
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(Iterables.transform(Arrays.asList(StringUtils.split(headerNames, SEPARATOR)), new Function<String, String>()
        {
            @Override
            public String apply(@Nullable String from)
            {
                // trim each header
                return from != null ? from.trim().toLowerCase(Locale.US) : null;
            }
        }));
    }

    private void addToPaths(final String path)
    {
        if (hasWildcardPattern(path))
        {
            excludedWildcardPatterns.add(removeWildcard(path));
        }
        else
        {
            excludedExactPatterns.add(path);
        }
    }

    private boolean hasWildcardPattern(final String path)
    {
        return path.endsWith(WILDCARD);
    }

    private String removeWildcard(String pathPattern)
    {
        return pathPattern.substring(0, pathPattern.length() - WILDCARD.length());
    }


    private void logInit()
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format(INIT_LOG_MSG, name, excludedExactPatterns, excludedWildcardPatterns, excludedHeaderNames));
        }
    }

    private String excludedMsg(boolean excluded)
    {
        return excluded ? "EXCLUDED" : "ACCEPTED";
    }
}
