package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.rest.api.issue.BulkOperationErrorResult;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssuesCreateResponse;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.status;

/**
 * Extensions to the JIRA issue resource. This code should eventually be moved into JIRA.
 */
public class CreateIssueResource
{
    private final IssueService issueService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ResourceUriBuilder resourceUriBuilder;
    private final SubTaskManager subTaskManager;
    private final IssueInputParametersAssembler issueInputParametersAssembler;

    public CreateIssueResource(JiraAuthenticationContext jiraAuthenticationContext, IssueService issueService, ResourceUriBuilder resourceUriBuilder, SubTaskManager subTaskManager, IssueInputParametersAssembler issueInputParametersAssembler)
    {
        this.issueService = issueService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.resourceUriBuilder = resourceUriBuilder;
        this.subTaskManager = subTaskManager;
        this.issueInputParametersAssembler = issueInputParametersAssembler;
    }

    public Response createIssue(IssueUpdateBean createRequest, @Context UriInfo uriInfo)
    {
        IssueInputParametersAssembler.Result result = issueInputParametersAssembler.makeCreateAssembler(createRequest);
        if (result.getErrors().hasAnyErrors())
        {
            throw error(ErrorCollection.of(result.getErrors()));
        }

        IssueInputParameters parameters = result.getParameters();
        MutableIssue issue;
        if (result.getParentIdorKey() == null)
        {
            issue = createIssue(parameters);
        }
        else
        {
            issue = createSubtask(parameters, result.getParentIdorKey());
        }

        IssueCreateResponse response = new IssueCreateResponse()
                .id(issue.getId().toString())
                .key(issue.getKey())
                .self(resourceUriBuilder.build(uriInfo, IssueResource.class, String.valueOf(issue.getId())).toString());

        return status(CREATED).entity(response).build();
    }

    public Response createIssues(final IssuesUpdateBean createRequests, @Context final UriInfo uriInfo)
    {
        if (createRequests.getIssueUpdates() == null || createRequests.getIssueUpdates().isEmpty())
		{
			final ErrorCollection errorCollection = ErrorCollection.of(
					jiraAuthenticationContext.getI18nHelper().getText("rest.must.provide.issue.data"));
			throw error(BAD_REQUEST, errorCollection);
		}

		final List<IssueInputParametersAssembler.Result> createAssemblerResults = issueInputParametersAssembler.makeCreateAssemblers(createRequests);

        final List<BulkOperationErrorResult> errorCollections = Lists.newArrayList();
        final List<IssueCreateResponse> createdIssues = Lists.newArrayList();

        for (final IssueInputParametersAssembler.Result assemblerResult : createAssemblerResults)
        {
			if (assemblerResult.getErrors().hasAnyErrors())
			{
				final ErrorCollection errorCollection = ErrorCollection.of(assemblerResult.getErrors());
				errorCollections.add(new BulkOperationErrorResult(errorCollection.getStatus(),
						ErrorCollection.of(assemblerResult.getErrors()),
						createAssemblerResults.indexOf(assemblerResult)));
			}
			else
			{
				final IssueInputParameters parameters = assemblerResult.getParameters();

				try
				{
					final MutableIssue issue = (assemblerResult.getParentIdorKey() == null) ? createIssue(parameters) : createSubtask(parameters, assemblerResult.getParentIdorKey());

					final IssueCreateResponse response = new IssueCreateResponse()
							.id(issue.getId().toString())
							.key(issue.getKey())
							.self(resourceUriBuilder.build(uriInfo, IssueResource.class, String.valueOf(issue.getId())).toString());

					createdIssues.add(response);
				}
				catch (WebApplicationException webEx)
				{
					final ErrorCollection entity = (ErrorCollection) webEx.getResponse().getEntity();
					errorCollections.add(new BulkOperationErrorResult(entity.getStatus(), entity,createAssemblerResults.indexOf(assemblerResult)));
				}
			}
        }

        if (errorCollections.size() == createAssemblerResults.size())
        {
            throw new WebApplicationException(Response.status(BAD_REQUEST).entity(new IssuesCreateResponse(createdIssues, errorCollections)).cacheControl(never()).build());
        }
        else
        {
			return status(CREATED).entity(new IssuesCreateResponse(createdIssues, errorCollections)).build();
        }
    }

    private MutableIssue createSubtask(IssueInputParameters parameters, String parentIdOrKey)
    {
        User user = callingUser();

        IssueService.IssueResult parentIssueResult;
        // If the idOrKey is a number convert to a Long and use that
        try
        {
            Long id = Long.parseLong(parentIdOrKey);
            parentIssueResult = issueService.getIssue(user, id);
        }
        catch (NumberFormatException nfe)
        {
            // Assume the value is an Issue Key
            parentIssueResult = issueService.getIssue(user, parentIdOrKey);
        }

        if (!parentIssueResult.isValid())
        {
            throw error(ErrorCollection.of(parentIssueResult.getErrorCollection()));
        }

        IssueService.CreateValidationResult validation = issueService.validateSubTaskCreate(user, parentIssueResult.getIssue().getId(), parameters);
        if (!validation.isValid())
        {
            throw error(ErrorCollection.of(validation.getErrorCollection()));
        }

        IssueService.IssueResult issueResult = issueService.create(user, validation);

        if (!issueResult.isValid())
        {
            throw error(ErrorCollection.of(issueResult.getErrorCollection()));
        }

        MutableIssue subtask = issueResult.getIssue();

        // so far so good. now create the issue link.
        try
        {
            subTaskManager.createSubTaskIssueLink(parentIssueResult.getIssue(), subtask, user);

            // return the created issue
            return subtask;
        }
        catch (CreateException e)
        {
            com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(jiraAuthenticationContext.getI18nHelper().getText("admin.errors.project.import.issue.link.error"));

            throw error(ErrorCollection.of(errors));
        }
    }

    private MutableIssue createIssue(IssueInputParameters parameters)
    {
        User user = callingUser();
        IssueService.CreateValidationResult validation = issueService.validateCreate(user, parameters);
        if (!validation.isValid())
        {
            throw error(ErrorCollection.of(validation.getErrorCollection()));
        }

        IssueService.IssueResult issueResult = issueService.create(user, validation);

        if (!issueResult.isValid())
        {
            throw error(ErrorCollection.of(issueResult.getErrorCollection()));
        }
        return issueResult.getIssue();
    }

    protected WebApplicationException error(final ErrorCollection errors)
    {
        convertFieldNames(errors);
        return new WebApplicationException(Response.status(errors.getStatus()).entity(errors).cacheControl(never()).build());
    }

	protected WebApplicationException error(final Response.Status status, final ErrorCollection errors) {
		convertFieldNames(errors);
		throw new WebApplicationException(Response.status(status).entity(errors).cacheControl(never()).build());
	}

    /**
     * Make sure the field names match JSON field names
     *
     * @param errors
     */
    private void convertFieldNames(final ErrorCollection errors)
    {
        final Map<String, String> fieldNameMappings = MapBuilder.<String, String>newBuilder()
                .add("pid", "project")
                .toMap();

        for (final Map.Entry<String, String> entry : fieldNameMappings.entrySet())
        {
            final String oldName = entry.getKey();
            final String newName = entry.getValue();

            if (errors.getErrors().containsKey(oldName))
            {
                final String value = errors.getErrors().get(oldName);
                errors.getErrors().put(newName, value);
                errors.getErrors().remove(oldName);
            }
        }
    }

    protected User callingUser()
    {
        return jiraAuthenticationContext.getLoggedInUser();
    }
}
