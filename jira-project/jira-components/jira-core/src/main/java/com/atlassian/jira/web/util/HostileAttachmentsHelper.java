package com.atlassian.jira.web.util;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.util.IOUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * A tool for loading and encapsulating the local policy for which MIME Content Types and file extensions may contain
 * active executable client-side content and which therefore should be treated carefully to avoid XSS attacks via
 * uploading these files as attachments.
 * <p/>
 * Browsers use Content-Type headers and file extensions to decide whether to attempt to execute a file in a client
 * context. Examples include javascript in html and ActionScript in .swf (Flash) binaries. Since these runtimes have
 * access to the client-side state of the browser, they represent a potential means to steal session cookie contents and
 * other XSS attacks.
 *
 * @since v3.13
 */
public class HostileAttachmentsHelper implements Serializable
{
    private static final Logger log = Logger.getLogger(HostileAttachmentsHelper.class);

    /**
     * File extensions and content types in the config file are parsed from a list of items delimited by this regex.
     */
    public static final String DELIMITER_REGEX = "\\s+";

    /**
     * Property key for list of executable MIME types.
     */
    static final String KEY_EXECUTABLE_CONTENT_TYPES = "executable.mime.types";

    /**
     * Property key for list of executable file extensions.
     */
    static final String KEY_EXECUTABLE_FILE_EXTENSIONS = "executable.file.extensions";

    /**
     * The name of the configuration file.
     */
    private static final String CONFIG_FILE = "hostile-attachments-config.properties";

    /**
     * http://www.flashcomguru.com/index.cfm/2007/11/13/flash-video-mime-types
     * http://www.kaourantin.net/2007/10/new-file-extensions-and-mime-types.html
     */
    private static final String[] DEFAULT_EXECUTABLE_FILE_EXTENSIONS = new String[] {
            ".htm", ".html", ".xhtml", ".xml",
            ".svg", ".swf", ".cab", ".flv", ".f4v", ".f4p", ".f4a", ".f4b"
    };

    /**
     * http://www.w3.org/TR/xhtml-media-types/
     * http://www.rfc-editor.org/rfc/rfc3023.txt
     * http://kb.adobe.com/selfservice/viewContent.do?externalId=tn_4151
     */
    private static final String[] DEFAULT_EXECUTABLE_CONTENT_TYPES = new String[] {
            "text/html", "text/html-sandboxed", "text/xhtml", "application/xhtml+xml",
            "text/xml", "application/xml", "text/xml-external-parsed-entity", "application/xml-external-parsed-entity", "application/xml-dtd",
            "application/x-shockwave-flash", "image/svg+xml", "image/svg-xml", "application/futuresplash"
    };

    private String[] executableFileExtensions = DEFAULT_EXECUTABLE_FILE_EXTENSIONS;

    private String[] executableContentTypes = DEFAULT_EXECUTABLE_CONTENT_TYPES;


    public HostileAttachmentsHelper()
    {
        loadConfiguration();
    }

    /**
     * Loads the configuration of what are executable file extensions and content types from the default configuration
     * file, {@link #CONFIG_FILE}.
     *
     * @throws IOException if there is a problem loading from the default configuration file.
     */
    private void loadConfiguration()
    {
        final Properties config = new Properties();
        final InputStream in = ClassLoaderUtils.getResourceAsStream(CONFIG_FILE, getClass());
        if (in != null)
        {
            try
            {
                config.load(in);
            }
            catch (IOException e)
            {
                log.warn("Unable to load config from '" + CONFIG_FILE + "' falling back to defaults ");
                return;
            }
            finally
            {
                IOUtil.shutdownStream(in);
            }
            parseConfiguration(config);
        }
        else
        {
            log.warn("Unable to load config from '" + CONFIG_FILE + "' falling back to defaults ");
        }
    }

    /**
     * Parses the lists of executable content types and file extensions from their respective config property values.
     *
     * @param config the Properties that contains the config.
     */
    void parseConfiguration(final Properties config)
    {
        final String extensions = config.getProperty(KEY_EXECUTABLE_FILE_EXTENSIONS);
        if (log.isDebugEnabled())
        {
            log.debug("Configured executable file extensions: '" + extensions + "'");
        }
        if (!StringUtils.isBlank(extensions))
        {
            executableFileExtensions = extensions.trim().split(DELIMITER_REGEX);
        }

        final String contentTypes = config.getProperty(KEY_EXECUTABLE_CONTENT_TYPES);
        if (log.isDebugEnabled())
        {
            log.debug("Executable content types: '" + contentTypes + "'");
        }
        if (!StringUtils.isBlank(contentTypes))
        {
            executableContentTypes = contentTypes.trim().split(DELIMITER_REGEX);
        }

    }

    /**
     * Determines if the given String has an extension denoting a client-executable active content type such that if the
     * browser opens the file, its execution could have access to the browser DOM etc. Examples include .html, .svg and
     * .swf. Note the check is case insensitive.
     *
     * @param name the file name.
     * @return true only if the name has one of the configured extensions.
     */
    public boolean isExecutableFileExtension(final String name)
    {
        boolean isExecutableFileExtension = false;
        if (!StringUtils.isBlank(name))
        {
            for (String executableFileExtension : executableFileExtensions)
            {
                if (endsWithIgnoreCase(name, executableFileExtension))
                {
                    isExecutableFileExtension = true;
                    break;
                }
            }
        }
        return isExecutableFileExtension;
    }

    /**
     * Determines if the given String is a MIME Content Type denoting client-executable active content such that if the
     * browser opens the file, its execution could have access to the browser DOM etc. E.g. text/html Note the check is
     * case insensitive.
     *
     * @param contentType the MIME Content Type string.
     * @return true only if the given contentType is one of the configured executable Content Types.
     */
    public boolean isExecutableContentType(final String contentType)
    {
        boolean isExecutableContentType = false;
        if (!StringUtils.isBlank(contentType))
        {
            for (String executableContentType : executableContentTypes)
            {
                if (executableContentType.equalsIgnoreCase(contentType))
                {
                    isExecutableContentType = true;
                    break;
                }
            }
        }
        return isExecutableContentType;
    }

    /**
     * Returns true if the given stringToSearch ends with the candidateSuffix ignoring case.
     *
     * @param stringToSearch the superstring.
     * @param candidateSuffix the suffix being searched for.
     * @return true only if stringToSearch ends with candidateSuffix ignoring case.
     */
    boolean endsWithIgnoreCase(final String stringToSearch, final String candidateSuffix)
    {
        final int stsLen = stringToSearch.length();
        final int csLen = candidateSuffix.length();
        return ((stsLen >= csLen) && stringToSearch.substring(stsLen - csLen, stsLen).equalsIgnoreCase(candidateSuffix));
    }

    String[] getExecutableFileExtensions()
    {
        return executableFileExtensions;
    }

    String[] getExecutableContentTypes()
    {
        return executableContentTypes;
    }
}
