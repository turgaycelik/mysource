<%--
  -- controlheader.jsp
  --
  -- Required Parameters:
  --
  -- Optional Parameters:
  --   * errorSameLine   - render errors inline
  --   * rowId           - HTML ID of the row
  --   * rowClass        - CSS class of the row
  --   * rowStyle        - style attribute of the row
  --   * labelposition   - position of the field label
  --   * nolabel         - disable label
  --   * row-data-*      - any parameter starting with a string 'row-data-' will be rendered as data-* attribute of the row
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<ww:property value="errors[parameters['name']]">
<%--Dealing with errors --%>
	<ww:if test=".">
        <ww:if test="!parameters['errorSameLine']">

            <tr <ww:property value="parameters['rowClass']"><ww:if test=".">class="<ww:property value="." />"</ww:if></ww:property>
                <ww:property value="parameters['rowStyle']"><ww:if test=".">style="<ww:property value="." />"</ww:if></ww:property>
                >
        </ww:if>
        <ww:if test="parameters['labelposition'] == 'top'">
			<td class="fieldLabelArea" colspan="2">
		</ww:if>
		<ww:else>
            <ww:if test="!parameters['nolabel']">
                <td class="fieldLabelArea formErrors">&nbsp;</td>
            </ww:if>
            <td class="fieldValueArea formErrors" <ww:property value="parameters['valueColSpan']"><ww:if test=".">colspan="<ww:property value="." />"</ww:if></ww:property>>
		</ww:else>
			<span class="errMsg"><ww:property value="."/></span>
			<ww:property value="errors[parameters['name']/concat('Exception')]">
			<ww:if test=".">
				<script language="javascript">
				<!--
				function toggle(id) {
					var element = document.getElementById(id);
					with (element.style) {
						if ( display == "none" ){
							display = ""
						} else{
							display = "none"
						}
					}
					var text = document.getElementById(id + "-switch").firstChild;
					if (text.nodeValue == "[show]") {
						text.nodeValue = "[hide]";
					} else {
						text.nodeValue = "[show]";
					}
				}
				//-->
				</script>
				<br />
				<b>Extra Information:</b>
				<span style="cursor: pointer; margin-left: 5px; text-decoration: underline;" id="<ww:property value="../parameters['name']/concat('Exception')"/>-switch" onclick="toggle('<ww:property value="../parameters['name']/concat('Exception')"/>')">[show]</span>
				<div id="<ww:property value="../parameters['name']/concat('Exception')"/>" style="display:none"><ww:property value="." escape="false"/></div>
			</ww:if>
			</ww:property>
		</td>
        <ww:if test="!parameters['errorSameLine']">

            </tr>
        </ww:if>
    </ww:if>
</ww:property>

<ww:if test="!parameters['nolabel']">
	<tr <ww:property value="parameters['rowClass']"><ww:if test=".">class="<ww:property value="." />"</ww:if></ww:property>
        <ww:property value="parameters['rowStyle']"><ww:if test=".">style="<ww:property value="." />"</ww:if></ww:property>
        <ww:property value="parameters['rowId']"><ww:if test=".">id="<ww:property value="." />"</ww:if></ww:property>
        <ww:iterator value="parameters/keySet">
             <ww:if test="./startsWith('row-data-') == true"> <ww:property value="./substring(4)" />="<ww:property value="parameters[.]" />"</ww:if>
        </ww:iterator>
    >
		<ww:if test="parameters['labelposition'] == 'top'">
			<td colspan="2" class="fieldLabelArea">
		</ww:if>
		<ww:else>
			<ww:if test="errors[parameters['name']]">
				<td class="fieldLabelArea formErrors">
			</ww:if>
			<ww:else>
				<td class="fieldLabelArea">
			</ww:else>
		</ww:else>
        <ww:property value="parameters['label']">
        <ww:if test=". && ./equals('') == false">
 		<ww:if test="parameters['mandatory'] == true"><ww:property value="."/><span class="icon icon-required"><span>(<ww:text name="'common.forms.requiredfields'"/>)</span></span></ww:if>
		<ww:else><ww:property value="."/></ww:else>
        </ww:if>
        </ww:property>
		</td>

	<%-- add the extra row  --%>
	<ww:if test="parameters['labelposition'] == 'top'">
		</tr><tr <ww:property value="parameters['rowClass']">class="<ww:property value="." />"</ww:property>
                <ww:property value="parameters['rowStyle']">style="<ww:property value="." />"</ww:property>>
	</ww:if>
</ww:if>

<%-- valueColSpan - controls how many columns the value TD spans --%>
<ww:if test="errors[parameters['name']]">
	<td class="fieldValueArea formErrors" <ww:property value="parameters['valueColSpan']"><ww:if test=".">colspan="<ww:property value="." />"</ww:if></ww:property>>
</ww:if>
<ww:else>
	<td class="fieldValueArea" <ww:property value="parameters['valueColSpan']"><ww:if test=".">colspan="<ww:property value="." />"</ww:if></ww:property>>
</ww:else>
