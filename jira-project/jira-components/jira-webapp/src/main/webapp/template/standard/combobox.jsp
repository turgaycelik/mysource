<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- combobox.jsp
  --
  -- Required Parameters:
  --   * label  - The description that will be used to identfy the control.
  --   * name   - The name of the attribute to put and pull the result from.
  --              Equates to the NAME parameter of the HTML SELECT tag.
  --   * list   - Iterator that will provide the options for the control.
  --              Equates to the HTML OPTION tags in the SELECT and supplies
  --              both the NAME and VALUE parameters of the OPTION tag.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * size       - SIZE parameter of the HTML INPUT tag.
  --   * maxlength  - MAXLENGTH parameter of the HTML INPUT tag.
  --   * disabled   - DISABLED parameter of the HTML INPUT tag.
  --   * onkeyup    - onkeyup parameter of the HTML INPUT tag.
  --   * tabindex  - tabindex parameter of the HTML INPUT tag.
  --   * onchange  - onkeyup parameter of the HTML INPUT tag.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>
<input type="text"
       name="<ww:property value="parameters['name']"/>"
         <ww:property value="parameters['size']">
            <ww:if test=".">size="<ww:property value="."/>"</ww:if>
         </ww:property>
         <ww:property value="parameters['maxlength']">
            <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
         </ww:property>
         <ww:property value="parameters['nameValue']">
            <ww:if test=".">value="<ww:property value="."/>"</ww:if>
         </ww:property>
         <ww:property value="parameters['disabled']">
            <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
         </ww:property>
         <ww:property value="parameters['onkeyup']">
            <ww:if test=".">onkeyup="<ww:property value="."/>"</ww:if>
         </ww:property>
         <ww:property value="parameters['tabindex']">
            <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
         </ww:property>
         <ww:property value="parameters['onchange']">
            <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
         </ww:property>
><br>
<ww:property value="parameters['list']">
   <ww:if test=".">
      <select onChange="this.form.elements['<ww:property value="parameters['name']"/>'].value=this.options[this.selectedIndex].value"
         <ww:property value="parameters['disabled']">
            <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
         </ww:property>
      >
            <ww:iterator value=".">
               <option value="<ww:property value="."/>" <ww:if test="parameters['nameValue'] == .">SELECTED</ww:if>><ww:property value="."/>
               </option>
            </ww:iterator>
      </select>
   </ww:if>
</ww:property>
<%@ include file="/template/standard/controlfooter.jsp" %>
