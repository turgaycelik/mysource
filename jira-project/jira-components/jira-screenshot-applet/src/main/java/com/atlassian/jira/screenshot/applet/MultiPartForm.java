/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.screenshot.applet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hchirino
 *         <p/>
 *         To change this generated comment edit the template variable "typecomment":
 *         Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class MultiPartForm
{
    private ArrayList fields = new ArrayList();
    private String boundary;
    private final String encoding;

    public MultiPartForm(String encoding)
    {
        this.encoding = encoding;
    }

    public void addPart(String name, String fileName, String type, byte[] value) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String t = "Content-Disposition: form-data; name=\"" + name + "\"";
        if (fileName != null)
            t += "; filename=\"" + fileName + "\"";
        t += "\r\n";

        byte[] line = encodeString(t);
        os.write(line);
        if (type != null)
        {
            line = encodeString("Content-Type: " + type + "\r\n");
            os.write(line);
        }
        line = encodeString("\r\n");
        os.write(line);

        os.write(value);

        line = encodeString("\r\n");
        os.write(line);
        fields.add(os.toByteArray());
    }

    public byte[] toByteArray() throws IOException
    {
        this.boundary = findBoundary();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Iterator iterator = fields.iterator();
        while (iterator.hasNext())
        {
            byte[] value = (byte[]) iterator.next();
            writeBoundary(os);
            os.write(value);
        }
        writeEndBoundary(os);
        return os.toByteArray();
    }

    private void writeBoundary(OutputStream os) throws IOException
    {
        byte[] line = encodeString("--" + boundary + "\r\n");
        os.write(line);
    }

    private void writeEndBoundary(OutputStream os) throws IOException
    {
        byte[] line = encodeString("--" + boundary + "--" + "\r\n");
        os.write(line);
    }

    /**
     * Method findBoundary.
     *
     * @return String
     */
    private String findBoundary()
    {
        try
        {
            long test = System.currentTimeMillis();
            while (true)
            {
                String s = Long.toHexString(test);
                byte[] d = encodeString("--" + s);
                if (!find(d))
                    return s;
                test++;
            }
        }
        catch (UnsupportedEncodingException e)
        {
        }
        return null;
    }

    /**
     * Method find.
     *
     * @param needle
     * @return boolean
     */
    private boolean find(byte[] needle)
    {
        byte[][] data = new byte[fields.size()][];
        Iterator iterator = fields.iterator();
        for (int i = 0; iterator.hasNext(); i++)
            data[i] = (byte[]) iterator.next();

        for (final byte[] aData : data)
        {
            for (int j = 0; j < (aData.length - needle.length); j++)
            {
                for (int k = 0; k < needle.length; k++)
                {
                    if (aData[j + k] == needle[k])
                    {
                        if (k == needle.length - 1)
                        {
                            return true;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the boundary.
     *
     * @return String
     */
    public String getBoundary()
    {
        return boundary;
    }

    /**
     * Converts the given string to an array of bytes in the given encoding
     *
     * @param s
     * @throws UnsupportedEncodingException
     */
    private byte[] encodeString(String s) throws UnsupportedEncodingException
    {
        return s.getBytes(encoding);
    }
}
