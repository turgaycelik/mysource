package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TestAbstractSingleFieldType
{
    @Mock
    private CustomFieldValuePersister mockCustomFieldValuePersister;
    @Mock
    private GenericConfigManager mockGenericConfigManager;

    @Test
    public void getValueFromCustomFieldParamsHandlesInteger() throws Exception
    {
        final AbstractSingleFieldType<Integer> abstractSingleFieldType = newIntegerAbstractSingleFieldType();
        Integer expectedValue = new Integer(1234);
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.put(null, ImmutableList.of(abstractSingleFieldType.getStringFromSingularObject(expectedValue)));

        final Integer value = abstractSingleFieldType.getValueFromCustomFieldParams(customFieldParams);
        assertThat(value, equalTo(expectedValue));
    }

    @Test
    public void getValueFromCustomFieldParamsHandlesString() throws Exception
    {
        final AbstractSingleFieldType<String> abstractSingleFieldType = newStringAbstractSingleFieldType();
        String expectedValue = "12345";
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.put(null, ImmutableList.of(abstractSingleFieldType.getStringFromSingularObject(expectedValue)));

        final String value = abstractSingleFieldType.getValueFromCustomFieldParams(customFieldParams);
        assertThat(value, equalTo(expectedValue));
    }

    @Test
    public void getValueFromCustomFieldParamsHandlesListOfInteger() throws Exception
    {
        final AbstractSingleFieldType<List<Integer>> abstractSingleFieldType = newListOfIntegerAbstractSingleFieldType();

        List<Integer> expectedValue = ImmutableList.of(new Integer(1234), new Integer(56789), new Integer(10111213));
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.put(null, ImmutableList.of(abstractSingleFieldType.getStringFromSingularObject(expectedValue)));

        final List<Integer> value = abstractSingleFieldType.getValueFromCustomFieldParams(customFieldParams);
        assertThat(value, equalTo(expectedValue));
    }

    private AbstractSingleFieldType<String> newStringAbstractSingleFieldType()
    {
        return new StringSingleFieldType(mockCustomFieldValuePersister, mockGenericConfigManager);
    }

    private AbstractSingleFieldType<Integer> newIntegerAbstractSingleFieldType()
    {
        return new IntegerSingleFieldType(mockCustomFieldValuePersister, mockGenericConfigManager);
    }

    private AbstractSingleFieldType<List<Integer>> newListOfIntegerAbstractSingleFieldType()
    {
        return new ListOfIntegerSingleFieldType(mockCustomFieldValuePersister, mockGenericConfigManager);
    }

    private class StringSingleFieldType extends SimpleSingleFieldType<String>
    {
        private StringSingleFieldType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
        {
            super(customFieldValuePersister, genericConfigManager);
        }

        @Override
        public String getStringFromSingularObject(final String singularObject)
        {
            return singularObject;
        }

        @Override
        public String getSingularObjectFromString(final String string) throws FieldValidationException
        {
            return string;
        }
    }

    private class IntegerSingleFieldType extends SimpleSingleFieldType<Integer>
    {
        private IntegerSingleFieldType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
        {
            super(customFieldValuePersister, genericConfigManager);
        }

        @Override
        public String getStringFromSingularObject(final Integer singularObject)
        {
            return singularObject.toString();
        }

        @Override
        public Integer getSingularObjectFromString(final String string) throws FieldValidationException
        {
            return NumberUtils.toInt(string, -1);
        }

        /**
         * Simulate ServiceDesk's implementation to catch https://jdog.jira-dev.com/browse/JDEV-27029
         * <p/>
         * The javadoc from {@link com.atlassian.jira.issue.customfields.CustomFieldType#getStringValueFromCustomFieldParams(com.atlassian.jira.issue.customfields.view.CustomFieldParams)}
         * says it could return String, List of String or anything else like CustomFieldParams,
         * even though for SingleFieldType it seems logical to handle only String.
         * @param parameters
         * @return
         */
        @Override
        public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
        {
            final Collection groups = parameters.getValuesForNullKey();
            if ((groups == null) || groups.isEmpty()) {
                return null;
            }
            return groups;
        }
    }

    private class ListOfIntegerSingleFieldType extends SimpleSingleFieldType<List<Integer>>
    {

        private ListOfIntegerSingleFieldType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
        {
            super(customFieldValuePersister, genericConfigManager);
        }

        @Override
        public String getStringFromSingularObject(final List<Integer> singularObject)
        {
            return StringUtils.join(singularObject, ',');
        }

        @Override
        public List<Integer> getSingularObjectFromString(final String string) throws FieldValidationException
        {
            return Lists.transform(ImmutableList.copyOf(string.split(",")), new Function<String, Integer>()
            {
                @Override
                public Integer apply(@Nullable final String input)
                {
                    return NumberUtils.toInt(input, -1);
                }
            });
        }

        @Override
        public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
        {
            return parameters;
        }
    }

    private abstract class SimpleSingleFieldType<T> extends AbstractSingleFieldType<T>
    {
        private SimpleSingleFieldType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
        {
            super(customFieldValuePersister, genericConfigManager);
        }

        @Nonnull
        @Override
        protected PersistenceFieldType getDatabaseType()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Nullable
        @Override
        protected Object getDbValueFromObject(final T customFieldObject)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Nullable
        @Override
        protected T getObjectFromDbValue(@Nonnull final Object databaseValue) throws FieldValidationException
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
