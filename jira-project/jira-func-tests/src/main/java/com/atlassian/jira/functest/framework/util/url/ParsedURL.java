package com.atlassian.jira.functest.framework.util.url;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A class that can parse and normalise a URL and its parameters
 *
 * @since v4.1
 */
public class ParsedURL
{
    private String mUrl;
    private String mProtocol;
    private int mPort;
    private String mHost;
    private String mPath;
    private String mFile;
    private String mQuery;
    private final Map<String, String> simpleQueryParms;
    private final Map<String, List<String>> multiQueryParams;

    /**
     * Construct a parsed URL
     *
     * @param url String
     */
    public ParsedURL(String url)
    {
        mUrl = url == null ? "" : url;
        simpleQueryParms = new TreeMap<String, String>();
        multiQueryParams = new TreeMap<String, List<String>>();
        parse();
    }

    private void parse()
    {
        String r = mUrl;

        // Protocol
        int i = r.indexOf("://");
        if (i < 0)
        {
            if (r.startsWith("/"))
            {
                r = "http://" + r;
            }
            else
            {
                r = "http:///" + r;
            }
            i = r.indexOf("://");
        }
        mProtocol = r.substring(0, i);
        r = r.substring(i + "://".length());

        // host[:port]
        i = r.indexOf('/');
        if (i < 0)
        {
            i = r.indexOf('?');
        }
        String server = i >= 0 ? r.substring(0, i) : r;
        r = i >= 0 ? r.substring(i) : "";

        i = server.indexOf(':');
        if (i >= 0)
        {
            mHost = server.substring(0, i);
            String port = server.substring(i + 1);
            if (port.length() > 0)
            {
                mPort = Integer.parseInt(port);
            }
            else
            {
                mPort = -1;
            }
        }
        else
        {
            mHost = server;
            mPort = -1;
        }

        // path
        if (r.length() > 0)
        {
            mFile = r.substring(0);
        }
        else
        {
            mFile = "";
        }

        // file
        if (!r.startsWith("/"))
        {
            mPath = "";
        }
        else
        {
            i = r.indexOf('?');
            if (i >= 0)
            {
                mPath = r.substring(0, i);
                r = r.substring(i);
            }
            else
            {
                mPath = r;
                r = "";
            }
        }

        // query
        if (r.startsWith("?"))
        {
            mQuery = r.substring(1);
        }
        parseQueryParms();
    }


    private void parseQueryParms()
    {
        String query = getQuery();
        if (query != null)
        {
            String parmBits[] = query.split("&");
            for (String parmBit : parmBits)
            {
                final Matcher matcher = Pattern.compile("(.*)=(.*)").matcher(parmBit);
                if (matcher.find())
                {
                    final String name = matcher.group(1);
                    final String value = matcher.group(2);

                    simpleQueryParms.put(name, value);
                    List<String> values = multiQueryParams.get(name);
                    if (values == null)
                    {
                        values = new ArrayList<String>();
                        multiQueryParams.put(name, values);
                    }
                    values.add(value);
                }
            }
        }
    }

    /**
     * Returns the URL in full string form
     *
     * @return String
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(mProtocol).append("://").append(mHost);
        if (mPort != -1)
        {
            sb.append(":").append(mPort);
        }
        sb.append(mPath);
        int count = 0;
        for (Map.Entry<String, List<String>> entry : multiQueryParams.entrySet())
        {
            final List<String> values = entry.getValue();
            for (String value : values)
            {
                sb.append(count == 0 ? "?" : "&");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(value);
                count++;
            }
        }
        return sb.toString();
    }

    /**
     * Gets the protocol name of this <code>URL</code>.
     *
     * @return the protocol of this <code>URL</code>.
     */
    public String getProtocol()
    {
        return nvl(mProtocol);
    }

    /**
     * Gets the host name of this <code>URL</code>
     *
     * @return the host name of this <code>URL</code>.
     */
    public String getHost()
    {
        return nvl(mHost);
    }

    /**
     * Gets the port number of this <code>URL</code>.
     *
     * @return the port number, or -1 if the port is not set
     */
    public int getPort()
    {
        return mPort;
    }

    /**
     * Gets the file name of this <code>URL</code>. The returned file portion will be the same as
     * <CODE>getPath()</CODE>, plus the concatenation of the value of <CODE>getQuery()</CODE>, if any. If there is
     * no query portion, this method and <CODE>getPath()</CODE> will return identical results.
     *
     * @return the file name of this <code>URL</code>, or an empty string if one does not exist
     */
    public String getFile()
    {
        return nvl(mFile);
    }

    /**
     * Gets the path part of this <code>URL</code>.
     *
     * @return the path part of this <code>URL</code>, or an empty string if one does not exist
     */
    public String getPath()
    {
        return nvl(mPath);
    }


    /**
     * Gets the query part of this <code>URL</code>.
     *
     * @return the query part of this <code>URL</code>, or <CODE>null</CODE> if one does not exist
     */
    public String getQuery()
    {
        return nvl(mQuery);
    }

    /**
     * @return a map of the query parameters, sorted into parameter name order
     */
    public Map<String, String> getQueryParameters()
    {
        return simpleQueryParms;
    }

    /**
     * @param names the query parameter names to include
     * @return a map of the only the named query parameters, sorted into parameter name order
     */
    public Map<String, String> getQueryParameters(String... names)
    {
        if (names == null)
        {
            return Collections.emptyMap();
        }
        Set<String> onlyKeys = new HashSet<String>(Arrays.asList(names));
        Map<String, String> returnMap = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : simpleQueryParms.entrySet())
        {
            if (onlyKeys.contains(entry.getKey()))
            {
                returnMap.put(entry.getKey(), entry.getValue());
            }
        }
        return returnMap;
    }

    /**
     * @param names the query parameter names to ignore
     * 
     * @return a map of the only the named query parameters ignoring the specified keys, sorted into parameter name order
     */
    public Map<String, String> getQueryParametersIgnoring(String... names)
    {
        if (names == null)
        {
            return Collections.emptyMap();
        }
        Set<String> ignoredKeys = new HashSet<String>(Arrays.asList(names));
        Map<String, String> returnMap = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : simpleQueryParms.entrySet())
        {
            if (!ignoredKeys.contains(entry.getKey()))
            {
                returnMap.put(entry.getKey(), entry.getValue());
            }
        }
        return returnMap;
    }

    /**
     * @return the values of the named query parameter
     */
    public String getQueryParameter(String name)
    {
        return simpleQueryParms.get(name);
    }

    public Map<String, List<String>> getMultiQueryParameters()
    {
        return multiQueryParams;
    }

    public Map<String, List<String>> getMultiQueryParameters(String... names)
    {
        if (names == null)
        {
            return Collections.emptyMap();
        }
        Set<String> onlyKeys = new HashSet<String>(Arrays.asList(names));
        Map<String, List<String>> returnMap = new TreeMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : multiQueryParams.entrySet())
        {
            if (onlyKeys.contains(entry.getKey()))
            {
                returnMap.put(entry.getKey(), entry.getValue());
            }
        }
        return returnMap;
    }

    public Map<String, List<String>> getMultiQueryParametersIgnoring(String... names)
    {
        if (names == null)
        {
            return Collections.emptyMap();
        }
        Set<String> ignoredKeys = new HashSet<String>(Arrays.asList(names));
        Map<String, List<String>> returnMap = new TreeMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : multiQueryParams.entrySet())
        {
            if (!ignoredKeys.contains(entry.getKey()))
            {
                returnMap.put(entry.getKey(), entry.getValue());
            }
        }
        return returnMap;
    }

    public List<String> getMultiQueryParameter(String name)
    {
        return multiQueryParams.get(name);
    }

    private String nvl(final String s)
    {
        return s == null ? "" : s;
    }
}
