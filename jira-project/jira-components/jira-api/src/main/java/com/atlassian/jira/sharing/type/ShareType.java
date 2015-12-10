package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Comparator;

/**
 * Interface for all ShareTypes in the JIRA. ShareType represents a way to share an object and contains all logic for rendering, validating and
 * searching.
 * 
 * @since v3.13
 */
public interface ShareType
{
    /**
     * Unique identifier for the ShareType.
     * 
     * @return A ShareType.Name representing a unique value that the ShareType is associated with. Should not be internationalised.
     */
    Name getType();

    /**
     * Represents whether this ShareType is a singleton. I.e the {@link SharedEntity} can only have a single instance of this ShareType and no
     * others.
     * 
     * @return true if this ShareType is a singleton, otherwise false.
     */
    boolean isSingleton();

    /**
     * Return the priority of the ShareType. The lower the value the higher the priority. This is used when determining when one ShareType should take
     * precedence over another.
     * 
     * @return the priority
     */
    int getPriority();

    /**
     * Retrieves the {@link ShareTypeRenderer} that contains all logic for rendering this ShareType
     * 
     * @return the ShareTypeRenderer responsible for displaying the ShareType.
     */
    ShareTypeRenderer getRenderer();

    /**
     * Retrieves the {@link ShareTypeValidator} that contains all logic for validating this ShareType
     * 
     * @return the ShareTypeValidator responsible for validating the ShareType.
     */
    ShareTypeValidator getValidator();

    /**
     * Responsible for checking that a user has permission to use {@link SharedEntity}
     * 
     * @return the ShareTypePermissionChecker for this {@link ShareType}
     */
    ShareTypePermissionChecker getPermissionsChecker();

    /**
     * Return the object that can be used to build query conditions to find instances of this ShareType.
     * 
     * @return the object that can be used to build a query.
     */
    ShareQueryFactory<? extends ShareTypeSearchParameter> getQueryFactory();

    /**
     * Return a comparator that can order a {@link SharePermission} of this type.
     * 
     * @return a comparator that can order the permissions of this type.
     */
    Comparator<SharePermission> getComparator();

    /**
     * The Name of this share type, constricts the {@link ShareType#getType()}
     */
    final class Name
    {
        public static final Name GLOBAL = new Name("global");
        public static final Name PROJECT = new Name("project");
        public static final Name GROUP = new Name("group");

        private final String name;

        public Name(final String name)
        {
            Assertions.notBlank("name", name);
            this.name = name;
        }

        public String get()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Name other = (Name) obj;
            if (name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!name.equals(other.name))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
