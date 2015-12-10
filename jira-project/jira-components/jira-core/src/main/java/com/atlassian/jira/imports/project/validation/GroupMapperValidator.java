package com.atlassian.jira.imports.project.validation;

/**
 * Validates that a Group Mapper has all the required groups.
 *
 * @since v3.13
 */
public class GroupMapperValidator extends AbstractSimpleMapperValidator
{
    protected String getEntityDoesNotExistKey()
    {
        return "admin.errors.project.import.group.validation.does.not.exist";
    }

    protected String getEntityName()
    {
        return "Group";
    }
}
