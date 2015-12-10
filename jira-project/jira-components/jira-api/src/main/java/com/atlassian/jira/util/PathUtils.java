package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * This class contains utility methods for manipulating paths.
 *
 * @since v4.3
 */
public class PathUtils
{
    private PathUtils()
    {
        // don't instantiate
    }

    public static String appendFileSeparator(final String filePath)
    {
        return (filePath == null) ? null : (filePath.endsWith("/") || filePath.endsWith("\\") ? filePath : filePath + java.io.File.separator);
    }

    public static String joinPaths(final String... paths)
    {
        return StringUtils.join(paths, File.separator);
    }

    /**
     * Checks if using <code>untrustedPath</code> would result in path traversal. We consider that a path traversal
     * occurs if the file or directory refered to in <code>untrustedPath</code> is not contained in
     * <code>secureDir</code>. This method uses the technique described in <a href="https://www.securecoding.cert.org/confluence/x/S4EVAQ">IDS02-J</a>
     * in the CERT Secure Coding Standard.
     * <p/>
     * Note that <b><code>secureDir</code> is assumed to be secure</b>, so this parameter must never contain
     * user-supplied input.
     *
     * @param secureDir a String containing the path to a "secure" base directory
     * @param untrustedPath a String containing a path that was built using user-supplied input (relative or absolute)
     * @throws PathTraversalException if <code>untrustedPath</code> is not below <code>secureDir</code>
     * @throws java.io.IOException if there is an I/O problem calling <code>java.io.File.getCanonicalPath()</code>
     */
    public static void ensurePathInSecureDir(String secureDir, String untrustedPath)
            throws PathTraversalException, IOException
    {
        if (!isPathInSecureDir(secureDir, untrustedPath))
        {
            throw new PathTraversalException();
        }
    }

    /**
     * Checks if using <code>untrustedPath</code> would result in path traversal. We consider that a path traversal
     * occurs if the file or directory refered to in <code>untrustedPath</code> is not contained in
     * <code>secureDir</code>. This method uses the technique described in <a href="https://www.securecoding.cert.org/confluence/x/S4EVAQ">IDS02-J</a>
     * in the CERT Secure Coding Standard.
     * <p/>
     * Note that <b><code>secureDir</code> is assumed to be secure</b>, so this parameter must never contain
     * user-supplied input.
     *
     * @param secureDir a String containing the path to a "secure" base directory
     * @param untrustedPath a String containing a path that was built using user-supplied input (relative or absolute)
     * @return a boolean indicating whether {@code untrustedPath} in {@code secureDir}
     * @throws java.io.IOException if there is an I/O problem calling <code>java.io.File.getCanonicalPath()</code>
     */
    public static boolean isPathInSecureDir(String secureDir, String untrustedPath)
            throws IOException
    {
        String canonicalSecureDir = new File(secureDir).getCanonicalPath();
        String canonicalUntrustedPath = new File(untrustedPath).getCanonicalPath();

        return canonicalUntrustedPath.startsWith(canonicalSecureDir);
    }
}
