<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.scheme.purge.picker.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>     
</head>

<body>

<script language="JavaScript" type="text/javascript">


    function swapTable(sel1)
    {
        // Feature test to see if there is enough W3C DOM support
        if (document.getElementById && document.getElementsByTagName)
        {

            // Obtain references to all cloned options
            var options = sel1.getElementsByTagName("option");

            for (var i = 0; i < options.length; i++)
            {
                var div = document.getElementById(options[i].value);
                if (options[i].selected)
                {
                    div.style.display = '';
                }
                else
                {
                    div.style.display = 'none';
                }
            }
        }
        return false;
    }

    function toggleCheckboxes(checked, schemeType)
    {
        var checkboxes = getElementsByAttribute("schemeType", "input", schemeType);

        // run through all the checkboxes in the row and uncheck
        for (var i = 0; i < checkboxes.length; i++)
        {
            var checkbox = checkboxes[i];
            checkbox.checked = checked;
        }

        return false;
    }

    function getElementsByAttribute(attribute, tag, name)
    {
        var elem = document.getElementsByTagName(tag);
        var arr = new Array();
        for (i = 0,iarr = 0; i < elem.length; i++)
        {
            att = elem[i].getAttribute(attribute);
            if (att == name)
            {
                arr[iarr] = elem[i];
                iarr++;
            }
        }
        return arr;
    }

    function clearCheckboxesForUnselectedType()
    {
        if (document.getElementById && document.getElementsByTagName)
        {

            var sel1 = document.getElementById("selectedSchemeType_select")

            // Obtain references to all cloned options
            var options = sel1.getElementsByTagName("option");

            for (var i = 0; i < options.length; i++)
            {
                var div = document.getElementById(options[i].value);
                if (!options[i].selected)
                {
                    toggleCheckboxes(false, options[i].value);
                }
            }
        }
        return true;
    }

</script>

<p>
    <page:applyDecorator name="jiraform">
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.purge.picker.title'"/></page:param>
        <page:param name="description">
            <ww:text name="'admin.scheme.purge.picker.desc'">
               <ww:param name="'value0'"><p/></ww:param>
            </ww:text>
        </page:param>
        <page:param name="action">SchemePurgeTypePicker.jspa</page:param>
        <page:param name="submitId">preview_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.preview'"/></page:param>
        <page:param name="onsubmit">clearCheckboxesForUnselectedType();</page:param>


        <ui:select label="text('admin.scheme.type.picker.select.type')" name="'selectedSchemeType'" list="/schemeTypes"
                   listKey="'./value'" listValue="'./key'" onchange="'swapTable(this);'">
            <ui:param name="'mandatory'" value="true"/>
            <ui:param name="'id'" value="'selectedSchemeType'"/>
        </ui:select>

        <tr><td colspan="2">

            <ww:iterator value="/schemeTypes" status="'status'">
                <div id="<ww:property value="./value"/>" <ww:if test="./value/equals(/selectedSchemeType) == false">style="display:none;"</ww:if>>
                    <ww:if test="/unassociatedSchemes(./value)/size() != 0">
                        <table class="grid defaultWidth centered" id="purge_schemes_<ww:property value="./value"/>">
                        <th width="2%"><input type="checkbox" id="selectAll"
                                              onclick="toggleCheckboxes(this.checked, '<ww:property value="./value"/>');"/>
                        </th>
                        <th width="49%"><ww:text name="'admin.scheme.purge.picker.scheme.name'"/></th>
                        <th width="49%"><ww:text name="'common.concepts.description'"/></th>

                        <ww:iterator value="/unassociatedSchemes(./value)" status="'status'">
                            <tr class="<ww:if test="@status/odd == true">rowNormal</ww:if><ww:else>rowAlternate</ww:else>">
                                <td valign="top"><input type="checkbox" name="selectedSchemeIds"
                                                        id="checkbox_<ww:property value="@status/index"/>"
                                                        value="<ww:property value="./id"/>"
                                                        schemeType="<ww:property value="../value"/>"
                                        <ww:if test="/selectedSchemeIdsAsList/contains(./id/toString()) == true && /selectedSchemeType/equals(./type) == true">
                                            checked</ww:if> /> <!-- NOTE: there must be a space between checked and the closing tag for safari to work -->
                                </td>
                                <td>
                                    <ww:property value="./name"/>
                                </td>
                                <td>
                                    <ww:property value="./description"/>
                                </td>
                            </tr>
                        </ww:iterator>
                    </ww:if>
                    <ww:else>
                        <table class="defaultWidth centered" id="merged_schemes">
                        <tr>
                            <td>
                                <div class="infoBox">
                                    <ww:text name="'admin.scheme.purge.picker.no.unassociated.schemes'"/>
                                </div>
                            </td>
                        </tr>
                    </ww:else>
                    </table>
                </div>
            </ww:iterator>
        </td></tr>
    </page:applyDecorator>
</p>


</body>
</html>
