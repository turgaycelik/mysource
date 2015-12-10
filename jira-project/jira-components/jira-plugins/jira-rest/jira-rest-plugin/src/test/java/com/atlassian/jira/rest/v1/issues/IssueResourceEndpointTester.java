package com.atlassian.jira.rest.v1.issues;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.util.Arrays;
import java.util.Collection;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

/**
 * Helper class providing mocks and stubbing API
 * for {@link com.atlassian.jira.rest.v1.issues.IssueResource } endpoint tests.
 *
 * @since v4.2
 */
class IssueResourceEndpointTester
{
    
    private JiraAuthenticationContext mockAuthenticationContext;
    private IssueService mockIssueService;
    private VoteService mockVoteService;

    IssueResource createTested()
    {
        setUpMocks();
        return new IssueResource(mockAuthenticationContext, null, null, null, mockIssueService, null, null, null,
                null, mockVoteService, null, null);
    }

    private void setUpMocks()
    {
        mockAuthenticationContext = createNiceMock(JiraAuthenticationContext.class);
        mockIssueService = createNiceMock(IssueService.class);
        mockVoteService = createNiceMock(VoteService.class);
    }

    MutableIssue newMutableIssue(Long id)
    {
        MutableIssue issue = createNiceMock(MutableIssue.class);
        expect(issue.getId()).andReturn(id);
        replay(issue);
        return issue;
    }

    boolean isXmlErrorCollection(Object responseEntity)
    {
        return com.atlassian.jira.rest.v1.model.errors.ErrorCollection.class.isInstance(responseEntity);
    }

    boolean isXmlVoteResult(Object responseEntity)
    {
        return IssueResource.VoteWatchResult.class.isInstance(responseEntity);
    }

    Collection<String> getErrorMessages(Object errorResponseEntity)
    {
        return ((com.atlassian.jira.rest.v1.model.errors.ErrorCollection) errorResponseEntity).getErrorMessages();
    }

    int getVoteCount(Object okResponseEntity)
    {
        return ((IssueResource.VoteWatchResult) okResponseEntity).voteCount();
    }

    void authenticateUser(User user)
    {
        reset(mockAuthenticationContext);
        expect(mockAuthenticationContext.getLoggedInUser()).andReturn(user);
        replay(mockAuthenticationContext);
    }

    void returnEmptyIssueResultFor(User user, Long issueId)
    {
        reset(mockIssueService);
        expect(mockIssueService.getIssue(user, issueId)).andReturn(newEmptyIssueResult());
        replay(mockIssueService);
    }

    void returnValidIssueResultFor(User user, Long issueId, MutableIssue toReturn)
    {
        reset(mockIssueService);
        expect(mockIssueService.getIssue(user, issueId)).andReturn(newValidIssueResult(toReturn));
        replay(mockIssueService);
    }

    void validateSuccessfully(User user, MutableIssue issue, int resultVotes)
    {
        reset(mockVoteService);
        VoteService.VoteValidationResult valResult = successfulValidationResult(user, issue);
        expect(mockVoteService.validateAddVote(user, user, issue)).andReturn(valResult);
        expect(mockVoteService.validateRemoveVote(user, user, issue)).andReturn(valResult);
        expect(mockVoteService.addVote(user, valResult)).andReturn(resultVotes);
        expect(mockVoteService.removeVote(user, valResult)).andReturn(resultVotes);
        replay(mockVoteService);
    }

    void validateUnsuccessfully(User user, MutableIssue issue, String... errors)
    {
        reset(mockVoteService);
        expect(mockVoteService.validateAddVote(user, user, issue)).andReturn(unsuccessfulValidationResult(user, issue,
                errors));
        expect(mockVoteService.validateRemoveVote(user, user, issue)).andReturn(unsuccessfulValidationResult(user,
                issue, errors));
        replay(mockVoteService);
    }

    IssueService.IssueResult newEmptyIssueResult()
    {
        return new IssueService.IssueResult(null);
    }

    IssueService.IssueResult newValidIssueResult(MutableIssue issue)
    {
        return new IssueService.IssueResult(issue);
    }

    VoteService.VoteValidationResult successfulValidationResult(User user, MutableIssue issue)
    {
        return new VoteService.VoteValidationResult(new SimpleErrorCollection(), user, issue);
    }


    VoteService.VoteValidationResult unsuccessfulValidationResult(User user, MutableIssue issue,
            String... errors)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessages(Arrays.asList(errors));
        return new VoteService.VoteValidationResult(errorCollection, user, issue);
    }
}
