package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import com.atlassian.jira.pageobjects.config.RestoreJiraData;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.inject.Inject;

/**
 * A class rule for setting up JIRA and restoring data if necessary.
 * Supports {@link com.atlassian.integrationtesting.runner.restore.RestoreOnce} and
 * {@link com.atlassian.jira.pageobjects.config.ResetDataOnce}.
 *
 * @since 5.1
 */
public class RestoreDataClassRule implements TestRule
{

    private final RestoreJiraData restoreJiraData;

    @Inject
    public RestoreDataClassRule(RestoreJiraData restoreJiraData)
    {
        this.restoreJiraData = restoreJiraData;
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                RestoreAnnotationValidator.validate(description);
                final AnnotatedDescription annotatedDescription = new AnnotatedDescription(description);
                if (annotatedDescription.isClassAnnotated(RestoreOnce.class))
                {
                    restoreJiraData.restore(annotatedDescription.getAnnotationFromTestClass(RestoreOnce.class).value());
                }
                else if (annotatedDescription.isClassAnnotated(ResetDataOnce.class))
                {
                    restoreJiraData.restoreBlank();
                }
                base.evaluate();
            }


        };
    }
}
