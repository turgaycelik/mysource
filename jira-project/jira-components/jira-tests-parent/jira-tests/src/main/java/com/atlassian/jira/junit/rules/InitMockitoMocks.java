package com.atlassian.jira.junit.rules;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * <p/>
 * Initializes Mocktio mocks before the tests.
 *
 * <p/>
 * Just add this to your test:
 * <pre>
 *     &#64;Rule InitMockitoMocks initMocks = new InitMockitoMocks(this);
 *
 *     &#64;Mock private UserService mockUserService;
 *     &#64;Mock private JiraAuthenticationContext jiraAuthenticationContext;
 * </pre>
 * and all your mocks annotated with &#64;Mock will be automatically initialized. No
 * need for runners, or before/after methods!
 *
 *
 * @since 5.1
 */
public class InitMockitoMocks extends TestWatcher
{
    private final Object test;

    public InitMockitoMocks(Object test)
    {
        this.test = test;
    }

    @Override
    protected void starting(Description description)
    {
        MockitoAnnotations.initMocks(test);
    }

    @Override
    protected void finished(Description description)
    {
        Mockito.validateMockitoUsage();
    }
}
