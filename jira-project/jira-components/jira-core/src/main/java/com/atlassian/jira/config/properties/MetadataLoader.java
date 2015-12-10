package com.atlassian.jira.config.properties;

import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.validation.ApplicationPropertyEnumerator;
import com.atlassian.validation.EnumValidator;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the loading and parsing of the application properties metadata into a map of keys to metadata objects.
 *
 * @since v4.4
 */
class MetadataLoader
{
    private static final Logger log = Logger.getLogger(MetadataLoader.class);

    /**
     * Load the ApplicationPropertyMetadata entries from the classpath resource with the given path name.
     *
     * @param xmlFilename path to classpath-loadable resource.
     * @return A map of key to {@link ApplicationPropertyMetadata} representing the config.
     * @throws DataAccessException only if there is a problem reading the resource.
     */
    LinkedHashMap<String, ApplicationPropertyMetadata> loadMetadata(String xmlFilename) throws DataAccessException
    {
        try
        {
            log.debug("Loading application properties metadata from " + xmlFilename);
            InputStream mxml = getClass().getClassLoader().getResourceAsStream(xmlFilename);
            return loadMetadata(mxml, xmlFilename);
        }
        catch (Exception e)
        {
            throw new DataAccessException("Cannot load the application properties metadata file " + xmlFilename, e);
        }
    }

    /**
     * Load the ApplicationPropertyMetadata entries from the given stream (and the given path name).
     *
     * @param stream the {@link InputStream} from which the XML is loaded.
     * @param streamDescriptor in the case of logging an error, what name should the resource at the stream be given.
     * @return A map of key to {@link ApplicationPropertyMetadata} representing the config.
     * @throws DocumentException only if there is a problem reading the resource.
     */
    LinkedHashMap<String, ApplicationPropertyMetadata> loadMetadata(InputStream stream, String streamDescriptor)
            throws DocumentException
    {
        LinkedHashMap<String, ApplicationPropertyMetadata> metadataMap = new LinkedHashMap<String, ApplicationPropertyMetadata>();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(stream);
        Element root = doc.getRootElement();

        Iterator properties = root.element("properties").elementIterator();
        while (properties.hasNext())
        {
            Element property = (Element) properties.next();
            String key = property.elementText("key");
            String defaultValue = property.elementText("default-value");
            String type = property.elementText("type");
            if (type == null)
            {
                type = "string";
            }
            String validator = property.elementText("validator");
            Supplier<EnumValidator> validatorObject = null;
            Map<String, ApplicationPropertyEnumerator> enumerators = Maps.newHashMap();
            if (StringUtils.isBlank(type))
            {
                // let's see if it's a structured type, only one we support for now is enum
                Element typeElement = property.element("type");
                if (typeElement != null) {
                    Element enm = typeElement.element("enum");
                    if (enm != null)
                    {
                        type = "enum";
                        ArrayList<String> options = new ArrayList<String>();
                        Iterator optionIter = enm.elementIterator("option");
                        while (optionIter.hasNext())
                        {
                            final Element optionElem = (Element) optionIter.next();
                            final String optionValue = optionElem.getText();

                            if (optionValue == null || "".equals(optionValue.trim()))
                            {
                                throw new IllegalArgumentException("No option may be blank");
                            }
                            options.add(optionValue);
                        }

                        enumerators.put(key, ApplicationPropertyEnumerator.of(options));
                        validatorObject = Suppliers.ofInstance(new EnumValidator(enumerators.get(key)));
                    }
                }
            }

            String name = property.elementText("name");
            String nameKey = property.elementText("nameKey");
            String desc = property.elementText("description");
            String descKey = property.elementText("descriptionKey");
            if (name == null && nameKey == null)
            {
                name = key;
            }
            // defaults to true if absent
            boolean sysAdminEditable = !"false".equalsIgnoreCase(property.elementText("sysadmin-editable"));
            if (!sysAdminEditable)
            {
                sysAdminEditable = !"false".equalsIgnoreCase(property.elementText("sysadmin-editable"));
            }
            // defaults to true if absent
            boolean requiresRestart = !"false".equalsIgnoreCase(property.elementText("requires-restart"));
            // defaults to true if absent
            boolean adminEditable = !"false".equalsIgnoreCase(property.elementText("admin-editable"));
            ExampleGenerator exampleGenerator = null;
            Pair<String,Boolean> requiredFeatureKey = null;
            if (property.element("feature-key") != null)
            {
                Element featureKey = property.element("feature-key");
                Boolean featureEnabled = Boolean.TRUE;
                if (featureKey.attribute("enabled") != null)
                {
                    featureEnabled = Boolean.parseBoolean(featureKey.attribute("enabled").getText());
                }
                requiredFeatureKey = Pair.nicePairOf(featureKey.getText(),featureEnabled);
            }

            if (property.elementText("example-generator") != null)
            {
                try
                {
                    exampleGenerator = (ExampleGenerator) Class.forName(property.elementText("example-generator")).newInstance();
                }
                catch (ClassNotFoundException e)
                {
                    log.debug("Couldn't find example generator class for: "+ name);
                }
                catch (InstantiationException e)
                {
                    log.debug("Couldn't create instance of example generator class for: "+ name);
                }
                catch (IllegalAccessException e)
                {
                    log.debug("Illegal access to example generator class for: "+ name);
                }
                catch (ClassCastException e)
                {
                    log.debug("Example generator class for: "+ name + "doesn't implement the ExampleGenerator interface");
                }
            }
            if (key != null)
            {
                ApplicationPropertyMetadata.Builder builder = new ApplicationPropertyMetadata.Builder()
                        .key(key)
                        .type(type)
                        .defaultValue(defaultValue)
                        .adminEditable(adminEditable)
                        .sysAdminEditable(sysAdminEditable)
                        .requiresRestart(requiresRestart)
                        .name(name)
                        .nameKey(nameKey)
                        .desc(desc)
                        .descKey(descKey)
                        .exampleGenerator(exampleGenerator)
                        .requiredFeatureKey(requiredFeatureKey)
                        .enumerator(enumerators.get(key));

                if (validatorObject == null) {
                    builder.validatorName(validator);
                } else {
                    builder.validator(validatorObject);
                }
                metadataMap.put(key, builder.build());
            }
            else
            {
                log.error(streamDescriptor + " contains null key");
            }
        }
        return metadataMap;
    }
}
