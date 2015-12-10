package com.atlassian.jira.webtest.webdriver.tests.admin.fields.screen;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.pageobjects.pages.admin.screen.AssociateIssueOperationToScreen;
import com.atlassian.jira.pageobjects.pages.admin.screen.AssociateIssueOperationToScreenDialog;
import com.atlassian.jira.pageobjects.pages.admin.screen.AssociateIssueOperationToScreenPage;
import com.atlassian.jira.pageobjects.pages.admin.screen.ConfigureScreenScheme;
import com.atlassian.jira.pageobjects.pages.admin.screen.ScreenOperation;
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
import com.google.common.collect.Maps;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test that checks associating an issue operation with a screen works (i.e. screen schemes work).
 *
 * @since v5.0.2
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.FIELDS })
public class TestAssociateIssueOperationToScreen extends BaseJiraWebTest
{
    private static final String SCREEN_DEFAULT = "Default Screen";
    private static final String SCREEN_RESOLVE = "Resolve Issue Screen";
    private static final String SCREEN_WORKFLOW = "Workflow Screen";
    private static final long SCREEN_SCHEME_ID = 1L;

    @Test
    public void associateScreenDialog()
    {
        backdoor.restoreBlankInstance();
        final ConfigureScreenScheme configureScreenScheme = jira.gotoLoginPage()
                .loginAsSysAdmin(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
        assertAssociateScreen(new Function<Void, AssociateIssueOperationToScreen>()
        {
            @Override
            public AssociateIssueOperationToScreen apply(@Nullable Void input)
            {
                return configureScreenScheme.associateScreen();
            }
        });
    }
    
    @Test
    public void associateScreenPage()
    {
        backdoor.restoreBlankInstance();
        jira.quickLoginAsSysadmin(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
        assertAssociateScreen(new Function<Void, AssociateIssueOperationToScreen>()
        {
            @Override
            public AssociateIssueOperationToScreen apply(@Nullable Void input)
            {
                return jira.goTo(AssociateIssueOperationToScreenPage.class, SCREEN_SCHEME_ID);
            }
        });
    }
    
    private void assertAssociateScreen(Function<Void, ? extends AssociateIssueOperationToScreen> opener)
    {
        AssociateIssueOperationToScreen associate = opener.apply(null);
        assertThat(associate.getOperations(), equalTo(asList(ScreenOperation.CREATE, ScreenOperation.EDIT, ScreenOperation.VIEW)));
        assertThat(associate.getScreens(), equalTo(asList(SCREEN_DEFAULT, SCREEN_RESOLVE, SCREEN_WORKFLOW)));

        ConfigureScreenScheme configureScreenScheme = associate.setOperation(ScreenOperation.EDIT).setScreen(SCREEN_WORKFLOW)
                .submit(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);

        Map<ScreenOperation, String> expectedMap = Maps.newEnumMap(ScreenOperation.class);
        expectedMap.put(ScreenOperation.EDIT, SCREEN_WORKFLOW);
        expectedMap.put(ScreenOperation.DEFAULT, SCREEN_DEFAULT);
        assertThat(configureScreenScheme.getSchemeMap(), equalTo(expectedMap));

        associate = opener.apply(null);
        assertThat(associate.getOperations(), equalTo(asList(ScreenOperation.CREATE, ScreenOperation.VIEW)));
        assertThat(associate.getScreens(), equalTo(asList(SCREEN_DEFAULT, SCREEN_RESOLVE, SCREEN_WORKFLOW)));
        configureScreenScheme = associate.setOperation(ScreenOperation.VIEW).setScreen(SCREEN_RESOLVE)
                .submit(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);

        expectedMap.put(ScreenOperation.VIEW, SCREEN_RESOLVE);
        assertThat(configureScreenScheme.getSchemeMap(), equalTo(expectedMap));

        associate = opener.apply(null);
        assertThat(associate.getOperations(), equalTo(asList(ScreenOperation.CREATE)));
        assertThat(associate.getScreens(), equalTo(asList(SCREEN_DEFAULT, SCREEN_RESOLVE, SCREEN_WORKFLOW)));
        configureScreenScheme = associate.setOperation(ScreenOperation.CREATE).setScreen(SCREEN_DEFAULT)
                .submit(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);

        expectedMap.put(ScreenOperation.CREATE, SCREEN_DEFAULT);
        assertThat(configureScreenScheme.getSchemeMap(), equalTo(expectedMap));

        assertFalse(configureScreenScheme.canAssociateScreen());
    }

    @Test
    public void associateScreenDialogWebSudo()
    {
        backdoor.restoreBlankInstance();
        final ConfigureScreenScheme configureScreenScheme = jira.gotoLoginPage()
                .loginAsSysAdmin(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);

        final PageWithArgs<ConfigureScreenScheme> cancelPage = PageWithArgs.create(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
        final PageWithArgs<AssociateIssueOperationToScreenDialog> associatePage = PageWithArgs.create(AssociateIssueOperationToScreenDialog.class);
        final Function<Void, JiraWebSudo> opener = new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(@Nullable Void input)
            {
                return configureScreenScheme.associateScreenAndBind(JiraSudoFormDialog.class, AssociateIssueOperationToScreenDialog.ID);
            }
        };
        assertAssociateScreenWebSudo(opener, cancelPage, associatePage, SCREEN_SCHEME_ID);
    }

    @Test
    public void associateScreenFormWebSudo()
    {
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage()
                .loginAsSysAdmin(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);

        final PageWithArgs<ViewProjectsPage> cancelPage = PageWithArgs.create(ViewProjectsPage.class);
        final PageWithArgs<AssociateIssueOperationToScreenPage> associatePage = PageWithArgs.create(AssociateIssueOperationToScreenPage.class, SCREEN_SCHEME_ID);
        final Function<Void, JiraWebSudo> opener = new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraWebSudo apply(@Nullable Void input)
            {
                jira.visitDelayed(AssociateIssueOperationToScreenPage.class, SCREEN_SCHEME_ID);
                return pageBinder.bind(JiraWebSudoPage.class);

            }
        };
        assertAssociateScreenWebSudo(opener, cancelPage, associatePage, SCREEN_SCHEME_ID);
    }

    private void assertAssociateScreenWebSudo(Function<Void, ? extends JiraWebSudo> opener,
            PageWithArgs<?> cancelPage, PageWithArgs<? extends AssociateIssueOperationToScreen> nextPage, long schemeId)
    {
        backdoor.websudo().enable();
        
        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.clearWebSudo();

        JiraWebSudo webSudo = opener.apply(null);
        webSudo.authenticateFail("failed");
        webSudo.cancel(cancelPage.page, cancelPage.args);

        webSudo = opener.apply(null);
        final AssociateIssueOperationToScreen screen = webSudo.authenticate(FunctTestConstants.ADMIN_PASSWORD, nextPage.page, nextPage.args);
        final ConfigureScreenScheme screenScheme = screen.setOperation(ScreenOperation.CREATE).setScreen(SCREEN_WORKFLOW)
                .submit(ConfigureScreenScheme.class, schemeId);
        
        assertThat(screenScheme.getSchemeMap(), hasEntry(ScreenOperation.CREATE, SCREEN_WORKFLOW));

        backdoor.websudo().disable();
    }

    // https://jdog.atlassian.net/browse/FLAKY-323
//    @Test
//    public void testSubmitSessionTimeoutDialog()
//    {
//        final ConfigureScreenScheme page = jira.quickLoginAsSysadmin(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
//        testSessionTimeout(PageWithArgs.create(ConfigureScreenScheme.class, SCREEN_SCHEME_ID), new TimeoutHelper()
//        {
//            @Override
//            public AssociateIssueOperationToScreen openAssociateIssueOperationToScreen()
//            {
//                return page.associateScreen();
//            }
//
//            @Override
//            public Xsrf submitExpired(AssociateIssueOperationToScreen associateIssueOperationToScreen)
//            {
//                return associateIssueOperationToScreen.submitFail(XsrfDialog.class, AssociateIssueOperationToScreenDialog.ID);
//            }
//        });
//    }

    @Test
    public void testSubmitSessionTimeoutPage()
    {
        jira.quickLoginAsSysadmin(ManageSubtasksPage.class);
        testSessionTimeout(PageWithArgs.create(AssociateIssueOperationToScreenPage.class, SCREEN_SCHEME_ID), new TimeoutHelper()
        {
            @Override
            public AssociateIssueOperationToScreen openAssociateIssueOperationToScreen()
            {
                return pageBinder.navigateToAndBind(AssociateIssueOperationToScreenPage.class, SCREEN_SCHEME_ID);
            }

            @Override
            public Xsrf submitExpired(AssociateIssueOperationToScreen associateIssueOperationToScreen)
            {
                return associateIssueOperationToScreen.submitFail(XsrfPage.class);
            }
        });
    }


    /**
     * Callback for abstracting page/dialog differences.
     */
    interface TimeoutHelper
    {
        AssociateIssueOperationToScreen openAssociateIssueOperationToScreen();
        Xsrf submitExpired(AssociateIssueOperationToScreen associateIssueOperationToScreen);
    }
    
    private <P extends Page> void testSessionTimeout(PageWithArgs<P> nextPage, TimeoutHelper timeoutHelper)
    {
        final AssociateIssueOperationToScreen associate = timeoutHelper.openAssociateIssueOperationToScreen();

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.invalidateSession();

        // check that we got the "session expired" page
        Xsrf xsrf = timeoutHelper.submitExpired(associate);
        assertTrue(xsrf.isSessionExpired());
        assertTrue(xsrf.hasParamaters());
        
        final JiraLoginPage jiraLoginPage = xsrf.login();
        jiraLoginPage.loginAsSystemAdminAndFollowRedirect(nextPage.page, nextPage.args);
    }

    @Test
    public void testAssociateScreenXsrfDialog()
    {
        backdoor.restoreBlankInstance();
        ConfigureScreenScheme config = jira.quickLoginAsSysadmin(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
        final AssociateIssueOperationToScreenDialog screenDialog = config.associateScreen();
        screenDialog.setOperation(ScreenOperation.VIEW).setScreen(SCREEN_RESOLVE);

        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.destoryAllXsrfTokens();

        final XsrfDialog xsrfPage = screenDialog.submitFail(XsrfDialog.class, AssociateIssueOperationToScreenDialog.ID);
        assertTrue(xsrfPage.canRetry());
        config = xsrfPage.retry(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
        
        assertThat(config.getSchemeMap(), hasEntry(ScreenOperation.VIEW, SCREEN_RESOLVE));
    }

    @Test
    public void testAssociateScreenXsrfPage()
    {
        backdoor.restoreBlankInstance();
        AssociateIssueOperationToScreenPage associate = jira.quickLoginAsSysadmin(AssociateIssueOperationToScreenPage.class, SCREEN_SCHEME_ID);
        associate.setOperation(ScreenOperation.VIEW).setScreen(SCREEN_RESOLVE);

        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.destoryAllXsrfTokens();

        final XsrfPage xsrfPage = associate.submitFail(XsrfPage.class);
        assertTrue(xsrfPage.canRetry());
        ConfigureScreenScheme config = xsrfPage.retry(ConfigureScreenScheme.class, SCREEN_SCHEME_ID);
        assertThat(config.getSchemeMap(), hasEntry(ScreenOperation.VIEW, SCREEN_RESOLVE));
    }

    private static class PageWithArgs<P>
    {
        private final Class<P> page;
        private final Object[] args;

        private PageWithArgs(Class<P> page, Object...args)
        {
            this.page = page;
            this.args = args;
        }

        public static <P> PageWithArgs<P> create(Class<P> page, Object... args)
        {
            return new PageWithArgs<P>(page, args);
        }
    }
}
