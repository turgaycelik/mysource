<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<ww:if test="!parameters['nobreak']">
<tr><td class="fieldLabelArea">&nbsp;</td><td class="rowClear">&nbsp;</td></tr>
</ww:if>
<tr class="jiraformSectionBreak">
    <td colspan="2">
        <h3 class="formtitle"><ww:property value="parameters['label']"/></h3>
    </td>
</tr>
<ww:property value="parameters['description']" >
<ww:if test=".">
	<tr>
		<td colspan="2" class="jiraformheader">
        <ww:property value="." />
        </td>
	</tr>
</ww:if>
</ww:property>

<ww:property value="parameters['instructions']" >
<ww:if test=".">
	<tr>
		<td colspan="2" class="instructions">
        <ww:property value="." />
        </td>
	</tr>
</ww:if>
</ww:property>
