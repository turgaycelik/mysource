package com.atlassian.jira.plugin.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WeightedDescriptor;

import com.google.common.collect.Ordering;

import javax.annotation.concurrent.Immutable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.4
 */
public final class ModuleDescriptors
{
    /**
     * Assists in implementing a consistent implementation of {@link Object#equals(Object)} methods for module
     * descriptors based on the descriptor's complete key.
     */
    @Immutable
    public static class EqualsBuilder
    {
        final ModuleDescriptor descriptor;

        /**
         * Creates an instance of the <code>EqualsBuilder</code> for a module descriptor.
         * @param descriptor the module descriptor to create an <code>equals</code> implementation for.
         */
        public EqualsBuilder(final ModuleDescriptor descriptor)
        {
            notNull("Tried to build an equals implementation for a null module descriptor.", descriptor);
            this.descriptor = descriptor;
        }

        /**
         * <p>Returns <tt>true</tt> if the given object is also a module descriptor and the two descriptors have the same
         * "complete key" as determined by {@link ModuleDescriptor#getCompleteKey()}.</p>
         *
         * @param obj object to be compared for equality with this module descriptor.
         * @return <tt>true</tt> if the specified object is equal to this module descriptor.
         */
        public boolean isEqualsTo(Object obj)
        {
            if (descriptor == obj) { return true; }

            if (!(obj instanceof ModuleDescriptor)) { return false; }

            ModuleDescriptor rhs = (ModuleDescriptor) obj;

            return new org.apache.commons.lang.builder.EqualsBuilder().
                    append(descriptor.getCompleteKey(), rhs.getCompleteKey()).
                    isEquals();
        }
    }

    /**
     * Assists in implementing {@link Object#hashCode()} methods for module descriptors based on the <code>hashCode</code>
     * of the descriptor's complete key.
     */
    @Immutable
    public static class HashCodeBuilder
    {
        final ModuleDescriptor descriptor;

        /**
         * Creates an instance of the <code>HashCodeBuilder</code> for a module descriptor. Must not be null.
         * @param descriptor the module descriptor to create a <code>hashCode</code> implementation for.
         */
        public HashCodeBuilder(final ModuleDescriptor descriptor)
        {
            notNull("Tried to calculate the hash code of a null module descriptor.", descriptor);
            this.descriptor = descriptor;
        }

        /**
         * Return the computed <code>hashCode</code> for this module descriptor.
         *
         * @return <code>hashCode</code> based on the hashCode of the descriptor's complete key.
         */
        public int toHashCode()
        {
            return descriptor.getCompleteKey() == null ? 0 : descriptor.getCompleteKey().hashCode();
        }

        /**
         * The computed <code>hashCode</code> from toHashCode() is returned due to the likelyhood
         * of bugs in mis-calling toHashCode() and the unlikelyness of it mattering what the hashCode for
         * HashCodeBuilder itself is.
         *
         * @return <code>hashCode</code> based on the complete key of the module descriptor.
         */
        public int hashCode()
        {
            return toHashCode();
        }
    }

    /**
     * Responsible for creating Ordering instances to sort collections of module descriptors.
     *
     * @see Ordering
     * @since v4.4
     */
    public static interface Orderings
    {
        /**
         * Creates an ordering instance that orders module descriptors according to the &quot;origin&quot; of the
         * plugin they come from.
         *
         * @see com.atlassian.jira.plugin.util.orderings.ByOriginModuleDescriptorOrdering
         * @return An ordering instance that orders module descriptors according to the &quot;origin&quot; of the
         * plugin they come from.
         */
        Ordering<ModuleDescriptor> byOrigin();

        /**
         * Creates an Ordering instance that orders module descriptors according to their &quot;natural&quot; order.
         *
         * Natural order is based on the module descriptor's
         * {@link com.atlassian.plugin.ModuleDescriptor#getCompleteKey() complete key}
         * @return An Ordering instance that orders module descriptors according to their &quot;natural&quot; order.
         */
        Ordering<ModuleDescriptor> natural();

        /**
         * Creates an Ordering instance that orders weighted module descriptors according to their weight.
         *
         * @see com.atlassian.plugin.web.descriptors.WeightedDescriptorComparator
         * @return An ordering instance that orders module descriptors according to their weight.
         */
        Ordering<WeightedDescriptor> weightedDescriptorComparator();
    }
}
