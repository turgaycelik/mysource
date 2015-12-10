package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.elements.AvatarId;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueType;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueTypeDialog;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueTypePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.JiraSudoFormDialog;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.pageobjects.xsrf.Xsrf;
import com.atlassian.jira.pageobjects.xsrf.XsrfDialog;
import com.atlassian.jira.pageobjects.xsrf.XsrfPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.Page;

import com.google.common.base.Function;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage.IssueType;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for adding issue types by the dialog.
 *
 * @since v5.0.1
 */
@ResetData
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.ISSUE_TYPES })
public class TestAddIssueTypes extends BaseJiraWebTest
{
    public static final String FIELD_NAME = "name";

    @Test
    public void testCreateIssueTypeDialog()
    {
        final ViewIssueTypesPage pages = jira.goTo(ViewIssueTypesPage.class);
        testIssueTypeAdd(new Function<Void, AddIssueTypeDialog>()
        {
            @Override
            public AddIssueTypeDialog apply(@Nullable final Void input)
            {
                return pages.addIssueType();
            }
        });
    }

    @Test
    public void testCreateIssueTypePage()
    {
        jira.goTo(ViewIssueTypesPage.class);
        testIssueTypeAdd(new Function<Void, AddIssueTypePage>()
        {
            @Override
            public AddIssueTypePage apply(@Nullable final Void input)
            {
                return jira.goTo(AddIssueTypePage.class);
            }
        });
    }

    private void testIssueTypeAdd(final Function<Void, ? extends AddIssueType> opener)
    {
        final IssueType issueTypeWithDescription = new IssueType("IssueTypeWithDescription",
            "Here is a description", false );

        final IssueType withoutDescription = new IssueType("IssueTypeWithoutDescription",
                null, false);

        final IssueType subtask = new IssueType("IssueTypeSubtask",
                null, true);

        backdoor.subtask().disable();

        AddIssueType addIssueType = opener.apply(null);

        assertFalse("We should not see the subtask selector when subtasks are disabled",
                addIssueType.isSubtasksEnabled());

        //Create an issue type with simple Name and Description.
        
        addIssueType.setName(issueTypeWithDescription.getName())
                .setDescription(issueTypeWithDescription.getDescription()).submit(ViewIssueTypesPage.class);

        //Create an issue type without a description.
        addIssueType = opener.apply(null);
        addIssueType.setName(withoutDescription.getName()).submit(ViewIssueTypesPage.class);

        backdoor.subtask().enable();

        addIssueType = opener.apply(null);

        assertTrue("We should see the subtask selector", addIssueType.isSubtasksEnabled());

        //Create a subtask.
        final ViewIssueTypesPage typesPage = addIssueType.setName(subtask.getName()).setSubtask(subtask.isSubtask()).submit(ViewIssueTypesPage.class);

        final List<IssueType> issueTypes = typesPage.getIssueTypes();
        assertThat(issueTypes, Matchers.<IssueType>hasItems(issueType(withoutDescription),
                issueType(issueTypeWithDescription), issueType(subtask)));
    }
    
    @Test
    public void testErrorsPage()
    {
        jira.goTo(ViewIssueTypesPage.class);
        testErrors(new Function<Void, AddIssueTypePage>()
        {
            @Override
            public AddIssueTypePage apply(@Nullable final Void input)
            {
                return jira.goTo(AddIssueTypePage.class);
            }
        });
    }
    
    @Test
    public void testErrorsDialog()
    {
        final ViewIssueTypesPage page = jira.goTo(ViewIssueTypesPage.class);
        testErrors(new Function<Void, AddIssueTypeDialog>()
        {
            @Override
            public AddIssueTypeDialog apply(@Nullable final Void input)
            {
                return page.addIssueType();
            }
        });
    }

    private void testErrors(final Function<Void, ? extends AddIssueType> opener)
    {
        final AddIssueType addIssueType = opener.apply(null);

        addIssueType.setName(null).submitFail();
        
        //Issue type with no name.
        assertThat(addIssueType.getFormErrors(), hasEntry(FIELD_NAME, "You must specify a name."));

        //Issue type with duplicate name.
        addIssueType.setName("Bug").submitFail();
        assertThat(addIssueType.getFormErrors(),
                hasEntry(FIELD_NAME, "An issue type with this name already exists."));

        //Make sure things can succeed.
        final ViewIssueTypesPage typesPage = addIssueType.setName("Bug2").submit(ViewIssueTypesPage.class);
        assertThat(typesPage.getIssueTypes(), Matchers.<IssueType>hasItem(
                issueType(new IssueType("Bug2", null, false))));
    }
    
    @Test
    public void testWebSudoDialog() throws IOException
    {
        final ViewIssueTypesPage page = jira.goTo(ViewIssueTypesPage.class);
        testWebsudo(new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraSudoFormDialog apply(@Nullable final Void input)
            {
                return page.addIssueTypeAndBind(JiraSudoFormDialog.class, AddIssueTypeDialog.ID);
            }
        }, AddIssueTypeDialog.class, ViewIssueTypesPage.class);
    }

    @Test
    public void testWebSudoPage() throws IOException
    {
        jira.goTo(ViewIssueTypesPage.class);
        testWebsudo(new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraWebSudoPage apply(@Nullable final Void input)
            {
                jira.visitDelayed(AddIssueTypePage.class);
                return pageBinder.bind(JiraWebSudoPage.class);
            }
        }, AddIssueTypePage.class, ViewProjectsPage.class);
    }

    private void testWebsudo(final Function<Void, JiraWebSudo> opener,
            final Class<? extends AddIssueType> nextPage, final Class<?> cancelPage)
    {
        final IssueType newIssueType = new IssueType("AnotherTest", null, false);

        backdoor.websudo().enable();

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.clearWebSudo();

        JiraWebSudo formDialog = opener.apply(null);
        formDialog = formDialog.authenticateFail("otherpassword");
        formDialog.cancel(cancelPage);

        formDialog = opener.apply(null);
        final AddIssueType issueTypeDialog = formDialog.authenticate(FunctTestConstants.ADMIN_PASSWORD, nextPage);
        final ViewIssueTypesPage typesPage = issueTypeDialog.setName(newIssueType.getName()).submit(ViewIssueTypesPage.class);
        assertThat(typesPage.getIssueTypes(), Matchers.<IssueType>hasItem(issueType(newIssueType)));

        backdoor.websudo().disable();
    }

    @Test
    public void testSubmitSessionTimeoutDialog()
    {
        final ViewIssueTypesPage page = jira.goTo(ViewIssueTypesPage.class);
        testSessionTimeout(ViewIssueTypesPage.class, new TimeoutHelper() {
            @Override
            public AddIssueType openAddIssueType()
            {
                return page.addIssueType();
            }

            @Override
            public Xsrf submitExpired(final AddIssueType addIssueType)
            {
                return addIssueType.submitFail(XsrfDialog.class, AddIssueTypeDialog.ID);
            }
        });
    }

    @Test
    public void testSubmitSessionTimeoutPage()
    {
        jira.goTo(ViewIssueTypesPage.class);
        testSessionTimeout(AddIssueTypePage.class, new TimeoutHelper() {
            @Override
            public AddIssueType openAddIssueType()
            {
                return pageBinder.navigateToAndBind(AddIssueTypePage.class);
            }

            @Override
            public Xsrf submitExpired(final AddIssueType addIssueType)
            {
                return addIssueType.submitFail(XsrfPage.class);
            }
        });
    }

    /**
     * Callback for abstracting page/dialog differences.
     */
    interface TimeoutHelper
    {
        AddIssueType openAddIssueType();
        Xsrf submitExpired(AddIssueType addIssueType);
    }
    
    private <P extends Page> void testSessionTimeout(final Class<P> nextPage, final TimeoutHelper timeoutHelper)
    {
        final AddIssueType addIssueType = timeoutHelper.openAddIssueType();
        addIssueType.setName("name");

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.invalidateSession();

        // check that we get the "session expired" page
        final Xsrf xsrf = timeoutHelper.submitExpired(addIssueType);
        assertTrue(xsrf.isSessionExpired());
        assertTrue(xsrf.hasParamaters());

        final JiraLoginPage jiraLoginPage = xsrf.login();
        jiraLoginPage.loginAsSystemAdminAndFollowRedirect(nextPage);
    }

    public static IssueTypeMatcher issueType(final IssueType expected)
    {
        return new IssueTypeMatcher(expected);
    }

    private static class IssueTypeMatcher extends BaseMatcher<ViewIssueTypesPage.IssueType>
    {
        private final IssueType expected;

        private IssueTypeMatcher(final IssueType expected)
        {
            this.expected = expected;
        }

        @Override
        public boolean matches(final Object o)
        {
            if (o instanceof IssueType)
            {
                final IssueType actual = (IssueType) o;
                return StringUtils.equals(actual.getName(), expected.getName()) &&
                        StringUtils.equals(actual.getDescription(), expected.getDescription()) &&
                        actual.getAvatarId().equals(expected.getAvatarId()) &&
                        actual.isSubtask() == expected.isSubtask();
            }
            else
            {
                return false;
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendValue(expected);
        }
    }
}
