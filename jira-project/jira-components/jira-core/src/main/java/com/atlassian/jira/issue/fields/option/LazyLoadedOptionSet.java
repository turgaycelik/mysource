package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.config.ConstantsManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LazyLoadedOptionSet implements OptionSet
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private List<LazyIssueConstant> lazyConstants;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ConstantsManager constantsManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public LazyLoadedOptionSet(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
        lazyConstants = new ArrayList<LazyIssueConstant>(25);
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    @SuppressWarnings ( { "unchecked" })
    public Collection<Option> getOptions()
    {
        return CollectionUtils.collect(lazyConstants, new Transformer()
        {
            public Object transform(Object input)
            {
                final LazyIssueConstant lazyIssueConstant = ((LazyIssueConstant) input);
                return new IssueConstantOption(constantsManager.getConstantObject(lazyIssueConstant.getConstantType(),
                                                                                  lazyIssueConstant.getId()));
            }
        });
    }

    @SuppressWarnings ( { "unchecked" })
    public Collection<String> getOptionIds()
    {
        return CollectionUtils.collect(lazyConstants, new Transformer()
        {
            public Object transform(Object input)
            {
                return ((LazyIssueConstant) input).getId();
            }
        });
    }

    public void addOption(String constantType, String constantId)
    {
        lazyConstants.add(new LazyIssueConstant(constantType, constantId));
    }


    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
}

class LazyIssueConstant
{
    private String constantType;
    private String id;

    public LazyIssueConstant(String constantType, String id)
    {
        this.constantType = constantType;
        this.id = id;
    }

    public String getConstantType()
    {
        return constantType;
    }

    public String getId()
    {
        return id;
    }
}
