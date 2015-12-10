package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.cache.AbstractCachingFilter;
import com.atlassian.core.filters.cache.CachingStrategy;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * Applies no-cache headers in JIRA.
 *
 * @since v4.0
 */
public class JiraCachingFilter extends AbstractCachingFilter
{
    private static final CachingStrategy jiraCachingStrategy = new JiraCachingStrategy();
    private static final CachingStrategy iconCachingStrategy = new IconCachingStrategy();

    private static final CachingStrategy[] strategies = new CachingStrategy[] { jiraCachingStrategy, iconCachingStrategy };

    protected CachingStrategy[] getCachingStrategies()
    {
        return strategies;
    }


    //JRADEV-671 Cache Icons
    static class IconCachingStrategy implements CachingStrategy
    {
        private static final int ABOUT_ONE_YEAR=60*60*24*365;

        public boolean matches(final HttpServletRequest request)
        {
            final String servletPath = StringUtils.defaultString(request.getServletPath());
            return(servletPath.startsWith("/images/icons"));
        }

        public void setCachingHeaders(final HttpServletResponse response)
        {
            response.setHeader("Cache-Control", "max-age=" + ABOUT_ONE_YEAR);
        }
    }

    static class JiraCachingStrategy implements CachingStrategy
    {
        private static final Set<String> nonCacheableUriSet = new HashSet<String>();

        static
        {
            nonCacheableUriSet.add("/browse");
            nonCacheableUriSet.add("/issues");
        }

        public boolean matches(final HttpServletRequest request)
        {
            final String servletPath = StringUtils.defaultString(request.getServletPath());
            // JRA-8179 - do not add no-cache headers to attachments
            // The no-cache headers cause problems under HTTPS on older versions of IE
            // as the attacment cannot be downloaded.
            // See also: http://support.microsoft.com/default.aspx?scid=kb;en-us;812935
            if (request.isSecure() && servletPath.startsWith("/attachment"))
            {
                return false;
            }
            else
            {
                for (String nonCacheableUri : nonCacheableUriSet)
                {
                    if (servletPath.startsWith(nonCacheableUri))
                    {
                        return true;
                    }
                }
            }
            //all JSPs also match, and should have no-cache headers applied.
            final String uri = request.getRequestURI();
            return StringUtils.indexOf(uri, ".jsp") > 0;
        }

        public void setCachingHeaders(final HttpServletResponse response)
        {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // http 1.1
            response.setHeader("Pragma", "no-cache"); // http 1.0
            response.setDateHeader("Expires", 0); // prevent proxy caching
        }
    }
}
