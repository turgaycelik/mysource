package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.pageobjects.pages.admin.subtask.AddSubtaskType;
import com.atlassian.jira.pageobjects.pages.admin.subtask.AddSubtaskTypeDialog;
import com.atlassian.jira.pageobjects.pages.admin.subtask.AddSubtaskTypePage;
import com.atlassian.jira.pageobjects.pages.admin.subtask.ManageSubtasksPage;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Check that adding subtask dialog works as expected.
 *
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.ISSUE_TYPES, Category.SUB_TASKS })
public class TestAddSubtaskType extends BaseJiraWebTest
{
    public static final String DEFAULT_ICON_URL = "/images/icons/issuetypes/subtask_alternate.png";
    public static final String FIELD_NAME = "name";
    public static final String ICONURL_FIELD = "iconurl";

    @Test
    public void addSubtaskDialog()
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        final ManageSubtasksPage page = jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testAddSubtask(new Function<Void, AddSubtaskTypeDialog>()
        {
            @Override
            public AddSubtaskTypeDialog apply(@Nullable Void input)
            {
                return page.addSubtask();
            }
        });
    }

    @Test
    public void addSubtaskPage()
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testAddSubtask(new Function<Void, AddSubtaskTypePage>()
        {
            @Override
            public AddSubtaskTypePage apply(@Nullable Void input)
            {
                return jira.goTo(AddSubtaskTypePage.class);
            }
        });
    }

    private void testAddSubtask(Function<Void, ? extends AddSubtaskType> opener)
    {
        ManageSubtasksPage.Subtask noDescription = new ManageSubtasksPage.Subtask("NoDescription", null);
        ManageSubtasksPage.Subtask description = new ManageSubtasksPage.Subtask("WithDescription", "Description of Something");
        ManageSubtasksPage.Subtask customIcon = new ManageSubtasksPage.Subtask("CustomIcon", null);

        
        AddSubtaskType addSubtaskType = opener.apply(null);
        addSubtaskType.setName(noDescription.getName()).submitSuccess();
        
        addSubtaskType = opener.apply(null);
        addSubtaskType.setName(description.getName()).setDescription(description.getDescription()).submitSuccess();
        
        addSubtaskType = opener.apply(null);
        final ManageSubtasksPage subtasksPage = addSubtaskType.setName(customIcon.getName())
                .submitSuccess();

        List<ManageSubtasksPage.Subtask> subtaks = subtasksPage.getSubtaks();
        assertThat(subtaks, Matchers.<ManageSubtasksPage.Subtask>hasItems(
                subtask(noDescription), subtask(description), subtask(customIcon)));
    }

    @Test
    public void testErrorsDialog()
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        final ManageSubtasksPage page = jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testErrors(new Function<Void, AddSubtaskType>()
        {
            @Override
            public AddSubtaskType apply(@Nullable Void input)
            {
                return page.addSubtask();
            }
        });
    }

    @Test
    public void testErrorsPage()
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testErrors(new Function<Void, AddSubtaskType>()
        {
            @Override
            public AddSubtaskType apply(@Nullable Void input)
            {
                return pageBinder.navigateToAndBind(AddSubtaskTypePage.class);
            }
        });
    }

    private void testErrors(Function<Void, AddSubtaskType> opener)
    {
        AddSubtaskType dialog = opener.apply(null);

        dialog.setName(null).submitFail();

        //Issue type with no name.
        assertThat(dialog.getErrors(),
                hasEntry(FIELD_NAME, "You must specify a name for this new sub-task issue type."));

        //Issue type with duplicate name.
        dialog.setName("Bug").submitFail();
        assertThat(dialog.getErrors(),
                hasEntry(FIELD_NAME, "An issue type with this name already exists."));

        ManageSubtasksPage subtask = dialog.setName("Bug2").submitSuccess();
        assertThat(subtask.getSubtaks(),Matchers.<ManageSubtasksPage.Subtask>hasItem(
                subtask(new ManageSubtasksPage.Subtask("Bug2", null))));
    }

    @Test
    public void testWebSudoDialog() throws IOException
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        final ManageSubtasksPage page = jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testWebsudo(new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraSudoFormDialog apply(@Nullable Void input)
            {
                return page.addSubtaskAndBind(JiraSudoFormDialog.class, AddSubtaskTypeDialog.ID);
            }
        }, AddSubtaskTypeDialog.class, ManageSubtasksPage.class);
    }

    @Test
    public void testWebSudoPage() throws IOException
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testWebsudo(new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraWebSudoPage apply(@Nullable Void input)
            {
                jira.visitDelayed(AddSubtaskTypePage.class);
                return pageBinder.bind(JiraWebSudoPage.class);
            }
        }, AddSubtaskTypePage.class, ViewProjectsPage.class);
    }

    private void testWebsudo(Function<Void, JiraWebSudo> opener,
            Class<? extends AddSubtaskType> nextPage, Class<?> cancelPage)
    {
        ManageSubtasksPage.Subtask subtask = new ManageSubtasksPage.Subtask("WithDescription", "Description of Something");

        backdoor.websudo().enable();

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.clearWebSudo();

        JiraWebSudo websudo = opener.apply(null);
        websudo = websudo.authenticateFail("otherpassword");
        websudo.cancel(cancelPage);

        websudo = opener.apply(null);
        final AddSubtaskType issueTypeDialog = websudo.authenticate(FunctTestConstants.ADMIN_PASSWORD, nextPage);
        ManageSubtasksPage typesPage = issueTypeDialog.setName(subtask.getName())
                .setDescription(subtask.getDescription()).submitSuccess();
        assertThat(typesPage.getSubtaks(), Matchers.<ManageSubtasksPage.Subtask>hasItem(subtask(subtask)));

        backdoor.websudo().disable();
    }

    @Test
    public void testSubmitSessionTimeoutDialog()
    {
        backdoor.subtask().enable();
        final ManageSubtasksPage page = jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testSessionTimeout(ManageSubtasksPage.class, new TimeoutHelper()
        {
            @Override
            public AddSubtaskType openAddSubtaskType()
            {
                return page.addSubtask();
            }

            @Override
            public Xsrf submitExpired(AddSubtaskType addSubtaskType)
            {
                return addSubtaskType.submitFail(XsrfDialog.class, AddSubtaskTypeDialog.ID);
            }
        });
    }

    @Test
    public void testSubmitSessionTimeoutPage()
    {
        backdoor.subtask().enable();
        jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testSessionTimeout(AddSubtaskTypePage.class, new TimeoutHelper()
        {
            @Override
            public AddSubtaskType openAddSubtaskType()
            {
                return pageBinder.navigateToAndBind(AddSubtaskTypePage.class);
            }

            @Override
            public Xsrf submitExpired(AddSubtaskType addSubtaskType)
            {
                return addSubtaskType.submitFail(XsrfPage.class);
            }
        });
    }

    /**
     * Callback for abstracting page/dialog differences.
     */
    interface TimeoutHelper
    {
        AddSubtaskType openAddSubtaskType();
        Xsrf submitExpired(AddSubtaskType addSubtaskType);
    }

    private <P extends Page> void testSessionTimeout(Class<P> nextPage, TimeoutHelper timeoutHelper)
    {
        final AddSubtaskType addSubtaskType = timeoutHelper.openAddSubtaskType();
        addSubtaskType.setName("name");

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.invalidateSession();

        // check that we get the "session expired" page
        Xsrf xsrf = timeoutHelper.submitExpired(addSubtaskType);
        assertTrue(xsrf.isSessionExpired());
        assertTrue(xsrf.hasParamaters());

        final JiraLoginPage jiraLoginPage = xsrf.login();
        jiraLoginPage.loginAsSystemAdminAndFollowRedirect(nextPage);
    }

    private static SubtaskMatcher subtask(ManageSubtasksPage.Subtask expected)
    {
        return new SubtaskMatcher(expected);
    }

    private static class SubtaskMatcher extends BaseMatcher<ManageSubtasksPage.Subtask>
    {
        private final ManageSubtasksPage.Subtask expected;

        private SubtaskMatcher(ManageSubtasksPage.Subtask expected)
        {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object o)
        {
            if (o instanceof ManageSubtasksPage.Subtask)
            {
                ManageSubtasksPage.Subtask actual = (ManageSubtasksPage.Subtask) o;
                return StringUtils.equals(actual.getName(), expected.getName()) &&
                        StringUtils.equals(actual.getDescription(), expected.getDescription()) &&
                        StringUtils.endsWith(actual.getIconUrl(), expected.getIconUrl());
            }
            else
            {
                return false;
            }
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendValue(expected);
        }
    }
}
