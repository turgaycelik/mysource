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

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index.UpdateMode;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

///CLOVER:OFF
class MockWriter implements Writer
{
    public void addDocument(final Document document) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void addDocuments(final Collection<Document> document) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void close()
    {
        throw new UnsupportedOperationException();
    }

    public void deleteDocuments(final Term identifyingTerm) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void commit()
    {
        throw new UnsupportedOperationException();
    }

    public void optimize() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void setMode(final UpdateMode mode)
    {
        throw new UnsupportedOperationException();
    }

    public void updateDocument(final Term identifyingTerm, final Document document) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDocumentConditionally(@Nonnull Term identifyingTerm, @Nonnull Document document, @Nonnull String optimisticLockField)
            throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void updateDocuments(final Term identifyingTerm, final Collection<Document> document) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public void clearWriteLock()
    {
        throw new UnsupportedOperationException();
    }
}
///CLOVER:ON
