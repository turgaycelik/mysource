package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.jelly.IssueContextAccessor;
import com.atlassian.jira.jelly.IssueContextAccessorImpl;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import com.atlassian.jira.jelly.WebWorkAdaptor;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.ProjectAwareActionTagSupport;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.SAXException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class AbstractCreateIssue extends ProjectAwareActionTagSupport
        implements ProjectContextAccessor, IssueContextAccessor, CustomFieldValuesAwareTag
{
    private static final Logger log = Logger.getLogger(AbstractCreateIssue.class);
    protected static final String KEY_ISSUE_ID = "key";
    protected static final String KEY_PROJECT_ID = "pid";
    protected static final String KEY_ISSUE_ASSIGNEE = "assignee";
    protected static final String KEY_ISSUE_CREATED_DATE = "created";
    protected static final String KEY_ISSUE_UPDATED_DATE = "updated";
    protected static final String KEY_ISSUE_TYPE = "issueType";
    protected static final String KEY_PRIORITY = "priority";
    protected static final String KEY_ISSUE_REPORTER = "reporter";
    protected static final String KEY_SUMMARY = "summary";
    protected static final String KEY_DESCRIPTION = "description";
    protected static final String KEY_ENVIRONMENT = "environment";
    protected static final String KEY_COMPONENTS = "components";
    protected static final String KEY_VERSIONS = "versions";
    protected static final String KEY_FIX_VERSIONS = "fixVersions";
    protected static final String KEY_ISSUE_SECURITY = "security";
    protected static final String KEY_DUPLICATE_SUMMARY = "duplicateSummary";
    protected static final String KEY_ISSUE_ID_VAR = "issueIdVar";
    protected static final String KEY_ISSUE_KEY_VAR = "issueKeyVar";
    protected static final String KEY_ISSUE_ASSIGNEE_VAR = "issueAssigneeVar";
    private Map issueTypeMap = null;
    private Map priorityMap = null;
    private final ProjectContextAccessor projectContextAccessor;
    private final IssueContextAccessor issueContextAccessor;
    private boolean hasPreviousUsername = false;
    private String previousUsername = null;
    private boolean hasPreviousIssueSchemeLevelId = false;
    private Long previousIssueSchemeLevelId = null;
    private static final String DUPLICATE_SUMMARY_IGNORE = "ignore";

    private final VersionManager versionManager;
    private UserManager userManager;

    public AbstractCreateIssue(VersionManager versionManager, UserManager userManager)
    {
        this.userManager = userManager;
        setExecuteWebworkOnClose(true);
        setActionName("CreateIssueDetails");

        // ManagerFactory.getStatisticsManager().loadStatisticsForAllProjects();
        issueContextAccessor = new IssueContextAccessorImpl(this);
        projectContextAccessor = new ProjectContextAccessorImpl(this);
        this.versionManager = versionManager; //ComponentAccessor.getVersionManager();
    }

    protected void preContextValidation()
    {
        if (getProperties().containsKey(KEY_ISSUE_REPORTER))
        {
            setPreviousUsername(getUsername());
            String reporterUsername = getProperty(KEY_ISSUE_REPORTER.toLowerCase());
            getContext().setVariable(JellyTagConstants.USERNAME, reporterUsername);
            //JRA-11546 - Set the user to the reporter in the authentication-context.
            setLoggedinUser(reporterUsername);
        }

        final String PROJECT_KEY = "project-key";
        if (getProperties().containsKey(PROJECT_KEY))
        {
            setProject(getProperty(PROJECT_KEY));
        }
    }

    private void setLoggedinUser(String reporterUsername)
    {
        User reporter = userManager.getUser(reporterUsername);
        ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(reporter);
    }

    protected boolean isDuplicateIssue(XMLOutput output) throws SAXException
    {
        log.debug("CreateIssue.isDuplicateIssue");

        String duplicateSummary = getProperty(KEY_DUPLICATE_SUMMARY);

        // Check if we are to ignore the duplicate summaries
        if (!DUPLICATE_SUMMARY_IGNORE.equals(duplicateSummary))
        {
            // Derive a key from the issue data and check if its already in the cache
            final List matchingIssues = ComponentAccessor.getOfBizDelegator().findByAnd("Issue", EasyMap.build("project", getProjectId(), "summary", getProperty(KEY_SUMMARY)));
            if (!matchingIssues.isEmpty())
            {
                String errorMsg = "Duplicate Issue : Another issue for this project with the same values for \"summary\"";
                WebWorkAdaptor.writeErrorToXmlOutput(output, new StringBuffer(errorMsg), getActionName(), this);
                return true;
            }
        }
        return false;
    }

    public boolean hasIssueScheme()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_SCHEME_ID);
    }

    public Long getIssueSchemeId()
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue getIssueScheme()
    {
        throw new UnsupportedOperationException();
    }

    protected String getPreviousUsername()
    {
        return previousUsername;
    }

    private void setPreviousUsername(String previousUsername)
    {
        this.hasPreviousUsername = true;
        this.previousUsername = previousUsername;
    }

    protected Long getPreviousIssueSchemeLevelId()
    {
        return previousIssueSchemeLevelId;
    }

    protected void setPreviousIssueSchemeLevelId(Long previousIssueSchemeLevelId)
    {
        this.hasPreviousIssueSchemeLevelId = true;
        this.previousIssueSchemeLevelId = previousIssueSchemeLevelId;
    }

    public void setProject(Long projectId)
    {
        projectContextAccessor.setProject(projectId);
    }

    public void setProject(String projectKey)
    {
        projectContextAccessor.setProject(projectKey);
    }

    public void setProject(GenericValue project)
    {
        projectContextAccessor.setProject(project);
    }

    public void loadPreviousProject()
    {
        projectContextAccessor.loadPreviousProject();
    }

    public void setIssue(Long issueId)
    {
        issueContextAccessor.setIssue(issueId);
    }

    public void setIssue(String issueKey)
    {
        issueContextAccessor.setIssue(issueKey);
    }

    public void setIssue(GenericValue issue)
    {
        issueContextAccessor.setIssue(issue);
    }

    public void loadPreviousIssue()
    {
        issueContextAccessor.loadPreviousIssue();
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        if (hasProject())
        {
            setProperty(KEY_PROJECT_ID, getProjectId().toString());
        }

        try
        {
            // We need to map some enum-type variables
            mapProperty(KEY_ISSUE_TYPE, IssueFieldConstants.ISSUE_TYPE, getIssueTypes(), "Issue Type", output);
            mapProperty(KEY_PRIORITY, getPriorities(), "Priority", output);
        }
        catch (SAXException e)
        {
            throw new JellyTagException(e);
        }

        // We need to set default values.
        if (getProperty(IssueFieldConstants.ISSUE_TYPE) == null && !getIssueTypes().isEmpty())
        {
            setProperty(IssueFieldConstants.ISSUE_TYPE, (String) getIssueTypes().keySet().iterator().next());
        }
        if (getProperty(KEY_PRIORITY) == null && !getPriorities().isEmpty())
        {
            setProperty(KEY_PRIORITY, (String) getPriorities().keySet().iterator().next());
        }

        // Make sure the usernames are lowercase.
        String assignee = getProperty(KEY_ISSUE_ASSIGNEE);
        if (assignee != null)
        {
            setProperty(KEY_ISSUE_ASSIGNEE, assignee.toLowerCase());
        }

        try
        {
            // We need to convert the names to ID's
            convertComponentNameToId();
            convertVersionNameToId();
        }
        catch (GenericEntityException e)
        {
            throw new JellyTagException(e);
        }
        catch (NumberFormatException e)
        {
            throw new JellyTagException(e);
        }
    }

    //TODO Try to remove these dependancies on EE/Jira internals
    private void convertVersionNameToId() throws GenericEntityException
    {
        log.debug("CreateIssue.convertVersionNameToId");

        GenericValue project = ManagerFactory.getProjectManager().getProject(Long.valueOf(getProperty(KEY_PROJECT_ID)));

        convertVersionNameToId(KEY_VERSIONS, project);
        convertVersionNameToId(KEY_FIX_VERSIONS, project);
    }

    private void convertVersionNameToId(String key, GenericValue project)
    {
        String versionNames = getProperty(key);
        if (StringUtils.isNotEmpty(versionNames))
        {
            // Attept to find possible versions
            StringTokenizer tokenizer = new StringTokenizer(versionNames, ",");
            String versionName;
            List versions = new ArrayList();
            while (tokenizer.hasMoreTokens())
            {
                versionName = tokenizer.nextToken().trim();
                Version version = versionManager.getVersion(project.getLong("id"), versionName);
                if (version != null)
                {
                    versions.add(version.getString("id"));
                }
                else
                {
                    log.warn(key + " \"" + versionName + "\" unknown : " + this.toString());
                }
            }

            if (versions.size() > 0)
            {
                getProperties().put(key, versions.toArray(new String[versions.size()]));
            }
        }
    }

    //TODO Try to remove these dependancies on EE/Jira internals
    private void convertComponentNameToId()
    {
        log.debug("CreateIssue.convertComponentNameToId");

        String componentNames = getProperty(KEY_COMPONENTS);
        if (StringUtils.isNotEmpty(componentNames))
        {
            ProjectManager projectManager = ManagerFactory.getProjectManager();
            GenericValue project = getProject();
            StringTokenizer tokenizer = new StringTokenizer(componentNames, ",");
            String componentName;
            List components = new ArrayList();
            while (tokenizer.hasMoreTokens())
            {
                componentName = tokenizer.nextToken().trim();
                GenericValue component = projectManager.getComponent(project, componentName);
                if (component != null)
                {
                    components.add(component.get("id").toString());
                }
                else
                {
                    log.warn("Component \"" + componentName + "\" unknown : " + this.toString());
                }
            }

            if (components.size() > 0)
            {
                getProperties().put(KEY_COMPONENTS, components.toArray(new String[components.size()]));
            }
        }
    }

    public Map getIssueTypes()
    {
        if (issueTypeMap == null)
        {
            issueTypeMap = new HashMap();

            final Collection<GenericValue> issueTypes = ManagerFactory.getConstantsManager().getIssueTypes();
            for (final GenericValue issueType : issueTypes)
            {
                issueTypeMap.put(issueType.getString("id"), issueType.getString("name"));
            }
        }
        return Collections.unmodifiableMap(issueTypeMap);
    }

    public Map getPriorities()
    {
        if (priorityMap == null)
        {
            priorityMap = new HashMap();

            final Collection<GenericValue> priorities = ManagerFactory.getConstantsManager().getPriorities();
            for (final GenericValue priority : priorities)
            {
                priorityMap.put(priority.getString("id"), priority.getString("name"));
            }
        }
        return Collections.unmodifiableMap(priorityMap);
    }

    protected boolean propertyValidation(XMLOutput output) throws JellyTagException
    {
        try
        {
            if (!super.propertyValidation(output) || isDuplicateIssue(output))
            {
                return FAILURE;
            }
        }
        catch (SAXException e)
        {
            throw new JellyTagException(e);
        }
        return SUCCESS;
    }

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        try
        {
            log.debug("CreateIssue.postTagExecution");

            // Derive key from the issue data and cache it - so we can approximately work out if we are creating duplicates.
            copyRedirectUrlParametersToTag(getResponse().getRedirectUrl());
            if (getProperties().containsKey(KEY_ISSUE_ID))
            {
                String key = getProperty(KEY_ISSUE_ID);
                setIssue(key);

                // Add the issue and issue key into the variables if they where passed in
                if (getProperties().containsKey(KEY_ISSUE_KEY_VAR))
                {
                    getContext().setVariable(getProperty(KEY_ISSUE_KEY_VAR), key);
                }

                GenericValue issue = null;
                try
                {
                    issue = ComponentAccessor.getIssueManager().getIssue(key);
                    if (getProperties().containsKey(KEY_ISSUE_ID_VAR) || getProperties().containsKey(KEY_ISSUE_ASSIGNEE_VAR))
                    {
                        // Retrieve the issue object so we can get the id
                        if (getProperties().containsKey(KEY_ISSUE_ID_VAR))
                        {
                            getContext().setVariable(getProperty(KEY_ISSUE_ID_VAR), issue.getLong("id").toString());
                        }

                        if (getProperties().containsKey(KEY_ISSUE_ASSIGNEE_VAR))
                        {
                            getContext().setVariable(getProperty(KEY_ISSUE_ASSIGNEE_VAR), issue.getString("assignee"));
                        }
                    }

                    // NOTE: Ensure that BOTH of the methods run - USE | instead of ||
                    boolean shouldStore = (modifyCreationDate(issue) | modifyUpdateDate(issue));
                    if (shouldStore)
                    {
                        issue.store();
                        ComponentAccessor.getIssueIndexManager().reIndex(issue);
                    }
                }
                catch (GenericEntityException e)
                {
                    throw new JellyTagException(e);
                }
                catch (IndexException e)
                {
                    log.error("Error while re-indexing issue '" + issue.getString("key") + "'. Seraching results may give incorrect results");
                }
            }
        }
        finally
        {
            //JRA-11546: Set the logged in user back to what it was.
            if (hasPreviousUsername)
            {
                setLoggedinUser(previousUsername);
            }
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        if (hasPreviousUsername)
        {
            getContext().setVariable(JellyTagConstants.USERNAME, getPreviousUsername());
        }
        if (hasPreviousIssueSchemeLevelId)
        {
            getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID, getPreviousIssueSchemeLevelId());
        }
        loadPreviousProject();
        loadPreviousIssue();
    }

    //TODO Try to remove these dependancies on EE/Jira internals
    private boolean modifyCreationDate(GenericValue issue)
    {
        log.debug("CreateIssue.modifyCreationDate");

        String creationDate = getProperty(KEY_ISSUE_CREATED_DATE);
        if (creationDate != null)
        {
            Timestamp created = JellyTagUtils.parseDate(creationDate);

            // Hack Directly to the Issue Entity via EE and change the creation date
            issue.set("created", created);
            return true;
        }
        else
        {
            log.debug("Creation date not set, using todays date");
            return false;
        }
    }

    //TODO Try to remove these dependencies on EE/Jira internals
    private boolean modifyUpdateDate(GenericValue issue)
    {
        log.debug("CreateIssue.modifyUpdateDate");

        String updatedDate = getProperty(KEY_ISSUE_UPDATED_DATE);
        if (updatedDate != null)
        {
            Timestamp updated = JellyTagUtils.parseDate(updatedDate);

            // Hack Directly to the Issue Entity via EE and change the creation date
            issue.set("updated", updated);
            return true;
        }
        else
        {
            log.debug("Update date not set, using todays date");
            return false;
        }
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_PROJECT_ID, IssueFieldConstants.ISSUE_TYPE, IssueFieldConstants.SUMMARY };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[] { JellyTagConstants.ISSUE_ID, JellyTagConstants.ISSUE_KEY };
    }

    public void addCustomFieldValue(CustomField customField, String customFieldValue, String key)
    {
        final String customFieldId = customField.getId();
        if (key != null)
        {
            setProperty(customFieldId + ":" + key, customFieldValue);
        }
        else
        {
            if (propertyContains(customFieldId))
            {
                // It's a multivalue, append dummy key
                setProperty(customFieldId + ":" + String.valueOf(getProperties().size()), customFieldValue);
            }
            else
            {
                setProperty(customFieldId, customFieldValue);
            }
        }
    }
}
