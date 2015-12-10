package com.atlassian.jira.web.action.admin.translation;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.IteratorEnumeration;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestTranslationManagerImpl
{
    private static final String TRANSLATION_KEY_PREFIX = "jira.translation.priority";
    private static final String DEFAULT_LOCALE = "es_ES";
    private static final String NON_DEFAULT_LOCALE = "en_UK";

    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private I18nHelper.BeanFactory beanFactory;

    private TranslationManagerImpl translationManager;

    @Before
    public void setUp()
    {
        translationManager = new TranslationManagerImpl(authenticationContext, applicationProperties, beanFactory);
        when(applicationProperties.getDefaultLocale()).thenReturn(localeFor(DEFAULT_LOCALE));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheNameTranslatedFromTheDatabaseIfItExists()
    {
        String issueConstantId = "1";
        PropertySet databaseTranslations = mockDatabaseTranslations(translationDatabaseKey(issueConstantId, DEFAULT_LOCALE), "expected name", "any description");
        IssueConstant issueConstant = mockIssueConstant(issueConstantId, databaseTranslations);

        boolean getName = true;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getName, DEFAULT_LOCALE, mock(I18nHelper.class));

        assertThat(translatedName, is("expected name"));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheDescriptionTranslatedFromTheDatabaseIfItExists()
    {
        String issueConstantId = "1";
        PropertySet databaseTranslations = mockDatabaseTranslations(translationDatabaseKey(issueConstantId, DEFAULT_LOCALE), "any name", "expected description");
        IssueConstant issueConstant = mockIssueConstant(issueConstantId, databaseTranslations);

        boolean getDescription = false;
        String translatedDescription = translationManager.getIssueConstantTranslation(issueConstant, getDescription, DEFAULT_LOCALE, mock(I18nHelper.class));

        assertThat(translatedDescription, is("expected description"));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheNameTranslatedFromI18NIfLocaleIsDifferentThanDefault()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(nameTranslationI18NKey(issueConstantName))).thenReturn("expected name");

        boolean getName = true;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getName, NON_DEFAULT_LOCALE, i18n);

        assertThat(translatedName, is("expected name"));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheDescriptionTranslatedFromI18NIfLocaleIsDifferentThanDefault()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(descriptionTranslationI18NKey(issueConstantName))).thenReturn("expected description");

        boolean getDescription = false;
        String translatedDescription = translationManager.getIssueConstantTranslation(issueConstant, getDescription, NON_DEFAULT_LOCALE, i18n);

        assertThat(translatedDescription, is("expected description"));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheNameFromTheIssueConstantObjectIfTheReturnedTranslationFromI18NIsNull()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(nameTranslationI18NKey(issueConstantName))).thenReturn(null);

        boolean getName = true;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getName, NON_DEFAULT_LOCALE, i18n);

        assertThat(translatedName, is(issueConstantName));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheDescriptionFromTheIssueConstantObjectIfTheReturnedTranslationFromI18NIsNull()
    {
        String issueConstantId = "1";
        String issueConstantName = "name";
        String issueConstantDescription = "description";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName, issueConstantDescription);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(descriptionTranslationI18NKey(issueConstantName))).thenReturn(null);

        boolean getDescription = false;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getDescription, NON_DEFAULT_LOCALE, i18n);

        assertThat(translatedName, is(issueConstantDescription));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheNameFromTheIssueConstantObjectIfTheReturnedTranslationFromI18NIsTheTranslationKey()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(nameTranslationI18NKey(issueConstantName))).thenReturn(nameTranslationI18NKey(issueConstantName));

        boolean getName = true;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getName, NON_DEFAULT_LOCALE, i18n);

        assertThat(translatedName, is(issueConstantName));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheDescriptionFromTheIssueConstantObjectIfTheReturnedTranslationFromI18NIsTheTranslationKey()
    {
        String issueConstantId = "1";
        String issueConstantName = "name";
        String issueConstantDescription = "description";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName, issueConstantDescription);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(descriptionTranslationI18NKey(issueConstantName))).thenReturn(descriptionTranslationI18NKey(issueConstantName));

        boolean getDescription = false;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getDescription, NON_DEFAULT_LOCALE, i18n);

        assertThat(translatedName, is(issueConstantDescription));
    }

    @Test
    public void getIssueConstantTranslationFallsBackToUsersI18NIfTheOnePassedAsParameterIsNull()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        I18nHelper userI18n = mock(I18nHelper.class);
        when(authenticationContext.getI18nHelper()).thenReturn(userI18n);

        translationManager.getIssueConstantTranslation(issueConstant, true, NON_DEFAULT_LOCALE, null);

        verify(userI18n).getText(anyString());
    }

    @Test
    public void getIssueConstantTranslationReturnsTheNameTranslatedFromI18NIfLocaleIsTheDefaultAndTheIssueConstantNameIsTheDefault()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(nameTranslationI18NKey(issueConstantName))).thenReturn("expected name");

        mockDefaultName(issueConstantName, issueConstantName);

        boolean getName = true;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getName, DEFAULT_LOCALE, i18n);

        assertThat(translatedName, is("expected name"));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheDescriptionTranslatedFromI18NIfLocaleIsTheDefaultAndTheIssueConstantDescriptionIsTheDefault()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        String issueConstantDescription = "description";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName, issueConstantDescription);

        I18nHelper i18n = mock(I18nHelper.class);
        when(i18n.getText(descriptionTranslationI18NKey(issueConstantName))).thenReturn("expected description");

        mockDefaultDescription(issueConstantName, issueConstantDescription);

        boolean getDescription = false;
        String translatedDesc = translationManager.getIssueConstantTranslation(issueConstant, getDescription, DEFAULT_LOCALE, i18n);

        assertThat(translatedDesc, is("expected description"));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheNameFromTheIssueConstantObjectIfLocaleIsTheDefaultAndTheIssueConstantNameIsNotTheDefault()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName);

        mockDefaultName(issueConstantName, "some default name");

        boolean getName = true;
        String translatedName = translationManager.getIssueConstantTranslation(issueConstant, getName, DEFAULT_LOCALE, null);

        assertThat(translatedName, is(issueConstantName));
    }

    @Test
    public void getIssueConstantTranslationReturnsTheDescriptionFromTheIssueConstantObjectIfLocaleIsTheDefaultAndTheIssueConstantDescriptionIsNotTheDefault()
    {
        String issueConstantId = "1";
        String issueConstantName = "blocker";
        String issueConstantDesc = "some desc";
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(issueConstantId, issueConstantName, issueConstantDesc);

        mockDefaultDescription(issueConstantName, "some default description");

        boolean getDescription = false;
        String translatedDesc = translationManager.getIssueConstantTranslation(issueConstant, getDescription, DEFAULT_LOCALE, null);

        assertThat(translatedDesc, is(issueConstantDesc));
    }

    private String translationDatabaseKey(String issueConstantId, String locale)
    {
        return TRANSLATION_KEY_PREFIX + issueConstantId + "." + locale;
    }

    private String nameTranslationI18NKey(final String issueConstantName)
    {
        return TRANSLATION_KEY_PREFIX + "." + issueConstantName + ".name";
    }

    private String descriptionTranslationI18NKey(final String issueConstantName)
    {
        return TRANSLATION_KEY_PREFIX + "." + issueConstantName + ".desc";
    }

    private IssueConstant mockIssueConstant(String id, PropertySet databaseTranslations)
    {
        IssueConstant issueConstant = mock(IssueConstant.class);

        GenericValue genericValue = mock(GenericValue.class);
        when(issueConstant.getGenericValue()).thenReturn(genericValue);
        when(issueConstant.getId()).thenReturn(id);

        when(genericValue.getEntityName()).thenReturn("Priority");
        when(issueConstant.getPropertySet()).thenReturn(databaseTranslations);

        return issueConstant;
    }

    private IssueConstant mockIssueConstantWithNoTranslationsOnDatabase(String id, String name)
    {
        IssueConstant issueConstant = mockIssueConstant(id, mock(PropertySet.class));
        when(issueConstant.getName()).thenReturn(name);
        return issueConstant;
    }

    private IssueConstant mockIssueConstantWithNoTranslationsOnDatabase(String id, String name, String description)
    {
        IssueConstant issueConstant = mockIssueConstantWithNoTranslationsOnDatabase(id, name);
        when(issueConstant.getDescription()).thenReturn(description);
        return issueConstant;
    }

    private PropertySet mockDatabaseTranslations(String key, String translatedName, String translatedDescription)
    {
        PropertySet propertySet = mock(PropertySet.class);
        when(propertySet.getString(key)).thenReturn(translatedName + "\n" + translatedDescription);
        return propertySet;
    }

    private Locale localeFor(final String localeString)
    {
        String[] parts = localeString.split("_");
        return new Locale(parts[0], parts[1]);
    }

    private void mockDefaultName(String issueConstantName, String defaultName)
    {
        I18nHelper defaultI18nHelper = mock(I18nHelper.class);
        when(defaultI18nHelper.getResourceBundle()).thenReturn(new ResourceBundleStub(nameTranslationI18NKey(issueConstantName), defaultName));
        when(beanFactory.getInstance(Locale.ROOT)).thenReturn(defaultI18nHelper);
    }

    private void mockDefaultDescription(String issueConstantName, String defaultDescription)
    {
        I18nHelper defaultI18nHelper = mock(I18nHelper.class);
        when(defaultI18nHelper.getResourceBundle()).thenReturn(new ResourceBundleStub(descriptionTranslationI18NKey(issueConstantName), defaultDescription));
        when(beanFactory.getInstance(Locale.ROOT)).thenReturn(defaultI18nHelper);
    }

    private static final class ResourceBundleStub extends ResourceBundle
    {
        private final Map<String, String> properties;

        public ResourceBundleStub(String key, String value)
        {
            this.properties = ImmutableMap.of(key, value);
        }

        @Override
        protected Object handleGetObject(final String key)
        {
            return properties.get(key);
        }

        @Override
        public Enumeration<String> getKeys()
        {
            return IteratorEnumeration.fromIterable(properties.keySet());
        }
    }
}
