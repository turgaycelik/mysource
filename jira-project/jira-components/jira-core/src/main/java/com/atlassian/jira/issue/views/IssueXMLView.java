package com.atlassian.jira.issue.views;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.RssViewUtils;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.issueview.IssueViewFieldParams;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.RequestContextParameterHolder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.CustomIssueXMLViewFieldsBean;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A view of an issue that produces a full XML view of an issue.  It is also valid RSS.
 */
public class IssueXMLView extends AbstractIssueView
{
    private static final Logger log = Logger.getLogger(IssueXMLView.class);

    private static final String RSS_MODE_RENDERED = "rendered";
    private static final String RSS_MODE_RAW = "raw";

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final FieldLayoutManager fieldLayoutManager;
    private final CommentManager commentManager;
    private final IssueViewUtil issueViewUtil;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final BuildUtilsInfo buildUtilsInfo;
    private final FieldVisibilityManager fieldVisibilityManager;

    public IssueXMLView(final JiraAuthenticationContext authenticationContext,
            final ApplicationProperties applicationProperties, final FieldLayoutManager fieldLayoutManager,
            final CommentManager commentManager, final IssueViewUtil issueViewUtil,
            final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory,
            final BuildUtilsInfo buildUtilsInfo, final DateTimeFormatterFactory dateTimeFormatterFactory,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.fieldLayoutManager = fieldLayoutManager;
        this.commentManager = commentManager;
        this.issueViewUtil = issueViewUtil;
        this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams)
    {
        String header = getHeader(issueViewRequestParams);
        String body = getBody(issue, issueViewRequestParams);
        String footer = getFooter();
        return header + body + footer;

    }

    private String getFooter()
    {
        return descriptor.getHtml("footer", Collections.<String, Object>emptyMap());
    }

    private String getHeader(IssueViewRequestParams issueViewRequestParams)
    {
        // header HTML
        Map<String, Object> headerParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        headerParams.put("title", applicationProperties.getString(APKeys.JIRA_TITLE));
        headerParams.put("buildInfo", buildUtilsInfo.getBuildInformation());
        headerParams.put("currentDate", new Date());
        headerParams.put("rssLocale", RssViewUtils.getRssLocale(authenticationContext.getLocale()));
        headerParams.put("version", buildUtilsInfo.getVersion());
        headerParams.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
        headerParams.put("buildDate", new SimpleDateFormat("dd-MM-yyyy").format(buildUtilsInfo.getCurrentBuildDate()));
        headerParams.put("customViewRequested", issueViewRequestParams.getIssueViewFieldParams().isCustomViewRequested());

        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        final RequestContextParameterHolder requestParameters = velocityRequestContext.getRequestParameters();
        if (requestParameters != null)
        {
            final String requestURL = requestParameters.getRequestURL();
            if (requestURL != null)
            {
                final String queryString = StringEscapeUtils.escapeXml(requestParameters.getQueryString());
                // Prepare the URL such that in the velocity template we can easily append parameters to it
                if (queryString != null)
                {
                    headerParams.put("exampleURLPrefix", requestURL + "?" + queryString + "&amp;");
                }
                else
                {
                    headerParams.put("exampleURLPrefix", requestURL + "?");
                }
            }
        }


        return descriptor.getHtml("header", headerParams);
    }

    public String getBody(Issue issue, IssueViewRequestParams issueViewRequestParams)
    {

        Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        bodyParams.put("issue", issue);
        bodyParams.put("i18n", authenticationContext.getI18nHelper());
        bodyParams.put("dateTimeFormatter", dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME));
        bodyParams.put("dateFormatter", dateTimeFormatterFactory.formatter().withSystemZone().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME));

        final CustomIssueXMLViewFieldsBean customIssueXmlViewFieldsBean = new CustomIssueXMLViewFieldsBean(
                fieldVisibilityManager, issueViewRequestParams.getIssueViewFieldParams(),
                issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());

        bodyParams.put("issueXmlViewFields", customIssueXmlViewFieldsBean);

        //JRA-13343: If rssMode has been specified in the URL, set it into the velocity parameter map.
        VelocityRequestContextFactory velocityRequestContextFactory = new DefaultVelocityRequestContextFactory(applicationProperties);
        VelocityRequestContext velocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        String rssMode = velocityRequestContext.getRequestParameter("rssMode");
        //for the moment lets only allow the 'raw' mode and nothing else
        if (StringUtils.isNotEmpty(rssMode) && RSS_MODE_RAW.equals(rssMode))
        {
            bodyParams.put("rssMode", RSS_MODE_RAW);
        }
        else
        {
            if (StringUtils.isNotEmpty(rssMode))
            {
                log.warn("Invalid rssMode parameter specified '" + rssMode + "'.  Currently only supports '" + RSS_MODE_RAW + "'");
            }
            bodyParams.put("rssMode", RSS_MODE_RENDERED);
        }
        bodyParams.put("votingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING));
        bodyParams.put("watchingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING));
        bodyParams.put("xmlView", this);
        final ApplicationUser user = authenticationContext.getUser();
        bodyParams.put("remoteUser", user);

        bodyParams.put("linkingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING));
        if (customIssueXmlViewFieldsBean.isFieldRequestedAndVisible(IssueFieldConstants.ISSUE_LINKS))
        {
            bodyParams.put("linkCollection", issueViewUtil.getLinkCollection(issue, user == null ? null : user.getDirectoryUser()));
        }

        if (customIssueXmlViewFieldsBean.isFieldRequestedAndVisible(IssueFieldConstants.COMMENT))
        {
            List comments = commentManager.getCommentsForUser(issue, user);
            if (applicationProperties.getDefaultBackedString(APKeys.JIRA_ISSUE_ACTIONS_ORDER).equals(ACTION_ORDER_DESC))
            {
                Collections.reverse(comments);
            }
            bodyParams.put("comments", comments);
        }

        final boolean timeTrackingEnabled = applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        final boolean subTasksEnabled = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS);
        bodyParams.put("timeTrackingEnabled", timeTrackingEnabled);
        if (timeTrackingEnabled && subTasksEnabled && !issue.isSubTask())
        {
            AggregateTimeTrackingBean bean = aggregateTimeTrackingCalculatorFactory.getCalculator(issue).getAggregates(issue);
            if (bean.getSubTaskCount() > 0)
            {
                bodyParams.put("aggregateTimeTrackingBean", bean);
            }
        }

        List customFields = getVisibleCustomFields(issue, user == null ? null : user.getDirectoryUser(), issueViewRequestParams.getIssueViewFieldParams());
        bodyParams.put("visibleCustomFields", customFields);

        return descriptor.getHtml("view", bodyParams);
    }

    public String getRenderedContent(String fieldName, String value, Issue issue)
    {
        return issueViewUtil.getRenderedContent(fieldName, value, issue);
    }

    public String getPrettyDuration(Long v)
    {
        return issueViewUtil.getPrettyDuration(v);
    }

    public List<FieldLayoutItem> getVisibleCustomFields(Issue issue, User user, IssueViewFieldParams issueViewFieldParams)
    {
        String issueTypeId = issue.getIssueTypeObject().getId();
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
        List<FieldLayoutItem> customFields = fieldLayout.getVisibleCustomFieldLayoutItems(issue.getProjectObject(), EasyList.build(issueTypeId));

        List<FieldLayoutItem> result = customFields;
        if (issueViewFieldParams != null)
        {
            if (!issueViewFieldParams.isAllCustomFields())
            {
                if (issueViewFieldParams.isCustomViewRequested())
                {
                    List<FieldLayoutItem> requestedCustomFields = new ArrayList<FieldLayoutItem>();
                    for (FieldLayoutItem customField : customFields)
                    {
                        if (issueViewFieldParams.getCustomFieldIds().contains(customField.getOrderableField().getId()))
                        {
                            requestedCustomFields.add(customField);
                        }
                    }
                    result = requestedCustomFields;
                }
            }
        }

        result = new ArrayList<FieldLayoutItem>(result);
        Collections.sort(result);
        return result;
    }

    public String getCustomFieldXML(CustomField field, Issue issue)
    {
        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field);

        // Only try to get the xml data if the custom field has a template defined for XML
        CustomFieldTypeModuleDescriptor moduleDescriptor = field.getCustomFieldType().getDescriptor();
        if (moduleDescriptor.isXMLTemplateExists())
        {
            String xmlValue = moduleDescriptor.getViewXML(field, issue, fieldLayoutItem, false);
            // If the templace generates a null value we don't want to return it and we want to alert the logs
            if (xmlValue != null)
            {
                return xmlValue;
            }
            else
            {
                log.info("No XML data has been defined for the customfield [" + field.getId() + "]");
            }
        }
        return "";
    }
}
