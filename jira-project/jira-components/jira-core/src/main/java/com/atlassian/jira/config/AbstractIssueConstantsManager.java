package com.atlassian.jira.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.0
 */
public abstract class AbstractIssueConstantsManager<T extends IssueConstant>
{
    protected final ConstantsManager constantsManager;
    protected final OfBizDelegator ofBizDelegator;
    protected final IssueIndexManager issueIndexManager;

    public AbstractIssueConstantsManager(ConstantsManager constantsManager, OfBizDelegator ofBizDelegator, IssueIndexManager issueIndexManager)
    {
        this.constantsManager = constantsManager;
        this.ofBizDelegator = ofBizDelegator;
        this.issueIndexManager = issueIndexManager;
    }

    protected GenericValue createConstant(Map<String, Object> fields)
    {
        return ofBizDelegator.createValue(getIssueConstantField(), fields);
    }

    protected long getMaxSequenceNo()
    {
        long maxSequence = 0;
        List<GenericValue> transform = Lists.transform(getAllValues(), new Function<T, GenericValue>()
        {
            @Override
            public GenericValue apply(T from)
            {
                return from.getGenericValue();
            }
        });
        for (GenericValue genericValue : transform)
        {
            long thisSequence = genericValue.getLong("sequence");
            if (thisSequence > maxSequence)
            {
                maxSequence = thisSequence;
            }
        }
        return maxSequence;
    }

    protected String getNextStringId() throws GenericEntityException
    {
        return ofBizDelegator.getDelegatorInterface().getNextSeqId(getIssueConstantField()).toString();
    }

    protected void removeConstant(String fieldName, T constant, String newId) throws GenericEntityException, IndexException
    {
        List<GenericValue> matchingIssues = getMatchingIssues(constant);
        for (GenericValue issue : matchingIssues)
        {
            issue.set(fieldName, newId);
            issueIndexManager.reIndex(issue);
        }
        ofBizDelegator.storeAll(matchingIssues);
        // Remove translations (if any)
        GenericValue constantGv = constant.getGenericValue();
        String id = constantGv.getString("id");
        constantGv.set("id", new Long(id));
        removePropertySet(constantGv);
        constantGv.set("id", id);
        constantGv.remove();

        clearCaches();

        postProcess(constant);
    }

    protected void removePropertySet(GenericValue constantGv)
    {
        OFBizPropertyUtils.removePropertySet(constantGv);
    }

    protected void postProcess(T constant)
    {
    }

    protected void clearCaches()
    {
    }

    protected abstract String getIssueConstantField();

    protected List<GenericValue> getMatchingIssues(T constant) throws GenericEntityException
    {
        List<GenericValue> matchingIssues = constant.getGenericValue().getRelated("ChildIssue");
        if (matchingIssues == null)
        {
            matchingIssues = Collections.emptyList();
        }
        return matchingIssues;
    }

    protected abstract List<T> getAllValues();

    protected void moveUp(T constant)
    {
        List<GenericValue> reordered = new ArrayList<GenericValue>();
        for (T cons : getAllValues())
        {
            GenericValue value = cons.getGenericValue();
            if (cons.getId().equals(constant.getId()) && reordered.size() == 0)
            {
                break;
            }
            else if (cons.getId().equals(constant.getId()))
            {
                reordered.add(reordered.size() - 1, value);
            }
            else
            {
                reordered.add(value);
            }
        }
        storeAndClearCaches(reordered);
    }

    protected void moveDown(T constant)
    {
       final List<GenericValue> reordered = new ArrayList<GenericValue>();
       for (Iterator<T> iterator = getAllValues().iterator(); iterator.hasNext();)
        {
            T cons = iterator.next();
            GenericValue value =  cons.getGenericValue();

            if (cons.getId().equals(constant.getId()) && !iterator.hasNext())
            {
                break;
            }
            else if (cons.getId().equals(constant.getId()))
            {
                reordered.add(iterator.next().getGenericValue());
                reordered.add(value);
            }
            else
                reordered.add(value);
        }
        storeAndClearCaches(reordered);
    }

    private void storeAndClearCaches(List<GenericValue> reordered)
    {
        for (int i = 0; i < reordered.size(); i++)
        {
            GenericValue value = reordered.get(i);
            value.set("sequence", new Long(i + 1));
        }

        ofBizDelegator.storeAll(reordered);

        clearCaches();
    }
}
