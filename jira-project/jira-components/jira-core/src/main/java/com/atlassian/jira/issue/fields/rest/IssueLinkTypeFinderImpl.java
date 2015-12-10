package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Collection;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_LINKS;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * This "finder" class is used to find issue link types based on 'id' or 'name'.
 *
 * @since v5.0
 */
public final class IssueLinkTypeFinderImpl implements IssueLinkTypeFinder
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public IssueLinkTypeFinderImpl(IssueLinkTypeManager issueLinkTypeManager, I18nBean.BeanFactory i18nFactory, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.i18nFactory = i18nFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    /**
     * Fetches an IssueLinkType from its manager based on either the 'id' or the 'name' property of
     * <code>linkTypeBean</code>. If the link type does not exist, the appropriate error will be added to the
     * <code>errors</code> parameter (and the return value will be null).
     *
     *
     *
     * @param linkTypeBean the IssueLinkTypeJsonBean
     * @param errors a SimpleErrorCollection
     * @return an IssueLinkType, or null
     */
    @Override
    public IssueLinkType findIssueLinkType(IssueLinkTypeJsonBean linkTypeBean, ErrorCollection errors)
    {
        if (isNotBlank(linkTypeBean.id()))
        {
            IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(Long.parseLong(linkTypeBean.id()));
            if (issueLinkType == null)
            {
                errors.addErrorMessage(getText("rest.issue.link.type.with.id.not.found", linkTypeBean.id()));
            }

            return issueLinkType;
        }

        if (isNotBlank(linkTypeBean.name()))
        {
            Collection<IssueLinkType> linkTypes = issueLinkTypeManager.getIssueLinkTypesByName(linkTypeBean.name());
            if (linkTypes == null || linkTypes.isEmpty())
            {
                errors.addErrorMessage(getText("rest.issue.link.type.not.found", linkTypeBean.name()));
                return null;
            }

            if (linkTypes.size() > 1)
            {
                errors.addErrorMessage(getText("rest.issue.link.type.ambiguous.name", linkTypeBean.name()));
                return null;
            }

            return linkTypes.iterator().next();
        }

        errors.addError(ISSUE_LINKS, getText("rest.issue.link.type.no.id.or.name"));
        return null;
    }

    private String getText(String key, String... params)
    {
        return i18nFactory.getInstance(jiraAuthenticationContext.getLoggedInUser()).getText(key, params);
    }
}
