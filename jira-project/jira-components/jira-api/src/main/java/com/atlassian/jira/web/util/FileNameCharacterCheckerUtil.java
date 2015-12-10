package com.atlassian.jira.web.util;

import java.util.Arrays;

/**
 * This is a centralized object for checking illegal characters in attachment file names. This is used by the
 * AttachmentManager, the Screenshot Applet, the Abstract Message Handler and others.
 */
public class FileNameCharacterCheckerUtil
{
    // JRA-16726 - I have investigated this and apparently only Windows is cranky and will not allow you to
    // create file names with these characters. Unix/Mac will not allow the ':'. It is suggested that '$' and '%'
    // can be confusing when used in network paths but this is not really an issue for our attachments.
    private static final char[] INVALID_CHARS = { '\\', '/','\"', ':','?', '*', '<','|','>' };

    /**
     * This will test the given filename string for any invalid characters. If it contains an invalid
     * character then the string returned will be the character. A return value of null means that the
     * string is a valid filename. if the filename is null it will return null for the caller to handle
     * the null filename.
     * @param filename the filename to be checked.
     * @return null if the filename is valid, else the character that is invalid.
     */
    public String assertFileNameDoesNotContainInvalidChars(String filename)
    {
        if (filename == null)
        {
            return null;
        }
        for (char invalidChar : INVALID_CHARS)
        {
            if (filename.indexOf(invalidChar) != -1)
            {
                return String.valueOf(invalidChar);
            }
        }
        return null;
    }

    /**
     * Replaces each invalid character of the given filename with the replacementChar. If the filename is null, returns
     * null so that the caller can handle the null filename
     * @param filename file name to replace invalid characters from
     * @param replacementChar character to replace invalid characters
     * @return new filename with valid characters
     * @throws IllegalArgumentException if replacementChar is an invalid character itself
     */
    public String replaceInvalidChars(String filename, char replacementChar)
    {
        if (assertFileNameDoesNotContainInvalidChars(String.valueOf(replacementChar)) != null)
        {
            throw new IllegalArgumentException("Replacement character '" + replacementChar + "' is invalid");
        }

        if (filename == null)
        {
            return null;
        }

        for (char invalidChar : INVALID_CHARS)
        {
            filename = filename.replace(invalidChar, replacementChar);
        }
        return filename;
    }

    public String getPrintableInvalidCharacters()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < INVALID_CHARS.length; i++)
        {
            char invalidChar = INVALID_CHARS[i];
            sb.append("'");
            sb.append(invalidChar);
            sb.append("'");
            if ((i + 1) < INVALID_CHARS.length)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static char[] getInvalidCharacters()
    {
        return Arrays.copyOf(INVALID_CHARS, INVALID_CHARS.length);
    }
}
