package com.atlassian.jira.mail;

import com.atlassian.core.util.HTMLUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestUtils;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.BuildUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.ColumnLayoutItemProvider;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.query.Query;
import com.atlassian.query.order.SearchSort;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.template.TemplateSources.fragment;
import static com.atlassian.jira.user.ApplicationUsers.toDirectoryUser;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class SubscriptionMailQueueItem extends AbstractMailQueueItem
{
    // Username was a used as a foreign key until JIRA 6.0, which is why the column is called "username" in the db table.
    public static final String USER_KEY_COLUMN_NAME = "username";
    private static final Logger log = Logger.getLogger(SubscriptionMailQueueItem.class);

    // As determined from the email-template-id-mappings.xml file
    private final Long FILTER_SUBSCRIPTION_TEMPLATE_ID = 10000L;
    private GenericValue subscription;
    private SearchRequest request;
    private ApplicationUser subscriptionCreator;
    private TemplateManager templateManager;
    private final UserManager userManager;
    private final GroupManager groupManager;

    private final MailingListCompiler mailingListCompiler;
    private final SearchService searchService;
    private static final int DEFAULT_MAIL_MAX_ISSUES = 200;

    SubscriptionMailQueueItem(final GenericValue sub, final MailingListCompiler mailingListCompiler,
            final SearchService searchService, final TemplateManager templateManager, final UserManager userManager,
            final GroupManager groupManager)
    {
        super();
        this.subscription = sub;
        this.mailingListCompiler = mailingListCompiler;
        this.searchService = searchService;
        this.templateManager = templateManager;
        this.userManager = userManager;
        this.groupManager = groupManager;
    }

    public void send() throws MailException
    {
        incrementSendCount();

        //Retrieve all the users to send the filter to.
        final String groupName = subscription.getString("group");
        if (isNotEmpty(groupName))
        {
            final Group group = userManager.getGroup(groupName);
            if (group == null)
            {
                log.warn(
                        format
                                (
                                        "Group '%s' referenced in subscription '%d' of filter '%d' does not exist.",
                                        groupName, subscription.getLong("id"), subscription.getLong("filterID")
                                )
                );
                return;
            }

            try
            {
                final Iterable<User> groupUser = groupManager.getUsersInGroup(groupName);
                for (final User user : groupUser)
                {
                    sendSearchRequestEmail(ApplicationUsers.from(user));
                }
            }
            catch (Exception ex)
            {
                log.error(ex, ex);
                throw new MailException(ex);
            }
        }
        else
        {
            final String getKey = subscription.getString(USER_KEY_COLUMN_NAME);
            final ApplicationUser user = getSubscriptionUser();
            if (user == null)
            {
                log.warn(
                        format
                                (
                                        "User '%s' referenced in subscription '%d' of filter '%d' does not exist.",
                                        getKey, subscription.getLong("id"), subscription.getLong("filterID")
                                )
                );
            }
            else
            {
                try
                {
                    sendSearchRequestEmail(user);
                }
                catch (Exception ex)
                {
                    log.error(ex, ex);
                    throw new MailException(ex);
                }
            }
        }
    }

    private void sendSearchRequestEmail(final ApplicationUser user) throws Exception
    {
        // JRA-16611 : put the current user in the authentication context
        final JiraAuthenticationContext jiraAuthenticationContext = getJiraAuthenticationContext();
        final ApplicationUser originalUser = jiraAuthenticationContext.getUser();
        jiraAuthenticationContext.setLoggedInUser(toDirectoryUser(user));

        try
        {
            //Send mail to each user with the correctly executed search request
            final Map<String, Object> params = getContextParams(subscription, getSearchRequest(), user);
            final Set<NotificationRecipient> recipient = newHashSet();
            recipient.add(new NotificationRecipient(user));
            if (subscription == null)
            {
                throw new RuntimeException("Null subscription for user " + (user == null ? "null" : user.getName()));
            }
            final String emailOnEmptyStr = subscription.getString("emailOnEmpty");
            if (emailOnEmptyStr == null)
            {
                throw new RuntimeException("emailOnEmpty not set for subscription " + subscription + ", user " + user);
            }
            if (params.get("issues") == null)
            {
                throw new RuntimeException("Null list of issues for subscription " + subscription + ", user " + user);
            }
            if (user != null && user.equals(getSubscriptionUser()))
            {
                params.put("recipientIsAuthor", Boolean.TRUE);
            }
            if (Boolean.valueOf(emailOnEmptyStr) || !((Collection) params.get("issues")).isEmpty())
            {
                mailingListCompiler.sendLists
                        (
                                recipient, null, null, FILTER_SUBSCRIPTION_TEMPLATE_ID,
                                getApplicationProperties().getString(APKeys.JIRA_BASEURL),
                                params, null
                        );
            }
        }
        finally
        {
            // restore the original user into the JiraAuthenticationContext
            jiraAuthenticationContext.setLoggedInUser(toDirectoryUser(originalUser));
        }
    }

    @VisibleForTesting
    JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    public IssueTableLayoutBean getTableLayout(final ApplicationUser user) throws Exception
    {
        final SearchRequest searchRequest = getSearchRequest();
        Collection<SearchSort> searchSorts = null;
        if (searchRequest != null)
        {
            final Query query = searchRequest.getQuery();
            if (query.getOrderByClause() != null)
            {
                searchSorts = query.getOrderByClause().getSearchSorts();
            }
        }
        final IssueTableLayoutBean bean = new IssueTableLayoutBean(getColumns(user), searchSorts);
        bean.setSortingEnabled(false);
        bean.addCellDisplayParam(FieldRenderingContext.EMAIL_VIEW, Boolean.TRUE);
        return bean;
    }

    public List <ColumnLayoutItem> getColumns(final ApplicationUser user) throws Exception
    {
        final SearchRequest searchRequest = getSearchRequest();

        if (searchRequest == null)
        {
            return getColumnsProvider().getUserColumns(toDirectoryUser(user));
        }
        else
        {
            return getColumnsProvider().getColumns(toDirectoryUser(user), searchRequest);
        }
    }

    @VisibleForTesting
    ColumnLayoutItemProvider getColumnsProvider()
    {
        return new ColumnLayoutItemProvider();
    }

    @VisibleForTesting
    I18nHelper.BeanFactory getI18nBeanFactory()
    {
        return ComponentAccessor.getComponent(I18nHelper.BeanFactory.class);
    }

    @VisibleForTesting
    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    @VisibleForTesting
    SearchRequestService getSearchRequestService()
    {
        return ComponentAccessor.getComponent(SearchRequestService.class);
    }

    /**
     * This is the subject as displayed in the Mail Queue Admin page when the mail is BEING sent.
     * The string is retrieved in the default language for the JIRA system.
     *
     * @return String   the subject as displayed on the mail queue admin page
     */
    public String getSubject()
    {
        final I18nHelper i18n = getI18nBeanFactory().getInstance(getApplicationProperties().getDefaultLocale());
        try {
            final String subjectTemplate = templateManager.getTemplateContent(FILTER_SUBSCRIPTION_TEMPLATE_ID, "subject");
            final Map<String, Object> contextParams = getContextParams(subscription, getSearchRequest(), null);
            contextParams.put("i18n", i18n);
            // Provide an OutlookDate formatter with the users locale
            final OutlookDate formatter = new OutlookDate(i18n.getLocale());
            contextParams.put("dateformatter", formatter);
            return getTemplatingEngine().render(fragment(subjectTemplate)).applying(contextParams).asPlainText();
        }
        catch (Exception e)
        {
            log.error("Could not determine subject", e);
            return i18n.getText("bulk.bean.initialise.error");
        }
    }

    @VisibleForTesting
    SearchRequest getSearchRequest()
    {
        //Retrieve the search request for this subscription
        if (request == null)
        {
            final JiraServiceContext ctx = new JiraServiceContextImpl(getSubscriptionUser());
            request = getSearchRequestService().getFilter(ctx, subscription.getLong("filterID"));
        }
        return request;
    }

    private ApplicationUser getSubscriptionUser()
    {
        if (subscriptionCreator == null)
        {
            subscriptionCreator = userManager.getUserByKey(subscription.getString(USER_KEY_COLUMN_NAME));
        }

        return subscriptionCreator;
    }

    private Map<String, Object> getContextParams(final GenericValue sub, final SearchRequest sr, final ApplicationUser u) throws Exception
    {
        final String baseURL = getApplicationProperties().getString(APKeys.JIRA_BASEURL);
        final String contextPath = getContextPath(baseURL);

        final Map<String, Object> contextParams = new HashMap<String, Object>();
        final IssueTableLayoutBean tableLayout = getTableLayout(u);
        final IssueTableWebComponent iwtc = new IssueTableWebComponent();
        // now we need to fake out the action context (bear in mind we keep the old one around - in case they're running from the web not a job, ie Flush mail queue operation)
        final ActionContext oldCtx = ActionContext.getContext();
        ActionContext.setContext(new ActionContext());
        ActionContext.setRequest(new SubscriptionMailQueueMockRequest(contextPath)); // our faked request
        // now put back the old context to be nice
        ActionContext.setContext(oldCtx);
        if(u != null)
        {
            final SearchResults results = getSearchProvider().search((sr != null) ? sr.getQuery() : null, u, getPageFilter());
            final List<Issue> issues = results.getIssues();
            final String issueTableHtml = iwtc.getHtml(tableLayout, issues, null);
            contextParams.put("totalIssueCount", results.getTotal());
            contextParams.put("actualIssueCount", issues.size());
            contextParams.put("issueTableHtml", issueTableHtml);
            contextParams.put("issues", issues);
            contextParams.put("user", u);
        }
        if(getSubscriptionUser() != null)
        {
            contextParams.put("username",getSubscriptionUser().getUsername());
        }
        contextParams.put("baseHREF", getBaseURLWithoutContext(baseURL));
        contextParams.put("constantsManager", getConstantsManager());
        contextParams.put("req", new SubscriptionMailQueueMockRequest(contextPath));
        contextParams.put("searchRequest", sr);
        contextParams.put("SRUtils", new SearchRequestUtils());
        contextParams.put("subscription", sub);
        contextParams.put("StringUtils", new StringUtils());
        contextParams.put("HTMLUtils", new HTMLUtils());
        contextParams.put("build", ComponentAccessor.getComponent(BuildUtilsInfo.class));
        contextParams.put("textutils", new TextUtils());
        contextParams.put("webResourceManager", getWebResourceManager());
        contextParams.put("urlModeAbsolute", UrlMode.ABSOLUTE);

        return contextParams;
    }

    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
    }

    @VisibleForTesting
    SearchProvider getSearchProvider()
    {
        return ComponentAccessor.getComponent(SearchProvider.class);
    }

    @VisibleForTesting
    WebResourceManager getWebResourceManager()
    {
        return ComponentAccessor.getWebResourceManager();
    }

    @VisibleForTesting
    ConstantsManager getConstantsManager()
    {
        return ComponentAccessor.getConstantsManager();
    }

    // Extracts the context path (if any) from a base URL.
    private String getContextPath(final String baseURL)
    {
        try
        {
            final URL url = new URL(baseURL);
            return url.getPath();
        }
        catch (MalformedURLException e)
        {
            log.error("Incorrect baseURL format: " + baseURL);
            return "";
        }
    }

    /**
     * Return's the base url minus the context. This is intended to be used as a
     * for a base href.
     *
     * @param baseURL The base url to remove the context from.
     *
     * @return A String containing the base url minus the context path.
     */
    private String getBaseURLWithoutContext(final String baseURL)
    {
        final String path = getContextPath(baseURL);

        return StringUtils.chomp(baseURL, path);
    }

    private PagerFilter getPageFilter()
    {
        final String application = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_MAIL_MAX_ISSUES);

        int maxEmail;
        if (isBlank(application))
        {
            log.warn(format("The maximum number of issues to include in subscription email '(%s)' is not configured. Using default of %d", APKeys.JIRA_MAIL_MAX_ISSUES, DEFAULT_MAIL_MAX_ISSUES));
            maxEmail = DEFAULT_MAIL_MAX_ISSUES;
        }
        else
        {
            try
            {
                maxEmail = Integer.parseInt(application);

                if (maxEmail == 0)
                {
                    log.warn(format("The maximum number of issues to include in subscription email '(%s)' cannot be zero. Using default of %d", APKeys.JIRA_MAIL_MAX_ISSUES, DEFAULT_MAIL_MAX_ISSUES));
                    maxEmail = DEFAULT_MAIL_MAX_ISSUES;
                }
                else if (maxEmail < 0)
                {
                    maxEmail = -1;
                }
            }
            catch (NumberFormatException e)
            {
                log.warn(format("The maximum number of issues to include in subscription email '(%s)' is not a valid number. Using default of %d", APKeys.JIRA_MAIL_MAX_ISSUES, DEFAULT_MAIL_MAX_ISSUES));
                maxEmail = DEFAULT_MAIL_MAX_ISSUES;
            }
        }

        return new PagerFilter(maxEmail);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof SubscriptionMailQueueItem))
        {
            return false;
        }

        final SubscriptionMailQueueItem subscriptionMailQueueItem = (SubscriptionMailQueueItem) o;

        if (request != null ? !request.equals(subscriptionMailQueueItem.request) : subscriptionMailQueueItem.request != null)
        {
            return false;
        }
        if (!subscription.equals(subscriptionMailQueueItem.subscription))
        {
            return false;
        }
        if (subscriptionCreator != null ? !subscriptionCreator.equals(subscriptionMailQueueItem.subscriptionCreator) : subscriptionMailQueueItem.subscriptionCreator != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = subscription.hashCode();
        result = 29 * result + (request != null ? request.hashCode() : 0);
        result = 29 * result + (subscriptionCreator != null ? subscriptionCreator.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        final ApplicationUser subscriptionUser;
        subscriptionUser = getSubscriptionUser();
        return this.getClass().getName() + " owner: '" + subscriptionUser + "'";
    }
}
