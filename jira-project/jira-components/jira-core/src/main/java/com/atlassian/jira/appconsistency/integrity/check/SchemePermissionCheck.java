package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: owenfellows
 * Date: 24-Jun-2004
 * Time: 14:57:21
 * To change this template use File | Settings | File Templates.
 */
public class SchemePermissionCheck extends CheckImpl
{
    public SchemePermissionCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.scheme.permission.check.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    private List doCheck(boolean correct) throws IntegrityException
    {
        List amendments = getAmendments();
        if (correct)
        {
            for (Object amendment : amendments)
            {
                DeleteEntityAmendment createEntityAmendment = (DeleteEntityAmendment) amendment;
                try
                {
                    ofBizDelegator.removeValue(createEntityAmendment.getEntity());
                }
                catch (Exception e)
                {
                    throw new IntegrityException(e);
                }
            }
        }
        return amendments;
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    private List getAmendments() throws IntegrityException
    {
        List amendments = new ArrayList();
        List firstDuplicate = new ArrayList();
        List schemePermissions;
        OfBizListIterator listIterator = null;
        try
        {
            listIterator = ofBizDelegator.findListIteratorByCondition("SchemePermissions", null);
            GenericValue genericValue = listIterator.next();
            while (genericValue != null)
            {
                schemePermissions = ofBizDelegator.findByAnd("SchemePermissions", FieldMap.build("scheme", genericValue.getLong("scheme"),
                        "permissionKey", genericValue.getString("permissionKey"),
                        "type", genericValue.getString("type"),
                        "parameter", genericValue.getString("parameter")));
                if (schemePermissions.size() > 1)
                {
                    List containsDuplicate = EntityUtil.filterByAnd(firstDuplicate, FieldMap.build("scheme", genericValue.getLong("scheme"),
                        "permissionKey", genericValue.getString("permissionKey"),
                        "type", genericValue.getString("type"),
                        "parameter", genericValue.getString("parameter")));

                    if (containsDuplicate.isEmpty())
                    {
                        firstDuplicate.add(genericValue);
                    } else
                    {
                        amendments.add(new DeleteEntityAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.scheme.permission.check.message"), genericValue));
                    }

                }
                genericValue = (GenericValue)listIterator.next();
            }

        }
        catch (Exception e)
        {
            throw new IntegrityException(e);
        }finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }

        }
        return amendments;
    }
}
