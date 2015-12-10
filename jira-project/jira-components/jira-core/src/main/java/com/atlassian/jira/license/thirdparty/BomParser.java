package com.atlassian.jira.license.thirdparty;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.plugin.aboutpagepanel.AboutPagePanelModuleDescriptorImpl;

import java.util.List;

/**
 * Parses Bill Of Materials files into information that can be used by plugins.
 *
 * @since 6.0.2
 */
@ExperimentalApi
public interface BomParser
{
    /**
     * Extracts LGPL information from a String.
     *
     * The String should be csv formatted and may contain multiple lines.
     * Each line should contain exactly 5 elements, separated by commas.
     * Lines beginning with # should be treated as comments.
     * Only lines containing the text <em>GNU Lesser General Public License</em> will be considered.
     *
     * <p>
     *     For example:
     *     <ul>
     *         <li>amf-serializer,com.exadel.flamingo.flex:amf-serializer:jar:2.2.0,GNU Lesser General Public License 2.1,,binary <p><strong>accepted</strong></p></li>
     *         <li>#amf-serializer,com.exadel.flamingo.flex:amf-serializer:jar:2.2.0,GNU Lesser General Public License 2.1,,binary <p><strong>ignored</strong> as a comment</p></li>
     *         <li>Abdera Core,org.apache.abdera:abdera-core:bundle:1.1,Apache License 2.0,http://abdera.apache.org/abdera-core,binary <p><strong>ignored</strong></p> as a non-LGPL license</li>
     *         <li>amf-serializer,com.exadel.flamingo.flex:amf-serializer:jar:2.2.0,LGPL 2.1,,binary <p><strong>ignored</strong> as a non-LGPL license</p></li>
     *         <li>,,GNU Lesser General Public License 2.1,,binary<p><strong>ignored</strong> as no identifying library information is supplied</p></li>
     *         <li>amf-serializer,com.exadel.flamingo.flex:amf-serializer:jar:2.2.0,GNU Lesser General Public License 2.1 <p><strong>ignored</strong> due to insufficient number of elements</p></li>
     *         <li>amf-serializer,com.exadel.flamingo.flex:amf-serializer:jar:2.2.0,GNU Lesser General Public License 2.1,,,,,<p><strong>ignored</strong> due to too many elements</p></li>
     *     </ul>
     * </p>
     * For example:
     * Abdera Core,org.apache.abdera:abdera-core:bundle:1.1,Apache License 2.0,http://abdera.apache.org/abdera-core,binary
     *
     *
     * @param bomContents usually the complete contents of a bom.csv file
     * @return a list of Materials that fall under the LGPL license
     * @since 6.0.2
     */
    List<AboutPagePanelModuleDescriptorImpl.Material> extractLgplMaterials(String bomContents);
}
