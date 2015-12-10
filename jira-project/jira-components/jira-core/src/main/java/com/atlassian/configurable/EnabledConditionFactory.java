package com.atlassian.configurable;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import static com.atlassian.core.util.ClassLoaderUtils.loadClass;
import static com.atlassian.jira.util.JiraUtils.loadComponent;

/**
 * Factory for constructing EnabledCondition instances.
 */
public class EnabledConditionFactory
{
    private static final Logger log = LoggerFactory.getLogger(EnabledConditionFactory.class);

    /**
     * Attempts to instantiate the given class name as an instance of EnabledCondition and returns it.
     * @param enabledConditionClass the name of an implementing class with a no-arg constructor, if null will return null.
     * @return the constructed instance or null if the class can't be found or instantiated.
     */
    static EnabledCondition create(String enabledConditionClass)
    {
        if (enabledConditionClass == null)
        {
            log.debug("Cannot instantiate null EnabledCondition");
            return null;
        }
        try
        {
            @SuppressWarnings("unchecked")
            Class<EnabledCondition> conditionClass = loadClass(enabledConditionClass, EnabledConditionFactory.class);
            return loadComponent(conditionClass);
        }
        catch (ClassNotFoundException e)
        {
            log.warn(String.valueOf(MessageFormatter.format("Cannot find Enabled Condition: '{}'", e)));
        }
        catch (ClassCastException e)
        {
            log.warn(String.valueOf(MessageFormatter.format("Enabled Condition : '{}' must implement {}, '{}'",
                    new String[] { enabledConditionClass, EnabledCondition.class.getName(), e.toString() })));
        }
        return null;
    }
}
