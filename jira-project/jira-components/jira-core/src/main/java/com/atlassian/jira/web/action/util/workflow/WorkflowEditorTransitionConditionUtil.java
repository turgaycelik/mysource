/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.util.workflow;

import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorkflowEditorTransitionConditionUtil
{
    /**
     * Separator for the 'count' variables that is used to represent a position in a hierarchy.
     */
    public static final String SEPARATOR = ".";
    public static final String OPERATOR_AND = "AND";
    public static final String OPERATOR_OR = "OR";


    /**
     * Returns a {@link ConditionsDescriptor} in the given {@link RestrictionDescriptor} in the 'place' specified by the
     * count. The count is a {@link #SEPARATOR} separated integers that represents the place of the ConditionsDescriptor
     * in the hierarchy of nested ConditionsDescriptors.
     * <p>
     * This method assumes that the last integer in count will be used to find a {@link com.opensymphony.workflow.loader.ConditionDescriptor} in the
     * returned {@link ConditionsDescriptor}, so is not used while looking through the hierarchy.
     * </p>
     * <p>
     * For example, if this method is called with count "1.2.4", this method will take the root ConditionsDescriptor of the passed
     * RestrictionDescriptor, look up a nested ConditionsDescriptor at index 0. Them another lookup will be done for another nested
     * ConditionsDescriptor at index 1. This ConditionsDescriptor will be returned, as "4" is ignored.
     * </p>
     * @throws IllegalArgumentException if the object at any index specified by the count is not an instance of {@link ConditionsDescriptor}
     */
    public ConditionsDescriptor getParentConditionsDescriptor(RestrictionDescriptor restrictionDescriptor, String count)
    {
        return findConfitionsDescriptor(restrictionDescriptor, count, 1);
    }

    /**
     * Same as {@link #getParentConditionsDescriptor(com.opensymphony.workflow.loader.RestrictionDescriptor, String)} only all the
     * indexes specified by the passed in count are used for lookups. That is, the last integer in count is <b>NOT</b> ignored.
     */
    public ConditionsDescriptor getConditionsDescriptor(RestrictionDescriptor restrictionDescriptor, String count)
    {
        return findConfitionsDescriptor(restrictionDescriptor, count, 0);

    }

    private ConditionsDescriptor findConfitionsDescriptor(RestrictionDescriptor restrictionDescriptor, String count, int offset)
    {
        ConditionsDescriptor conditionsDescriptor = restrictionDescriptor.getConditionsDescriptor();

        String[] counts = StringUtils.split(count, SEPARATOR);

        if (counts != null && counts.length > offset)
        {
            for (int i = 0; i < counts.length - offset; i++)
            {
                int c = Integer.parseInt(counts[i]);
                List<?> conditions = conditionsDescriptor.getConditions();
                if (conditions.get(c - 1) instanceof ConditionsDescriptor)
                {
                    conditionsDescriptor = (ConditionsDescriptor) conditions.get(c - 1);
                }
                else
                {
                    throw new IllegalArgumentException("The descriptor at count " + count + " is not a ConditionsDescriptor.");
                }
            }
        }

        return conditionsDescriptor;
    }


    public void getGrandParentConditionsDescriptor(RestrictionDescriptor restriction, String count)
    {


    }

    /**
     * Checks if restrictionDescriptor has no {@link com.opensymphony.workflow.loader.ConditionDescriptor}s or
     * {@link ConditionsDescriptor}s left.
     */
    public boolean isEmpty(RestrictionDescriptor restrictionDescriptor)
    {
        ConditionsDescriptor conditionsDescriptor = restrictionDescriptor.getConditionsDescriptor();
        if (conditionsDescriptor == null)
            return true;

        Collection<?> conditions = conditionsDescriptor.getConditions();
        return (conditions == null) || (conditions.isEmpty());
    }

    /**
     * Checks if restrictionDescriptor has no {@link ConditionsDescriptor}s left.
     */
    public boolean isEmpty(ConditionsDescriptor conditionsDescriptor)
    {
        Collection<?> conditions = conditionsDescriptor.getConditions();
        return (conditions == null) || (conditions.isEmpty());
    }

    public String increaseCountLevel(String c)
    {
        if (c == null)
            return null;

        int index = c.lastIndexOf(SEPARATOR);

        if (index < 0)
            throw new IllegalArgumentException("The string '" + c + "' does not contain '" + SEPARATOR + "'.");
        else
            return c.substring(0, index);
    }

    public int getLastCount(String count)
    {
        int index = count.lastIndexOf(SEPARATOR);

        if (index < 0)
            return Integer.parseInt(count);
        else if (index + 1 >= count.length())
            throw new IllegalArgumentException("Cannot find last index in '" + count + "'.");
        else
            return Integer.parseInt(count.substring(index + 1));
    }

    public String addCondition(ActionDescriptor transition, String count, ConditionDescriptor condition)
    {
        RestrictionDescriptor restriction = initRestriction(transition);

        ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
        if (conditionsDescriptor == null)
        {
            // A first condition is being added
            conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
            restriction.setConditionsDescriptor(conditionsDescriptor);
            conditionsDescriptor.setType(OPERATOR_AND);  //this happens by default anyhow I believe, but stating explicitly
            conditionsDescriptor.getConditions().add(condition);
            return "1";
        }
        else
        {
            conditionsDescriptor = getParentConditionsDescriptor(restriction, count);
            if (conditionsDescriptor.getConditions() == null)
            {
                conditionsDescriptor.setConditions(new ArrayList());
            }

            // Add the condition ot the end of the list
            conditionsDescriptor.getConditions().add(condition);

            if (!TextUtils.stringSet(conditionsDescriptor.getType()))
            {
                conditionsDescriptor.setType(OPERATOR_AND);
            }

            if (isRootCount(count))
            {
                return "" + conditionsDescriptor.getConditions().size();
            }
            else
            {
                return increaseCountLevel(count) + SEPARATOR + conditionsDescriptor.getConditions().size();
            }
        }
    }

    public String addNestedCondition(ActionDescriptor transition, String count, ConditionDescriptor condition)
    {
        RestrictionDescriptor restriction = initRestriction(transition);

        ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
        if (conditionsDescriptor == null)
        {
            // A first condition is being added
            conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
            restriction.setConditionsDescriptor(conditionsDescriptor);
            conditionsDescriptor.setType(OPERATOR_AND);  //this happens by default anyhow I believe, but stating explicitly
            conditionsDescriptor.getConditions().add(condition);
            return "1";
        }
        else
        {
            conditionsDescriptor = getParentConditionsDescriptor(restriction, count);

            int index = getLastCount(count) - 1;
            // Remove the existing condition as we will put a nested ConditionsDescriptor in its place.
            // The nested ConditionsDescriptor will have this condition and the new one in it.
            Object d = conditionsDescriptor.getConditions().remove(index);
            // This should always be a condition descriptor
            if (d instanceof ConditionDescriptor)
            {
                ConditionDescriptor existingCondition = (ConditionDescriptor) d;
                ConditionsDescriptor nestedDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
                nestedDescriptor.setType(OPERATOR_AND);

                // Ensure that the conditions list is initialised for the ConditionsDescriptor
                if (nestedDescriptor.getConditions() == null)
                {
                    nestedDescriptor.setConditions(new ArrayList());
                }

                // Add the existing condition and the new one to the nested ConditionsDescriptor
                nestedDescriptor.getConditions().add(existingCondition);
                nestedDescriptor.getConditions().add(condition);

                // Find the place where we can insert the nested block. All the ConditionsDescriptors must
                // appear before ConditionDescriptors as mandated by the OSWorkflow DTD
                int insertIndex = findNestedBlockInsertIndex(conditionsDescriptor);

                // Add the nested descriptor to where the existing condition used to be
                conditionsDescriptor.getConditions().add(insertIndex, nestedDescriptor);


                StringBuilder returnIndex = new StringBuilder();
                if (!isRootCount(count))
                {
                    returnIndex.append(increaseCountLevel(count));
                    returnIndex.append(SEPARATOR);
                }

                returnIndex.append(insertIndex + 1);
                returnIndex.append(SEPARATOR);
                returnIndex.append(nestedDescriptor.getConditions().size());

                return returnIndex.toString();
            }
            else
            {
                throw new IllegalArgumentException("Descriptor at position '" + count + "' must be a ConditionDescriptor");
            }
        }
    }

    private RestrictionDescriptor initRestriction(ActionDescriptor transition)
    {
        RestrictionDescriptor restriction = transition.getRestriction();

        if (restriction == null)
        {
            restriction = new RestrictionDescriptor();
            transition.setRestriction(restriction);
        }

        return restriction;
    }

    private int findNestedBlockInsertIndex(ConditionsDescriptor conditionsDescriptor)
    {
        Collection<?> conditions = conditionsDescriptor.getConditions();
        if (conditions == null)
            return 0;

        int i = 0;
        for (Object descriptor : conditions)
        {
            if (descriptor instanceof ConditionDescriptor)
            {
                return i;
            }
            else if (descriptor instanceof ConditionsDescriptor)
            {
                i++;
            }
            else
            {
                throw new IllegalArgumentException("Cannot process class '" + descriptor.getClass().getName() + "'.");
            }
        }

        return i;
    }

    public void deleteCondition(ActionDescriptor transition, String count)
    {
        RestrictionDescriptor restriction = transition.getRestriction();
        if (restriction != null)
        {
            ConditionsDescriptor conditionsDescriptor = getParentConditionsDescriptor(restriction, count);
            conditionsDescriptor.getConditions().remove(getLastCount(count) - 1);

            if (isEmpty(conditionsDescriptor))
            {
                while (isEmpty(conditionsDescriptor))
                {
                    if (isRootCount(count))
                    {
                        transition.setRestriction(null);
                        break;
                    }
                    else
                    {
                        count = increaseCountLevel(count);
                        conditionsDescriptor = getParentConditionsDescriptor(restriction, count);
                        conditionsDescriptor.getConditions().remove(getLastCount(count) - 1);
                    }
                }
            }
            else if (conditionsDescriptor.getConditions().size() == 1)
            {
                Object descriptor = conditionsDescriptor.getConditions().get(0);

                if (descriptor instanceof ConditionDescriptor)
                {
                    // Only fallten out the structure if we are not dealing with the Root Conditions block
                    if (!isRootCount(count))
                    {
                        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;
                        count = increaseCountLevel(count);
                        ConditionsDescriptor conditionsDescriptor2 = getParentConditionsDescriptor(restriction, count);
                        int index = getLastCount(count) - 1;
                        conditionsDescriptor2.getConditions().remove(index);

                        // As we are inserting a ConditionDescriptor we need to find a place for it after all the
                        // ConditionsDescriptos in conditionsDescriptor2. Remember that the OSWorkflow DTD forces us
                        // To have ConditionsDescriptors before ConditionDescriptos
                        index = findNestedBlockInsertIndex(conditionsDescriptor2);
                        conditionsDescriptor2.getConditions().add(index, conditionDescriptor);

                    }
                }
                else if (descriptor instanceof ConditionsDescriptor)
                {
                    ConditionsDescriptor cd = (ConditionsDescriptor) descriptor;
                    conditionsDescriptor.getConditions().remove(0);

                    if (cd.getConditions() != null && !cd.getConditions().isEmpty())
                    {
                        conditionsDescriptor.getConditions().addAll(cd.getConditions());
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Cannot deal with '" + descriptor.getClass().getName() + "'.");
                }
            }
        }
    }

    private boolean isRootCount(String count)
    {
        if (TextUtils.stringSet(count))
            return count.indexOf(SEPARATOR) < 0;
        else
            return true;
    }

    public void changeLogicOperator(ActionDescriptor transition, String count)
    {
        RestrictionDescriptor restriction = transition.getRestriction();
        if (restriction != null)
        {
            ConditionsDescriptor conditionsDescriptor = getParentConditionsDescriptor(restriction, count);
            if (OPERATOR_AND.equals(conditionsDescriptor.getType()))
            {
                conditionsDescriptor.setType(OPERATOR_OR);
            }
            else
            {
                conditionsDescriptor.setType(OPERATOR_AND);
            }
        }
    }
}
