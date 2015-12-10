package com.atlassian.jira.web.util;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.google.common.base.Supplier;

/**
 * @since v5.2.x
 */
public class ProductVersionDataBeanProvider implements Supplier<ProductVersionDataBean>
{
    private final ProductVersionDataBean productVersionDataBean;

    public ProductVersionDataBeanProvider(BuildUtilsInfo buildUtilsInfo)
    {
        productVersionDataBean = new ProductVersionDataBean(buildUtilsInfo);
    }

    @Override
    public ProductVersionDataBean get()
    {
        return productVersionDataBean;
    }
}
