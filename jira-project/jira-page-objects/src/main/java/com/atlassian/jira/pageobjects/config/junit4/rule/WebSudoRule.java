package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.EnableWebSudo;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.inject.Inject;

/**
 * Disables web-sudo in JIRA, unless the {@link com.atlassian.jira.pageobjects.config.EnableWebSudo} annotation is
 * present for given test.
 *
 * @since 5.1
 */
public class WebSudoRule implements TestRule
{
    private final JiraTestedProduct jiraTestedProduct;

    @Inject
    public WebSudoRule(final JiraTestedProduct jiraTestedProduct)
    {
        this.jiraTestedProduct = jiraTestedProduct;
    }


    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                if(!jiraTestedProduct.isOnDemand()) {
                    if (shouldEnable(description))
                    {
                        jiraTestedProduct.backdoor().websudo().enable();
                    }
                    else
                    {
                        jiraTestedProduct.backdoor().websudo().disable();
                    }
                }
                base.evaluate();
            }

        };
    }


    private boolean shouldEnable(Description description)
    {
        return new AnnotatedDescription(description).hasAnnotation(EnableWebSudo.class);
    }
}
