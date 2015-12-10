package com.atlassian.jira.rest.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.easymock.EasyMock;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

/**
 * Creates simple mock implementations of {@link com.atlassian.jira.security.JiraAuthenticationContext}.
 *
 * @since v4.2
 */
public class MockAuthenticationContextFactory
{

    public static JiraAuthenticationContext withAuthenticatedUser(User user)
    {
        JiraAuthenticationContext answer = EasyMock.createNiceMock(JiraAuthenticationContext.class);
        reset(answer);
        expect(answer.getLoggedInUser()).andReturn(user).anyTimes();
        replay(answer);
        return answer;
    }

}
