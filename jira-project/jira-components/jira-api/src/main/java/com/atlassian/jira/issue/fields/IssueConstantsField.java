package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.IssueConstant;

import java.util.Collection;

/**
 * At the moment this is a fairly useless marker interface. Essentially it could be used for performing "common" actions
 * on the {@link OrderableField} objects that resolves around {@link IssueConstant}
 */
public interface IssueConstantsField
{
    Collection getIssueConstants();
}
