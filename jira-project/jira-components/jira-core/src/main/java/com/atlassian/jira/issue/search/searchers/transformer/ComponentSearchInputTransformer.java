package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A component-specific {@link com.atlassian.jira.issue.search.searchers.transformer.IdIndexedSearchInputTransformer}.
 *
 * @since v4.0
 */
public class ComponentSearchInputTransformer extends AbstractProjectConstantsSearchInputTransformer<ProjectComponent, ComponentSearchInput>
{
    public ComponentSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, JqlOperandResolver operandResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry, ComponentResolver componentResolver)
    {
        super(clauseNames, urlParameterName, operandResolver, fieldFlagOperandRegistry, componentResolver);
    }

    @Nonnull
    @Override
    ComponentSearchInput parseInputParam(String[] parts)
    {
        if (parts[0].equals(ProjectComponentManager.NO_COMPONENTS))
        {
            return ComponentSearchInput.noComponents();
        }

        if (parts[0].equals("id"))
        {
            return ComponentSearchInput.component(parts[1]);
        }

        return ComponentSearchInput.component(parts[0]);
    }

    @Nonnull
    @Override
    ComponentSearchInput noValueInput()
    {
        return ComponentSearchInput.noComponents();
    }

    @Nonnull
    @Override
    ComponentSearchInput inputValue(String value)
    {
        return ComponentSearchInput.component(value);
    }

    @Override
    void parseFunctionOperand(FunctionOperand operand, Set<ComponentSearchInput> values)
    {
        // No functions for components.
    }

    @Nonnull
    @Override
    Operand parseInputValue(ComponentSearchInput value)
    {
        if (value.isNoComponent())
        {
            return EmptyOperand.EMPTY;
        }

        return new SingleValueOperand(value.getValue());
    }

    /**
     * The component must exist for it to be visible on the issue navigator. Not much to worry about otherwise.
     *
     * @param literal the literals to check.
     * @return true iff all the passed components exist.
     */
    @Override
    boolean checkClauseValuesForBasic(QueryLiteral literal)
    {
        if (literal.getStringValue() != null)
        {
            return nameResolver.nameExists(literal.asString());
        }
        else if (literal.getLongValue() != null)
        {
            //Basic NAV only accepts names. JQL assumes tries to match a number to a name if that ID does not
            //exist in the DB.
            return nameResolver.nameExists(literal.asString()) && !nameResolver.idExists(literal.getLongValue());
        }
        else
        {
            return false;
        }
    }
}
