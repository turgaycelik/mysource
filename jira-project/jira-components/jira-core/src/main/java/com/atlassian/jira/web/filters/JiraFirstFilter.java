package com.atlassian.jira.web.filters;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.debug.BreakpointReadyHttpServletRequest;
import com.atlassian.jira.web.debug.BreakpointReadyHttpServletResponse;
import com.atlassian.jira.web.debug.ClearDomainFromCookiesHttpServletResponse;
import com.atlassian.jira.web.filters.steps.ChainedFilterStepRunner;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.atlassian.jira.web.filters.steps.i18n.I18nTranslationsModeThreadlocaleStep;
import com.atlassian.jira.web.filters.steps.instrumentation.InstrumentationStep;
import com.atlassian.jira.web.filters.steps.newrelic.NewRelicTransactionNameStep;
import com.atlassian.jira.web.filters.steps.requestinfo.RequestInfoFirstStep;
import com.atlassian.jira.web.filters.steps.senderror.CaptureSendErrorMessageStep;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the first filter that is run during a web request to JIRA.  At this point you know that the request is
 * pristine.
 * <p/>
 * You are at the outer most entry point for the request for the filter chain.
 * <p/>
 * After extensive market research, this filter has been carefully named to indicate that its the "first" filter and it
 * should remain that way.
 *
 * @since v4.2
 */
public class JiraFirstFilter extends ChainedFilterStepRunner
{

    @Override
    protected List<FilterStep> getFilterSteps()
    {
        return Lists.newArrayList(
                new RequestInfoFirstStep(),
                new InstrumentationStep(),
                new CaptureSendErrorMessageStep(),
                new I18nTranslationsModeThreadlocaleStep(),
                new NewRelicTransactionNameStep()
        );
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        Pair<ServletRequest, ServletResponse> debugWrapped = wrap(servletRequest, servletResponse);

        super.doFilter(debugWrapped.first(), debugWrapped.second(), filterChain);
    }

    private Pair<ServletRequest, ServletResponse> wrap(final ServletRequest servletRequest, final ServletResponse servletResponse)
    {
        // only in dev mode do we have these classes.  There are only there as a development aid
        if (JiraSystemProperties.isDevMode())
        {
            return Pair.<ServletRequest, ServletResponse>of(
                    new BreakpointReadyHttpServletRequest((HttpServletRequest) servletRequest),
                    ComponentAccessor.getComponentOfType(FeatureManager.class).isOnDemand() ?
                            new ClearDomainFromCookiesHttpServletResponse((HttpServletResponse) servletResponse) :
                            new BreakpointReadyHttpServletResponse((HttpServletResponse) servletResponse)

            );
        }
        return Pair.of(servletRequest, servletResponse);
    }
}
