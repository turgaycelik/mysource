package com.atlassian.jira.workflow.function.misc;

import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.WorkflowContext;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestCreateCommentFunction
{
    private Map<String, Object> transientVars;
    private CreateCommentFunction ccf;

    @Mock
    @AvailableInContainer
    private CommentService commentService;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    private WorkflowContext workflowContext;

    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    private MockIssue issue = new MockIssue(333);

    @Before
    public void setUp() throws Exception
    {
        ccf = new CreateCommentFunction();

        transientVars = Maps.newHashMap();
        transientVars.put(WorkflowTransitionUtil.FIELD_COMMENT, "comment body");
        transientVars.put(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL, "jira-users");
        transientVars.put(WorkflowTransitionUtil.FIELD_COMMENT_ROLE_LEVEL, "544");
        transientVars.put("issue", issue);
        transientVars.put("context", workflowContext);
        when(commentService.validateCommentCreate(Mockito.any(ApplicationUser.class), Mockito.any(CommentService.CommentParameters.class)))
                .thenReturn(new CommentService.CommentCreateValidationResult(new SimpleErrorCollection(), Option.some(CommentService.CommentParameters.builder().build())));
        when(commentService.create(Mockito.any(ApplicationUser.class), Mockito.any(CommentService.CommentCreateValidationResult.class), Mockito.eq(false)))
                .thenReturn(new MockComment("author", "comment"));
    }

    @Test
    public void shouldGetParmasFromTransientVarsAndPassThemToManager()
    {
        ccf.execute(transientVars, null, null);
        verify(commentService).validateCommentCreate(Mockito.any(ApplicationUser.class), Mockito.any(CommentService.CommentParameters.class));
        verify(commentService).create(Mockito.any(ApplicationUser.class), Mockito.any(CommentService.CommentCreateValidationResult.class), Mockito.anyBoolean());
    }

    @Test
    public void shouldSetCommentAuthorBasingOffCurrentCaller()
    {
        when(workflowContext.getCaller()).thenReturn("fred");
        final MockApplicationUser author = new MockApplicationUser("fred", "Fred");
        when(userManager.getUserByKey("fred")).thenReturn(author);

        ccf.execute(transientVars, null, null);

        verify(commentService).validateCommentCreate(Mockito.any(ApplicationUser.class), Mockito.any(CommentService.CommentParameters.class));
        verify(commentService).create(Mockito.any(ApplicationUser.class), Mockito.any(CommentService.CommentCreateValidationResult.class), Mockito.anyBoolean());
    }

    @Test
    public void shouldPutCreatedCommentIntoContext()
    {
        ccf.execute(transientVars, null, null);
        assertThat(transientVars.get("commentValue"), Matchers.hasProperty("body", Matchers.is("comment")));
    }

    @Test
    public void shouldNotAddAnythingWhenCommentIsEmpty()
    {
        transientVars.put(WorkflowTransitionUtil.FIELD_COMMENT, "");

        ccf.execute(transientVars, null, null);
        verifyNoMoreInteractions(commentService);
    }
}
