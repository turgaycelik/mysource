<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>

<ww:iterator value="parameters">
	<li><ww:property value="key" /> = <ww:property value="value" /></li>
</ww:iterator>

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
>

   <ww:iterator value="parameters['list']">
      <option value="<ww:property value="."/>" <ww:if test="parameters['nameValue'] == .">SELECTED</ww:if>>
            <ww:property value="."/>
      </option>
   </ww:iterator>

</select>

<%@ include file="/template/standard/controlfooter.jsp" %>
