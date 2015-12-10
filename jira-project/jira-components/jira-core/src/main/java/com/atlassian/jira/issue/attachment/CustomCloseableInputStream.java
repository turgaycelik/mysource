package com.atlassian.jira.issue.attachment;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Effect;

import com.google.common.base.Preconditions;

import org.apache.commons.io.input.ProxyInputStream;

/**
 * A proxy input stream that allows for custom behaviour when the close() method is called.
 *
 * @since v6.3
 */
public class CustomCloseableInputStream extends ProxyInputStream
{
    private Effect<InputStream> onClose;

    public CustomCloseableInputStream(@Nonnull final InputStream proxy, @Nonnull final Effect<InputStream> onClose)
    {
        super(proxy);
        Preconditions.checkNotNull(proxy);
        Preconditions.checkNotNull(proxy);
        this.onClose = onClose;
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            super.close();
        }
        finally
        {
            onClose.apply(this.in);
        }
    }
}