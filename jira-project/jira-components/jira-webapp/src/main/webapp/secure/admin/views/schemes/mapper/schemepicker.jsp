<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.scheme.picker.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>     
</head>

<body>

<script type="text/javascript">
    function submitForm(typeOfSchemesToDisplay)
    {
        document.forms['jiraform'].action = "SchemePicker!switch.jspa?typeOfSchemesToDisplay=" + typeOfSchemesToDisplay + "&selectedSchemeType=" + document.getElementById("selectedSchemeType").value;
        document.forms['jiraform'].submit();
        return false;
    }
</script>

<page:applyDecorator name="jiraform">
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.scheme.picker.title'"/></page:param>
    <page:param name="helpURL">scheme_tools</page:param>
    <page:param name="description">
        <ww:text name="'admin.scheme.picker.desc'">
           <ww:param name="'value0'"><p/></ww:param>
           <ww:param name="'value1'"><span class="redText"></ww:param>
           <ww:param name="'value2'"></span></ww:param>
        </ww:text>
    </page:param>
    <page:param name="action">SchemePicker.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="submitId">schemepicker_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.scheme.picker.submit'"/></page:param>
    <tr>
        <td>
            <div class="tabwrap tabs2">
                <ul class="tabs horizontal">
                    <li <ww:if test="/typeOfSchemesToDisplay/equals('associated') == true">class="active"</ww:if>>
                        <a href="#" onclick="submitForm('associated')">
                            <strong><ww:text name="'admin.scheme.picker.associated'"/></strong>
                        </a>
                    </li>
                    <li <ww:if test="/typeOfSchemesToDisplay/equals('all') == true">class="active"</ww:if>>
                        <a href="#" onclick="submitForm('all')">
                            <strong><ww:text name="'admin.scheme.picker.all'"/></strong>
                        </a>
                    </li>
                </ul>
                <input type="hidden" name="typeOfSchemesToDisplay" value="<ww:property value='/typeOfSchemesToDisplay'/>"/>
            </div>
            <ww:property value="/schemePickerWebComponentHtml" escape="false"/>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
