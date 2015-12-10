package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestI18nBean
{
    public static final Locale DEFAULT_LOCALE = Locale.CANADA;
    public static final Locale APP_USER_LOCALE = Locale.FRENCH;
    public static final Locale USER_LOCALE = Locale.ENGLISH;

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @AvailableInContainer(interfaceClass = UserLocaleStore.class)
    private MockUserLocaleStore store = new MockUserLocaleStore(DEFAULT_LOCALE);

    @AvailableInContainer
    private I18nHelper.BeanFactory beanFactory = new NoopI18nFactory();

    private ApplicationUser appUserWithoutProperties = new MockApplicationUser("appUserWithoutProperties");
    private ApplicationUser appUserWithoutLocale = new MockApplicationUser("appUserWithoutLocale");
    private ApplicationUser appUserWithLocale = new MockApplicationUser("appUserWithLocale");

    private User userWithoutProperties = new MockUser("userWithoutProperties");
    private User userWithoutLocale = new MockUser("userWithoutLocale");
    private User userWithLocale = new MockUser("userWithLocale");

    @Before
    public void setup()
    {
        store.setLocale(appUserWithLocale, APP_USER_LOCALE);
        store.setLocale(userWithLocale, USER_LOCALE);
    }

    @Test
    public void returnsDefaultLocaleForNullUser()
    {
        assertThat(I18nBean.getLocaleFromUser((ApplicationUser)null), equalTo(DEFAULT_LOCALE));
        assertThat(I18nBean.getLocaleFromUser((User)null), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsDefaultLocaleForUserWithoutProperties()
    {
        assertThat(I18nBean.getLocaleFromUser(appUserWithoutProperties), equalTo(DEFAULT_LOCALE));
        assertThat(I18nBean.getLocaleFromUser(userWithoutProperties), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsDefaultLocaleForUserWithoutLocale()
    {
        assertThat(I18nBean.getLocaleFromUser(appUserWithoutLocale), equalTo(DEFAULT_LOCALE));
        assertThat(I18nBean.getLocaleFromUser(userWithoutLocale), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsLocaleForUserWithConfiguredLocale()
    {
        assertThat(I18nBean.getLocaleFromUser(appUserWithLocale), equalTo(APP_USER_LOCALE));
        assertThat(I18nBean.getLocaleFromUser(userWithLocale), equalTo(USER_LOCALE));
    }

    @Test
    public void noArgConstructorUsesDefaultLocale()
    {
        final I18nBean i18nBean = new I18nBean();
        assertThat(i18nBean.getLocale(), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void canBeCreatedWithLocale()
    {
        final I18nBean i18nBean = new I18nBean(Locale.JAPANESE);
        assertThat(i18nBean.getLocale(), equalTo(Locale.JAPANESE));
    }

    @Test
    public void createdWithUsersLocale()
    {
        I18nBean i18nBean = new I18nBean(userWithLocale);
        assertThat(i18nBean.getLocale(), equalTo(USER_LOCALE));

        i18nBean = new I18nBean(appUserWithLocale);
        assertThat(i18nBean.getLocale(), equalTo(APP_USER_LOCALE));
    }

    @Test
    public void createdWithDefaultLocaleIfUserHasNoLocale()
    {
        I18nBean i18nBean = new I18nBean(userWithoutLocale);
        assertThat(i18nBean.getLocale(), equalTo(DEFAULT_LOCALE));

        i18nBean = new I18nBean(appUserWithoutProperties);
        assertThat(i18nBean.getLocale(), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void createdWithDelegateI18nHelper()
    {
        I18nBean i18nBean = new I18nBean(new NoopI18nHelper(Locale.GERMANY));
        assertThat(i18nBean.getLocale(), equalTo(Locale.GERMANY));
    }

    @Test
    public void createdWithLocaleString()
    {
        I18nBean i18nBean = new I18nBean(Locale.TAIWAN.toString());
        assertThat(i18nBean.getLocale(), equalTo(Locale.TAIWAN));
    }

    @Test
    public void getTextTranslatesUsingDelegate()
    {
        final NoopI18nHelper delegate = new NoopI18nHelper();
        final I18nBean bean = new I18nBean(delegate);

        assertThat(bean.getText("test"), equalTo(delegate.getText("test")));
        assertThat(bean.getText("test", "value"), equalTo(delegate.getText("test", "value")));
        assertThat(bean.getText("test", 1), equalTo(delegate.getText("test", 1)));
        assertThat(bean.getText("test", 1, 2, 3), equalTo(delegate.getText("test", 1, 2, 3)));
        assertThat(bean.getText("test", "one", "two"), equalTo(delegate.getText("test", "one", "two")));
        assertThat(bean.getText("test", "one", "two", "three"),
                equalTo(delegate.getText("test", "one", "two", "three")));
        assertThat(bean.getText("test", 1, 2, 3, 4), equalTo(delegate.getText("test", 1, 2, 3, 4)));
        assertThat(bean.getText("test", "one", "two", "three", "four"),
                equalTo(delegate.getText("test", "one", "two", "three", "four")));

        assertThat(bean.getText("test", "one", "two", "three", "four", "five"),
                equalTo(delegate.getText("test", "one", "two", "three", "four", "five")));

        assertThat(bean.getText("test", "one", "two", "three", "four", "five", "six"),
                equalTo(delegate.getText("test", "one", "two", "three", "four", "five", "six")));

        assertThat(bean.getText("test", "one", "two", "three", "four", "five", "six", "seven"),
                equalTo(delegate.getText("test", "one", "two", "three", "four", "five", "six", "seven")));

        assertThat(bean.getText("test", 1, 2, 3, 4, 5, 6, 7),
                equalTo(delegate.getText("test", 1, 2, 3, 4, 5, 6, 7)));

        assertThat(bean.getText("test", "one", "two", "three", "four", "five", "six", "seven", 8),
                equalTo(delegate.getText("test", "one", "two", "three", "four", "five", "six", "seven", 8)));

        assertThat(bean.getText("test", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"),
                equalTo(delegate.getText("test", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")));
    }
}
