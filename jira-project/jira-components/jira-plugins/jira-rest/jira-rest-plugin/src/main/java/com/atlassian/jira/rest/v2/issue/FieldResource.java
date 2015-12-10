package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.customfield.CreateValidationResult;
import com.atlassian.jira.bc.customfield.CustomFieldDefinition;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.api.customfield.CustomFieldDefinitionJsonBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.status;

/**
 * @since 5.0
 */
@Path ("field")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class FieldResource
{
    private static final Logger LOG = Logger.getLogger(FieldResource.class);

    private final FieldManager fieldManager;
    private final JiraAuthenticationContext authenticationContext;
    private final CustomFieldService customFieldService;
    private final SearchHandlerManager searchHandlerManager;

    public FieldResource(final FieldManager fieldManager, final JiraAuthenticationContext authenticationContext,
                         final CustomFieldService customFieldService, final SearchHandlerManager searchHandlerManager)
    {
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
        this.customFieldService = customFieldService;
        this.searchHandlerManager = searchHandlerManager;
    }

    /**
     * Returns a list of all fields, both System and Custom
     *
     * @return a response containing all fields as short Field Meta Beans
     *
     * @response.representation.200.qname
     *      List of field
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Contains a full representation of all visible fields in JSON.
     *
     * @response.representation.200.example
     *      {@link FieldBean#DOC_EXAMPLE_LIST}
     *
     */
    @GET
    public Response getFields()
    {
        Set<Field> fields = new HashSet<Field>();
        Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
        for (OrderableField orderableField : orderableFields)
        {
            // We only add the non-navigable fields here.  We get the navigable ones next, but only if the user can see them.
            if (!(orderableField instanceof NavigableField))
            {
                fields.add(orderableField);
            }
        }
        
        try
        {
            fields.addAll(fieldManager.getAvailableNavigableFields(authenticationContext.getLoggedInUser()));
        }
        catch (FieldException e)
        {
            throw new RESTException(Response.Status.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }

        return Response.ok(FieldBean.shortBeans(fields, fieldManager, searchHandlerManager)).build();
    }


    /**
     * Creates a custom field using a definition (object encapsulating custom field data)
     *
     * @param customFieldDefinitionJsonBean definition of custom field to create
     * @return Response with information about created field
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.api.customfield.CustomFieldDefinitionJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.201.doc
     *      Returned if custom field was created. {@link FieldBean#DOC_EXAMPLE_CF}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. invalid values).
     *
     * @response.representation.500.doc
     *     Returned if exception occured during custom field creation.
     */
    @POST
    public Response createCustomField(final CustomFieldDefinitionJsonBean customFieldDefinitionJsonBean)
    {

        final CustomFieldDefinition customFieldDefinition = CustomFieldDefinition.builder()
                .name(customFieldDefinitionJsonBean.name())
                .description(customFieldDefinitionJsonBean.description())
                .cfType(customFieldDefinitionJsonBean.type())
                .searcherKey(customFieldDefinitionJsonBean.searcherKey())
                .isGlobal(true)
                .isAllIssueTypes(true).build();

        final ServiceOutcome<CreateValidationResult> outcome = customFieldService.validateCreate(authenticationContext.getLoggedInUser(), customFieldDefinition);

        if (!outcome.isValid())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(outcome.getErrorCollection()));
        }

        final ServiceOutcome<CustomField> serviceOutcome = customFieldService.create(outcome.getReturnedValue());
        final Field field = fieldManager.getField(serviceOutcome.getReturnedValue().getId());
        return status(Response.Status.CREATED).entity(FieldBean.shortBean(field, fieldManager, searchHandlerManager)).build();
    }

}