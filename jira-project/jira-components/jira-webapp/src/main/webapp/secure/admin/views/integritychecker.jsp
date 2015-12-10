<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.integritychecker.integrity.checker'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="integrity_checker"/>    
</head>
<body>
<script language="JavaScript">
    function changeAll(checkbox)
    {
        checkStartingWith("<ww:property value="/integrityCheckPrefix"/>", checkbox.checked);
    }

    function changeIntegrity(checkbox)
    {
        checkStartingWith("<ww:property value="/checkPrefix"/>" + checkbox.name.split("_")[1], checkbox.checked);
    }

    function checkStartingWith(prefix, checked)
    {
        var elements = document.forms['jiraform'].elements;
        for (var i=0;i<elements.length;i++)
        {
            if (elements[i].name.indexOf(prefix) == 0)
            {
                elements[i].checked = checked;
            }
        }
    }
</script>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.integritychecker.integrity.checker'"/></page:param>
    <page:param name="action">IntegrityChecker.jspa</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="buttons">
        <input class="aui-button" type="submit" name="check" value="<ww:text name="'admin.common.words.check'"/>" />
    </page:param>

    <page:param name="description">
        <p><ww:text name="'admin.integritychecker.select.one.or.more.integrity.checks'"/></p>
    </page:param>

    <tr>
        <td>
            <table class="aui aui-table-rowhover">
                <thead>
                    <tr>
                        <th width="1%">
                            <input id="selectAllChecks" type="checkbox" name="all" onClick="changeAll(this)" />
                        </th>
                        <th>
                            <label for="selectAllChecks"><ww:text name="'admin.integritychecker.select.all.checks'"/></label>
                        </th>
                    </tr>
                </thead>
                <tbody>
                <ww:iterator value="integrityChecks" status="'status'">
                    <tr>
                        <td>
                            <ww:if test="./available == true">
                                <input type="checkbox" name="integrity_<ww:property value="./id"/>" id="integrity_<ww:property value="./id"/>" value="<ww:property value="./id"/>" onClick="changeIntegrity(this)">
                            </ww:if>
                            <ww:else>
                                &nbsp;
                            </ww:else>
                        </td>
                        <td>
                            <label for="integrity_<ww:property value="./id"/>"><ww:property value="./description" /></label>
                            <ww:iterator value="./checks">
                                <div class="integrity-checks">
                                    <ww:if test="available == true">
                                        <ww:component name="/checkId(.)" value="/checked(.)" label="''" template="checkbox.jsp" theme="'single'" >
                                            <ww:param name="'fieldValue'" value="./id" />
                                            <ww:param name="'id'" value="/checkId(.)" />
                                        </ww:component>
                                        <label for="<ww:property value="/checkId(.)"/>"><ww:property value="./description"/></label>
                                    </ww:if>
                                    <ww:else>
                                        <div class="secondary-text"><ww:property value="./description"/> - <ww:property value="unavailableMessage"/></div>
                                    </ww:else>
                                </div>
                            </ww:iterator>
                        </td>
                    </tr>
                </ww:iterator>
                </tbody>
            </table>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
