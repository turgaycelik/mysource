package com.atlassian.jira.sharing;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSharedEntityAccessorFactory implements SharedEntityAccessor.Factory
{
    @ClusterSafe
    private final LazyReference<Map<TypeDescriptor<?>, SharedEntityAccessor<?>>> adjusters = new LazyReference<Map<TypeDescriptor<?>, SharedEntityAccessor<?>>>()
    {
        @Override
        protected Map<TypeDescriptor<?>, SharedEntityAccessor<?>> create() throws Exception
        {
            final Map<TypeDescriptor<?>, SharedEntityAccessor<?>> localAdjusters = new HashMap<TypeDescriptor<?>, SharedEntityAccessor<?>>();

            // NOTE: The following managers should be obtained through the Component manager to reduce the chance of circular
            // dependencies. This commonly occurs when the manager that implements the SharedEntityAccessor also has
            // a dependency on the ShareSearcher.

            for (final SharedEntityAccessor<?> sharedEntityAccessor : getAccessors())
            {
                localAdjusters.put(sharedEntityAccessor.getType(), sharedEntityAccessor);
            }
            return Collections.unmodifiableMap(localAdjusters);
        }
    };

    public <S extends SharedEntity> SharedEntityAccessor<S> getSharedEntityAccessor(final SharedEntity.TypeDescriptor<S> type)
    {
        if (type == null)
        {
            return null;
        }
        @SuppressWarnings("all")
        final SharedEntityAccessor<S> sharedEntityAccessor = (SharedEntityAccessor<S>) adjusters.get().get(type);
        return sharedEntityAccessor;
    }

    public <S extends SharedEntity> SharedEntityAccessor<S> getSharedEntityAccessor(final String type)
    {
        if (StringUtils.isBlank(type))
        {
            return null;
        }
        else
        {
            return getSharedEntityAccessor(new TypeDescriptor<S>(type));
        }
    }

    ///CLOVER:OFF
    @SuppressWarnings("unchecked")
    List<SharedEntityAccessor> getAccessors()
    {
        return ComponentManager.getComponentsOfType(SharedEntityAccessor.class);
    }
    ///CLOVER:ON
}
