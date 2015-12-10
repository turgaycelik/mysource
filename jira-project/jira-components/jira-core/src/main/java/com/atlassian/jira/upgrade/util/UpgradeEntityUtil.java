package com.atlassian.jira.upgrade.util;

import javax.annotation.Nullable;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericValue;

/**
 * Utility tool for deleting data in upgrade tasks. The main purpose of this tool is to log data that is being deleted
 * in order to be able to perform revert in case of incidents
 *
 * @since 6.2.3
 */
public class UpgradeEntityUtil
{
    private final String taskName;
    private final EntityEngine entityEngine;
    private static final Logger log = Logger.getLogger(UpgradeEntityUtil.class);

    public UpgradeEntityUtil(final String taskName, final EntityEngine entityEngine)
    {
        this.taskName = taskName;
        this.entityEngine = entityEngine;
    }


    private void log(final Level level, final String message)
    {
        final String logMessage = String.format("%s: %s", taskName, message);
        doLogInt(level, logMessage);
    }

    @VisibleForTesting
    void doLogInt(final Level level, final String logMessage) {
        log.log(level, logMessage);
    }

    public void deleteEntityByCondition(final String entityName, final EntityCondition entityCondition)
    {
        Select.from(entityName).whereCondition(entityCondition).runWith(entityEngine).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue element)
            {
                logEntity(entityName, element, "DELETING");

            }
        });
        entityEngine.delete(Delete.from(entityName).whereCondition(entityCondition));
    }

    public void logEntity(final String entityName, final GenericValue element, final String action)
    {
        try
        {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("entityName", entityName);
            jsonObject.put("fields", Maps.transformValues(element.getAllFields(), new Function<Object, String>()
            {
                @Override
                public String apply(@Nullable final Object input)
                {
                    return input != null ? input.toString() : "NULL";
                }
            }));
            log(Level.WARN, action + " " + jsonObject.toString());
        }
        catch (final JSONException e)
        {
            log(Level.ERROR, "Unable to log for " + element.toString());
            log.error("This should never happen", e);
        }
    }
}
