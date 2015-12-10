package com.atlassian.jira.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.apache.commons.codec.binary.Base64;

/**
 * Basic slurp implementation of an InputStream Consumer which provides Base64 encoded output.
 * Note that this reads the whole input into memory and should not be used for data that may grow big.
 *
 * @since v4.0
 */
public class Base64InputStreamConsumer implements Consumer<InputStream>
{
    private String encoded;
    private boolean chunked;

    /**
     * Creates this encoder. Set chunked to true if you want a 72 character formatted ("chunked")
     * String or false if you don't need formatting.
     * @param chunked whether you want a chunked Base64 encoded output.
     */
    public Base64InputStreamConsumer(boolean chunked)
    {
        this.chunked = chunked;
        this.encoded = "";
    }

    public void consume(@Nonnull final InputStream element)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            IOUtil.copy(element, baos);
            encoded = new String(Base64.encodeBase64(baos.toByteArray(), chunked), "UTF-8");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives the Base64 encoded String only if the consume method ran without exception. Otherwise returns the empty
     * String.
     * @return the empty String or the base64 encoded content of the InputStream.
     */
    public String getEncoded() {
        return encoded;
    }
}
