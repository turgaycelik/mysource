package com.atlassian.jira.gadgets.system.util;

import com.atlassian.jira.rest.v1.model.errors.ValidationError;

import java.util.Collection;
import java.util.Set;

public interface ProjectsAndCategoriesHelper
{
    void validate(String projectsOrCategories, Collection<ValidationError> errors, String fieldName);

    Set<Long> getProjectIds(String projectAndCategoryIds);
}
