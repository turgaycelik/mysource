package com.atlassian.jira.plugins.share.search;

import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.plugins.share.event.AbstractShareEvent;
import com.atlassian.jira.plugins.share.event.ShareJqlEvent;
import com.atlassian.jira.plugins.share.event.ShareSearchRequestEvent;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.jira.web.action.issue.IssueNavigatorConstants.JQL_QUERY_PARAMETER;

@Component
public class ShareSearchRequestService
{

    private final EventPublisher eventPublisher;
    private final ShareSearchEmailsSender shareSearchEmailsSender;

    @Autowired
    public ShareSearchRequestService(@ComponentImport final EventPublisher eventPublisher, final ShareSearchEmailsSender shareSearchEmailsSender)
    {
        this.eventPublisher = eventPublisher;
        this.shareSearchEmailsSender = shareSearchEmailsSender;
    }

    public void shareSearchRequest(ShareService.ValidateShareSearchRequestResult result)
    {
        final Map<String, Object> params = prepareParams(result);

        shareSearchEmailsSender.sendShareSearchEmails(result, params);

        publishShareEvent(result);
    }

    private Map<String, Object> prepareParams(final ShareService.ValidateShareSearchRequestResult result)
    {
        Map<String, Object> params = Maps.newHashMap();
        params.put("remoteUser", result.getUser());

        prepareJqlParams(result, params);

        String message = result.getShareBean().getMessage();
        if (StringUtils.isNotBlank(message))
        {
            params.put("comment", message);
            params.put("htmlComment", TextUtils.htmlEncode(message));  // required by templates/email/html/includes/fields/comment.vm
        }

        return params;
    }

    private void prepareJqlParams(final ShareService.ValidateShareSearchRequestResult result, final Map<String, Object> params)
    {
        final String jql = getJql(result);

        UrlBuilder jqlUrlBuilder = new UrlBuilder(false);
        jqlUrlBuilder.addParameter("reset", true);
        jqlUrlBuilder.addParameter(JQL_QUERY_PARAMETER, jql);
        params.put("jqlSearchLinkUrlParams", jqlUrlBuilder.asUrlString());

        if (result.getSearchRequest() != null)
        {
            putSearchRequestParams(params, result.getSearchRequest());
        }
    }

    private String getJql(final ShareService.ValidateShareSearchRequestResult result)
    {
        return Option.option(result.getSearchRequest()).fold(
                new Supplier<String>()
                {
                    @Override
                    public String get()
                    {
                        return result.getShareBean().getJql();
                    }
                }, new Function<SearchRequest, String>()
                {
                    @Override
                    public String apply(final SearchRequest request)
                    {
                        return request.getQuery().getQueryString();
                    }
                }
        );
    }

    private void putSearchRequestParams(final Map<String, Object> params, final SearchRequest searchRequest)
    {
        UrlBuilder savedSearchUrlBuilder = new UrlBuilder(false);
        savedSearchUrlBuilder.addParameter("mode", "hide");
        savedSearchUrlBuilder.addParameter("requestId", searchRequest.getId());
        params.put("savedSearchLinkUrlParams", savedSearchUrlBuilder.asUrlString());
        params.put("filterName", searchRequest.getName());
    }

    private void publishShareEvent(final ShareService.ValidateShareSearchRequestResult result)
    {
        final AbstractShareEvent event = getProperEvent(result.getSearchRequest(), result.getShareBean(), result.getUser());
        eventPublisher.publish(event);
    }

    private AbstractShareEvent getProperEvent(final SearchRequest searchRequest, final ShareBean shareSearchBean, final ApplicationUser user)
    {
        return Option.option(searchRequest).fold(
                new Supplier<AbstractShareEvent>()
                {
                    @Override
                    public AbstractShareEvent get()
                    {
                        return new ShareJqlEvent(user.getDirectoryUser(), shareSearchBean.getUsernames(),
                                shareSearchBean.getEmails(), shareSearchBean.getMessage(), shareSearchBean.getJql());

                    }
                }, new Function<SearchRequest, AbstractShareEvent>()
                {
                    @Override
                    public AbstractShareEvent apply(final SearchRequest request)
                    {
                        return new ShareSearchRequestEvent(user.getDirectoryUser(), shareSearchBean.getUsernames(),
                                shareSearchBean.getEmails(), shareSearchBean.getMessage(), request);
                    }
                }
        );
    }
}
