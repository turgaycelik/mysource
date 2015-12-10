<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- hidden.jsp
  --
  -- Optional Parameters:
  --   * hint       - hint to display on the page; if not specified, the default random hint will be retrieved from
  --                  the current action's property                    
  --%>
<%@ taglib uri="webwork" prefix="ww" %>

<input type="hidden"
       <ww:if test="parameters['id']">
           id="<ww:property value="parameters['id']"/>"
       </ww:if>
       <ww:else>
           id="<ww:property value="parameters['name']"/>"
       </ww:else>
       name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['nameValue']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
         <ww:elseIf test="parameters['defaultValue']">value="<ww:property value="parameters['defaultValue']" />"</ww:elseIf>
      </ww:property>
     <ww:property value="parameters['cssClass']">
        <ww:if test=".">class="<ww:property value="."/>"</ww:if>
    </ww:property>
/>
