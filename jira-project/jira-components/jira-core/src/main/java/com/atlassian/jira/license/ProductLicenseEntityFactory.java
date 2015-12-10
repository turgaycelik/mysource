package com.atlassian.jira.license;

import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.ofbiz.FieldMap;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v6.3
 */
public class ProductLicenseEntityFactory extends AbstractEntityFactory<ProductLicense>
{

    @Override
    public Map<String, Object> fieldMapFrom(final ProductLicense value)
    {
        return new FieldMap(ProductLicense.LICENSE, value.getLicenseKey());
    }

    @Override
    public String getEntityName()
    {
        return "ProductLicense";
    }

    @Override
    public ProductLicense build(final GenericValue genericValue)
    {
        return new ProductLicense(genericValue.getString(ProductLicense.LICENSE));
    }
}
