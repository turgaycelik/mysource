package com.atlassian.jira.rest.v2.healthcheck;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quick and dirty database health check so Hosted Operations can check whether JIRA's database is all locked up.
 *
 * @since v5.2
 * @deprecated Utilize jira-healthcheck-plugin instead. See JDEV-23665 for more details. Remove with 7.0
 */
@Deprecated
public class ValidationQuery
{
    private static final Logger log = LoggerFactory.getLogger(ValidationQuery.class);
    private final OfBizDelegator ofBizDelegator;

    public ValidationQuery(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public HealthCheckResult doCheck()
    {
        final String entityName = "SequenceValueItem";
        final HealthCheckResult result = new HealthCheckResult("db_validation_query", "performs a database query", true);

        // any SELECT will do, as long as we hit the database.
        OfBizListIterator rows = null;
        try
        {
            rows = ofBizDelegator.findListIteratorByCondition(entityName, null, null, null, null, EntityFindOptions.findOptions().fetchSize(1));
            for (GenericValue row : rows)
            {
                // make sure we consume all the results to keep all db drivers happy
                log.trace("addHealthCheckInfo: select from {} limit 1: {}", entityName, row);
            }
        }
        catch (DataAccessException e)
        {
            // mark the check as failed
            result.passed = false;
        }
        finally
        {
            if (rows != null)
            {
                try
                {
                    rows.close();
                }
                catch (DataAccessException e)
                {
                    // ignore
                }
            }
        }

        // this check should still return normally even if the database is down
        return result;
    }
}
