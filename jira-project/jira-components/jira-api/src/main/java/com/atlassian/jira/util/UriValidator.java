package com.atlassian.jira.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.annotation.Nullable;

import org.apache.log4j.Logger;

/**
 * Verify that a url is not executable
 * Needs to be instantiated to support testing - oh PowerMock wherefore art thou
 *
 * @since v4.2
 */
public class UriValidator {

    private static final Logger LOGGER = Logger.getLogger(UriValidator.class);

    private final String encoding;

    public UriValidator(String encoding) {
        this.encoding = encoding;
    }

    /**
     * This method returns a safe URI - it firstly URL decodes and then only allows HTTP(s) schemes
     *
     * @param canonicalBaseUri The base URI - if no trailing slash one will be provided
     * @param uri              The uri to resolve
     * @return A resolved URI, null if it is an unsuppported scheme, viz. Javascript
     */
    @Nullable
    public String getSafeUri(@Nullable String canonicalBaseUri, String uri) {
        if (uri == null) {
            return null;
        }
        return  buildSafeUri(canonicalBaseUri,uri);
    }

    private String buildSafeUri(String canonicalBaseUri, String uri) {
        URI parsedUri = null;
        try {
            try {
                parsedUri = new URI(decode(uri, encoding));
            }
            catch (URISyntaxException e) {
                parsedUri = new URI(uri);
            }
            if (parsedUri.isOpaque()) {
                return null;
            }
            String scheme = parsedUri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                return uri;
            } else {
                if (mergeUris(canonicalBaseUri,uri) != null) {
                    return uri;
                }
            }
        }
        catch (URISyntaxException e) {
            LOGGER.debug("Cannot parse URI " + uri);
            return null;
        }
        return null;
    }

    /**
     * JIRA ofren starts relative URLs with a leading slash
     * @param canonicalBaseUri
     * @param unsafeUri
     * @return
     * @throws URISyntaxException
     */
    private URI mergeUris(String canonicalBaseUri, String unsafeUri) throws URISyntaxException {
        URI baseUri = canonicalBaseUri.endsWith("/") ? new URI(canonicalBaseUri) : new URI(canonicalBaseUri + "/");
        URI pathUri = unsafeUri.startsWith("/") ? new URI(unsafeUri.substring(1)) : new URI(unsafeUri);
        return baseUri.resolve(pathUri);
    }

    private String decode(String value, String encoding) {
        try {
            return URLDecoder.decode(value, encoding);
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unable to decode '" + value + "' with encoding '" + encoding + "'. Decoding with UTF8.");
            try {
                return URLDecoder.decode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException ignore) {
                return null;
            }
        }
    }
}