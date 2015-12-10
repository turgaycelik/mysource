package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FieldNameComparator;
import com.atlassian.jira.issue.fields.NavigableField;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

import com.atlassian.jira.rest.v2.search.ColumnOptions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.component.TableLayoutUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource for retreiving available columns.
 *
 * @since v4.0
 */
@Path ("/availableColumns")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class AvailableColumnsResource
{
    private JiraAuthenticationContext authenticationContext;
    private final FieldManager fieldManager;
    private final TableLayoutUtils tableLayoutUtils;

    public AvailableColumnsResource(final JiraAuthenticationContext authenticationContext, final FieldManager fieldManager, final TableLayoutUtils tableLayoutUtils)
    {
        this.authenticationContext = authenticationContext;
        this.fieldManager = fieldManager;
        this.tableLayoutUtils = tableLayoutUtils;
    }

    @GET
    public Response getColumns()
    {
        try
        {
            final List<NavigableField> fields = getFields();
            final List<NavigableField> defaults = getDefaultColumns(fields);

            final ColumnOptions layout = new ColumnOptions(convertToColumnItems(fields), convertToColumnItems(defaults));
            return Response.ok(layout).cacheControl(NO_CACHE).build();
        }
        catch (FieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<NavigableField> getFields()
            throws FieldException
    {
        final User user = authenticationContext.getLoggedInUser();
        final Set<NavigableField> fields = fieldManager.getAvailableNavigableFields(user);

        // Remove all custom fields that do not have view values
        for (Iterator<NavigableField> iterator = fields.iterator(); iterator.hasNext();)
        {
            final NavigableField field = iterator.next();
            if (field instanceof CustomField)
            {
                final CustomField customField = (CustomField) field;
                if (!customField.getCustomFieldType().getDescriptor().isViewTemplateExists() &&
                        !customField.getCustomFieldType().getDescriptor().isColumnViewTemplateExists())
                {
                    iterator.remove();
                }
            }
        }
        final Comparator<Field> nameComparator = new FieldNameComparator(authenticationContext.getI18nHelper());
        final List<NavigableField> orderedFields = new ArrayList<NavigableField>(fields);
        Collections.sort(orderedFields, nameComparator);

        return orderedFields;
    }

    private List<NavigableField> getDefaultColumns(final List<NavigableField> availableFields)
    {
        final List<String> colStrings = tableLayoutUtils.getDefaultColumnNames(APKeys.ISSUE_TABLE_COLS_DASHBOARD);
        final List<NavigableField> defaultFields = new ArrayList<NavigableField>(colStrings.size());

        for (String colString : colStrings)
        {
            for (NavigableField field : availableFields)
            {
                if (field.getId().equals(colString))
                {
                    defaultFields.add(field);
                    break;
                }
            }
        }
        return defaultFields;
    }

    private List<ColumnOptions.ColumnItem> convertToColumnItems(final List<NavigableField> fields)
    {
        final List<ColumnOptions.ColumnItem> items = new ArrayList<ColumnOptions.ColumnItem>(fields.size());

        for (NavigableField field : fields)
        {
            items.add(new ColumnOptions.ColumnItem(field.getId(), field.getName()));
        }

        return items;
    }

}
