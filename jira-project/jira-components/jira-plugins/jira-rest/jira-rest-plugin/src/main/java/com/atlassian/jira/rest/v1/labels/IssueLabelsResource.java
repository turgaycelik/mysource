package com.atlassian.jira.rest.v1.labels;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * REST resource to interact with the labels for an issue.
 *
 * @since v4.2
 */
@AnonymousAllowed
@Path ("labels")
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@CorsAllowed
public class IssueLabelsResource
{

    private static final Logger log = Logger.getLogger(IssueLabelsResource.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LabelService labelService;
    private final I18nHelper i18nHelper;
    private final ApplicationProperties applicationProperties;
    private static final int DEFAULT_MAX_SUGGESTIONS = 20;
    private final XsrfInvocationChecker xsrfChecker;

    public IssueLabelsResource(JiraAuthenticationContext jiraAuthenticationContext, LabelService labelService,
                               I18nHelper i18nHelper, ApplicationProperties applicationProperties, XsrfInvocationChecker xsrfChecker)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.labelService = labelService;
        this.i18nHelper = i18nHelper;
        this.applicationProperties = applicationProperties;
        this.xsrfChecker = xsrfChecker;
    }

    private Response createInvalidIssueResponse(Long issueId)
    {
        return Response
                .ok()
                .entity(ErrorCollection.Builder.newBuilder().addErrorMessage(i18nHelper.getText("label.service.error.issue.doesnt.exist", issueId)).build())
                .cacheControl(CacheControl.NO_CACHE)
                .build();
    }

    @GET
    @Path ("{issueId}")
    public Response getLabels(
            @PathParam ("issueId") final Long issueId,
            @QueryParam ("customFieldId") final Long customFieldId)
    {
        if (issueId == null)
        {
            return createInvalidIssueResponse(issueId);
        }

        final User user = jiraAuthenticationContext.getLoggedInUser();

        LabelService.LabelsResult result;
        if (customFieldId == null)
        {
            result = labelService.getLabels(user, issueId);
        }
        else
        {
            result = labelService.getLabels(user, issueId, customFieldId);
        }

        return createGetLabelsResponse(result);
    }

    @GET
    @Path ("suggest")
    public Response getSuggestions(
            @QueryParam ("customFieldId") final Long customFieldId,
            @QueryParam ("query") final String token)
    {
        return getSuggestionsHelper(null, customFieldId, token);
    }

    @GET
    @Path ("{issueId}/suggest")
    public Response getSuggestions(
            @PathParam ("issueId") final Long issueId,
            @QueryParam ("customFieldId") final Long customFieldId,
            @QueryParam ("query") final String token)
    {
        if (issueId == null)
        {
            return createInvalidIssueResponse(issueId);
        }

        return getSuggestionsHelper(issueId, customFieldId, token);
    }

    private Response getSuggestionsHelper(final Long issueId, final Long customFieldId, final String token)
    {
        final User user = jiraAuthenticationContext.getLoggedInUser();

        LabelService.LabelSuggestionResult suggestionResult;
        if (customFieldId == null)
        {
            suggestionResult = labelService.getSuggestedLabels(user, issueId, token);
        }
        else
        {
            suggestionResult = labelService.getSuggestedLabels(user, issueId, customFieldId, token);
        }

        if (suggestionResult.isValid())
        {
            final Set<String> suggestions = suggestionResult.getSuggestions();
            return createGetSuggestionsResponse(token, suggestions, addHighlights(suggestions, token));
        }
        else
        {
            return createErrorResponse(suggestionResult);
        }
    }

    private Set<String> addHighlights(final Set<String> suggestions, final String token)
    {
        final Set<String> highlightedSuggestions = new LinkedHashSet<String>(suggestions.size());

        for (String suggestion : suggestions)
        {
            final String matched = "<b>" + TextUtils.htmlEncode(suggestion.substring(0, token.length())) + "</b>"
                    + TextUtils.htmlEncode(suggestion.substring(token.length()));
            highlightedSuggestions.add(matched);
        }

        return highlightedSuggestions;
    }

    private Response createGetSuggestionsResponse(final String input, final Set<String> suggestions, final Set<String> highlightedSuggestions)
    {
        long limit = getSuggestionLimit();
        int count = 0;
        SuggestionListStruct result = new SuggestionListStruct(StringUtils.trim(input));

        if (suggestions.size() != highlightedSuggestions.size())
        {
            log.warn("Somehow we have different lengths for the highlighted suggestions.");
        }

        final Iterator<String> suggestionIterator = suggestions.iterator();
        final Iterator<String> highlightedIterator = highlightedSuggestions.iterator();

        while (suggestionIterator.hasNext() && highlightedIterator.hasNext())
        {
            final String suggestion = suggestionIterator.next();
            final String highlighted = highlightedIterator.next();

            result.addSuggestion(suggestion, highlighted);
            count++;
            if(count >= limit)
            {
                break;
            }
        }

        return Response
                .ok(result)
                .cacheControl(CacheControl.NO_CACHE)
                .build();
    }

    private long getSuggestionLimit()
    {
        final String labelString = applicationProperties.getDefaultString(APKeys.JIRA_AJAX_LABEL_SUGGESTION_LIMIT);
        long limit = DEFAULT_MAX_SUGGESTIONS;
        if(StringUtils.isNumeric(labelString))
        {
            limit = Long.parseLong(labelString);
            if(limit == 0)
            {
                //0 means ignore the limit.
                limit = Long.MAX_VALUE;
            }
        }
        return limit;
    }   

    @POST
    @Path ("{issueId}")
    public Response setLabels(
            @PathParam ("issueId") final Long issueId,
            @QueryParam ("customFieldId") final Long customFieldId,
            @FormParam ("labels") final String concatenatedLabels)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        if (issueId == null)
        {
            return createInvalidIssueResponse(issueId);
        }

        final User user = jiraAuthenticationContext.getLoggedInUser();
        final Set<String> labels = splitLabels(concatenatedLabels);

        LabelService.SetLabelValidationResult validationResult;
        if (customFieldId == null)
        {
            validationResult = labelService.validateSetLabels(user, issueId, labels);
        }
        else
        {
            validationResult = labelService.validateSetLabels(user, issueId, customFieldId, labels);
        }

        if (validationResult.isValid())
        {
            return createGetLabelsResponse(labelService.setLabels(user, validationResult, false, true));
        }
        else
        {
            return createErrorResponse(validationResult);
        }
    }

    public static Set<String> splitLabels(String concatenatedLabels)
    {
        if (concatenatedLabels == null || StringUtils.isBlank(concatenatedLabels))
        {
            return Collections.emptySet();
        }
        else
        {
            return new LinkedHashSet<String>(Arrays.asList(concatenatedLabels.split("\\s")));
        }
    }

    private static Response createGetLabelsResponse(LabelService.LabelsResult result)
    {
        if (result.isValid())
        {
            LabelListStruct labels = new LabelListStruct();
            for (Label label : result.getLabels())
            {
                labels.addLabel(label);
            }
            return Response
                    .ok(labels)
                    .cacheControl(CacheControl.NO_CACHE)
                    .build();
        }
        else
        {
            return createErrorResponse(result);
        }
    }

    private static Response createErrorResponse(ServiceResultImpl result)
    {
        return Response
                .ok()
                .entity(ErrorCollection.Builder.newBuilder().addErrorCollection(result.getErrorCollection()).build())
                .cacheControl(CacheControl.NO_CACHE)
                .build();
    }

    @XmlRootElement
    public static class LabelListStruct
    {
        @XmlElement
        private Set<LabelStruct> labels = new LinkedHashSet<LabelStruct>();

        public LabelListStruct addLabel(Label label)
        {
            labels.add(new LabelStruct(label.getId(), label.getLabel()));
            return this;
        }
    }

    @XmlRootElement
    public static class LabelStruct
    {
        @XmlElement
        private Long id;

        @XmlElement
        private String label;

        public LabelStruct() {}

        public LabelStruct(Long id, String label)
        {
            this.id = id;
            this.label = label;
        }
    }

    @XmlRootElement
    public static class SuggestionListStruct
    {
        @XmlElement
        private String token;

        @XmlElement
        private Set<SuggestionStruct> suggestions = new LinkedHashSet<SuggestionStruct>();

        public SuggestionListStruct() {}

        public SuggestionListStruct(final String token)
        {
            this.token = token;
        }

        public SuggestionListStruct addSuggestion(final String label, final String highlightedLabel)
        {
            suggestions.add(new SuggestionStruct(label, highlightedLabel));
            return this;
        }

        public Set<SuggestionStruct> suggestions()
        {
            return new LinkedHashSet<SuggestionStruct>(suggestions);
        }
    }

    @XmlRootElement
    public static class SuggestionStruct
    {
        @XmlElement
        private String label;

        @XmlElement
        private String html;

        public SuggestionStruct() {}

        public SuggestionStruct(final String label, final String highLightedLabel)
        {
            this.label = label;
            this.html = highLightedLabel;
        }

        public String label()
        {
            return label;
        }

        public String html()
        {
            return html;
        }
    }
}
