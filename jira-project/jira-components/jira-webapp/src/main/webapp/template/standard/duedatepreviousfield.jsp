<%@ taglib uri="webwork" prefix="ww" %>
<input type="text"
       name="<ww:property value="parameters['nameNext']"/>"
      <ww:property value="parameters['sizeNext']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['maxlengthNext']">
         <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['valueNext']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['disabledNext']">
         <ww:if test="{parameters['disabledNext']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['readonly']">
         <ww:if test="{parameters['readonlyNext']}">READONLY</ww:if>
      </ww:property>
      <ww:property value="parameters['onkeyupNext']">
         <ww:if test=".">onkeyup="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindexNext']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchangeNext']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['styleNext']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
>
