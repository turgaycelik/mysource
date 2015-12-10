package com.atlassian.jira.config.webwork.actions;

import com.atlassian.jira.security.Permissions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import webwork.config.Configuration;
import webwork.config.util.ActionInfo;

import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBefore;

/**
 * Represents the list of web action configuration entries stored in actions.xml
 *
 * @since v5.0.7
 */
public interface ActionConfiguration
{
    /**
     * Returns an action configuration {@link Entry} describing the action to be executed for a given alias by querying
     * the current configuration. If there is no explicit alias defined for an action in the configuration, we consider
     * that the alias for that action is the action's name.
     *
     * @param alias The alias to use to lookup the action configuration entry.
     * @return An action configuration entry instance specifying the action, command, and permissions required to
     *         execute it. <p>If there's no entry for the specified alias <code>null</code> is returned.</p>
     */
    Entry getActionCommand(String alias);

    /**
     * Retrieves the web action configuration through the webwork {@link Configuration} object.
     */
    public class FromWebWorkConfiguration implements ActionConfiguration
    {

        @Override
        @Nullable
        public Entry getActionCommand(final String alias)
        {
            String rolesRequired = getRolesRequiredMappingEntryFor(alias);
            ActionInfo actionInfo;
            try
            {
                actionInfo = getActionInfoMappingEntryFor(alias);
                return Entry.newBuilder().
                        className(parseActionClassFrom(getActionMappingEntryFor(alias))).
                        commandMethod(parseCommandMethodFrom(getActionMappingEntryFor(alias))).
                        rolesRequired(rolesRequired).
                        actionInfo(actionInfo).
                        build();
            }
            catch (IllegalArgumentException aliasNotFound)
            {
                final ActionInfo actionMappingForAlias = getActionInfoMappingEntryFor(alias);
                if ((actionMappingForAlias != null) && ("".equals(actionMappingForAlias.getActionAlias())))
                {
                    // if there is an un-aliased <action> with the same name=, use that.
                    return Entry.newBuilder().
                            className(alias).
                            rolesRequired(rolesRequired).
                            actionInfo(actionMappingForAlias).
                            build();
                }
                else
                {
                    return null;
                }
            }
        }

        private String parseCommandMethodFrom(String actionNameAndCommand)
        {
            if (actionNameAndCommand.contains("!"))
            {
                Iterables.getLast(Splitter.on('!').split(actionNameAndCommand));
            }
            return "";
        }

        private String parseActionClassFrom(final String actionNameAndCommand)
        {
            if (actionNameAndCommand.contains("!"))
            {
                Iterables.getFirst(Splitter.on('!').split(actionNameAndCommand), "");
            }
            return actionNameAndCommand;
        }

        private String getRolesRequiredMappingEntryFor(final String alias)
        {
            return getStringFromConfiguration(alias + ".actionRoles");
        }

        private String getActionMappingEntryFor(final String alias)
        {
            return getStringFromConfiguration(alias + "." + getStringFromConfiguration("webwork.action.extension"));
        }

        private ActionInfo getActionInfoMappingEntryFor(final String alias)
        {
            return (ActionInfo) getObjectFromConfiguration(alias + ".actionInfo");
        }

        @VisibleForTesting
        String getStringFromConfiguration(final String key)
        {
            return (String) getObjectFromConfiguration(key);
        }

        @VisibleForTesting
        Object getObjectFromConfiguration(final String key)
        {
            try
            {
                return Configuration.get(key);
            }
            catch (IllegalArgumentException miss)
            {
                String prefix = substringBefore(key, "!");
                String suffix = key.contains(".") ? "." + substringAfterLast(key, ".") : "";

                return Configuration.get(prefix + suffix);

            }
        }
    }

    /**
     * An entry providing the webwork configuration for an action alias.  The entry contains the action class, the
     * command, and any permissions required in order to execute the action.
     */
    public static class Entry
    {
        private final String name;

        private final String command;

        private final Iterable<Integer> permissionsRequired;

        private final ActionInfo actionInfo;

        public Entry(final String name, final String command, final Iterable<Integer> permissionsRequired, final ActionInfo actionInfo)
        {
            this.name = name;
            this.command = command;
            this.permissionsRequired = permissionsRequired;
            this.actionInfo = actionInfo;
        }

        /**
         * Returns a String representing this action configuration entry if the format expected by the action factory
         * chain, i.e. <code>ManageSubTasks!enableSubTasks</code> where <code>ManageSubTasks</code> is the action class
         * name and <code>enableSubTasks</code> is the command method. If no command method is supplied, then the string
         * will only contain the action name, i.e. <code>ViewProjects</code>.
         *
         * @return the action factory string
         */
        public String toActionFactoryString()
        {
            if (Strings.isNullOrEmpty(command))
            {
                return name;
            }
            return name + "!" + command;
        }

        public static EntryBuilder newBuilder()
        {
            return new EntryBuilder();
        }

        /**
         * Returns a list of permissions required for the action.
         *
         * @return a list of permissions required for the action.
         */
        public Iterable<Integer> getPermissionsRequired()
        {
            return permissionsRequired;
        }

        /**
         * @return the underlying {@link ActionInfo} for this action
         */
        public ActionInfo getActionInfo()
        {
            return actionInfo;
        }

        public static class EntryBuilder
        {
            private String name;
            private String command = "";
            private String rolesRequired;
            private ActionInfo actionInfo;

            public EntryBuilder className(final String name)
            {
                this.name = name;
                return this;
            }

            public EntryBuilder commandMethod(final String command)
            {
                this.command = command;
                return this;
            }

            /**
             * Supplies a comma-separated list of the roles that are required to execute this action.  When multiple
             * roles are given, they are <strong>all</strong> required.  When {@link #build()} is invoked, this list of
             * roles is transformed to a list of integer permissions as specified by {@link
             * Permissions#getType(String)}.
             *
             * @param rolesRequired a comma-separated string containing the required roles
             * @return <code>this</code>
             */
            public EntryBuilder rolesRequired(final String rolesRequired)
            {
                this.rolesRequired = rolesRequired;
                return this;
            }

            public EntryBuilder actionInfo(final ActionInfo actionInfo)
            {
                this.actionInfo = actionInfo;
                return this;
            }

            public Entry build()
            {
                return new Entry(name, command, transformRolesToPermissions(rolesRequired), actionInfo);
            }

            private Iterable<Integer> transformRolesToPermissions(@Nullable final String roles)
            {
                if (roles == null) { return emptyList(); }

                final Iterable<String> splitRoles = Splitter.on(",").omitEmptyStrings().
                        trimResults().
                        split(roles);

                return transform(splitRoles, new Function<String, Integer>()
                {
                    @Override
                    public Integer apply(@Nullable String input)
                    {
                        return Permissions.getType(input);
                    }
                });

            }
        }
    }

}
