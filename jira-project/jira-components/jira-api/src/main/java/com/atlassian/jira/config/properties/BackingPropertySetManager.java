package com.atlassian.jira.config.properties;

import com.atlassian.annotations.PublicApi;
import com.atlassian.util.concurrent.Supplier;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * The PropertySet that backs the PropertiesSet needs to be Switchable - to allow the PicoContainer to
 * manage this switching via ComponentAdapters we need an interface
 *
 * @since v4.4
 */
@PublicApi
public interface BackingPropertySetManager
{
    /**
     *
     * @return  the {@link Supplier} that references the underlying property set
     */
    Supplier<? extends PropertySet> getPropertySetSupplier();

    /**
     *  refresh the underlying property set - this can also cause properties to be lost
     *  if the PropertySet has not been written to a backing store
     */
    void refresh();

    /**
     *  If the backing property set supports switching to different backing storage, then switch - otherwise noop
     *  For now the switch is one way
     */
     void switchBackingStore();
}
