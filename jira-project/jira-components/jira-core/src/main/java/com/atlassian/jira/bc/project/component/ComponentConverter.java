package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.project.AssigneeTypes;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Originally this was part of the OfBizProjectComponentStore. I've extracted it to a separate class so that the logic
 * can be reused elsewhere.
 * @since 4.2
 */
public class ComponentConverter
{
    public ComponentConverter() { }

    /**
     * Convert the specified collection of GenericValues representing MutableProjectComponent object to a collection of
     * MutableProjectComponent objects.
     *
     * @param componentGVCollection collection of GenericValues - each GenericValue represents a ProjectComponent
     * @return collection of new MutableProjectComponent objects that represents the original collection of
     *         GenericValues
     */
    public Collection<MutableProjectComponent> convertToComponents(Collection<GenericValue> componentGVCollection)
    {
        Collection<MutableProjectComponent> components = new ArrayList<MutableProjectComponent>(componentGVCollection.size());
        for (final GenericValue gv : componentGVCollection)
        {
            if (gv != null)
            {
                components.add(convertToComponent(gv));
            }
        }
        return components;
    }

    /**
     * Convert the specified GenericValue representing a MutableProjectComponent object to a MutableProjectComponent
     * object.
     *
     * @param componentGV GenericValue object representing a MutableProjectComponent object to be converted to a
     * MutableProjectComponent object
     * @return new MutableProjectComponent object that represents the original GenericValue object
     */
    public MutableProjectComponent convertToComponent(GenericValue componentGV)
    {
        Long id = componentGV.getLong(OfBizProjectComponentStore.FIELD_ID);
        String name = componentGV.getString(OfBizProjectComponentStore.FIELD_NAME);
        String description = componentGV.getString(OfBizProjectComponentStore.FIELD_DESCRIPTION);
        String lead = componentGV.getString(OfBizProjectComponentStore.FIELD_LEAD);
        Long projectId = componentGV.getLong(OfBizProjectComponentStore.FIELD_PROJECT);
        Long assigneeTypeLong = componentGV.getLong(OfBizProjectComponentStore.FIELD_ASSIGNEE_TYPE);
        long assigneeType;
        if (assigneeTypeLong != null)
        {
            assigneeType = assigneeTypeLong;
        }
        else
        {
            assigneeType = AssigneeTypes.PROJECT_DEFAULT;
        }
        MutableProjectComponent mutableProjectComponent = new MutableProjectComponent(id, name, description, lead, assigneeType, projectId);
        mutableProjectComponent.setGenericValue(componentGV);
        return mutableProjectComponent;
    }

    /**
     * Convert the specified MutableProjectComponent to a ProjectComponent object.
     *
     * @param value MutableProjectComponent to be converted into a ProjectComponent.
     * @return new instance of ProjectComponent with same values as given in the parameter object
     */    
    public ProjectComponent convertToProjectComponent(MutableProjectComponent value)
    {
        return new ProjectComponentImpl(value.getId(), value.getName(), value.getDescription(), value.getLead(), value.getAssigneeType(), value.getProjectId(), value.getGenericValue());
    }

    /**
     * Convert a collection of MutableProjectComponent objects to a collection of ProjectComponent objects.
     *
     * @param mutables collection of MutableProjectComponent objects to convert
     * @return collection of new ProjectComponent objects that represent objects in the given MutableProjectComponent
     *         collection
     */
    public Collection<ProjectComponent> convertToProjectComponents(Collection<MutableProjectComponent> mutables)
    {
        Collection<ProjectComponent> components = new ArrayList<ProjectComponent>(mutables.size());
        for (final MutableProjectComponent mutable : mutables)
        {
            components.add(convertToProjectComponent(mutable));
        }
        return components;
    }

}