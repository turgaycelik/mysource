package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.util.dbc.Assertions;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertTrue;

public class LoginUserRule extends TestWatcher
{
    @Nonnull
    private final JiraTestedProduct product;

    public LoginUserRule(@Nonnull final JiraTestedProduct product)
    {
        this.product = Assertions.notNull(product);
    }

    @Override
    protected void starting(@Nonnull final Description description)
    {
        final User userAnnotation = description.getAnnotation(User.class);
        final Users user = userAnnotation != null ? userAnnotation.value() : null;
        if (user != null && user.requiresAuthentication())
        {
            final DashboardPage dashboardPage = product.quickLogin(user.getUserName(), user.getPassword(), DashboardPage.class);
            assertTrue("Failed to login as: " + user, dashboardPage.isAt().byDefaultTimeout());
        }
        else
        {
            assertTrue("Failed to go to the login page.", product.gotoLoginPage().isAt().byDefaultTimeout());
        }
    }

    @Override
    protected void finished(@Nonnull final Description description)
    {
        product.logout();
    }
}
