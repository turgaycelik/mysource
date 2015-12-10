package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This task cleans up after the CustomFieldValue upgrade (633)
 *
 * @since v4.4
 */
public class UpgradeTask_Build634 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build634.class);

    private final CustomFieldManager customFieldManager;

    public UpgradeTask_Build634(CustomFieldManager customFieldManager)
    {
        super(false);
        this.customFieldManager = customFieldManager;
    }

    public String getBuildNumber()
    {
    return "634";
    }

    public String getShortDescription()
    {
        return "Converting Custom field values for Select and MultiSelect types to store the id of the option rather than the value -  cleanup.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        List<CustomField> customFieldList = getCustomFields();

        Connection connection = getDatabaseConnection();
        try
        {
            connection.setAutoCommit(false);

            for (CustomField customField : customFieldList)
            {
                processCustomField(connection, customField);
            }

        }
        finally
        {
            connection.close();
        }

    }

    private void processCustomField(Connection connection, CustomField customField) throws GenericEntityException, SQLException
    {
        String sql = "update " + convertToSchemaTableName("customfieldvalue")
                     + " set numbervalue = null"
                     + " where customfield = " +  customField.getIdAsLong();

        Statement stmt = connection.createStatement();
        stmt.execute(sql);
        stmt.close();

        connection.commit();
    }

    /**
     * Create a list of all the custom fields we need to migrate.
     *
     * @return List of Custom fields to convert.
     */
    private List<CustomField> getCustomFields()
    {
        /*
           * We use the manager here, even though that is basically bad practise in an Upgrade Task,
           * because the code to fiend the CustomFieldType is really deep and ugly and it would seem
           * wrong to copy and paste reams of it into this class.
           */

        List<CustomField> selectCustomFields = new ArrayList<CustomField>();
        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : customFields)
        {
            CustomFieldType type = customField.getCustomFieldType();
            if (type instanceof SelectCFType || type instanceof MultiSelectCFType)
            {
                selectCustomFields.add(customField);
            }
        }
        return selectCustomFields;
    }

}