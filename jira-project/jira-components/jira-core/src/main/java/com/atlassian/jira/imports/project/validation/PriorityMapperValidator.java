package com.atlassian.jira.imports.project.validation;

/**
 * Validates that a PriorityMapper has all required priorities mapped.
 *
 * @since v3.13
 */
public class PriorityMapperValidator extends AbstractSimpleMapperValidator
{
    protected String getEntityDoesNotExistKey()
    {
        return "admin.errors.project.import.priority.validation.does.not.exist";
    }

    protected String getEntityName()
    {
        return "Priority";
    }
}
