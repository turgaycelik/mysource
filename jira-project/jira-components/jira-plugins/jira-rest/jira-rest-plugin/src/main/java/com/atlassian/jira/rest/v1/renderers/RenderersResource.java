package com.atlassian.jira.rest.v1.renderers;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Ajax bean that exposes a transform for a specified renderer.
 *
 * @since 4.0
 */
@Path("render")
@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Produces (MediaType.TEXT_HTML)
@CorsAllowed
public class RenderersResource
{
    private static final String NBSP = "&nbsp;";

    private final RendererManager rendererManager;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final IssueFactory issueFactory;
    private final ConstantsManager constantsManager;

    public RenderersResource(final RendererManager rendererManager, final IssueManager issueManager, final ProjectManager projectManager, final IssueFactory issueFactory, final ConstantsManager constantsManager)
    {
        this.rendererManager = rendererManager;
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.issueFactory = issueFactory;
        this.constantsManager = constantsManager;
    }


    @POST
    public Response getRenderedContent(final ContentToRender contentToRender)
    {
        final String content = getPreviewHtml(contentToRender.rendererType, contentToRender.unrenderedMarkup, contentToRender.issueKey, contentToRender.projectId, contentToRender.issueType);

        return Response.ok(content).cacheControl(NO_CACHE).build();
    }

    public String getPreviewHtml(final String rendererType, final String unrenderedMarkup, final String issueKey, final String projectId, final String issueType)
    {
        final Issue issue = issueManager.getIssueObject(issueKey);
        if (issue != null)
        {
            return doGetPreviewHtml(rendererType, unrenderedMarkup, issue.getIssueRenderContext());
        }

        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(issueType))
        {
            return doGetPreviewHtml(rendererType, unrenderedMarkup, null);
        }

        final IssueRenderContext renderContext = generateRenderContext(projectId, issueType);
        return doGetPreviewHtml(rendererType, unrenderedMarkup, renderContext);
    }

    /**
     * When no issue context is available, the chances are that someone is attempting to render the preview from the CreateIssue screen, rather than
     * the EditIssue screen. Some renderers need an issue context to render properly so we create a dummy issue and context.
     *
     * @param projectId the ID of the project that the issue is being created in
     * @param issueType a long in String format representing the kind of issue being created (for example 1 for a Bug or 2 for a New Feature)
     * @return a context which is used to provide issue and project details for later use, or null if the project ID does not refer to an actual
     *         project
     */
    private IssueRenderContext generateRenderContext(final String projectId, final String issueType)
    {
        final Project projectObj;

        try
        {
            projectObj = projectManager.getProjectObj(Long.valueOf(projectId));
            if (projectObj == null)
            {
                return null;
            }
        }
        catch(NumberFormatException e)
        {
            return null;
        }

        final MutableIssue issue = issueFactory.getIssue();
        issue.setProjectObject(projectObj);

        // We want to provide as much info to the renderers as possible so give them the issue type.
        // But we are in preview mode, so if the issue type isn't set then that's not a big deal.
        // Still, only set the issue type if it's a valid type.
        if (constantsManager.getIssueTypeObject(issueType) != null)
        {
            issue.setIssueTypeId(issueType);
        }

        final IssueRenderContext renderContext = new IssueRenderContext(issue);

        renderContext.addParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY, issue);
        return renderContext;
    }

    private String doGetPreviewHtml(final String rendererType, final String unrenderedMarkup, final IssueRenderContext renderContext)
    {
        // The issueRenderContext allows us to resolve links to attached files and is also used by the wiki renderer, if
        // the issue is null then it is probably the case that we are on the CreateIssue screen and therefore we will
        // not have any attachments. The worst that will happen rendering wiki markup without an issue context is that some links
        // will not render. JRA-11464(JIRA), JST-763(JIRA Studio).
        final String result = rendererManager.getRenderedContent(rendererType, unrenderedMarkup, renderContext);
        return (StringUtils.isBlank(result)) ? RenderersResource.NBSP : result;
    }

    @XmlRootElement
    public static class ContentToRender
    {
        @XmlElement
        private String rendererType;
        @XmlElement
        private String unrenderedMarkup;
        @XmlElement
        private String issueKey;
        @XmlElement
        private String projectId;
        @XmlElement
        private String issueType;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        public ContentToRender() {}

        public ContentToRender(String rendererType, String unrenderedMarkup, String issueKey, String projectId, String issueType)
        {
            this.rendererType = rendererType;
            this.unrenderedMarkup = unrenderedMarkup;
            this.issueKey = issueKey;
            this.projectId = projectId;
            this.issueType = issueType;
        }
    }


}
