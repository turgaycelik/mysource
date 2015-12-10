package com.atlassian.jira.action.issue.customfields.option;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class MockOption implements Option
{

    private GenericValue gv;
    private Option parentOption;
    private List childOptions;
    private Long sequence;
    private String value;
    private FieldConfig relatedField;
    private Long optionId;
    private Long parentoptionid;
    public static final Long PARENT_OPTION_ID = new Long(1000);
    public static final Long CHILD_1_ID = new Long(1002);
    private Boolean disabled;


    public MockOption(Option parentOption, List childOptions, Long sequence, String value, FieldConfig relatedField, Long optionId)
    {
        this.parentOption = parentOption;
        this.childOptions = childOptions;
        this.sequence = sequence;
        this.value = value;
        this.relatedField = relatedField;
        this.optionId = optionId;
        this.disabled = false;
    }

    public MockOption(Map params)
    {
        this.parentOption = (Option) params.get("parentOption");
        this.childOptions = (List) params.get("childOptions");
        this.sequence = (Long) params.get("sequence");
        this.value = (String) params.get("value");
        this.relatedField = (FieldConfig) params.get("relatedField");
        this.optionId = (Long) params.get("optionId");
        if (optionId == null)
        {
            this.optionId = (Long) params.get("id");
        }
        this.disabled = (Boolean) params.get("disabled");
        if (disabled == null)
        {
            disabled = Boolean.FALSE;
        }
    }

    public MockOption(GenericValue params)
    {
        gv = params;
        this.parentOption = (Option) params.get("parentOption");
        this.parentoptionid = (Long) params.get("parentoptionid");
        this.childOptions = (List) params.get("childOptions");
        this.sequence = (Long) params.get("sequence");
        this.value = (String) params.get("value");
        this.relatedField = (FieldConfig) params.get("relatedField");
        this.optionId = (Long) params.get("optionId");
        if (optionId == null)
        {
            this.optionId = (Long) params.get("id");
        }
        this.disabled = (Boolean) params.get("disabled");
        if (disabled == null)
        {
            disabled = Boolean.FALSE;
        }
    }

// Insert getters & setters here

    public Option getParentOption()
    {
        return parentOption;
    }

    public void setParentOption(Option parentOption)
    {
        this.parentOption = parentOption;
    }

    @Nonnull
    public List getChildOptions()
    {
        return childOptions;
    }

    public void setChildOptions(List childOptions)
    {
        this.childOptions = childOptions;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(Long sequence)
    {
        this.sequence = sequence;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public FieldConfig getRelatedCustomField()
    {
        return relatedField;
    }

    public void setRelatedCustomField(FieldConfig relatedField)
    {
        this.relatedField = relatedField;
    }

    public Long getOptionId()
    {
        return optionId;
    }

    public void setOptionId(Long optionId)
    {
        this.optionId = optionId;
    }

    @Override
    public Boolean getDisabled()
    {
        return disabled;
    }

    @Override
    public void setDisabled(Boolean disabled)
    {
        this.disabled = disabled;
    }

    //convenience method - candidate to be removed to a util class?
    public List retrieveAllChildren(List listToAddTo)
    {
        return allChildren;
    }

    List allChildren = new ArrayList();


    public void setAllChildren(List allChildren)
    {
        this.allChildren = allChildren;
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
        return new EqualsBuilder().append(getOptionId(), rhs.getOptionId()).isEquals();
    }

    @Override
    public String toString()
    {
        return getValue();
    }

    public Long getParentoptionid()
    {
        return parentoptionid;
    }

    public void setParentoptionid(Long parentoptionid)
    {
        this.parentoptionid = parentoptionid;
    }


    public static MockGenericValue _newMockChild2GV()
    {
        MockGenericValue child2;
        child2 = new MockGenericValue("CustomFieldOption",
                                      EasyMap.build("id", new Long(1003),
                                                     "parentoptionid", PARENT_OPTION_ID,
                                                     "value", "holden",
                                                     "customfieldconfig", new Long(10001),
                                                     "sequence", new Long(1)));
        return child2;
    }

    public static MockGenericValue _newMockChild1GV()
    {
        MockGenericValue child1;
        child1 = new MockGenericValue("CustomFieldOption",
                                      EasyMap.build("id", CHILD_1_ID,
                                                     "value", "ford",
                                                     "parentoptionid", PARENT_OPTION_ID,
                                                     "customfieldconfig", new Long(10001),
                                                     "sequence", new Long(0)));
        return child1;
    }

    public static MockGenericValue _newMockParentOptionGV()
    {
        MockGenericValue parent;
        parent = new MockGenericValue("CustomFieldOption",
                                            EasyMap.build("id", PARENT_OPTION_ID,
                                                           "parentoptionid", null,
                                                           "value", "cars",
                                                           "customfield", new Long(10001),
                                                           "customfieldconfig", new Long(10001),
                                                           "sequence", new Long(0)));
        return parent;
    }

    public static MockOption _getMockParentOption()
    {
        final MockOption mockOption = new MockOption(_newMockParentOptionGV());

        final MockOption mockOption1 = new MockOption(_newMockChild1GV());
        mockOption1.setParentOption(mockOption);

        final MockOption mockOption2 = new MockOption(_newMockChild2GV());
        mockOption2.setParentOption(mockOption);


        final List childOptions = EasyList.build(mockOption1, mockOption2);
        mockOption.setChildOptions(childOptions);
        mockOption.setAllChildren(childOptions);
        return mockOption;
    }

    public static MockOption _getMockChild1Option()
    {
        final MockOption mockOption1 = new MockOption(_newMockChild1GV());
        mockOption1.setParentOption(_getMockParentOption());
        return mockOption1;
    }

    public static MockOption _getMockChild2Option()
    {
        final MockOption mockOption2 = new MockOption(_newMockChild2GV());
        mockOption2.setParentOption(_getMockParentOption());
        return mockOption2;
    }

    public static Option _getMockParent2Option()
    {
        MockGenericValue parent;
        parent = new MockGenericValue("CustomFieldOption",
                                      EasyMap.build("id", new Long(1001),
                                                     "parentoptionid", null,
                                                     "value", "2",
                                                     "customfieldconfig", new Long(10001),
                                                     "sequence", new Long(1)));

        return new MockOption(parent);

    }
}
