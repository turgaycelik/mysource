package com.atlassian.jira.dev.reference.plugin.rpc;

/**
 * Reference XML-RPC service interface.
 *
 * @since v4.4
 */
public interface ReferenceXmlRpcService
{

    /**
     * Get version of the plugin this service is running in.
     *
     * @return reference plugin version
     */
    String getVersion();
}
