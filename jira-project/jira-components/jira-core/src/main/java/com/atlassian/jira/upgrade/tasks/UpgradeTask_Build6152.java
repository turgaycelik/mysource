package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.Visitor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Swaps Cloners links to be in correct direction
 *
 * @since v6.1
 */
public class UpgradeTask_Build6152 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6152.class);

    private EntityEngine entityEngine;

    public UpgradeTask_Build6152(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "6152";
    }

    @Override
    public String getShortDescription()
    {
        return "Swaps Cloners links to be in correct direction";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        entityEngine.run(Select.from("IssueLinkType").whereEqual("linkname", "Cloners"))
                .visitWith(new Visitor<GenericValue>()
                {
                    @Override
                    public void visit(final GenericValue clonersLinkType)
                    {
                        try
                        {
                            swapLinkDirectionsIfLegacy(clonersLinkType.getLong("id"));
                        }
                        catch (GenericEntityException e)
                        {
                            throw new RuntimeException(e);
                        }
                        catch (SQLException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE"}, justification="Non-constant but safe.")
    private void swapLinkDirectionsIfLegacy(final Long linkType) throws GenericEntityException, SQLException
    {
        final Connection connection = getDatabaseConnection();
        try
        {
            final Statement statement = connection.createStatement();
            final ModelEntity issueTable = getOfBizDelegator().getModelReader().getModelEntity("IssueLink");
            final String idColumn = issueTable.getField("id").getColName();
            final String linkTypeColumn = issueTable.getField("linktype").getColName();
            final String sourceColumn = issueTable.getField("source").getColName();
            final String destinationColumn = issueTable.getField("destination").getColName();
            try
            {
                final String update;
                if (isMYSQL())
                {
                    update = "UPDATE " + convertToSchemaTableName("issuelink") + " links1, "
                            + convertToSchemaTableName("issuelink") + " links2 "
                            + " SET links1." + sourceColumn + " = links1." + destinationColumn + ", links1." + destinationColumn + " = links2." + sourceColumn
                            + " WHERE links1." + idColumn + " = links2." + idColumn
                            + " AND links1." + linkTypeColumn + " = " + linkType
                            + " AND links1." + sourceColumn + " < links1." + destinationColumn
                            + " AND links1." + sourceColumn + " IS NOT NULL "
                            + " AND links1." + destinationColumn + " IS NOT NULL ";
                }
                else
                {
                    update = "UPDATE " + convertToSchemaTableName("issuelink")
                            + " SET " + sourceColumn + " = " + destinationColumn + ", " + destinationColumn + " = " + sourceColumn
                            + " WHERE " + linkTypeColumn + " = " + linkType
                            + " AND " + sourceColumn + " < " + destinationColumn
                            + " AND " + sourceColumn + " IS NOT NULL "
                            + " AND " + destinationColumn + " IS NOT NULL ";
                }
                final int updated = statement.executeUpdate(update);
                log.info(String.format("Swapped %d link(s).", updated));
            }
            finally
            {
                statement.close();
            }
        }
        finally
        {
            connection.close();
        }
    }
}
