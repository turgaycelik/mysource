package com.atlassian.jira.plugin.webresource;

import com.atlassian.plugin.servlet.ResourceDownloadUtils;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Forwards static resource requests with web-resource prefix to the actual resource and sets the required caching
 * headers.
 *
 * @since v6.0
 */
public class CachingResourceDownloadRewriteRule extends RewriteRule
{
    private static final Pattern NON_WEB_INF_RESOURCES_URI_PATTERN = Pattern.compile("^/s/(.*)/_/((?i)(?!WEB-INF).*)");

        @Override
        public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response)
        {
            final String normalisedRequestUriPath;

            try
            {
                // Apply URI normalisation to the incoming request, to avoid path transversal into WEB-INF resources
                normalisedRequestUriPath = getNormalisedPathFrom(request);
            }
            catch (URISyntaxException invalidUriInRequest)
            {
                return null;
            }

            final Matcher nonWebInfResourcesPatternMatcher =
                    NON_WEB_INF_RESOURCES_URI_PATTERN.matcher(normalisedRequestUriPath);

            if (!nonWebInfResourcesPatternMatcher.matches())
            {
                return null;
            }

            final String rewrittenUriPath = "/" + nonWebInfResourcesPatternMatcher.group(2);

            final String rewrittenUrl = request.getContextPath() + rewrittenUriPath;

            return new RewriteMatch() {
                
                @Override
                public String getMatchingUrl()
                {
                    return rewrittenUrl;
                }

                @Override
                public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                        IOException
                {
                    ResourceDownloadUtils.addPublicCachingHeaders(request, response);
                    request.setAttribute("cachingHeadersApplied", true);
                    request.setAttribute("_statichash", nonWebInfResourcesPatternMatcher.group(1));
                    request.getRequestDispatcher(rewrittenUriPath).forward(request, response);

                    return true;
                }
            };
        }

    private String getNormalisedPathFrom(final HttpServletRequest request) throws URISyntaxException
    {
        return new URI(stripContextFrom(request)).normalize().toString();
    }

    private String stripContextFrom(final HttpServletRequest request)
    {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
