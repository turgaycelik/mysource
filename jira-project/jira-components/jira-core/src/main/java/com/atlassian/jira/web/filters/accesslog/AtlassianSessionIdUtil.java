package com.atlassian.jira.web.filters.accesslog;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * You can use this to generate a hash of the JSESSIONID so we can get session specific information into logs without
 * giving away the keys to the house!
 *
 * @since v3.13.2
 */
public class AtlassianSessionIdUtil
{
    public static final String ASESSIONID_NAME = "ASESSIONID";
    public static final String JSESSIONID_NAME = "JSESSIONID";

    private static final Logger log = Logger.getLogger(AtlassianSessionIdUtil.class);

    /**
     * This will look for an existing HTTP session and if its present, it will generate a ASESSIONID base on a hash of
     * the session's id.
     * <p/>
     * It will check in the session for such a beast, and hence only calculate it once and its lifecycle is therefore
     * properly tied to the session itself.
     * <p/>
     * We can then safely put this ASESSIONID in the logs and let someone upstream, say in a proxy, examine it.
     * <p/>
     * CALLING NOTES - This method MUST be invoked before any content output is sent to the client.  It sets cookies
     * etc. and hence it needs the response to be be in a suitable state.  It is expected that this method will be
     * called early on in say a filter.
     *
     * @param httpServletRequest  the HTTP request
     * @param httpServletResponse the HTTP response
     *
     * @return the atlassian session id hash or null if there is no session (it does not exist) or if the hash of the
     *         session id cannot be generated (e.g. if the sessions id is null, which should not happen)
     */
    public static String generateAtlassianSessionHash(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        String ASESSIONID;

        // Without creating a session, have we got an ASESSIONID value available?
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null)
        {
            // Since there is no session, we should not generate a new ASESSIONID cookie
            return null;
        }
        else
        {
            // Retrieve the ASESSIONID value from the session
            ASESSIONID = (String) session.getAttribute(ASESSIONID_NAME);
        }

        // If we do not have a ASESSIONID stored, but have a session then we need to generate ASESSIONID and store it in the session
        if (ASESSIONID == null)
        {
            // Lets make a hash of the session's id
            ASESSIONID = generateNewASESSIONID(session);
        }

        //
        // we only allow the digest value part to escape this function
        return getDigestEncodedValue(ASESSIONID);
    }

    private static String generateNewASESSIONID(HttpSession session)
    {
        final String sessionId = session.getId();
        String ASESSIONID = generateASESSIONID(sessionId);

        // If generated ASESSIONID is null then we should print a warning and not do anything
        if (ASESSIONID == null)
        {
            // Print with debug severity as if this happens it is likely to happen with every request, and we do not
            // want to fill up the logs
            if (log.isDebugEnabled())
            {
                log.debug("Session with id '" + sessionId + "' generated a null hash. Not setting ASESSIONID cookie or header.");
            }
            return null;
        }
        final String sessionIdHex = smartHexEncode(sessionId, getinUTF8(sessionId));

        ASESSIONID = ASESSIONID + '-' + sessionIdHex;

        // Store the value in the session so that we do not have to calculate it again
        session.setAttribute(ASESSIONID_NAME, ASESSIONID);
        return ASESSIONID;

    }

    /**
     * This will generate a hashed version of the passed in HttpSession sessionId
     * <p/>
     * It will be returned in the form <i>digestEncodedValue</i>-<i>hexEncodedValue</i>
     *
     * @param sessionId the session id string
     *
     * @return a hashed version of that session string or null if the input is null
     */
    public static String generateASESSIONID(final String sessionId)
    {
        if (sessionId == null)
        {
            return null;
        }
        final byte[] bytes = getinUTF8(sessionId);

        MessageDigest md = getMessageDigest("SHA");
        if (md == null)
        {
            md = getMessageDigest("MD5");
        }
        if (md == null)
        {
            return null;
        }
        md.update(bytes);

        byte[] digest = md.digest();
        return encode(digest);
    }

    /**
     * Takes a generated ASESSION id value in the form <i>digestEncodedValue</i>-<i>hexEncodedValue</i> and returns the
     * digest encoded part
     *
     * @param asessionId the full asessionId in play
     *
     * @return null if its blank or the part before last '-' character
     */
    private static String getDigestEncodedValue(final String asessionId)
    {
        if (StringUtils.isBlank(asessionId))
        {
            return null;
        }
        int index = asessionId.lastIndexOf('-');
        if (index == -1 || index == 0)
        {
            return null;
        }
        return asessionId.substring(0, index);
    }

    private static byte[] getinUTF8(final String sessionId)
    {
        byte[] input = null;
        try
        {
            input = sessionId.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // just not going to happen since every implementation of the Java platform is required to support UTF-8
            //
            // if you don't believe me see http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html
        }
        return input;
    }

    private static String smartHexEncode(final String sessionId, final byte[] bytes)
    {
        // some apps servers such as Tomcat give us the sessionId already in hex so lets double check that
        boolean isAllHex = true;
        for (char c : sessionId.toCharArray())
        {
            if (!isHex(c))
            {
                isAllHex = false;
                break;
            }
        }
        if (isAllHex)
        {
            return sessionId;
        }
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes)
        {
            sb.append(Integer.toHexString(aByte));
        }
        return sb.toString();
    }

    private static boolean isHex(final char c)
    {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static String encode(final byte[] bytes)
    {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return Long.toString(crc32.getValue(), Character.MAX_RADIX);
    }

    private static MessageDigest getMessageDigest(String digestName)
    {
        try
        {
            return MessageDigest.getInstance(digestName);
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
    }
}
