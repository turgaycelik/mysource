package com.atlassian.jira.workflow;

import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

/**
 * A Validator that is Skippable
 */
public class SkippableValidator implements Validator
{
    private final Validator validator;

    private SkippableValidator(Validator validator)
    {
        this.validator = validator;
    }

    /**
     * Generate a new SkippableValidator for a given condition.
     *
     * This is done to preserve existing behaviour around returning nulls.
     *
     * @param validator
     * @return condition == null ? null : new SkippableValidator(condition);
     */
    public static SkippableValidator of(Validator validator)
    {
        if (validator == null)
        {
            return null;
        }
        else
        {
            return new SkippableValidator(validator);
        }
    }


    @Override
    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException, WorkflowException
    {
        TransitionOptions transitionOptions= TransitionOptions.toTransitionOptions(transientVars);
        if (transitionOptions.skipValidators())
        {
            return;
        }

        validator.validate(transientVars, args, ps);
    }
}
