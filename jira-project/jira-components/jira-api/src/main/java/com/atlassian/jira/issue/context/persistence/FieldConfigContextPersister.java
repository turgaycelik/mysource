package com.atlassian.jira.issue.context.persistence;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaPersister;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

public interface FieldConfigContextPersister extends BandanaPersister
{
    List<JiraContextNode> getAllContextsForCustomField(String key);

    List<JiraContextNode> getAllContextsForConfigScheme(FieldConfigScheme fieldConfigScheme);

    /**
     * Remove contexts for a scheme
     * @param fieldConfigSchemeId ID of scheme to remove.
     * @deprecated since v6.3 Use {@link #removeContextsForConfigScheme(FieldConfigScheme fieldConfigScheme)}
     */
    @Deprecated
    void removeContextsForConfigScheme(Long fieldConfigSchemeId);

    void removeContextsForConfigScheme(FieldConfigScheme fieldConfigScheme);

    /**
     * Bulk store context/key/value triplets.
     * @since 6.0.7
     */
    void store(Collection<? extends BandanaContext> contexts, String key, Object value);

    /**
     * @deprecated Use {@link #removeContextsForProject(com.atlassian.jira.project.Project)} instead. Since v5.1.
     * @param project the project
     */
    void removeContextsForProject(GenericValue project);

    void removeContextsForProject(Project project);

    void removeContextsForProjectCategory(ProjectCategory projectCategory);
}
