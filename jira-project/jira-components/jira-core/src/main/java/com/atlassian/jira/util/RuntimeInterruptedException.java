/**
 * Copyright 2008 Atlassian Pty Ltd 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.atlassian.jira.util;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Convenience class for re-throwing {@link InterruptedException}. Sets the
 * {@link Thread#interrupted()} flag to true.
 */
public class RuntimeInterruptedException extends RuntimeException
{

    private static final long serialVersionUID = -5025209597479375477L;

    public RuntimeInterruptedException(final InterruptedException cause)
    {
        super(notNull("cause", cause));
    }

    public RuntimeInterruptedException(final String message, final InterruptedException cause)
    {
        super(message, notNull("cause", cause));
    }

    @Override
    public InterruptedException getCause()
    {
        return (InterruptedException) super.getCause();
    }
}