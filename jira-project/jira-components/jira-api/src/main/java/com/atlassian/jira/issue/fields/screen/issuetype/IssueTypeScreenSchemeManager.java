package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface IssueTypeScreenSchemeManager
{
    String ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME = "IssueTypeScreenScheme";
    String ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME = "IssueTypeScreenSchemeEntity";

    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes();

    public IssueTypeScreenScheme getIssueTypeScreenScheme(Long id);

    IssueTypeScreenScheme getIssueTypeScreenScheme(GenericValue project);

    IssueTypeScreenScheme getIssueTypeScreenScheme(Project project);

    FieldScreenScheme getFieldScreenScheme(Issue issue);

    Collection getIssueTypeScreenSchemeEntities(IssueTypeScreenScheme issueTypeScreenScheme);

    void createIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    void updateIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    void removeIssueTypeSchemeEntities(IssueTypeScreenScheme issueTypeScreenScheme);

    void removeIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    void createIssueTypeScreenSchemeEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    void updateIssueTypeScreenSchemeEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    void removeIssueTypeScreenSchemeEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    Collection getIssueTypeScreenSchemes(FieldScreenScheme fieldScreenScheme);

    void addSchemeAssociation(GenericValue project, IssueTypeScreenScheme issueTypeScreenScheme);

    void addSchemeAssociation(Project project, IssueTypeScreenScheme issueTypeScreenScheme);

    void removeSchemeAssociation(GenericValue project, IssueTypeScreenScheme issueTypeScreenScheme);

    void removeSchemeAssociation(Project project, IssueTypeScreenScheme issueTypeScreenScheme);

    Collection<GenericValue> getProjects(IssueTypeScreenScheme issueTypeScreenScheme);

    void associateWithDefaultScheme(GenericValue project);

    void associateWithDefaultScheme(Project project);

    IssueTypeScreenScheme getDefaultScheme();

    void refresh();
}
