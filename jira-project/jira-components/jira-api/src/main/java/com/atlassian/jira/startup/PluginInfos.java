package com.atlassian.jira.startup;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable collection of PluginInfo.
 *
 * @since v5.0
 */
@Immutable
public interface PluginInfos extends Iterable<PluginInfo>
{
    /**
     * Returns the name of this PluginInfos (e.g. "User Plugins", "System Plugins").
     *
     * @return a String containing the name of this PluginInfos
     */
    String name();

    /**
     * Returns the size of this PluginInfos.
     *
     * @return the number of PluginInfo instances in this instance
     */
    int size();

    /**
     * Returns a String describing this PluginInfos in human-readable form. The text will include a header containing
     * this PluginInfo's name, followed by a list of plugins. Sample output:
     * <p/>
     * <pre>
     * ___ User Plugins ____________________________
     *
     *     Number                                        : 1
     *
     *     JIRA Charting Plugin                          : com.atlassian.jira.ext.charting
     *          Version                                       : 1.8
     *          Status                                        : enabled
     *          Vendor                                        : Atlassian
     *          Description                                   : JIRA Plugin for graphical charts and graphs.
     * </pre>
     *
     * @return a String
     */
    String prettyPrint();
}
