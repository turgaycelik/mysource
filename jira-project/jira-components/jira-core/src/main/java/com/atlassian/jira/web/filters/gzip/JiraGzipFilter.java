/*
 * Copyright (c) 2002-2007
 * All rights reserved.
 */

package com.atlassian.jira.web.filters.gzip;

import java.io.IOException;

import com.atlassian.gzipfilter.GzipFilter;
import com.atlassian.gzipfilter.integration.GzipFilterIntegration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import org.apache.log4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.jira.config.CoreFeatures.ON_DEMAND;

public class JiraGzipFilter extends GzipFilter
{
    private static final Logger log = Logger.getLogger(JiraGzipFilter.class);
    private static final String ALREADY_FILTERED = GzipFilter.class.getName() + "_already_filtered";

    public JiraGzipFilter()
    {
        super(createGzipIntegration());
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException
    {
        /**
         *
         * JRADEV-21029 : when an error occurs, there is made an internal redirection but request stays the same.
         *
         * This behaviour causes a situation when request is already marked as filtered with gzip filter, but response
         * content is actually not gzipped.
         * From the browser point of view it looks like corrupted content (header "Content-Encoding: gzip"
         * with plain-text content )
         *
         * Here we are detecting that an error occured and un-marking filter as applied
         *
         */
        //
        if ( req.getAttribute("javax.servlet.error.request_uri") != null && req.getAttribute(ALREADY_FILTERED) != null){
            req.setAttribute(ALREADY_FILTERED, null);
        }

        super.doFilter(req, res, chain);
    }

    private static GzipFilterIntegration createGzipIntegration()
    {
        if (ON_DEMAND.isSystemPropertyEnabled())
        {
            return new JiraOnDemandGzipFilterIntegration();
        }

        return new JiraGzipFilterIntegration();
    }

    private static class JiraGzipFilterIntegration implements GzipFilterIntegration
    {
        public boolean useGzip()
        {
            try
            {
                // normally we would use GzipCompression here, but if we do that then we end up deadlocking inside
                // ComponentAccessor when a web request comes in and JIRA is being restarted after XML import. sooooo,
                // instead we do a bit of copy & paste to achieve the same effect.
                //
                // basically I fought the ComponentAccessor and the ComponentAccessor won.
                return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_WEB_USEGZIP);
            }
            catch (RuntimeException e)
            {
                log.debug("Cannot get application properties, defaulting to no GZip compression");
                return false;
            }
        }

        public String getResponseEncoding(HttpServletRequest httpServletRequest)
        {
            return ComponentAccessor.getApplicationProperties().getEncoding();
        }
    }

    /**
     * Forces GZIP to off in OnDemand: the content will be GZIPed by our proxy before it leave DC.
     */
    private static class JiraOnDemandGzipFilterIntegration extends JiraGzipFilterIntegration
    {
        @Override
        public boolean useGzip()
        {
            return false;
        }
    }
}
