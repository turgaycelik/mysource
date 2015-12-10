package com.atlassian.jira.config;

import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.JiraLocaleUtils;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestDefaultLocaleManager
{
    @Mock
    private JiraLocaleUtils jiraLocaleUtils;

    @Mock
    private I18nHelper.BeanFactory beanFactory;
    private DefaultLocaleManager defaultLocaleManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        defaultLocaleManager = new DefaultLocaleManager(jiraLocaleUtils, beanFactory);

    }

    @Test
    public void testValidateLocale()
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        //default locale is valid.
        defaultLocaleManager.validateUserLocale(null, "-1", errors);
        assertThat(errors.hasAnyErrors(), is(false));

        when(jiraLocaleUtils.getInstalledLocales()).thenReturn(Lists.newArrayList(new Locale("fr", "FR"), new Locale("en", "UK")));

        //lets try a valid locale
        defaultLocaleManager.validateUserLocale(null, "fr_FR", errors);
        assertThat(errors.hasAnyErrors(), is(false));

        final MockI18nHelper mockI18n = new MockI18nHelper();
        when(beanFactory.getInstance((User) null)).thenReturn(mockI18n);

        //finally an invalid locale!
        defaultLocaleManager.validateUserLocale(null, "invalid!", errors);
        assertThat(errors.hasAnyErrors(), is(true));
        assertThat(errors.getErrors().get("userLocale"), equalTo("preferences.invalid.locale [invalid!]"));

    }

}
