package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Validates a Project Import Mapper.
 *
 * @since v3.13
 */
public interface MapperValidator
{
    /**
     * This is a simple validation method that will only check to see that all the required values of the
     * provided mapper have been mapped to the current instance of JIRA.
     *
     * If there is a required value that is missing the message collection will contain an error.
     *
     * @param i18nHelper used to resolve i18n messages
     * @param simpleProjectImportIdMapper the mapper that will be inspected to make sure that all its required values
     * are mapped.
     * @return a MessageSet that will contain any errors or warnings that have been encountered.
     */
    public MessageSet validateMappings(I18nHelper i18nHelper, SimpleProjectImportIdMapper simpleProjectImportIdMapper);
}
