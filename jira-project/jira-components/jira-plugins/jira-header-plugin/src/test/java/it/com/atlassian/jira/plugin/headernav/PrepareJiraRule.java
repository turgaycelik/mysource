package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.util.TimeBombLicence;
import com.atlassian.jira.util.dbc.Assertions;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.annotation.Nonnull;

public class PrepareJiraRule extends TestWatcher
{
    private final JiraTestedProduct product;
    private final String dataResourceFileName;

    public PrepareJiraRule(@Nonnull final JiraTestedProduct product, @Nonnull final String dataResourceFileName)
    {
        this.product = Assertions.notNull(product);
        this.dataResourceFileName = Assertions.notNull(dataResourceFileName);
    }

    @Override
    protected void starting(@Nonnull final Description description)
    {
        final Backdoor testkit = product.backdoor().getTestkit();
        testkit.restoreDataFromResource(dataResourceFileName, TimeBombLicence.LICENCE_FOR_TESTING);
        testkit.darkFeatures().enableForSite("com.atlassian.jira.darkfeature.CommonHeader");
    }
}
