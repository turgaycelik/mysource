<%@ taglib uri="webwork" prefix="ww" %>
<%-- Only show message if errors are available. This will be done if ActionFormSupport is used. --%>
<ww:property value="errors[parameters['namePrevious']]">
   <ww:if test=".">
      <tr>
        <ww:if test="parameters['labelposition'] == 'top'">
            <td align="left" valign="top" colspan="2" class="formErrors">
        </ww:if>
        <ww:else>
			<td class="formErrors">&nbsp;</td>
            <td valign="top" class="formErrors">
        </ww:else>
            <span class="errMsg"><ww:property value="."/></span>
        </td>
      </tr>
   </ww:if>
</ww:property>
<ww:property value="errors[parameters['nameNext']]">
   <ww:if test=".">
      <tr>
        <ww:if test="parameters['labelposition'] == 'top'">
            <td align="left" valign="top" colspan="2" class="formErrors">
        </ww:if>
        <ww:else>
			<td class="formErrors">&nbsp;</td>
            <td valign="top" class="formErrors">
        </ww:else>
            <span class="errMsg"><ww:property value="."/></span>
        </td>
      </tr>
   </ww:if>
</ww:property>
<%-- if the label position is top, then give the label it's own row in the table --%>
<ww:if test="parameters['labelposition'] == 'top'">
<tr>
    <td align="left" valign="top" colspan="2"<ww:if test="errors[parameters['namePrevious']] || errors[parameters['nameNext']]"> class="formErrors"</ww:if>>
        <span class="label"><ww:property value="parameters['label']"/>:</span>
    </td>
</tr>
<tr>
    <td colspan="2"<ww:if test="errors[parameters['namePrevious']] || errors[parameters['nameNext']]"> class="formErrors"</ww:if>>
</ww:if>
<ww:else>
<tr>
    <td align="right" valign="top"<ww:if test="errors[parameters['namePrevious']] || errors[parameters['nameNext']]"> class="formErrors"</ww:if>>
        <span class="label"><ww:property value="parameters['label']"/></span>
    </td>
    <td<ww:if test="errors[parameters['namePrevious']] || errors[parameters['nameNext']]"> class="formErrors"</ww:if>>
</ww:else>