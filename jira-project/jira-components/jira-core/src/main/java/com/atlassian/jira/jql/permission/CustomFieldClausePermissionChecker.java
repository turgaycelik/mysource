package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.NonInjectableComponent;

import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause permission checker for custom fields that will check the users permission to see the field and also that
 * the user has permission to see at least one context on the custom field.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class CustomFieldClausePermissionChecker implements ClausePermissionChecker
{
    private final FieldManager fieldManager;
    private final CustomField field;
    private final FieldConfigSchemeClauseContextUtil clauseContextUtil;

    public CustomFieldClausePermissionChecker(final CustomField field, final FieldManager fieldManager, final FieldConfigSchemeClauseContextUtil clauseContextUtil)
    {
        this.clauseContextUtil = notNull("clauseContextUtil", clauseContextUtil);
        this.fieldManager = notNull("fieldManager", fieldManager);
        this.field = notNull("field", field);
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return hasPermissionToUseClause(user, fieldManager.getVisibleFieldLayouts(user));
    }

    @Override
    public boolean hasPermissionToUseClause(User user, Set<FieldLayout> fieldLayouts)
    {
        if (!fieldManager.isFieldHidden(fieldLayouts, field))
        {
            final List<FieldConfigScheme> fieldConfigSchemes = field.getConfigurationSchemes();

            for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
            {
                final ClauseContext context = clauseContextUtil.getContextForConfigScheme(user, fieldConfigScheme);
                if (!context.getContexts().isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    @InjectableComponent
    public interface Factory
    {
        ClausePermissionChecker createPermissionChecker(CustomField field, FieldConfigSchemeClauseContextUtil contextUtil);

        ClausePermissionChecker createPermissionChecker(String fieldId, FieldConfigSchemeClauseContextUtil contextUtil);
    }

    /**
     * This is a factory so that we don't have a circular dependency on the Field manager. It looks like
     *
     * Field Manager -> Field -> SearchHandler -> FieldClausePermissionHandler -> Field Manager.
     *
     * @since 4.0
     */
    ///CLOVER:OFF
    public static final class DefaultFactory implements Factory
    {
        public ClausePermissionChecker createPermissionChecker(final CustomField field, FieldConfigSchemeClauseContextUtil contextUtil)
        {
            return new CustomFieldClausePermissionChecker(field, getFieldManager(), contextUtil);
        }

        public ClausePermissionChecker createPermissionChecker(final String fieldId,  FieldConfigSchemeClauseContextUtil contextUtil)
        {
            CustomField field = getFieldManager().getCustomField(fieldId);
            return new CustomFieldClausePermissionChecker(field, getFieldManager(), contextUtil);
        }

        private static FieldManager getFieldManager()
        {
            return ComponentAccessor.getFieldManager();
        }
    }
    ///CLOVER:ON
}
