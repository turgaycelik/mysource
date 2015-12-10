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

package com.atlassian.jira.index;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

///CLOVER:OFF
class MockFuture<T> implements Future<T>
{
    public T get() throws InterruptedException, ExecutionException
    {
        throw new UnsupportedOperationException();
    }

    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        throw new UnsupportedOperationException();
    }

    public boolean isCancelled()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isDone()
    {
        throw new UnsupportedOperationException();
    }

    public boolean cancel(final boolean mayInterruptIfRunning)
    {
        throw new UnsupportedOperationException();
    }
}
///CLOVER:ON
