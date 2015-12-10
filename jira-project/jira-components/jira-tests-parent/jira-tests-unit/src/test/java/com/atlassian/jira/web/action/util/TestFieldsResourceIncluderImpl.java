package com.atlassian.jira.web.action.util;

import java.util.Locale;

import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.webresource.WebResourceManager;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.web.action.util.FieldsResourceIncluderImpl}.
 *
 * @since v4.2
 */
public class TestFieldsResourceIncluderImpl
{
    @Test
    public void testIncludeResourcesForCurrentUser()
    {
        final Locale expectedLocale = Locale.CANADA_FRENCH;

        final IMocksControl iMocksControl = EasyMock.createControl();
        final CalendarResourceIncluder includer = iMocksControl.createMock(CalendarResourceIncluder.class);
        final WebResourceManager wrm = iMocksControl.createMock(WebResourceManager.class);
        final JiraAuthenticationContext ctx = new MockSimpleAuthenticationContext(null, Locale.ENGLISH, new MockI18nHelper(expectedLocale));

        wrm.requireResource("jira.webresources:jira-fields");
        includer.includeForLocale(expectedLocale);

        iMocksControl.replay();

        final FieldsResourceIncluderImpl fieldsIncluder = new FieldsResourceIncluderImpl(ctx, wrm, includer);
        fieldsIncluder.includeFieldResourcesForCurrentUser();

        iMocksControl.verify();
    }
}
