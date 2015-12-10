package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Validates the required Users
 * This validator makes sure that the Users that are required:
 * <ul>
 * <li>exist in the current instance</li>
 * </ul>
 *
 * @since v3.13
 */
public interface UserMapperValidator
{

    /**
     * This validator makes sure that the Users that are required:
     * <ul>
     * <li>exist in the current instance</li>
     * </ul>
     *
     * @param i18nHelper helper bean that allows us to get i18n translations
     * @param userMapper is the populated UserMapper
     * @return a MessageSet that will contain any generated errors (which should stop the import) or warnings
     * (which should be displayed to the user). The error and warning collection's will be empty if all validation
     * passes.
     */
    MessageSet validateMappings(I18nHelper i18nHelper, UserMapper userMapper);
}
