package com.atlassian.jira.avatar;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class copied from http://en.gravatar.com/site/implement/images/java/
 */
public class MD5Util
{
    public static String hex(byte[] array)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : array)
        {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
        }

        return sb.toString();
    }

    public static String md5Hex(String message)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException();
        }
    }
}
