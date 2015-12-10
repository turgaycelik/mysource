package com.atlassian.jira.util.resourcebundle;

import org.junit.Test;

import java.util.Locale;

import static com.atlassian.jira.util.resourcebundle.ResourceLoaderInvocation.Mode.HELP;
import static com.atlassian.jira.util.resourcebundle.ResourceLoaderInvocation.Mode.I18N;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.3
 */
public class TestResourceLoaderInvocation
{
    private MockResourceLoaderInvocation invocation = new MockResourceLoaderInvocation();

    @Test
    public void localeChangesLocaleSetting()
    {
        assertThat(invocation.getMode(), equalTo(I18N));
        assertThat(invocation.getLocale(), equalTo(Locale.getDefault()));
        assertThat(invocation.locale(Locale.FRANCE).getLocale(), equalTo(Locale.FRANCE));
        assertThat(invocation.getLocale(), equalTo(Locale.FRANCE));
        assertThat(invocation.getMode(), equalTo(I18N));
    }

    @Test
    public void possibleToChangeType()
    {
        final Locale locale = Locale.getDefault();

        assertThat(invocation.getLocale(), equalTo(locale));
        assertThat(invocation.getMode(), equalTo(I18N));
        assertThat(invocation.help().getMode(), equalTo(HELP));
        assertThat(invocation.getLocale(), equalTo(locale));
        assertThat(invocation.getMode(), equalTo(HELP));
        assertThat(invocation.languages().getMode(), equalTo(I18N));
        assertThat(invocation.getLocale(), equalTo(locale));
        assertThat(invocation.getMode(), equalTo(I18N));
    }
}
