package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

public class IssueConstantOption extends AbstractOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final Transformer TRANSFORMER = new Transformer()
    {
        public Object transform(Object input)
        {
            return new IssueConstantOption((IssueConstant) input);
        }
    };

    public static final Predicate STANDARD_OPTIONS_PREDICATE = new Predicate()
    {
        public boolean evaluate(Object object)
        {
            return !((IssueConstantOption) object).isSubTask();
        }
    };

    public static final Predicate SUB_TASK_OPTIONS_PREDICATE = new Predicate()
    {
        public boolean evaluate(Object object)
        {
            return ((IssueConstantOption) object).isSubTask();
        }
    };


    // ------------------------------------------------------------------------------------------------- Type Properties
    private IssueConstant constant;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public IssueConstantOption(IssueConstant constant)
    {
        this.constant = constant;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return constant.getId();
    }

    public String getName()
    {
        return constant.getNameTranslation();
    }

    public String getDescription()
    {
        return constant.getDescTranslation();
    }

    public String getImagePath()
    {
        return constant.getIconUrl();
    }

    public boolean isSubTask()
    {
        if (constant instanceof IssueType)
        {
            IssueType issueType = (IssueType) constant;
            return issueType.isSubTask();
        }
        else
        {
            return false;
        }
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods

}
