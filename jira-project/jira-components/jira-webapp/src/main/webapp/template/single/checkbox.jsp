<%@ taglib uri="webwork" prefix="ww" %>
<input type="checkbox" <ww:if test="parameters['nameValue'] == true">checked="checked"</ww:if>
       name="<ww:property value="parameters['name']"/>"
       value="<ww:property value="parameters['fieldValue']"/>"
     <ww:property value="parameters['checked']">
        <ww:if test=".">checked="checked"</ww:if>
     </ww:property>
     <ww:property value="parameters['id']">
        <ww:if test=".">id="<ww:property value="." />"</ww:if>
     </ww:property>
     <ww:property value="parameters['class']">
        <ww:if test=".">class="<ww:property value="." />"</ww:if>
     </ww:property>
     <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="." />"</ww:if>
     </ww:property>     
     <ww:property value="parameters['disabled']">
        <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
     </ww:property>
     <ww:property value="parameters['tabindex']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
     </ww:property>
     <ww:property value="parameters['onchange']">
        <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
     </ww:property>
/>
