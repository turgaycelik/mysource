/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.SimpleColumnLayoutItem;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.List;
import java.util.Map;

public class BulkEdit1 extends AbstractBulkOperationAction
{
    private String reset;
    private long tempMax = -1;
    private static final PagerFilter ALL_ISSUES_PAGER = PagerFilter.getUnlimitedFilter();

    private final SearchProvider searchProvider;
    private final IssueManager issueManager;
    private ApplicationProperties applicationProperties;
    private final BulkEditBeanSessionHelper bulkEditBeanSessionHelper;

    public BulkEdit1(SearchService searchService, SearchProvider searchProvider, IssueManager issueManager,
            ApplicationProperties applicationProperties, final BulkEditBeanSessionHelper bulkEditBeanSessionHelper)
    {
        super(searchService, bulkEditBeanSessionHelper);
        this.searchProvider = searchProvider;
        this.issueManager = issueManager;
        this.applicationProperties = applicationProperties;
        this.bulkEditBeanSessionHelper = bulkEditBeanSessionHelper;
    }

    /**
     * Initiate bulk edit by passing in the issues from the first page of the navigator search request
     *
     * @throws Exception
     */
    public String doDefault() throws Exception
    {
        if (getSearchRequest() == null)
        {
            return getRedirect("IssueNavigator.jspa");
        }

        // reset BulkEditBean
        if (isNewBulkEdit())
        {
            initialiseBulkEditBean();
        }

        return super.doDefault();
    }

    private void initialiseBulkEditBean() throws SearchException
    {
        // set BulkEditBean to use the issues from the navigator (cached search request) IF the user has just arrived at the
        // first step of BulkEdit
        BulkEditBean bulkEditBean = new BulkEditBeanImpl(issueManager);
        // TODO: But bulkEditBean.getIssuesFromSearchRequest() is always null, so this operation is pointless
        bulkEditBean.setIssuesInUse(bulkEditBean.getIssuesFromSearchRequest());

        // If we have a temp max then we are bulk editing the first tempMax Issues
        // Else we are bulk editing the current page
        PagerFilter pager;
        if (getTempMax() > -1)
        {
            // clamp tempMax to maxint just in case someone sets temp max too big
            int max = (int)Math.min((long) Integer.MAX_VALUE, getTempMax());
            pager = new PagerFilter(max);
            bulkEditBean.setMaxIssues(max);
        }
        else
        {
            // Record the pager start from the current pager, if one exists
            pager = getSessionPagerFilterManager().getCurrentObject();
            if (pager == null)
            {
                pager = new PagerFilter(PropertiesUtil.getIntProperty(applicationProperties, APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT, Integer.MAX_VALUE));
            }
        }
        List issues = searchProvider.search((getSearchRequest() != null) ? getSearchRequest().getQuery() : null, getLoggedInUser(), pager).getIssues();
        bulkEditBean.setIssuesFromSearchRequest(issues);

        bulkEditBeanSessionHelper.storeToSession(bulkEditBean);

    }

    protected void doValidation()
    {
        if (getSearchRequest() != null)
        {
            // reset the bean and setup the parameters as the issues have either been picked for the first time, or user came back to the
            // first step of the wizard and have reselected.
            // NOTE This probably shouldn't be in here as it changes things, but it doesn't make sense to put the validation in the execute method either
            try
            {
                initialiseBulkEditBean();
            }
            catch (SearchException e)
            {
                log.error("Error occurred while initialising BulkEditBean.", e);
                addErrorMessage(getText("bulk.bean.initialise.error"));
            }
            getBulkEditBean().setParams(ActionContext.getParameters());

            // the list of selected issues will be lazy-loaded by the BulkEdit Bean using the HTTP params set above.
            final List selectedIssues = getBulkEditBean().getSelectedIssues();
            if (selectedIssues.isEmpty())
            {
                addErrorMessage(getText("bulk.edit.you.must.select.at.least.one.issue"));
            }
            else
            {
                // Check if we have exceeded the max bulk edit issue count.
                final String maxIssuesString = getApplicationProperties()
                        .getDefaultBackedString(APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT);
                if (StringUtils.isNotEmpty(maxIssuesString))
                {
                    try
                    {
                        int maxIssues = Integer.parseInt(maxIssuesString);

                        if (maxIssues >= 0 && selectedIssues.size() > maxIssues)
                        {
                            addErrorMessage(getText("bulk.edit.limit.issue.count.error", Integer.toString(maxIssues)));
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        log.error("Error parsing property '" + APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT + "' = '"
                                  + maxIssuesString + "' as it is not a number. Ignoring property.", e);
                    }
                }
            }
        }
    }

    public List getColumns() throws Exception
    {
        List columns = super.getColumns();
        columns.add(0, getCheckboxColumn());
        return columns;
    }

    private SimpleColumnLayoutItem getCheckboxColumn()
    {
        return new SimpleColumnLayoutItem()
        {
            public String getHtml(Map displayParams, Issue issue)
            {
                final String name = getBulkEditBean().getCheckboxName(issue);
                final String checked = getBulkEditBean().isChecked(issue) ? "checked" : "";

                return String.format("<input type=\"checkbox\" name=\"%s\" %s />", name, checked);
            }

            public String getHeaderHtml()
            {
                return "<input type='checkbox' id='bulkedit-select-all' name='all' value='on'>";
            }
        };
    }

    /**
     * The first step in bulk edit collects and stores the issues that the user wants to bulkedit
     *
     * @throws Exception
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getSearchRequest() == null)
        {
            return redirectToSessionTimeout();
        }

        getBulkEditBean().setCurrentStep(2);
        return getRedirect("BulkChooseOperation!default.jspa");
    }

    public String getCheckboxName(GenericValue issue)
    {
        return BulkEditBean.BULKEDIT_PREFIX + issue.getLong("id");
    }

    public String getReset()
    {
        return reset;
    }

    public void setReset(String reset)
    {
        this.reset = reset;
    }

    private boolean isNewBulkEdit()
    {
        return "true".equals(getReset());
    }

    public void setCurrentStep(int step)
    {
        getBulkEditBean().setCurrentStep(step);
    }

    public long getTempMax()
    {
        return tempMax;
    }

    public void setTempMax(long tempMax)
    {
        this.tempMax = Math.min(PropertiesUtil.getIntProperty(applicationProperties, APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT, Integer.MAX_VALUE), tempMax);
    }

    public boolean isBulkLimited()
    {
        String maxBulkEdit = applicationProperties.getDefaultBackedString(APKeys.JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT);
        if (maxBulkEdit != null) {
            try
            {
                int configuredMax = Integer.parseInt(maxBulkEdit);
                return tempMax >= configuredMax;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
        return false;
    }

    private String redirectToSessionTimeout()
    {
        ActionContext.getSession().put(SessionKeys.SESSION_TIMEOUT_MESSAGE, getText("bulk.edit.session.timeout.message"));
        return getRedirect("SessionTimeoutMessage.jspa");
    }
}
