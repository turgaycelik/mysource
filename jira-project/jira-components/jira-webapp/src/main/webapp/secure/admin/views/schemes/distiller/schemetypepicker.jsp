<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.scheme.type.picker.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/> 
</head>

<body>

<script type="text/javascript">
    function submitForm(typeOfSchemesToDisplay)
    {
        document.forms['jiraform'].action = "SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=" + typeOfSchemesToDisplay + "&selectedSchemeType=" + document.getElementById("selectedSchemeType").value;
        document.forms['jiraform'].submit();
        return false;
    }
</script>

<page:applyDecorator name="jiraform">
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.scheme.type.picker.title'"/></page:param>
    <page:param name="description"><ww:text name="'admin.scheme.type.picker.desc'"/></page:param>
    <page:param name="helpURL">scheme_tools</page:param>
    <page:param name="action">SchemeTypePicker.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="submitId">analyse_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.scheme.type.picker.analyse'"/></page:param>
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
            <h4><ww:text name="'admin.scheme.type.picker.select.type'"/></h4>
            <ui:select label="" name="'selectedSchemeType'" list="/schemeTypes" listKey="'./value'" listValue="'./key'" theme="'single'">
                <ui:param name="'mandatory'" value="true"/>
                <ui:param name="'noTable'" value="true"/>
                <ui:param name="'id'" value="'selectedSchemeType'"/>
            </ui:select>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
