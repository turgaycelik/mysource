<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/single/controlheader.jsp" %>
<select name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['disabled']">
         <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchange']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['style']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['size']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
        <ww:property value="parameters['id']">
         <ww:if test=".">id="<ww:property value="."/>"</ww:if>
      </ww:property>
>
   <ww:property value="parameters['headerrow']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue']" />"
		<ww:if test="../parameters['headervalue'] == ../parameters['nameValue']">selected</ww:if>
		><ww:property value="parameters['headerrow']" /></option></ww:if>
   </ww:property>
   <ww:property value="parameters['headerrow2']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue2']" />"
		<ww:if test="../parameters['headervalue2'] == ../parameters['nameValue']">selected</ww:if>
		><ww:property value="parameters['headerrow2']" /></option></ww:if>
   </ww:property>

   <%-- Check if an explicit selected value has been specified --%>
   <ww:if test="parameters['selectedValue']">
   <%-- If it has, compare each value to the specified selected value and if they are equal prit 'SELECTED' next to it --%>
   <ww:iterator value="parameters['list']">
	  <option value="<ww:property value="{parameters['listKey']}"/>" <ww:if test="{parameters['listKey']} == parameters['selectedValue']">SELECTED</ww:if>>
         <ww:property value="{parameters['listValue']}"/>
      </option>
   </ww:iterator>
   </ww:if>
   <ww:else>
   <%-- Otherwise use the 'nameValue' for xomparison and selection of the value in the list --%>
    <ww:iterator value="parameters['list']">
	  <option value="<ww:property value="{parameters['listKey']}"/>" <ww:if test="{parameters['listKey']} == parameters['nameValue']">SELECTED</ww:if>>
         <ww:property value="{parameters['listValue']}"/>
      </option>
   </ww:iterator>
   </ww:else>
</select>
<%@ include file="/template/single/controlfooter.jsp" %>
