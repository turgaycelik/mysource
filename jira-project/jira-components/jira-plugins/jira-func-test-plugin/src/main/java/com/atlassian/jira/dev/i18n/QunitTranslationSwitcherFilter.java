package com.atlassian.jira.dev.i18n;

import com.atlassian.core.filters.AbstractHttpFilter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter switches the locale to en_MOON for the current user and default locale when browsing to any /qunit page
 */
public class QunitTranslationSwitcherFilter extends AbstractHttpFilter
{
    private final QunitLocaleSwitcher qunitLocaleSwitcher;

    public QunitTranslationSwitcherFilter(final QunitLocaleSwitcher qunitLocaleSwitcher)
    {
        this.qunitLocaleSwitcher = qunitLocaleSwitcher;
    }

    @Override
    protected void doFilter(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        qunitLocaleSwitcher.disableTranslations();
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
