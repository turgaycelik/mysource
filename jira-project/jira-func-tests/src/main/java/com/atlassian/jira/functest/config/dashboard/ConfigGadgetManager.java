package com.atlassian.jira.functest.config.dashboard;

import java.util.Collection;
import java.util.List;

/**
 * The CRUD operations for the {@link ConfigGadget}.
 *
 * @since v4.2
 */
public interface ConfigGadgetManager
{
    List<ConfigGadget> loadGadgets();
    boolean saveGadgets(Collection<? extends ConfigGadget> gadgets);

    List<ConfigExternalGadget> loadExternalGadgets();
    boolean saveExternalGadgets(Collection<? extends ConfigExternalGadget> gadgets);    
}
