package com.atlassian.jira.rest.v2.issue;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.jql.resolver.ResolverManager;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.rest.v2.issue.watcher.WatcherOps;
import com.atlassian.jira.rest.v2.issue.worklog.WorklogResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import junit.framework.TestCase;

import javax.ws.rs.core.UriInfo;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Abstract base class to take care of instantiating all of IssueResource's dependencies.
 *
 * @since v4.2
 */
public abstract class IssueResourceTest extends TestCase
{
    @Mock
    protected UriInfo uriInfo;
    @Mock
    protected Issue issue;
    @Mock
    protected ResourceUriBuilder uriBuilder;
    @Mock
    protected IssueBean bean;
    @Mock
    protected IssueService issueService;
    @Mock
    protected JiraAuthenticationContext authContext;
    @Mock
    protected CommentService commentService;
    @Mock
    protected FieldLayout fieldLayout;
    @Mock
    protected FieldLayoutManager fieldLayoutManager;
    @Mock
    protected VelocityRequestContext velocityRequestContext;
    @Mock
    protected VelocityRequestContextFactory velocityRequestContextFactory;
    @Mock
    protected WorkflowManager workflowManager;
    @Mock
    protected FieldScreenRendererFactory fieldScreenRendererFactory;
    @Mock
    protected UserManager userManager;
    @Mock
    protected AttachmentManager attachmentManager;
    @Mock
    protected ApplicationProperties applicationProperties;
    @Mock
    protected IssueLinkManager issueLinkManager;
    @Mock
    protected FieldManager fieldManager;
    @Mock
    protected RendererManager rendererManager;
    @Mock
    protected ProjectRoleManager projectRoleManager;
    @Mock
    protected IssueSecurityLevelManager issueSecurityLevelManager;
    @Mock
    protected WorklogService worklogService;
    @Mock
    protected ResolverManager resolverManager;
    @Mock
    protected VoteService voteService;
    @Mock
    protected I18nHelper i18n;
    @Mock
    protected WatcherOps watcherOps;
    @Mock
    protected AttachmentService attachmentService;
    @Mock
    protected IssueUpdater issueUpdater;
    @Mock
    protected WatcherService watcherService;
    @Mock
    protected BeanBuilderFactory beanBuilderFactory;
    @Mock
    protected ContextUriInfo contextUriInfo;
    @Mock
    protected IssueManager issueManager;
    @Mock
    protected PermissionManager permissionManager;
    @Mock
    protected ProjectBeanFactory projectBeanFactory;
    @Mock
    protected VersionBeanFactory versionBeanFactory;
    @Mock
    protected CreateIssueResource createIssueResource;
    @Mock
    protected UpdateIssueResource updateIssueResource;
    @Mock
    protected DeleteIssueResource deleteIssueResource;
    @Mock
    protected RemoteIssueLinkResource remoteIssueLinkResource;
    @Mock
    protected TimeZoneManager timeZoneManager;
    @Mock
    protected JiraBaseUrls jiraBaseUrls;
    @Mock
    protected IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    @Mock
    protected WorklogResource worklogResource;
    @Mock
    protected EventPublisher eventPublisher;

    @Override
    protected final void setUp() throws Exception
    {
        super.setUp();
        createDependencies();
        doSetUp();
    }

    /**
     * Perform per-test setup.
     */
    protected void doSetUp()
    {
        // empty
    }

    protected void replayMocks()
    {
        replay(uriInfo,
                issue,
                uriBuilder,
                bean,
                issueService,
                authContext,
                commentService,
                fieldLayout,
                fieldLayoutManager,
                velocityRequestContext,
                velocityRequestContextFactory,
                workflowManager,
                fieldScreenRendererFactory,
                userManager,
                attachmentManager,
                applicationProperties,
                issueLinkManager,
                fieldManager,
                rendererManager,
                projectRoleManager,
                issueSecurityLevelManager,
                worklogService,
                resolverManager,
                voteService,
                i18n,
                watcherOps,
                attachmentService,
                issueUpdater,
                watcherService,
                beanBuilderFactory,
                contextUriInfo,
                issueManager,
                permissionManager,
                projectBeanFactory,
                versionBeanFactory,
                createIssueResource,
                updateIssueResource,
                deleteIssueResource,
                remoteIssueLinkResource,
                timeZoneManager,
                jiraBaseUrls, 
                worklogResource,
                eventPublisher
        );
    }

    /**
     * Creates all the mocks.
     */
    private void createDependencies()
    {
        EasyMockAnnotations.initMocks(this);
    }

    protected IssueLinksBeanBuilder createIssueLinkBeanBuilder()
    {
        return new IssueLinksBeanBuilder(applicationProperties, issueLinkManager, authContext, jiraBaseUrls, issue);
    }

    protected Issue createMockIssue(final Long id, final String key)
    {
        final Issue mockIssue = createNiceMock(Issue.class);
        expect(mockIssue.getId()).andStubReturn(id);
        expect(mockIssue.getKey()).andStubReturn(key);
        expect(mockIssue.getSummary()).andStubReturn("issue " + key);
        replay(mockIssue);

        return mockIssue;
    }
}
