/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class SearchRequestRelationCheck extends CheckImpl
{

    public SearchRequestRelationCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);

    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.search.request.relation.check.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    private List doCheck(boolean correct) throws IntegrityException
    {
        List results = new ArrayList();
        String message;

        List filtersToRemove = new ArrayList();

        OfBizListIterator listIterator = null;
        try
        {
            listIterator = ofBizDelegator.findListIteratorByCondition("SearchRequest", null);
            GenericValue filter = (GenericValue) listIterator.next();
            while (filter != null)
            {
                if (TextUtils.stringSet(filter.getString("project")))
                {
                    if (ofBizDelegator.findById("Project", filter.getLong("project")) == null)
                    {
                        if (correct)
                        {
                            message = getI18NBean().getText("admin.integrity.check.search.request.relation.check.message", filter.getString("name"));
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-2279"));
                            filtersToRemove.add(filter);
                        }
                        else
                        {
                            message = getI18NBean().getText("admin.integrity.check.search.request.relation.check.preview", filter.getString("name"));
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-2279"));
                        }
                    }

                }
                filter = (GenericValue) listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }

        }

        if (correct && !filtersToRemove.isEmpty())
        {
            try
            {
                ofBizDelegator.removeAll(filtersToRemove);
            }
            catch (DataAccessException e)
            {
                throw new IntegrityException(e);
            }
        }

        return results;
    }

}
