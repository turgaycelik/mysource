package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.Page;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class LoginRule extends TestWatcher
{

    @Inject private JiraTestedProduct jira;

    @Override
    protected void starting(@Nonnull final Description description)
    {
        final AnnotatedDescription annotatedDescription = new AnnotatedDescription(description);
        if (annotatedDescription.hasAnnotation(LoginAs.class))
        {
            login(annotatedDescription.getAnnotation(LoginAs.class));
        }
        else
        {
            // default :)
            jira.quickLoginAsAdmin();
        }
    }

    private void login(final LoginAs loginAs)
    {
        if (loginAs.anonymous())
        {
            return;
        }
        final Class<? extends Page> target = loginAs.targetPage();
        if (loginAs.sysadmin())
        {
            jira.quickLoginAsSysadmin(target);
        }
        else if (loginAs.admin())
        {
            jira.quickLoginAsAdmin(target);
        }
        else
        {
            final String user = Assertions.notNull("user", loginAs.user());
            final String password = isNotEmpty(loginAs.password())? loginAs.password() : user;
            jira.quickLogin(user, password, target);
        }
    }
}
