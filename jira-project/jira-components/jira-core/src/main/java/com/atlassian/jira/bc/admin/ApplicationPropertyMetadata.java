package com.atlassian.jira.bc.admin;

import com.atlassian.jira.config.properties.ExampleGenerator;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.validation.ApplicationPropertyEnumerator;
import com.atlassian.validation.BooleanValidator;
import com.atlassian.validation.IntegerValidator;
import com.atlassian.validation.NonValidator;
import com.atlassian.validation.Validator;
import com.atlassian.validation.ValidatorFactory;
import com.google.common.base.Supplier;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an individual property setting as defined in the jpm.xml file. This implementation models the various
 * metadata about a single JIRA application property and also provides access to some type and validation logic.
 *
 * @since v4.4
 */
public class ApplicationPropertyMetadata
{
    private static final Map<String, Validator> DEFAULT_VALIDATORS_BY_TYPE = new HashMap<String, Validator>();

    static
    {
        DEFAULT_VALIDATORS_BY_TYPE.put("string", new NonValidator());
        DEFAULT_VALIDATORS_BY_TYPE.put("uint", new IntegerValidator(0, Integer.MAX_VALUE));
        DEFAULT_VALIDATORS_BY_TYPE.put("int", new IntegerValidator());
        DEFAULT_VALIDATORS_BY_TYPE.put("boolean", new BooleanValidator());
    }

    private String key;
    private String type;
    private String defaultValue;
    private Supplier<? extends Validator> validator;
    private boolean sysadminEditable;
    private boolean adminEditable;
    private boolean requiresRestart;
    private String name;
    private String nameKey;
    private String desc;
    private String descKey;
    private ExampleGenerator exampleGenerator;
    private ApplicationPropertyEnumerator enumerator;
    private Pair<String, Boolean> requiredFeatureKey;

    private static Supplier<Validator> validatorResolver(final String type)
    {
        return new LazyReference<Validator>()
        {
            @Override
            protected Validator create() throws Exception
            {
                Validator validator = DEFAULT_VALIDATORS_BY_TYPE.get(type);
                if (validator == null)
                {
                    validator = new NonValidator();
                }
                return validator;
            }
        };

    }

    private ApplicationPropertyMetadata(final String key, final String type, final String defaultValue, final Supplier<? extends Validator> validatorSupplier,
            boolean sysadminEditable, boolean adminEditable, boolean requiresRestart, final String name, final String nameKey, final String desc,
            final String descKey, ExampleGenerator exampleGenerator, final ApplicationPropertyEnumerator enumerator, Pair<String, Boolean> requiredFeatureKey)
    {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.sysadminEditable = sysadminEditable;
        this.adminEditable = adminEditable;
        this.requiresRestart = requiresRestart;
        this.name = name;
        this.nameKey = nameKey;
        this.desc = desc;
        this.descKey = descKey;
        this.validator = validatorSupplier;
        this.exampleGenerator = exampleGenerator;
        this.enumerator = enumerator;
        this.requiredFeatureKey = requiredFeatureKey;
    }

    public String getType()
    {
        return type;
    }

    public String getKey()
    {
        return key;
    }

    public boolean isSysadminEditable()
    {
        return sysadminEditable;
    }

    /**
     * Whether or not changing the property value requires a restart in order to take effect.
     *
     * @return true only if the property requires a restart.
     */
    public boolean isRequiresRestart()
    {
        return requiresRestart;
    }

    public String getName()
    {
        return name;
    }
    
    public String getNameKey()
    {
        return nameKey;
    }

    public String getDescription()
    {
        return desc;
    }

    public String getDescriptionKey()
    {
        return descKey;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public Validator getValidator()
    {
        return validator.get();
    }

    public ApplicationPropertyEnumerator getEnumerator()
    {
        if(!"enum".equals(type))
        {
            throw new IllegalStateException("Tried to get enumerator for a non-enum type");
        }
        return enumerator;
    }

    public Validator.Result validate(String value)
    {
        return validator.get().validate(value);
    }

    public boolean isAdminEditable()
    {
        return adminEditable;
    }

    public ExampleGenerator getExampleGenerator()
    {
        return exampleGenerator;
    }

    public Pair<String, Boolean> getRequiredFeatureKey()
    {
        return requiredFeatureKey;
    }

    public static class Builder
    {
        private String key;
        private String type;
        private String defaultValue;
        private Supplier<? extends Validator> validator;
        private boolean sysAdminEditable;
        private boolean adminEditable;
        private boolean requiresRestart;
        private String name;
        private String nameKey;
        private String desc;
        private String descKey;
        private ApplicationPropertyEnumerator enumerator;
        private ExampleGenerator exampleGenerator;
        private Pair<String, Boolean> requiredFeatureKeys;

        public String key()
        {
            return key;
        }

        public Builder key(String key)
        {
            this.key = key;
            return this;
        }

        public String type()
        {
            return type;
        }

        public Builder type(String type)
        {
            this.type = type;
            return this;
        }

        public String defaultValue()
        {
            return defaultValue;
        }

        public Builder defaultValue(String defaultValue)
        {
            this.defaultValue = defaultValue;
            return this;
        }

        public Supplier<? extends Validator> validator()
        {
            return validator;
        }
        
        public Builder validatorName(String validatorName)
        {
            if (validatorName == null)
            {
                this.validator = validatorResolver(type);
            }
            else
            {
                this.validator = new ValidatorFactory().getInstanceLazyReference(validatorName);
            }
            return this;
        }

        public Builder validator(Supplier<? extends Validator> validator)
        {
            this.validator = validator;
            return this;
        }

        public boolean sysAdminEditable()
        {
            return sysAdminEditable;
        }

        public Builder sysAdminEditable(boolean sysAdminEditable)
        {
            this.sysAdminEditable = sysAdminEditable;
            return this;
        }

        public boolean adminEditable()
        {
            return adminEditable;
        }

        public Builder adminEditable(boolean adminEditable)
        {
            this.adminEditable = adminEditable;
            return this;
        }

        public boolean requiresRestart()
        {
            return requiresRestart;
        }

        public Builder requiresRestart(boolean requiresRestart)
        {
            this.requiresRestart = requiresRestart;
            return this;
        }

        public String name()
        {
            return name;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public String nameKey()
        {
            return nameKey;
        }

        public Builder nameKey(String nameKey)
        {
            this.nameKey = nameKey;
            return this;
        }

        public String desc()
        {
            return desc;
        }

        public Builder desc(String desc)
        {
            this.desc = desc;
            return this;
        }

        public String descKey()
        {
            return descKey;
        }

        public Builder descKey(String descKey)
        {
            this.descKey = descKey;
            return this;
        }

        public ApplicationPropertyEnumerator enumerator()
        {
            return enumerator;
        }

        public Builder enumerator(ApplicationPropertyEnumerator enumerator)
        {
            this.enumerator = enumerator;
            return this;
        }
        
        public ApplicationPropertyMetadata build()
        {
            return new ApplicationPropertyMetadata(key, type, defaultValue, validator, sysAdminEditable, adminEditable, requiresRestart,
                    name, nameKey, desc, descKey, exampleGenerator, enumerator, requiredFeatureKeys);
        }

        public Builder exampleGenerator(ExampleGenerator exampleGenerator)
        {
            this.exampleGenerator = exampleGenerator;
            return this;
        }

        public Builder requiredFeatureKey(Pair<String,Boolean> requiredFeatureKeys)
        {
            this.requiredFeatureKeys = requiredFeatureKeys;
            return this;
        }
    }
}

