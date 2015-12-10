package com.atlassian.jira.auditing.handlers;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.user.AutoUserUpdatedEvent;
import com.atlassian.crowd.event.user.ResetPasswordEvent;
import com.atlassian.crowd.event.user.UserEditedEvent;
import com.atlassian.crowd.event.user.UserRenamedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.auditing.AffectedUser;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.ChangedValueImpl;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
@RunWith (ListeningMockitoRunner.class)
public class TestUserEventHandlerImpl
{
    @Mock
    User user;

    @Mock
    User originalUser;

    @Mock
    AutoUserUpdatedEvent autoUserUpdatedEvent;

    @Mock
    UserEditedEvent userEditedEvent;

    @Mock
    ResetPasswordEvent resetPasswordEvent;

    @Mock
    UserRenamedEvent userRenamedEvent;

    @Mock
    I18nHelper.BeanFactory beanFactory;

    @Mock
    I18nHelper i18nHelper;

    @Mock
    UserKeyService userKeyService;

    @Mock
    UserManager userManager;

    @Mock
    Directory directory;

    @Before
    public void setUp()
    {
        new MockComponentWorker()
                .addMock(I18nHelper.BeanFactory.class, beanFactory)
                .addMock(UserKeyService.class, userKeyService)
                .addMock(UserManager.class, userManager).init();
        when(beanFactory.getInstance(Locale.ENGLISH)).thenReturn(i18nHelper);
        when(i18nHelper.getText(anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return "i18n." + invocation.getArguments()[0];
            }
        });
        when(userKeyService.getKeyForUsername("username")).thenReturn("userkey");

        when(user.getName()).thenReturn("username");
        when(originalUser.getName()).thenReturn("old username");
        when(user.getDisplayName()).thenReturn("display name");
        when(originalUser.getDisplayName()).thenReturn("old display name");
        when(user.isActive()).thenReturn(true);
        when(originalUser.isActive()).thenReturn(false);
        when(userManager.getDirectory(anyLong())).thenReturn(directory);
    }

    @Test
    public void testUserAutoUpdated()
    {
        when(autoUserUpdatedEvent.getUser()).thenReturn(user);
        when(autoUserUpdatedEvent.getOriginalUser()).thenReturn(originalUser);

        RecordRequest request = new UserEventHandlerImpl().handleUserUpdatedEvent(autoUserUpdatedEvent).get();

        assertThat(request.getCategory(), equalTo(AuditingCategory.USER_MANAGEMENT));
        assertThat(request.getSummary(), equalTo("i18n.jira.auditing.user.updated"));
        assertThat(request.getObjectItem(), associatedUser("username", "userkey"));
        Matcher[] changedValues = { changedValue("i18n.common.words.username", "old username", "username"),
                changedValue("i18n.common.words.fullname", "old display name", "display name"),
                changedValue("i18n.admin.common.phrases.active.inactive", "Inactive", "Active") };
        Matcher<Iterable<? super ChangedValue>> hasChangedValues = CoreMatchers.hasItems(changedValues);
        assertThat(request.getChangedValues(), hasChangedValues);
    }

    @Test
    public void testUserPasswordReset()
    {
        when(resetPasswordEvent.getUser()).thenReturn(user);

        RecordRequest request = new UserEventHandlerImpl().handleUserUpdatedEvent(resetPasswordEvent).get();

        assertThat(request.getCategory(), equalTo(AuditingCategory.USER_MANAGEMENT));
        assertThat(request.getSummary(), equalTo("i18n.jira.auditing.user.password.reset"));
        assertThat(request.getObjectItem(), associatedUser("username", "userkey"));
    }

    @Test
    public void testUserRenamed()
    {
        when(userRenamedEvent.getUser()).thenReturn(user);
        when(userRenamedEvent.getOldUsername()).thenReturn("old username");

        RecordRequest request = new UserEventHandlerImpl().handleUserUpdatedEvent(userRenamedEvent).get();

        assertThat(request.getCategory(), equalTo(AuditingCategory.USER_MANAGEMENT));
        assertThat(request.getSummary(), equalTo("i18n.jira.auditing.user.renamed"));
        assertThat(request.getObjectItem(), associatedUser("username", "userkey"));
        Matcher<Iterable<? super ChangedValue>> hasChangedValues = hasItem(changedValue("i18n.common.words.username", "old username", "username"));
        assertThat(request.getChangedValues(), hasChangedValues);
    }

    @Test
    public void testUserUpdated()
    {
        when(userEditedEvent.getUser()).thenReturn(user);
        when(userEditedEvent.getOriginalUser()).thenReturn(originalUser);

        RecordRequest request = new UserEventHandlerImpl().handleUserUpdatedEvent(userEditedEvent).get();

        assertThat(request.getCategory(), equalTo(AuditingCategory.USER_MANAGEMENT));
        assertThat(request.getSummary(), equalTo("i18n.jira.auditing.user.updated"));
        assertThat(request.getObjectItem(), associatedUser("username", "userkey"));
        Matcher[] changedValues = { changedValue("i18n.common.words.username", "old username", "username"),
                changedValue("i18n.common.words.fullname", "old display name", "display name"),
                changedValue("i18n.admin.common.phrases.active.inactive", "Inactive", "Active") };
        Matcher<Iterable<? super ChangedValue>> hasChangedValues = CoreMatchers.hasItems(changedValues);
        assertThat(request.getChangedValues(), hasChangedValues);
    }

    @Test
    public void testUserUpdatedEventIsAbstract() throws Exception
    {
        assertTrue("We assume in this handler that UserUpdatedEvent is abstract, but it is not",
                Modifier.isAbstract(UserUpdatedEvent.class.getModifiers()));
    }

    @Test
    public void testAllUserUpdatedEventsAreSupported() throws Exception
    {
        final Set<Class<? extends UserUpdatedEvent>> subTypesOf = getSubTypesOf("com.atlassian.crowd", UserUpdatedEvent.class);
        assertTrue("No subtypes of UserUpdatedEvent found.",subTypesOf.size() > 0);
        for (Class<? extends UserUpdatedEvent> aClass : subTypesOf)
        {
            final UserUpdatedEvent mock = Mockito.mock(aClass);
            when(mock.getUser()).thenReturn(user);
            new UserEventHandlerImpl().handleUserUpdatedEvent(mock);
        }
    }

    @Test
    public void testAllMethodsOnUserAreConsideredForChangedValues() throws Exception
    {
        when(userEditedEvent.getUser()).thenReturn(user);

        new UserEventHandlerImpl().handleUserUpdatedEvent(userEditedEvent);

        verifyAllMethodsCalledOn(user, User.class,
                "getName",
                "getDirectoryId",
                "getDisplayName",
                "getEmailAddress",
                "isActive");
    }

    private Set<Class<? extends UserUpdatedEvent>> getSubTypesOf(final String prefix, final Class<UserUpdatedEvent> type)
    {
        return new Reflections(prefix).getSubTypesOf(type);
    }

    private BaseMatcher<ChangedValue> changedValue(
            @Nonnull final String name, @Nonnull final String from, @Nonnull final String to)
    {
        return new BaseMatcher<ChangedValue>()
        {
            @Override
            public boolean matches(final Object o)
            {
                if (!(o instanceof ChangedValue))
                {
                    return false;
                }
                final ChangedValue changedValue = (ChangedValue) o;
                return name.equals(changedValue.getName())
                        && from.equals(changedValue.getFrom())
                        && to.equals(changedValue.getTo());
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText(new ChangedValueImpl(name, from, to).toString());
            }
        };
    }

    private BaseMatcher<AssociatedItem> associatedUser(@Nonnull final String username, @Nonnull final String userkey)
    {
        return new BaseMatcher<AssociatedItem>()
        {
            @Override
            public boolean matches(final Object o)
            {
                if (!(o instanceof AffectedUser))
                {
                    return false;
                }
                final AffectedUser affectedUser = (AffectedUser) o;

                return username.equals(affectedUser.getObjectName()) && userkey.equals(affectedUser.getObjectId());
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("AssociatedUser{" + username + ", " + userkey + "}");
            }
        };
    }

    private <T> void verifyAllMethodsCalledOn(final T mock, final Class<T> aClass, String ... includeMehotds)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        for (String name : includeMehotds)
        {
            final T verify = verify(mock, atLeastOnce());
            aClass.getMethod(name).invoke(verify);
        }
    }
}
