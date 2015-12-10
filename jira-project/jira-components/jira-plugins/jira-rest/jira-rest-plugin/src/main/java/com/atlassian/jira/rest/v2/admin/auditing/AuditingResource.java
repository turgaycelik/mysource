package com.atlassian.jira.rest.v2.admin.auditing;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditRecord;
import com.atlassian.jira.auditing.AuditingFilter;
import com.atlassian.jira.auditing.AuditingService;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.Records;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.Response.status;

/**
 * Resource representing the auditing records
 *
 * @since 6.3
 */
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("auditing/")

public class AuditingResource
{
    private final static int REST_RECORD_LIMIT = 1000;
    public static final String JIRA_REST_PLUGIN_KEY = "com.atlassian.jira.rest";

    private final I18nHelper i18nHelper;
    private final AuditingService auditingService;

    public AuditingResource(final I18nHelper i18nHelper, final AuditingService auditingService)
    {
        this.i18nHelper = i18nHelper;
        this.auditingService = auditingService;
    }


    /**
     * Returns auditing records filtered using provided parameters
     *
     * @since 6.3
     *
     * @param offset - the number of record from which search starts
     * @param limit - maximum number of returned results (if is limit is <= 0 or > 1000, it will be set do default value: 1000)
     * @param filter - text query; each record that will be returned must contain the provided text in one of its fields
     * @param from - timestamp in past; 'from' must be less or equal 'to', otherwise the result set will be empty
     * only records that where created in the same moment or after the 'from' timestamp will be provided in response
     * @param to - timestamp in past; 'from' must be less or equal 'to', otherwise the result set will be empty
     * only records that where created in the same moment or earlier than the 'to' timestamp will be provided in response
     *
     * @return auditing records with all associated objects
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list auditing records filtered with request query parameters
     *
     * @response.representation.200.example
     *      {@link AuditRecordBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      In case of unhandled error while fetching auditing records
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     *
     */
    @GET
    @Path ("record")
    public Response getRecords(@QueryParam("offset") final Integer offset,
                               @QueryParam("limit") final Integer limit,
                               @QueryParam("filter") final String filter,
                               @QueryParam("from") final DateTime from,
                               @QueryParam("to") final DateTime to)
    {

        final Integer recordsLimit = (limit != null && (limit > 0 && limit < REST_RECORD_LIMIT)) ? limit : REST_RECORD_LIMIT;
        final Long fromAsTimestamp = from==null ? null : from.toInstant().getMillis();
        final Long toAsTimestamp = to==null ? null : to.toInstant().getMillis();
        final ServiceOutcome<Records> outcome = auditingService.getRecords(offset, recordsLimit,
                AuditingFilter.builder().filter(filter).fromTimestamp(fromAsTimestamp).toTimestamp(toAsTimestamp).build());

        if (!outcome.isValid())
        {
            if (outcome.getErrorCollection().getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN))
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(outcome.getErrorCollection()));
            }
            if ((outcome.getErrorCollection().getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED)))
            {
                return status(Response.Status.PRECONDITION_FAILED).entity(ErrorCollection.of(outcome.getErrorCollection())).cacheControl(never()).build();
            }
            //in case of any other error
            if (outcome.getErrorCollection().hasAnyErrors())
            {
                return status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(outcome.getErrorCollection())).cacheControl(never()).build();
            }
            else
            {
                return status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
            }
        }

        final Records records = outcome.get();
        final Collection<AuditRecordBean> results = ImmutableList.copyOf(Iterables.transform(records.getResults(), new Function<AuditRecord, AuditRecordBean>()
        {
            @Override
            public AuditRecordBean apply(final AuditRecord auditRecord)
            {
                return new AuditRecordBean(auditRecord, i18nHelper.getText(auditRecord.getCategory().getNameI18nKey()));
            }
        }));

        final Integer responseOffset = (offset != null) ? offset: 0;
        final Integer responseLimit = (limit != null) ? limit : REST_RECORD_LIMIT;

        final AuditingResponseBean auditingResponseBean = new AuditingResponseBean(results, responseOffset, responseLimit, auditingService.getTotalNumberOfRecords());
        return Response.ok(auditingResponseBean).cacheControl(never()).build();

    }

    /**
     * Store a record in Audit Log
     *
     * @since 6.3
     *
     * @request.representation.doc
     *      The POST should contain details of the record to store
     *
     * @request.representation.qname
     *      record
     *
     * @request.representation.example
     *      {@link AuditRecordBean#POST_EXAMPLE}
     *
     * @response.representation.201.doc
     *      Returned if the record is successfully stored.
     *
     * @response.representation.400.doc
     *      In case of unhandled error while fetching auditing records
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     *
     */
    @POST
    @Path ("record")
    public Response addRecord(final AuditRecordBean bean, @Context HttpServletRequest request)
    {
        final AssociatedItem objectItem;
        try
        {
            objectItem = bean.getObjectItem() != null ? bean.getObjectItem().toAssociatedItem() : null;
        }
        catch (Exception e)
        {
            return status(Response.Status.PRECONDITION_FAILED).entity(ErrorCollection.of("Error parsing objectItem: " + e.getMessage())).cacheControl(never()).build();
        }

        final List<ChangedValue> values;
        try
        {
            values = bean.getChangedValues() != null ? newArrayList(Iterables.transform(bean.getChangedValues(), ChangedValueBean.mapToChangedValue())) : null;
        }
        catch (Exception e)
        {
            return status(Response.Status.PRECONDITION_FAILED).entity(ErrorCollection.of("Error parsing changedValues: " + e.getMessage())).cacheControl(never()).build();
        }

        final List<AssociatedItem> associatedItems;
        try
        {
            associatedItems = bean.getAssociatedItems() != null ? newArrayList(Iterables.transform(bean.getAssociatedItems(), AssociatedItemBean.mapToAssociatedItem())) : null;
        }
        catch (Exception e)
        {
            return status(Response.Status.PRECONDITION_FAILED).entity(ErrorCollection.of("Error parsing associatedItems: " + e.getMessage())).cacheControl(never()).build();
        }

        final String eventSourceName= getEventSourceNameFromRequest(request);

        final com.atlassian.jira.util.ErrorCollection outcome =
                (StringUtils.isEmpty(eventSourceName)) ?
                        auditingService.storeRecord(bean.getCategory(),
                        bean.getSummary(), objectItem, values, associatedItems)
                        :
                        auditingService.storeRecord(bean.getCategory(),
                        bean.getSummary(), eventSourceName, objectItem, values, associatedItems);

        if (outcome.getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN))
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(outcome));
        }
        if ((outcome.getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED)))
        {
            return status(Response.Status.PRECONDITION_FAILED).entity(ErrorCollection.of(outcome)).cacheControl(never()).build();
        }
        //in case of any other error
        if (outcome.hasAnyErrors())
        {
            return status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(outcome)).cacheControl(never()).build();
        }
        return status(Response.Status.CREATED).cacheControl(never()).build();
    }

    private String getEventSourceNameFromRequest(final HttpServletRequest request)
    {
       final Object attribute = request.getAttribute("Plugin-Key");
       if (attribute == null)
       {
           //we indicate that this record was added using REST
           return JIRA_REST_PLUGIN_KEY;
       }
       else
       {
           final String eventSource = (String) attribute;
           return StringUtils.defaultIfBlank(eventSource, JIRA_REST_PLUGIN_KEY);
       }
    }

}
