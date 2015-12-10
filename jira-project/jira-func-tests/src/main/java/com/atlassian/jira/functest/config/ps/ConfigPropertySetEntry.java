package com.atlassian.jira.functest.config.ps;

/**
 * Represents an entry in a property set. Instances should be created through the static factory methods.
 *
 * @since v4.0
 */
public abstract class ConfigPropertySetEntry
{
    public static enum Type
    {
        STRING(5), TEXT(6), INTEGER(2), LONG(3), BOOLEAN(1);

        private int psType;

        Type(final int psType)
        {
            this.psType = psType;
        }

        public int getPropertySetType()
        {
            return psType;
        }

        public static Type forPropertySetType(int psType)
        {
            for (Type type : Type.values())
            {
                if (type.getPropertySetType() == psType)
                {
                    return type;
                }
            }
            return null;
        }
    }

    private final String propertyName;
    private final Type propertyType;

    public ConfigPropertySetEntry(final String propertyName, final Type propertyType)
    {
        this.propertyName = propertyName;
        this.propertyType = propertyType;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public Type getPropertyType()
    {
        return propertyType;
    }

    public abstract String asString();
    public abstract Long asLong();
    public abstract Boolean asBoolean();
    public abstract Integer asInteger();
    public abstract Object asObject();

    public static ConfigPropertySetEntry createStringEntry(String name, String value)
    {
        return new StringEntry(name, Type.STRING, value);
    }

    public static ConfigPropertySetEntry createTextEntry(String name, String value)
    {
        return new StringEntry(name, Type.TEXT, value);
    }

    public static ConfigPropertySetEntry createIntegerEntry(String name, Integer value)
    {
        return new IntegerEntry(name, value);
    }

    public static ConfigPropertySetEntry createLongEntry(String name, Long value)
    {
        return new LongEntry(name, value);
    }

    public static ConfigPropertySetEntry createBooleanEntry(String name, Boolean b)
    {
        return new BooleanEntry(name, b);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ConfigPropertySetEntry that = (ConfigPropertySetEntry) o;

        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null)
        {
            return false;
        }
        if (propertyType != that.propertyType)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = propertyName != null ? propertyName.hashCode() : 0;
        result = 31 * result + (propertyType != null ? propertyType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("%s(%s): %s", propertyName, propertyType.name(), asString());
    }

    private static class StringEntry extends ConfigPropertySetEntry
    {
        private final String value;

        public StringEntry(final String propertyName, final Type propertyType, final String value)
        {
            super(propertyName, propertyType);
            this.value = value;
        }

        public String asString()
        {
            return value;
        }

        public Long asLong()
        {
            if (value != null)
            {
                try
                {
                    return new Long(value);
                }
                catch (NumberFormatException e)
                {
                    //fall through.
                }
            }
            return null;

        }

        public Boolean asBoolean()
        {
            final Integer integer = asInteger();
            return integer != null && integer != 0;
        }

        public Integer asInteger()
        {
            if (value != null)
            {
                try
                {
                    return new Integer(value);
                }
                catch (NumberFormatException e)
                {
                    //fall through.
                }
            }
            return null;
        }

        public Object asObject()
        {
            return value;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            if (!super.equals(o))
            {
                return false;
            }

            final StringEntry that = (StringEntry) o;

            if (value != null ? !value.equals(that.value) : that.value != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = super.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private static class IntegerEntry extends ConfigPropertySetEntry
    {
        private final Integer value;

        private IntegerEntry(final String propertyName, final Integer value)
        {
            super(propertyName, Type.INTEGER);
            this.value = value;
        }

        public String asString()
        {
            return value != null ? value.toString() : null;
        }

        public Long asLong()
        {
            return value != null ? value.longValue() : null;
        }

        public Boolean asBoolean()
        {
            return value != null && value != 0;
        }

        public Integer asInteger()
        {
            return value;
        }

        @Override
        public Object asObject()
        {
            if (getPropertyType() == Type.BOOLEAN)
            {
                return asBoolean();
            }
            else
            {
                return value;
            }
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            if (!super.equals(o))
            {
                return false;
            }

            final IntegerEntry that = (IntegerEntry) o;

            if (value != null ? !value.equals(that.value) : that.value != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = super.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private static class LongEntry extends ConfigPropertySetEntry
    {
        private final Long value;

        private LongEntry(final String propertyName, final Long value)
        {
            super(propertyName, Type.LONG);
            this.value = value;
        }

        public String asString()
        {
            return value != null ? value.toString() : null;
        }

        public Long asLong()
        {
            return value;
        }

        public Boolean asBoolean()
        {
            return value != null && value != 0;
        }

        public Integer asInteger()
        {
            if (value != null)
            {
                int tmpValue = value.intValue();
                return tmpValue != value ? null : tmpValue;
            }
            else
            {
                return null;
            }
        }

        public Object asObject()
        {
            return value;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            if (!super.equals(o))
            {
                return false;
            }

            final LongEntry longEntry = (LongEntry) o;

            if (value != null ? !value.equals(longEntry.value) : longEntry.value != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = super.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private static class BooleanEntry extends ConfigPropertySetEntry
    {
        private final Boolean value;

        private BooleanEntry(final String propertyName, final Boolean value)
        {
            super(propertyName, Type.BOOLEAN);
            this.value = value;
        }

        public String asString()
        {
            return value != null ? value.toString() : null;
        }

        public Long asLong()
        {
            if (value != null)
            {
                return value ? 1L : 0L;
            }
            else
            {
                return null;
            }
        }

        public Boolean asBoolean()
        {
            return value;
        }

        public Integer asInteger()
        {
            if (value != null)
            {
                return value ? 1 : 0;
            }
            else
            {
                return null;
            }

        }

        @Override
        public Object asObject()
        {
            return value;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            if (!super.equals(o))
            {
                return false;
            }

            final BooleanEntry that = (BooleanEntry) o;

            if (value != null ? !value.equals(that.value) : that.value != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = super.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}
