<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.csvimport.import.data.from'">
        <ww:param name="'value0'"><ww:property value="/title" /></ww:param>
    </ww:text> </title>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action"><ww:property value="/actionName" />!viewLogs.jspa</page:param>
    <page:param name="onsubmit">document.getElementById('importLogs').value='';return true;</page:param>
    <page:param name="submitId">refresh_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
    <page:param name="title"><ww:text name="'admin.csvimport.import.data.from'">
        <ww:param name="'value0'"><ww:property value="/title" /></ww:param>
    </ww:text></page:param>
    <ww:if test="importer/aborted == false">
        <page:param name="buttons">
            <input class="aui-button" type="submit" name="abortButton" value="<ww:text name="'admin.csvimport.abort.import'"/>" />
        </page:param>
    </ww:if>

    <tr>
    <td colspan="2">
        <ww:if test="importer/aborted == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.csvimport.import.in.progress'"><ww:param name="'value0'"><ww:property value="/title" /></ww:param></ww:text></p>
                    <p><ww:text name="'admin.csvimport.abort.instruction'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                            <ww:param name="'value2'"><strong><ww:text name="'admin.csvimport.abort.import'"/></strong></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>

        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.csvimport.cancelling.import'"><ww:param name="'value0'"><ww:property value="/title" /></ww:param></ww:text></p>
                    <p>
                        <ww:text name="'admin.csvimport.you.have.chosen.to.abort'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                        </ww:text>
                        <ww:if test="/importer/stats/issuesImported > 0">
                            <ww:text name="'admin.csvimport.you.have.imported'">
                                <ww:param name="'value0'"><ww:property value="/importer/stats/issuesImported" /></ww:param>
                            </ww:text>
                        </ww:if>
                    </p>
                </aui:param>
            </aui:component>
        </ww:else>

        <p>
        <ww:text name="'admin.csvimport.logs.refreshed'">
            <ww:param name="'value0'"><b><ww:property value="refreshInterval" /></b></ww:param>
            <ww:param name="'value1'"><a href="#bottom"></ww:param>
            <ww:param name="'value2'"></a></ww:param>
        </ww:text>
        </p>

    <%@include file="../importlogs.jsp" %>
    </td>
    </tr>

    <ui:textfield label="text('admin.csvimport.page.refresh.interval')" name="'refreshInterval'" value="refreshInterval"  >
        <ui:param name="'description'" value="text('admin.csvimport.page.refresh.interval.description')" />
        <ui:param name="'extrahtml'" ><a name="bottom">&nbsp;</a></ui:param>
    </ui:textfield>

    <ui:component name="'importLocation'" template="hidden.jsp" theme="'single'"/>
    <ui:component name="'configFileLocation'" template="hidden.jsp" theme="'single'"/>


</page:applyDecorator>

<script language="JavaScript">
<!--
    setTimeout("document.getElementById('importLogs').value='';document.jiraform.submit();", (<ww:property value="refreshInterval" /> * 1000));
//-->
</script>

</body>
</html>
