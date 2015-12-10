package com.atlassian.jira.rest.v1.issues;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.IssuePickerResults;
import com.atlassian.jira.bc.issue.search.IssuePickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Rest end point for IssuePicker searching
 *
 * @since v4.0
 */
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class IssuePickerResource
{
    private static final Logger log = Logger.getLogger(IssuePickerResource.class);

    private final JiraAuthenticationContext authContext;
    private final IssuePickerSearchService service;
    private final IssueManager issueManager;
    private final ApplicationProperties applicationProperties;
    private final ProjectManager projectManager;
    private final I18nHelper i18nHelper;

    public IssuePickerResource(JiraAuthenticationContext authContext, I18nHelper i18nHelper,
                               IssuePickerSearchService service, IssueManager issueManager,
                               ApplicationProperties applicationProperties, ProjectManager projectManager)
    {
        this.authContext = authContext;
        this.service = service;
        this.issueManager = issueManager;
        this.applicationProperties = applicationProperties;
        this.projectManager = projectManager;
        this.i18nHelper = i18nHelper;
    }

    @GET
    /**
     * This is the AJAX entry point to find issues given a query string.  The particluar instance of this call can "grey out" issues
     * by providing extra filterer parameters.
     *
     * @param query             what the user type in to search on
     * @param currentJQL        the JQL of the current Search.
     * @param currentIssueKey   the current issue or null
     * @param currentProjectId  the current project id or null
     * @param showSubTasks      set to false if sub tasks should be greyed out
     * @param showSubTaskParent set to false to have parent issue greyed out
     * @return a Response containing a list of {@link com.atlassian.jira.rest.v1.issues.IssuePickerResource.IssueSection} containing matching issues
     */
    public Response getIssuesResponse(@QueryParam("query") final String query,
                                      @QueryParam("currentJQL") String currentJQL,
                                      @QueryParam("currentIssueKey") final String currentIssueKey,
                                      @QueryParam("currentProjectId") final String currentProjectId,
                                      @QueryParam("showSubTasks") final boolean showSubTasks,
                                      @QueryParam("showSubTaskParent") final boolean showSubTaskParent)
    {
        if (query == null || StringUtils.isEmpty(query))
        {
            currentJQL = null; // Just show recents if we have no query
        }
        { return Response.ok(getIssues(query, currentJQL, currentIssueKey, currentProjectId, showSubTasks, showSubTaskParent)).cacheControl(NO_CACHE).build(); }
    }


    /**
     * This is the AJAX entry point to find issues given a query string.  The particluar instance of this call can "grey out" issues
     * by providing extra filterer parameters.
     *
     * @param query             what the user type in to search on
     * @param currentJQL        the JQL of the current Search.
     * @param currentIssueKey   the current issue or null
     * @param currentProjectId  the current project id or null
     * @param showSubTasks      set to false if sub tasks should be greyed out
     * @param showSubTaskParent set to false to have parent issue greyed out
     * @return A list of {@link com.atlassian.jira.rest.v1.issues.IssuePickerResource.IssueSection} containing matching issues
     */
    public IssuePickerResultsWrapper getIssues(String query, String currentJQL, String currentIssueKey, String currentProjectId, boolean showSubTasks, boolean showSubTaskParent)
    {
        final JiraServiceContext context = getContext();
        final IssuePickerResultsWrapper results = new IssuePickerResultsWrapper();

        Issue currentIssue = null;

        if (TextUtils.stringSet(currentIssueKey))
        {
            currentIssue = issueManager.getIssueObject(currentIssueKey);
        }

        int limit = getLimit();
        Project project = null;

        if (TextUtils.stringSet(currentProjectId))
        {
            project = projectManager.getProjectObj((new Long(currentProjectId)));
        }

        final IssuePickerSearchService.IssuePickerParameters pickerParameters = new IssuePickerSearchService.IssuePickerParameters(query, currentJQL, currentIssue, project, showSubTasks, showSubTaskParent, limit);
        final Collection<IssuePickerResults> pickerResults = service.getResults(context, pickerParameters);
        for (IssuePickerResults pickerResult : pickerResults)
        {
            final Collection<Issue> issues = pickerResult.getIssues();
            final String labelKey = pickerResult.getLabel();
            final String id = pickerResult.getId();
            final String label = i18nHelper.getText(labelKey);
            if (!issues.isEmpty())
            {
                final IssueSection section = new IssueSection(id, label, i18nHelper.getText("jira.ajax.autocomplete.showing.x.of.y", Integer.toString(issues.size()), Integer.toString(pickerResult.getTotalIssues())), null);
                results.addSection(section);

                for (Issue issue : issues)
                {
                    section.addIssue(getIssue(issue, pickerResult));
                }

            }
            else
            {
                final IssueSection section = new IssueSection(id, label, null, i18nHelper.getText("jira.ajax.autocomplete.no.matching.issues"));
                results.addSection(section);
            }
        }

        return results;
    }

    // get the number of items to display.
    private int getLimit()
    {
        //Default limit to 20
        int limit = 20;

        try
        {
            limit = Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        catch (NumberFormatException nfe)
        {
            log.error("jira.ajax.autocomplete.limit does not exist or is an invalid number in jira-application.properties.", nfe);
        }

        return limit;
    }

    /*
    * We use direct html instead of velocity to ensure the AJAX lookup is as fast as possible
    */
    private IssuePickerIssue getIssue(Issue issue, IssuePickerResults result)
    {
        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>");
        // Lets assume that anything that has one of the following one char before it, is a new word.
        delimeterInserter.setConsideredWhitespace("-_/\\,.+=&^%$#*@!~`'\":;<>");

        final String[] keysTerms = result.getKeyTerms().toArray(new String[result.getKeyTerms().size()]);
        final String[] summaryTerms = result.getSummaryTerms().toArray(new String[result.getSummaryTerms().size()]);

        final String issueKey = delimeterInserter.insert(TextUtils.htmlEncode(issue.getKey()), keysTerms);
        final String issueSummary = delimeterInserter.insert(TextUtils.htmlEncode(issue.getSummary()), summaryTerms);

        return new IssuePickerIssue(issue.getKey(), issueKey, getIconURI(issue.getIssueTypeObject()), issueSummary, issue.getSummary());
    }

    private String getIconURI(IssueConstant issueConstant)
    {
        //mainly here for unit tests.
        if (issueConstant == null)
        {
            return "";
        }

        return issueConstant.getIconUrl();
    }

    // protected for unit testing
    protected JiraServiceContext getContext()
    {
        final User user = authContext.getLoggedInUser();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(user, errorCollection);
    }

    @XmlRootElement
    public static class IssuePickerResultsWrapper
    {
        @XmlElement
        private List<IssueSection> sections = null;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private IssuePickerResultsWrapper() {}

        public IssuePickerResultsWrapper(List<IssueSection> sections)
        {
            this.sections = sections;
        }

        public void addSection(IssueSection section)
        {
            if (sections == null)
            {
                sections = new ArrayList<IssueSection>();
            }
            sections.add(section);
        }
    }

    @XmlRootElement
    public static class IssueSection
    {
        @XmlElement
        private String label;
        @XmlElement
        private String sub;
        @XmlElement
        private String id;
        @XmlElement
        private String msg;
        @XmlElement
        private List<IssuePickerIssue> issues = null;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private IssueSection() {}

        public IssueSection(String id, String label, String sub, String msg, List<IssuePickerIssue> issues)
        {
            this.label = label;
            this.sub = sub;
            this.id = id;
            this.issues = issues;
            this.msg = msg;
        }

        public IssueSection(String id, String label, String sub, String msg)
        {
            this.label = label;
            this.sub = sub;
            this.id = id;
            this.msg = msg;
        }

        public void addIssue(IssuePickerIssue issue)
        {
            if (issues == null)
            {
                issues = new ArrayList<IssuePickerIssue>();
            }
            issues.add(issue);
        }
    }

    @XmlRootElement
    public static class IssuePickerIssue
    {
        @XmlElement
        private String key;
        @XmlElement
        private String keyHtml;
        @XmlElement
        private String img;
        @XmlElement
        private String summary;
        @XmlElement
        private String summaryText;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private IssuePickerIssue() {}

        public IssuePickerIssue(String key, String keyHtml, String img, String summary, String summaryText)
        {
            this.key = key;
            this.keyHtml = keyHtml;
            this.img = img;
            this.summary = summary;
            this.summaryText = summaryText;
        }
    }
}
