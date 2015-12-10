package com.atlassian.jira.util.io;

import com.atlassian.annotations.PublicSpi;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementors consume an {@link java.io.InputStream}.
 * <p/>
 * Typical usage pattern for methods that accept a consumer is to pass in an anonymous implementation of this interface:
 * <p/>
 * <pre>
 *      something.withConsumer(new InputStreamConsumer<Boolean>(){
 *          @Override
 *          public Boolean withInputStream(InputStream is)  throws IOException {
 *              // do something with the InputStream
 *              return true;
 *          }
 *      });
 * </pre>
 * <p/>
 * The {@code withConsumer} method can then easily ensure that the {@link java.io.InputStream}
 * will be closed after the consumer is done with it.
 * <p/>
 * <pre>
 *   private <T> T withConsumer(final InputStreamConsumer<T> sc) {
 *      InputStream inputStream = new BufferedInputStream(<something something>);
 *      try {
 *          return sc.withInputStream(inputStream);
 *      } catch (IOException e) {
 *          throw new ThumbnailRenderException(e);
 *      } finally {
 *          IOUtils.closeQuietly(inputStream);
 *      }
 *  }
 * </pre>
 *
 * @param <T>
 *
 * @since 4.4
 */
@PublicSpi
public interface InputStreamConsumer<T>
{
    T withInputStream(InputStream is) throws IOException;
}
