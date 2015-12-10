package com.atlassian.jira.workflow;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;

/**
 * This is a holder object for Transition options. Currently it is used to optionally skip conditions, validators and
 * permissions while performing transitions
 * <p/>
 * This could be expanded in future to cover further transition options.
 *
 * @since v6.3
 */
@PublicApi
public class TransitionOptions
{
    private final static String SKIP_CONDITIONS_WORKFLOW_PARAMETER = "com.atlassian.jira.internal.workflow.skipConditions";
    private final static String SKIP_VALIDATORS_WORKFLOW_PARAMETER = "com.atlassian.jira.internal.workflow.skipValidators";
    private final static String AUTOMATIC_TRANSITION_WORKFLOW_PARAMETER = "com.atlassian.jira.internal.workflow.automaticTransition";

    private final boolean skipConditions;
    private final boolean skipValidators;
    private final boolean skipPermissions;
    private final boolean automaticTransition;

    @Internal
    private TransitionOptions(boolean skipConditions, boolean skipValidators, boolean skipPermissions, final boolean automaticTransition)
    {
        this.skipConditions = skipConditions;
        this.skipValidators = skipValidators;
        this.skipPermissions = skipPermissions;
        this.automaticTransition = automaticTransition;
    }

    /**
     * Default options for transitioning.
     *
     * This will not skip conditions, validators or permissions. Business as usual.
     *
     * @return default TransitionOptions
     */
    public static TransitionOptions defaults()
    {
        return new TransitionOptions(false, false, false, false);
    }

    public static class Builder
    {
        private boolean skipConditions;
        private boolean skipValidators;
        private boolean skipPermissions;
        private boolean automaticTransition;

        public Builder skipConditions()
        {
            this.skipConditions = true;
            return this;
        }

        public Builder skipValidators()
        {
            this.skipValidators = true;
            return this;
        }

        public Builder skipPermissions()
        {
            this.skipPermissions = true;
            return this;
        }

        public Builder setAutomaticTransition()
        {
            this.automaticTransition = true;
            return this;
        }

        public TransitionOptions build()
        {
            return new TransitionOptions(skipConditions, skipValidators, skipPermissions, automaticTransition);
        }
    }

    public boolean skipConditions()
    {
        return skipConditions;
    }

    public boolean skipValidators()
    {
        return skipValidators;
    }

    public boolean skipPermissions()
    {
        return skipPermissions;
    }

    public boolean isAutomaticTransition()
    {
        return automaticTransition;
    }

    public Map<String, Object> getWorkflowParams()
    {
        Map<String, Object> result = new HashMap<String, Object>();

        if (skipConditions)
        {
            result.put(SKIP_CONDITIONS_WORKFLOW_PARAMETER, skipConditions);
        }

        if (skipValidators)
        {
            result.put(SKIP_VALIDATORS_WORKFLOW_PARAMETER, skipValidators);
        }

        if (automaticTransition)
        {
            result.put(AUTOMATIC_TRANSITION_WORKFLOW_PARAMETER, automaticTransition);
        }
        return result;
    }

    public static TransitionOptions toTransitionOptions(Map<String, Object> workflowMap)
    {
        return new TransitionOptions(
                getBooleanValue(SKIP_CONDITIONS_WORKFLOW_PARAMETER, workflowMap),
                getBooleanValue(SKIP_VALIDATORS_WORKFLOW_PARAMETER, workflowMap),
                false,
                getBooleanValue(AUTOMATIC_TRANSITION_WORKFLOW_PARAMETER, workflowMap)
        );
    }

    private static boolean getBooleanValue(final String key, final Map<String, Object> map)
    {
        if (map.containsKey(key))
        {
            Boolean value = (Boolean) map.get(key);
            return value.booleanValue();
        }

        return false;
    }
}
