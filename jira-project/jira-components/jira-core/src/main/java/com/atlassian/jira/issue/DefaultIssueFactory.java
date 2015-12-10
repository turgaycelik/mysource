package com.atlassian.jira.issue;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import org.apache.lucene.document.Document;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultIssueFactory implements IssueFactory
{
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final VersionManager versionManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final FieldManager fieldManager;
    private final AttachmentManager attachmentManager;
    private final ProjectFactory projectFactory;
    private final LabelManager labelManager;
    private final ProjectComponentManager projectComponentManager;
    private UserManager userManager;
    private final JqlLocalDateSupport jqlLocalDateSupport;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public DefaultIssueFactory(final IssueManager issueManager, final ProjectManager projectManager,
            final VersionManager versionManager, final IssueSecurityLevelManager issueSecurityLevelManager,
            final ConstantsManager constantsManager, final SubTaskManager subTaskManager,
            final FieldManager fieldManager, final AttachmentManager attachmentManager,
            final ProjectFactory projectFactory, final LabelManager labelManager, final ProjectComponentManager projectComponentManager,
            final UserManager userManager, JqlLocalDateSupport jqlLocalDateSupport, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.fieldManager = fieldManager;
        this.attachmentManager = attachmentManager;
        this.projectFactory = projectFactory;
        this.labelManager = labelManager;
        this.projectComponentManager = projectComponentManager;
        this.userManager = userManager;
        this.jqlLocalDateSupport = jqlLocalDateSupport;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public MutableIssue getIssue()
    {
        return getIssue((GenericValue) null);
    }

    public MutableIssue getIssue(final GenericValue issueGV)
    {
        return new IssueImpl(issueGV, issueManager, projectManager, versionManager, issueSecurityLevelManager, constantsManager, subTaskManager,
            attachmentManager, labelManager, projectComponentManager, userManager, jiraAuthenticationContext);
    }

    @Override
    public MutableIssue getIssueOrNull(GenericValue issueGV)
    {
        if (issueGV == null)
            return null;
        else
            return getIssue(issueGV);
    }

    public MutableIssue cloneIssue(final Issue issue)
    {
        return new IssueImpl(issue, issueManager, projectManager, versionManager, issueSecurityLevelManager, constantsManager, subTaskManager,
            attachmentManager, labelManager, projectComponentManager, userManager, jiraAuthenticationContext);
    }

    public List<Issue> getIssues(final Collection<GenericValue> issueGVs)
    {
        final List<Issue> issues = new ArrayList<Issue>(issueGVs.size());
        for (final GenericValue issue : issueGVs)
        {
            issues.add(getIssue(issue));
        }
        return issues;
    }

    public Issue getIssue(final Document issueDocument)
    {
        return new DocumentIssueImpl(issueDocument, constantsManager, fieldManager, issueManager, this, attachmentManager, projectFactory, jqlLocalDateSupport);
    }

    public MutableIssue cloneIssueNoParent(final Issue issue)
    {
        final IssueImpl clonedIssue = new IssueImpl((GenericValue) issue.getGenericValue().clone(), issueManager, projectManager, versionManager,
            issueSecurityLevelManager, constantsManager, subTaskManager, attachmentManager, labelManager, projectComponentManager, userManager, jiraAuthenticationContext);
        clonedIssue.hasNoParentId = true;

        return clonedIssue;
    }
}
