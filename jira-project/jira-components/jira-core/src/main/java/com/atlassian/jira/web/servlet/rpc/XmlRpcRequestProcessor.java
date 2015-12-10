package com.atlassian.jira.web.servlet.rpc;

import java.io.InputStream;

/**
 * Component responsible for processing XML-RPC requests.
 *
 * @since v4.4
 */
public interface XmlRpcRequestProcessor {

    /**
     * Execute given XML-RPC <tt>request</tt>.
     *
     * @param request XML-RPC request to execute
     * @return XML-RPC-comppatible response as byte array
     */
    public byte[] process(InputStream request);
}
