package com.atlassian.jira.plugin.aboutpagepanel;

import com.atlassian.jira.license.thirdparty.BomParser;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class BomParserImpl implements BomParser
{
    private static final Logger log = LoggerFactory.getLogger(BomParserImpl.class);

    @Override
    public List<AboutPagePanelModuleDescriptorImpl.Material> extractLgplMaterials(String bomContents)
    {
        ArrayList<AboutPagePanelModuleDescriptorImpl.Material> al = Lists.newArrayList();
        bomContents = bomContents.replaceAll("\\r", "\n");
        String[] materialLines = bomContents.split("\\n");
        materialLines = org.apache.commons.lang.StringUtils.stripAll(materialLines);
        for (String materialLine : materialLines)
        {
            if (materialLine.startsWith("#")) continue;
            if (materialLine.contains("GNU Lesser General Public License"))
            {
                // example
                // Abdera Core,org.apache.abdera:abdera-core:bundle:1.1,Apache License 2.0,http://abdera.apache.org/abdera-core,binary

                String[] materialInfo = materialLine.split(",", -1);
                if (materialInfo.length < 5)
                {
                    log.warn(String.format("License info line '%s' could not be parsed because it has '%d' components, "
                            + "a minimum of 5 components is required", materialLine, materialInfo.length));
                    continue;
                }

                materialInfo = org.apache.commons.lang.StringUtils.stripAll(materialInfo);
                String libraryName = materialInfo[0];
                String mavenInfo = materialInfo[1];
                String url = materialInfo[3];

                if (isEmpty((libraryName + mavenInfo + url))) continue;

                String license = materialInfo[2];
                String artifactType = materialInfo[4];
                al.add(new AboutPagePanelModuleDescriptorImpl.Material(libraryName, mavenInfo, license, url, artifactType));
            }
        }
        return al;
    }
}
