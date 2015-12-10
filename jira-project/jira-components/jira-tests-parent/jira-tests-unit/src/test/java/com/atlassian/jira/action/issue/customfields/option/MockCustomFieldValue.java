package com.atlassian.jira.action.issue.customfields.option;

import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.customfields.customfieldvalue.CustomFieldValue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class MockCustomFieldValue implements CustomFieldValue
{

    private GenericValue gv;
    private String value;
    private Long parentKey;


    public MockCustomFieldValue(String value, Long parentKey)
    {
        this.value = value;
        this.parentKey = parentKey;
    }

    /**
     * To use:
     * Map mockValues = EasyMap.build( "value", new String();                                       , "parentKey", new Long();                                                                                                                                                                                                                                           )
     * MockCustomFieldValue newMockObject = new MockCustomFieldValue(mockValues);
     */

    public MockCustomFieldValue(Map<String, ?> params)
    {
        this.value = (String) params.get("value");
        this.parentKey = (Long) params.get("parentKey");
    }

    public MockCustomFieldValue(GenericValue params)
    {
        gv = params;
        this.value = (String) params.get("value");
        this.parentKey = (Long) params.get("parentKey");
    }

// Insert getters & setters here
    public String getValue()
    {
        return value;
    }

    public Long getParentKey()
    {
        return parentKey;
    }

    public void store()
    {
        if (gv != null)
        {
            try
            {
                gv.store();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    public GenericValue getGenericValue()
    {
        return gv;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof MockOption))
        {
            return false;
        }

        MockOption rhs = (MockOption) o;
        return new EqualsBuilder().append(getParentKey(), rhs.getParentOption()).append(getValue(), rhs.getValue()).isEquals();
    }
}
