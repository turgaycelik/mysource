package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.beans.LinkIssueRequestJsonBean;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_LINKS;
import static com.atlassian.jira.issue.fields.IssueLinksSystemField.PARAMS_ISCREATEISSUE;
import static com.atlassian.jira.issue.fields.IssueLinksSystemField.PARAMS_ISSUE_KEYS;
import static com.atlassian.jira.issue.fields.IssueLinksSystemField.PARAMS_LINK_TYPE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @since v5.0
 */
public class IssueLinksRestFieldOperationsHandler implements RestFieldOperationsHandler
{
    private final I18nHelper i18nHelper;
    private final IssueLinkTypeFinder issueLinkTypeFinder;
    private final IssueFinder issueFinder;

    public IssueLinksRestFieldOperationsHandler(I18nHelper i18nHelper, IssueLinkTypeFinder issueLinkTypeFinder, IssueFinder issueFinder)
    {
        this.i18nHelper = i18nHelper;
        this.issueLinkTypeFinder = issueLinkTypeFinder;
        this.issueFinder = issueFinder;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName());
    }

    @Override
    public ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations)
    {
        SimpleErrorCollection errors = new SimpleErrorCollection();
        if (operations.isEmpty())
        {
            return errors;
        }

        if (operations.size() > 1)
        {
            errors.addError(ISSUE_LINKS, i18nHelper.getText("rest.operations.morethanone", String.valueOf(operations.size()), fieldId));
            return errors;
        }

        LinkIssueRequestJsonBean linkRequest = operations.get(0).getData().convertValue(ISSUE_LINKS, LinkIssueRequestJsonBean.class, errors);
        if (errors.hasAnyErrors())
        {
            return errors;
        }
        if (linkRequest == null)
        {
            errors.addErrorMessage(i18nHelper.getText("rest.issue.link.null"));
            return errors;
        }

        IssueLinkType linkType = issueLinkTypeFinder.findIssueLinkType(linkRequest.getType(), errors);

        Issue inwardIssue = linkRequest.inwardIssue() != null ? issueFinder.findIssue(linkRequest.inwardIssue(), errors) : null;
        Issue outwardIssue = linkRequest.outwardIssue() != null ? issueFinder.findIssue(linkRequest.outwardIssue(), errors) : null;
        if (inwardIssue != null && outwardIssue != null)
        {
            errors.addErrorMessage(i18nHelper.getText("rest.issue.link.error.too.many.keys"));
        }

        if (linkRequest.getComment() != null)
        {
            errors.addErrorMessage(i18nHelper.getText("rest.issue.link.comment.not.allowed"));
        }

        if (errors.hasAnyErrors())
        {
            return errors;
        }

        Map<String, String[]> paramsMap = inputParameters.getActionParameters();
        paramsMap.put(ISSUE_LINKS, new String[] { TRUE.toString() });
        paramsMap.put(PARAMS_ISCREATEISSUE, new String[] { FALSE.toString() });
        paramsMap.put(PARAMS_LINK_TYPE, new String[] { outwardIssue != null ? linkType.getOutward() : linkType.getInward() });
        paramsMap.put(PARAMS_ISSUE_KEYS, new String[] { outwardIssue != null ? outwardIssue.getKey() : inwardIssue.getKey() });

        return errors;
    }
}
