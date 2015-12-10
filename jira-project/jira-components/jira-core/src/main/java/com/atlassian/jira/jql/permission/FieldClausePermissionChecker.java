package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.NonInjectableComponent;

import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause permission checker that will check only the users permission to see the field, based on the
 * configured field configuration schemes.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class FieldClausePermissionChecker implements ClausePermissionChecker
{
    private final FieldManager fieldManager;
    private final Field field;

    public FieldClausePermissionChecker(final Field field, final FieldManager fieldManager)
    {
        this.fieldManager = notNull("fieldManager", fieldManager);
        this.field = notNull("field", field);
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return !fieldManager.isFieldHidden(user, field);
    }

    @Override
    public boolean hasPermissionToUseClause(User searcher, Set<FieldLayout> fieldLayouts)
    {
        return !fieldManager.isFieldHidden(fieldLayouts, field);
    }

    @InjectableComponent
    public interface Factory
    {
        ClausePermissionChecker createPermissionChecker(Field field);

        ClausePermissionChecker createPermissionChecker(String fieldId);
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
        public ClausePermissionChecker createPermissionChecker(final Field field)
        {
            return new FieldClausePermissionChecker(field, getFieldManager());
        }

        public ClausePermissionChecker createPermissionChecker(final String fieldId)
        {
            Field field = getFieldManager().getField(fieldId);
            return new FieldClausePermissionChecker(field, getFieldManager());
        }

        private static FieldManager getFieldManager()
        {
            return ComponentAccessor.getFieldManager();
        }
    }
    ///CLOVER:ON
}
