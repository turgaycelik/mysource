package com.atlassian.jira.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * This class converts the URI query parameters into a map.
 */
public class UriQueryParser
{
    private static final Pattern correctness = Pattern.compile("(([^&=]+)=([^&]*))?(&([^&=]+)=([^&]*))*");
    private static final Pattern extractor = Pattern.compile("([^&=]+)=([^&]*)");

    public Map<String, String> parse(final URI uri) throws URISyntaxException
    {
        return parse(uri.getQuery() == null ? "" : uri.getQuery());
    }

    public Map<String, String> parse(final String urlQueryParametersString) throws URISyntaxException
    {
        if (!validate(urlQueryParametersString))
        {
            throw new URISyntaxException(urlQueryParametersString, "Not a query parameter list.");
        }

        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        final Scanner scanner = new Scanner(urlQueryParametersString.trim());
        while (scanner.findWithinHorizon(extractor, 0) != null)
        {
            builder.put(scanner.match().group(1), scanner.match().group(2));
        }
        return builder.build();
    }

    @VisibleForTesting
    boolean validate(final String urlQueryParametersString)
    {
        return correctness.matcher(urlQueryParametersString.trim()).matches();
    }
}
