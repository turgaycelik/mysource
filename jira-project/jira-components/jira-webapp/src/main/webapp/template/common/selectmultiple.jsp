<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- select.jsp
  --
  -- Required Parameters:
  --   * label  - The description that will be used to identfy the control.
  --   * name   - The name of the attribute to put and pull the result from.
  --              Equates to the NAME parameter of the HTML SELECT tag.
  --   * list   - Iterator that will provide the options for the control.
  --              Equates to the HTML OPTION tags in the SELECT and supplies
  --              both the NAME and VALUE parameters of the OPTION tag.
  --   * listKey   - Where to get the values for the OPTION tag.  Equates to
  --                 the VALUE parameter of the OPTION tag.
  --   * listValue - The value displayed by the control.  Equates to the body
  --                 of the HTML OPTION tag.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in
relation
  --                       to the control.  Default is to the left of the
control.
  --   * disabled        - DISABLED parameter of the HTML SELECT tag.
  --   * tabindex        - tabindex parameter of the HTML SELECT tag.
  --   * onchange        - onkeyup parameter of the HTML SELECT tag.
  --   * size            - the size parameter of the HTML SELECT tag.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<select multiple name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['id']">
         <ww:if test=".">id="<ww:property value="."/>"</ww:if>
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
      <ww:property value="parameters['onclick']">
         <ww:if test=".">onclick="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['style']"><ww:if test=".">style="<ww:property value="."/>"</ww:if></ww:property>
      <ww:property value="parameters['class']">
         <ww:if test=".">class="<ww:property value="."/>"</ww:if>
      </ww:property>
	  <ww:property value="parameters['size']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
>
   <ww:property value="parameters['headeroptgroup']">
      <ww:if test=". && . != ''"><optgroup label="<ww:property value="parameters['headeroptgroup']"/>"></ww:if>
   </ww:property>

    <ww:property value="parameters['headerrow']">
      <ww:if test=". && . != ''"><option <ww:property value="parameters['optionIcon']"><ww:if test="."> class="selectall" </ww:if></ww:property> value="<ww:property value="parameters['headervalue']" />" <ww:if test="parameters['nameValue']"><ww:iterator value="parameters['nameValue']"><ww:if test=". == parameters['headervalue']"> SELECTED</ww:if></ww:iterator></ww:if>><ww:property value="parameters['headerrow']" /></option></ww:if>
   </ww:property>
   <ww:property value="parameters['headerrow2']">
      <ww:if test=". && . != ''"><option <ww:property value="parameters['optionIcon']"><ww:if test="."> class="selectall" </ww:if></ww:property> value="<ww:property value="parameters['headervalue2']" />" <ww:if test="parameters['nameValue']"><ww:iterator value="parameters['nameValue']"><ww:if test=". == parameters['headervalue2']"> SELECTED</ww:if></ww:iterator></ww:if>><ww:property value="parameters['headerrow2']" /></option></ww:if>
   </ww:property>

   <%-- JRA-6147 - The equality comparison to determine if the select option has been selected will not work if the iterator is passed Long objects. --%>
   <%-- Need to ensure that the iterator is passed strings --%>
   <ww:iterator value="parameters['list']">
		<ww:property value="./{parameters['listKey']}"><option value="<ww:property value="."/>"<ww:if test="parameters['nameValue']"><ww:iterator value="parameters['nameValue']"><ww:if test=". == .."> SELECTED</ww:if></ww:iterator></ww:if></ww:property>
        <ww:property value="parameters['optionIcon']"><ww:if test="."> class="imagebacked" style="background-image: url(<ww:url value="../{.}" />);"</ww:if></ww:property>
        <ww:property value="parameters['optionTitle']"><ww:if test="."> title="<ww:property value="../{.}" />"</ww:if></ww:property>
        >
        <ww:if test="parameters['internat'] == true">
            <ww:text name="{parameters['listValue']}"/>
        </ww:if>
        <ww:else>
            <ww:property value="{parameters['listValue']}"/>
        </ww:else>
        </option>
   </ww:iterator>
</select>
