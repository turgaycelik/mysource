/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.meterware.httpunit;

import java.io.IOException;

public class WebResponseUtil
{
    /**
     * This method takes a given webResponse and replaces the content type with the one supplied.
     * This is useful for writing func tests that look at Excel or Word HTML which do not have the 'text/html'
     * content type. HTTPUnit can only deal with 'text/html' content types.
     * <p/>
     * This needs to be in teh com.meterware.httpunit package, since the replaceText method() only has package level
     * access.
     *
     * @param webResponse The webResponse object returned by the request.
     * @param contentType The new content type.
     * @return True if the replace was successful.  False if the webResponse could not be parsed.
     */
    public static boolean replaceResponseContentType(WebResponse webResponse, String contentType)
    {
        try
        {
            return webResponse.replaceText(webResponse.getText(), contentType);
        }
        catch (IOException e)
        {
            //Should really never get into here, as the webResponse should alreay have the text loaded.
            return false;
        }
    }
}
