<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.merge.title'"/></title>
</head>

<body>

<script type="text/javascript">
    function submitForm(typeOfSchemesToDisplay)
    {
        document.forms['jiraform'].action = "SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=" + typeOfSchemesToDisplay + "&selectedSchemeType=" + document.getElementById("selectedSchemeType").value;
        document.forms['jiraform'].submit();
        return false;
    }

    function toggleCheckboxes(checked)
    {
        var checkboxes = getElementsByName("input", "selectedDistilledSchemes");

        // run through all the checkboxes in the row and uncheck
        for (var i = 0; i < checkboxes.length; i++)
        {
            var checkbox = checkboxes[i];
            checkbox.checked = checked;
            if (checked)
            {
                setTextFieldDisabled(checkbox.className, false);
            }
            else
            {
                setTextFieldDisabled(checkbox.className, true);
            }
        }

        return false;
    }

    function setTextFieldDisabled(id, value)
    {
        document.getElementById(id).disabled = value;
    }

    function toggleTextFieldEnabled(id)
    {
        document.getElementById(id).disabled = !document.getElementById(id).disabled;
    }

    function getElementsByName(tag, name)
    {
        var elem = document.getElementsByTagName(tag);
        var arr = new Array();
        for (i = 0,iarr = 0; i < elem.length; i++)
        {
            att = elem[i].getAttribute("name");
            if (att == name)
            {
                arr[iarr] = elem[i];
                iarr++;
            }
        }
        return arr;
    }

    function initializeInputBoxes()
    {
        var checkboxes = getElementsByName("input", "selectedDistilledSchemes");

        // run through all the checkboxes in the row and uncheck
        for (var i = 0; i < checkboxes.length; i++)
        {
            var checkbox = checkboxes[i];
            var checked = checkbox.checked;
            if (checked)
            {
                setTextFieldDisabled(checkbox.className, false);
            }
            else
            {
                setTextFieldDisabled(checkbox.className, true);
            }
        }
    }
</script>

<page:applyDecorator name="jiraform">
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.scheme.merge.title'"/></page:param>
    <page:param name="helpURL">scheme_tools</page:param>
    <page:param name="description">

        <ww:if test="/distilledSchemeResults/distilledSchemeResults/size !=0">
            <ww:text name="'admin.scheme.merge.desc.1'">
               <ww:param name="'value0'"><strong></ww:param>
               <ww:param name="'value1'"><ww:property value="/totalDistilledFromSchemes"/></ww:param>
               <ww:param name="'value2'"></strong></ww:param>
               <ww:param name="'value3'"><ww:property value="/distilledSchemeResults/distilledSchemeResults/size"/></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:if test="/typeOfSchemesToDisplay/equals('associated') == true">
                <ww:text name="'admin.scheme.merge.desc.2'">
                   <ww:param name="'value0'"><ww:property value="/schemeTypeDisplayName(/selectedSchemeType)"/></ww:param>
                   <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonPicker!default.jspa?typeOfSchemesToDisplay=<ww:property value="/typeOfSchemesToDisplay" />&selectedSchemeType=<ww:property value="/selectedSchemeType"/>"></ww:param>
                   <ww:param name="'value2'"></a></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.scheme.merge.desc.3'">
                   <ww:param name="'value0'"><ww:property value="/schemeTypeDisplayName(/selectedSchemeType)"/></ww:param>
                   <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonPicker!default.jspa?typeOfSchemesToDisplay=<ww:property value="/typeOfSchemesToDisplay" />&selectedSchemeType=<ww:property value="/selectedSchemeType"/>"></ww:param>
                   <ww:param name="'value2'"></a></ww:param>
                </ww:text>
            </ww:else>
        </ww:else>
    </page:param>
    <page:param name="action">SchemeMerge.jspa</page:param>
    <page:param name="columns">1</page:param>
    <ww:if test="/distilledSchemeResults/distilledSchemeResults/size !=0">
        <page:param name="submitId">changes_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.scheme.merge.preview.changes'"/></page:param>
        <page:param name="cancelURI">SchemeTypePicker!default.jspa</page:param>
    </ww:if>
    <page:param name="autoSelectFirst">false</page:param>
    <tr>
        <td>
            <ww:if test="/distilledSchemeResults/distilledSchemeResults/size !=0">
            <table class="aui aui-table-rowhover" id="merged_schemes">
                <thead>
                    <tr>
                        <th width="2%">
                            <input type="checkbox" id="selectAll" onclick="toggleCheckboxes(this.checked);"/>
                        </th>
                        <th width="48%">
                            <ww:text name="'admin.scheme.merge.merged.schemes'"/>
                        </th>
                        <th width="48%">
                            <ww:text name="'admin.scheme.merge.new.scheme.name'"/>
                        </th>
                    </tr>
                </thead>
                <tbody>
                <ww:iterator value="/distilledSchemeResults/distilledSchemeResults" status="'status'">
                    <tr>
                        <td>
                            <input type="checkbox" name="selectedDistilledSchemes"
                            id="checkbox_<ww:property value="@status/index"/>"
                            class="<ww:property value="./resultingScheme/name"/>"
                            onclick="toggleTextFieldEnabled('<ww:property value="./resultingScheme/name"/>')"
                            value="<ww:property value="./resultingScheme/name"/>"
                            <ww:if test="/selectedDistilledSchemesAsList/contains(./resultingScheme/name) ==true || ./selected == true">checked</ww:if> />
                        </td>
                        <td>
                            <span class="small">
                            <ww:iterator value="./originalSchemes" status="'status'">
                                <a href="<%=request.getContextPath()%>/secure/admin/<ww:property value="/editPage"/>!default.jspa?schemeId=<ww:property value="./id"/>"
                                   id="<ww:property value="./id"/>_editScheme" title="Edit Scheme"><ww:property value="./name"/></a>
                                <ww:if test="@status/last == false">, </ww:if>
                            </ww:iterator>
                            </span>
                        </td>
                        <ui:textfield tabindex="@status/index + 1" label="" theme="'single'" name="./resultingScheme/name" value="./resultingSchemeTempName" size="50">
                            <ui:param name="'id'"><ww:property value="./resultingScheme/name"/></ui:param>
                        </ui:textfield>
                    </tr>
                </ww:iterator>
                <ui:component name="'selectedSchemeType'" value="/selectedSchemeType" template="hidden.jsp" theme="'single'" />
                <ui:component name="'typeOfSchemesToDisplay'" value="/typeOfSchemesToDisplay" template="hidden.jsp" theme="'single'" />
                </tbody>
            </table>
            </ww:if>
            <ww:else>
                <ul class="optionslist">
                    <li><a id="return_link" href="<%=request.getContextPath()%>/secure/admin/SchemeTypePicker!default.jspa"><ww:text name="'admin.scheme.comparison.return.link'"/></a></li>
                </ul>
            </ww:else>
        </td>
    </tr>
</page:applyDecorator>



<script type="text/javascript">
    initializeInputBoxes();
</script>
</body>
</html>
