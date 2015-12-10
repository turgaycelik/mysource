package com.atlassian.jira.webtest.webdriver.setup;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.google.common.base.Predicate;
import org.junit.runner.Description;

import javax.annotation.Nullable;

/**
 * A predicate deciding whether a test class suite is splittable or not, basing on it being marked with the
 * {@link com.atlassian.integrationtesting.runner.restore.RestoreOnce} annotation. If a test is marked with
 * {@link com.atlassian.integrationtesting.runner.restore.RestoreOnce}, it is assumed not splittable, as
 * its single tests are operating on the same data and may be inter-dependent.
 *
 * @since v4.4
 */
public final class RestoreOnceSplittablePredicate implements Predicate<Description>
{
    public static final RestoreOnceSplittablePredicate INSTANCE = new RestoreOnceSplittablePredicate();

    @Override
    public boolean apply(@Nullable Description description)
    {
        return description.getAnnotation(RestoreOnce.class) == null;
    }
}
