package com.atlassian.jira.pageobjects.config.junit4.rule;

import javax.inject.Inject;

import com.atlassian.annotations.Internal;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.config.RestoreJiraData;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * <p/>
 * A class rule for setting up JIRA and restoring data if necessary.
 * Supports {@link com.atlassian.integrationtesting.runner.restore.RestoreOnce}
 *
 *
 * @since 5.1
 */
@Internal
public class RestoreDataMethodRule implements TestRule
{
    private final RestoreJiraData restoreJiraData;

    @Inject
    public RestoreDataMethodRule(final RestoreJiraData restoreJiraData)
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
                if (annotatedDescription.isAnnotatedWith(Restore.class))
                {
                    restoreJiraData.restore(annotatedDescription.getAnnotation(Restore.class).value());
                }
                else if (annotatedDescription.isAnnotatedWith(ResetData.class)) // only reset on method level
                {
                    restoreJiraData.restoreBlank();
                }
                base.evaluate();
            }


        };
    }
}
