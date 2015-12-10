package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint to retieve a list of date fields in the system.
 *
 * @since v4.0
 */
@Path ("/dateFields")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class DateFieldsResource
{
    private static final Logger log = Logger.getLogger(DateFieldsResource.class);

    private final FieldManager fieldManager;

    public DateFieldsResource(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }

    @GET
    public Response getAvailableDateFields()
    {
        try
        {
            final Collection<DateFieldBean> fields = new ArrayList<DateFieldBean>();
            @SuppressWarnings ("unchecked")
            final List<Field> navigableFields = new ArrayList<Field>(fieldManager.getAllAvailableNavigableFields());
            //sort the fields to make sure we get a predictable order in the drop down list.
            Collections.sort(navigableFields);

            for (final Field field : navigableFields)
            {
                if (isDateTypeField(field))
                {
                    fields.add(new DateFieldBean(field.getId(), field.getName()));
                }
            }

            DateFieldBean[] returnArray = new DateFieldBean[fields.size()];
            returnArray = fields.toArray(returnArray);

            return Response.ok(returnArray).cacheControl(NO_CACHE).build();
        }
        catch (FieldException e)
        {
            log.error("Error thrown while retreiving navigable fields", e);
            return Response.serverError().cacheControl(NO_CACHE).build();
        }
    }

    private boolean isDateTypeField(Field field)
    {
        if (fieldManager.isCustomField(field))
        {
            final CustomFieldType customFieldType = ((CustomField) field).getCustomFieldType();
            return customFieldType instanceof DateField;
        }
        else
        {
            return field instanceof DateField;
        }
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class DateFieldBean
    {
        @XmlElement
        private String label;
        @XmlElement
        private String value;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private DateFieldBean()
        {}

        DateFieldBean(String value, String label)
        {
            this.label = label;
            this.value = value;
        }
    }
    ///CLOVER:ON
}
