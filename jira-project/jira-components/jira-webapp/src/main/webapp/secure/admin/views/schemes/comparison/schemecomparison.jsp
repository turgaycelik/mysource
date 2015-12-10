<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.comparison.title'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.scheme.comparison.title'"/></page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="helpURL">scheme_tools</page:param>
    <page:param name="description">
        <ww:property value="/comparisonToolDescription" escape="false"/>
        <p>
        <ww:text name="'admin.scheme.comparison.desc.4'">
           <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonTool!default.jspa<ww:property value="/parameters"/>"></ww:param>
           <ww:param name="'value1'"></a></ww:param>
        </ww:text>
        </p>
        <p>
        <ww:text name="'admin.scheme.comparison.desc.2'">
            <ww:param name="'value0'"><span class="<ww:if test="/schemeDifferencePercentage != 0">redText</ww:if><ww:else>greenText</ww:else>">&nbsp;<ww:property value="/schemeComparisonDifference"/>&nbsp;</span></ww:param>
        </ww:text>
        </p>
        <p>
            <ww:text name="'admin.scheme.comparison.desc.3'"/>
        </p>
        <ww:if test="/schemeDifferencePercentage != 0">
            <ul class="optionslist">
                <li><a id="return_link" href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonPicker!default.jspa"><ww:text name="'admin.scheme.comparison.return.link'"/></a></li>
            </ul>
        </ww:if>
    </page:param>
    <tr>
        <td>
        <ww:if test="/schemeDifferencePercentage != 0">
            <table class="aui aui-table-rowhover" id="scheme_comparison_table">
                <thead>
                    <tr>
                        <th><ww:property value="/schemeDisplayName"/></th>
                    <ww:iterator value="/schemeRelationships/schemes" status="'schemeCount'">
                        <th>
                            <ww:if test="/schemeRelationships/schemeDistilled(.) == true">
                                <ww:text name="'admin.scheme.comparison.matching.schemes'"/>
                                <br/><span style="font-size:10px;font-weight:300; "><ww:text name="'admin.scheme.comparison.distilled.from'"/>
                                    <ww:iterator
                                            value="/schemeRelationships/distilledSchemeResultForScheme(.)/originalSchemes"
                                            status="'status'">
                                        <a href="<%=request.getContextPath()%>/secure/admin/<ww:property value="/editPage"/>!default.jspa?schemeId=<ww:property value="./id"/>"
                                           id="<ww:property value="./id"/>_editScheme" title="Edit Scheme"><ww:property value="./name"/></a>
                                        <ww:if test="@status/last == false">, </ww:if>
                                    </ww:iterator>
                                    )</span>
                            </ww:if>
                            <ww:else>
                                <a href="<%=request.getContextPath()%>/secure/admin/<ww:property value="/editPage"/>!default.jspa?schemeId=<ww:property value="./id"/>"
                                   id="<ww:property value="./id"/>_editScheme" title="Edit Scheme"><ww:property value="./name"/></a>
                            </ww:else>
                        </th>
                    </ww:iterator>
                    </tr>
                </thead>
                <tbody>
                <ww:iterator value="/schemeRelationships/schemeRelationships">
                    <ww:if test="./allMatch() == false">
                    <tr>
                        <th width="<ww:property value="/columnWidthPercentage"/>">
                            <b><ww:text name="./entityTypeDisplayName"/></b>
                        </th>
                        <ww:iterator value="/schemeRelationships/schemes" status="'schemeCount'">
                            <td width="<ww:property value="/columnWidthPercentage"/>">
                                <span class="status-active">
                                    <ww:iterator value="/schemeEntitiesByDisplayName(../matchingSchemeEntities)" status="'status'">
                                        <ww:property value="."/>
                                        <ww:if test="@status/last == false"><br/></ww:if>
                                    </ww:iterator>
                                </span>
                                <ww:if test="/schemeEntitiesByDisplayName(../nonMatchingSchemeEntities(.))/size > 0 && /schemeEntitiesByDisplayName(../matchingSchemeEntities)/size > 0">
                                    <br/>
                                </ww:if>
                                <strong class="status-inactive">
                                    <ww:iterator value="/schemeEntitiesByDisplayName(../nonMatchingSchemeEntities(.))" status="'status'">
                                        <ww:property value="."/>
                                        <ww:if test="@status/last == false"><br/></ww:if>
                                    </ww:iterator>
                                </strong>
                            </td>
                        </ww:iterator>
                    </tr>
                    </ww:if>
                </ww:iterator>
                </tbody>
            </table>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.scheme.comparison.no.difference.schemes'">
                <ww:param name="'value0'">
                    <ww:iterator value="/distilledSchemeResults/distilledSchemeResults" status="'outsideStatus'">
                        <ww:iterator value="./originalSchemes" status="'status'">
                            <a href="<%=request.getContextPath()%>/secure/admin/<ww:property value="/editPage"/>!default.jspa?schemeId=<ww:property value="./id"/>"
                               id="<ww:property value="./id"/>_editScheme" title="Edit Scheme"><ww:property value="./name"/></a>
                            <ww:if test="@status/last == false">, </ww:if>
                        </ww:iterator>
                    </ww:iterator>
                </ww:param>
                <ww:param name="'value1'">
                    <a href="<%=request.getContextPath()%>/secure/admin/SchemeTypePicker!selectSchemes.jspa<ww:property value="/parameters"/>">
                </ww:param>
                <ww:param name="'value2'">
                    </a>
                </ww:param>
            </ww:text>

            <ul class="optionslist">
                <li><a id="return_link" href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonPicker!default.jspa"><ww:text name="'admin.scheme.comparison.return.link'"/></a></li>
            </ul>
        </ww:else>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
