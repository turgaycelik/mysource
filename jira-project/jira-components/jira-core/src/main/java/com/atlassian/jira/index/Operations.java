package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.index.Index.UpdateMode;
import javax.annotation.Nonnull;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class Operations
{
    public static Operation newDelete(@Nonnull final Term term, @Nonnull final UpdateMode mode)
    {
        return new Delete(term, mode);
    }

    public static Operation newCreate(@Nonnull final Document document, @Nonnull final UpdateMode mode)
    {
        return new Create(document, mode);
    }

    public static Operation newCreate(@Nonnull final Collection<Document> documents, @Nonnull final UpdateMode mode)
    {
        return new Create(documents, mode);
    }

    public static Operation newUpdate(@Nonnull final Term term, @Nonnull final Document document, @Nonnull final UpdateMode mode)
    {
        return new Update(term, document, mode);
    }

    public static Operation newConditionalUpdate(@Nonnull final Term term, @Nonnull final Document document, @Nonnull final UpdateMode mode, @Nonnull final String optimisticLockField)
    {
        return new ConditionalUpdate(term, document, mode, optimisticLockField);
    }

    public static Operation newUpdate(@Nonnull final Term term, @Nonnull final Collection<Document> documents, @Nonnull final UpdateMode mode)
    {
        return new Update(term, documents, mode);
    }

    public static Operation newOptimize()
    {
        return new Optimize();
    }

    /**
     * Create an operation that delegates to another Operation and then runs the supplied
     * completionJob as soon as the operation is performed (in whatever thread the operation
     * is performed in).
     *
     * @param operation the operation to delegate the actual work to.
     * @param completionJob the Runnable instance that is run after the supplied operation completes.
     * @return the new composite operation.
     */
    public static Operation newCompletionDelegate(final Operation operation, final Runnable completionJob)
    {
        return new Completion(operation, completionJob);
    }

    ///CLOVER:OFF
    private Operations()
    {
        throw new AssertionError("cannot instantiate!");
    }

    ///CLOVER:ON

    /**
     * Holds a an identifying {@link Term} so we can delete pre-existing documents.
     */
    static final class Delete extends Operation
    {
        private final Term term;
        private final UpdateMode mode;

        Delete(@Nonnull final Term term, @Nonnull final UpdateMode mode)
        {
            this.term = notNull("term", term);
            this.mode = notNull("mode", mode);
        }

        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            writer.deleteDocuments(term);
        }

        @Override
        UpdateMode mode()
        {
            return mode;
        }
    }

    /**
     * Holds {@link Document documents} to be created.
     */
    static final class Create extends Operation
    {
        private final List<Document> documents;
        private final UpdateMode mode;

        Create(@Nonnull final Document document, @Nonnull final UpdateMode mode)
        {
            documents = Collections.unmodifiableList(new ArrayList<Document>(Arrays.asList(notNull("document", document))));

            this.mode = notNull("mode", mode);
        }

        Create(@Nonnull final Collection<Document> documents, @Nonnull final UpdateMode mode)
        {
            notNull("documents", documents);
            this.documents = Collections.unmodifiableList(new ArrayList<Document>(documents));
            this.mode = notNull("mode", mode);
        }

        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            writer.addDocuments(documents);
        }

        @Override
        UpdateMode mode()
        {
            return mode;
        }
    }

    static final class Update extends Operation
    {
        private final Delete delete;
        private final Create create;

        Update(@Nonnull final Term term, @Nonnull final Document document, final UpdateMode mode)
        {
            delete = new Delete(term, mode);
            create = new Create(document, mode);
        }

        Update(@Nonnull final Term term, @Nonnull final Collection<Document> documents, final UpdateMode mode)
        {
            delete = new Delete(term, mode);
            create = new Create(documents, mode);
        }

        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            writer.updateDocuments(delete.term, create.documents);
        }

        @Override
        UpdateMode mode()
        {
            return delete.mode();
        }
    }

    static final class ConditionalUpdate extends Operation
    {
        private final Create create;
        private final Delete delete;
        private final String optimisticLockField;

        ConditionalUpdate(@Nonnull final Term term, @Nonnull final Document document, final UpdateMode mode, @Nonnull final String optimisticLockField)
        {
            this.optimisticLockField = optimisticLockField;
            create = new Create(document, mode);
            delete = new Delete(term, mode);
        }

        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            writer.updateDocumentConditionally(delete.term, create.documents.get(0), optimisticLockField);
        }

        @Override
        UpdateMode mode()
        {
            return create.mode();
        }
    }

    static final class Optimize extends Operation
    {
        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            writer.optimize();
        }

        @Override
        UpdateMode mode()
        {
            return UpdateMode.BATCH;
        }
    }

    static final class Completion extends Operation
    {
        private final Runnable completionJob;
        private final Operation delegate;

        public Completion(final Operation delegate, final Runnable completionJob)
        {
            this.delegate = delegate;
            this.completionJob = completionJob;
        }

        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            try
            {
                delegate.perform(writer);
            }
            finally
            {
                completionJob.run();
            }
        }

        @Override
        UpdateMode mode()
        {
            return delegate.mode();
        }
    }
}
