package com.atlassian.jira.mock.ofbiz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.entity.model.ModelReader;

import com.atlassian.jira.junit.rules.AvailableInContainer;

/**
 * Mock for {@link ModelReader}, which does not have any interface, which can be used by {@link AvailableInContainer}. Instead of this one,
 * by this mock some functionality is delegated to interface {@link ModelReaderMock.Delegate}.
 */
public class ModelReaderMock
{

    public interface Delegate
    {

        ModelEntity getModelEntity(String entityName);
    }

    private static class DefaultDelegate implements Delegate
    {

        public static Delegate INSTANCE = new DefaultDelegate();

        private DefaultDelegate()
        {
        }

        @Override
        public ModelEntity getModelEntity(final String entityName)
        {
            return new MockModelEntity(entityName);
        }

    }

    private static class MockModelEntity extends ModelEntity
    {

        public MockModelEntity(final String entityName)
        {
            setEntityName(entityName);
        }

        /**
         * Because of mock purposes, it still creates any field, instead of null.
         */
        @Override
        public ModelField getField(final String fieldName)
        {
            final ModelField result = new ModelField();
            result.setName(fieldName);
            return result;
        }

    }

    /**
     * @see #getMock()
     */
    private ModelReaderMock()
    {
    }

    public static ModelReader getMock()
    {
        return getMock(null);
    }

    public static ModelReader getMock(final ModelReaderMock.Delegate delegate)
    {
        final ModelReader result = mock(ModelReader.class);
        try
        {
            when(result.getModelEntity(Matchers.anyString())).thenAnswer(new Answer<ModelEntity>()
            {

                @Override
                public ModelEntity answer(final InvocationOnMock invocation) throws Throwable
                {
                    final String entityName = (String) invocation.getArguments()[0];
                    final Delegate sanitizedDelegate = delegate != null ? delegate : DefaultDelegate.INSTANCE;
                    return sanitizedDelegate.getModelEntity(entityName);
                }

            });
        }
        catch (final GenericEntityException e)
        {
            throw new RuntimeException(e); // it is mock, it is can not happen
        }
        return result;
    }

}
