package com.atlassian.jira.issue.util;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.issue.MovedIssueKey;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Visitor;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

import java.util.HashSet;
import java.util.Set;

/**
 * @since v6.1
 */
public class MovedIssueKeyStoreImpl implements MovedIssueKeyStore
{
    private EntityEngine entityEngine;
    private OfBizDelegator ofBizDelegator;

    public MovedIssueKeyStoreImpl(final EntityEngine entityEngine, final OfBizDelegator ofBizDelegator)
    {
        this.entityEngine = entityEngine;
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public void recordMovedIssueKey(final String oldIssueKey, final Long oldIssueId)
    {
        entityEngine.createValue(Entity.MOVED_ISSUE_KEY, new MovedIssueKey(null, oldIssueKey, oldIssueId));
    }

    @Override
    public Long getMovedIssueId(final String key)
    {
        final MovedIssueKey movedIssueKey = Select.from(Entity.MOVED_ISSUE_KEY)
                .whereEqual(MovedIssueKey.OLD_ISSUE_KEY, key)
                .runWith(ofBizDelegator)
                .singleValue();

        if (movedIssueKey != null)
        {
            return movedIssueKey.getIssueId();
        }
        else
        {
            return null;
        }
    }

    @Override
    public Set<String> getMovedIssueKeys(final Set<String> keys)
    {
        if (keys.isEmpty())
            return new HashSet<String>();
        final Set<String> movedIssueKeys = new HashSet<String>();
        EntityCondition condition = new EntityExpr(MovedIssueKey.OLD_ISSUE_KEY, EntityOperator.IN, keys);
        Select.from(Entity.MOVED_ISSUE_KEY)
                .whereCondition(condition)
                .runWith(ofBizDelegator)
                .visitWith(new Visitor<MovedIssueKey>()
                {
                    @Override
                    public void visit(MovedIssueKey element)
                    {
                        movedIssueKeys.add(element.getOldIssueKey());
                    }
                });
        return movedIssueKeys;
    }

    @Override
    public void deleteMovedIssueKeyHistory(final Long issueId)
    {
        entityEngine.delete(Delete.from(Entity.MOVED_ISSUE_KEY).whereEqual(MovedIssueKey.ISSUE_ID, issueId));
    }
}
