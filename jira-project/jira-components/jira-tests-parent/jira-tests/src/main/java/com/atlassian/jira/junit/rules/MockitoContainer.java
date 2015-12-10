package com.atlassian.jira.junit.rules;

import com.atlassian.jira.mock.component.MockComponentWorker;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * Rule that combines mockito initialization with
 * {@link com.atlassian.jira.junit.rules.MockComponentContainer}. This will take care of initializing mocks using
 * Mockito and also putting them in a mock component container so that they're available via
 * {@link com.atlassian.jira.component.ComponentAccessor#getComponent(Class)}. After the test has run, the mock
 * container will be tear down so that next test does not accidentally access stale state.
 *
 * <p/>
 * Usage:
 * <pre>
 *     &#64;Rule public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);
 *
 *     &#64;Mock
 *     &#64;AvailableInContainer
 *     private UserService mockUserService;
 *
 *     &#64;Mock private JiraAuthenticationContext jiraAuthenticationContext;
 * </pre>
 * where the <tt>userService</tt> will be available via {@link com.atlassian.jira.component.ComponentAccessor}, but the
 * <tt>jiraAuthenticationContext</tt> will just be instantiated as a Mockito mock
 *
 * <p/>
 * <p/>
 * Gives also ability to access initialized {@link MockComponentWorker} which allows to manipulate mocks directly from tests
 * <p/>
 * Example:
 * <pre>
 *     &#64;Rule public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);
 *
 *     &#64;Mock
 *     public void testSomething(){
 *         mockitoContainer.getMockWorker().setMockUserKeyStore().useDefaultMapping(false);
 *         (...)
 *     }
 * </pre>
 * @since v6.1
 */
public class MockitoContainer implements TestRule
{
    private final MockComponentContainer mockComponentContainer;
    private RuleChain innerChain;

    public MockitoContainer(Object test)
    {
        mockComponentContainer = new MockComponentContainer(test);
        innerChain = RuleChain.emptyRuleChain()
                .around(new InitMockitoMocks(test))
                .around(mockComponentContainer);
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return innerChain.apply(base, description);
    }

    public MockComponentContainer getMockComponentContainer()
    {
        return mockComponentContainer;
    }

    /**
     * A shorthand for getMockComponentContainer().getMockWorker()
     * @return worker
     */
    public MockComponentWorker getMockWorker()
    {
        return mockComponentContainer.getMockWorker();
    }

    public RuleChain getInnerChain()
    {
        return innerChain;
    }
}
