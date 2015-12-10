package com.atlassian.jira.imports.project.validation;

/**
 * Validates that a ResolutionMapper has all required resolutions mapped.
 *
 * @since v3.13
 */
public class ResolutionMapperValidator extends AbstractSimpleMapperValidator
{
    protected String getEntityDoesNotExistKey()
    {
        return "admin.errors.project.import.resolution.validation.does.not.exist";
    }

    protected String getEntityName()
    {
        return "Resolution";
    }
}
