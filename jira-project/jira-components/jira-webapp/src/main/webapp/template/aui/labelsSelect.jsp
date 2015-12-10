<%--
  -- Renders the DOM structure required for the labels field in the edit labels page (system field and custom fields)
  -- and the labels dialog (system field and custom fields).
  --
  -- Attribute parameters:
  -- @param label the label for the <select>
  -- @param id the DOM ID of the <select> element
  --
  -- Required page parameters:
  -- @param issueId the ID of the issue
  -- @param labels a List<String> of the labels
  -- @param errorCollectionKey the key used to retrieve errors for this field
  --
  -- For additional optional params see
  -- - formFieldLabel.jsp
  -- - formFieldIcon.jsp
  -- - formFieldError.jsp
  --%>

<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:if test="parameters['labelAfter'] != true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>

<%-- The DOM structure here should match labels-edit.vm and edit-label.vm for JavaScript initialization to work. --%>
<fieldset class="hidden labelpicker-params">
    <input type="hidden" title="id" value="<ww:property value="parameters['id']"/>">
    <input type="hidden" title="issueId" value="<ww:property value="parameters['issueId']"/>">
</fieldset>
<div class="ajs-multi-select-placeholder textarea long-field"></div>
<select id="<ww:property value="parameters['id']"/>"
        class="multi-select hidden long-field edit-labels-inline"
        name="labels"
        multiple="multiple">
    <optgroup class="labels-all" label="<ww:property value="text('common.words.existing')"/>">
    <ww:iterator value="parameters['labels']">
        <option selected="selected" value="<ww:property value="."/>"><ww:property value="."/></option>
    </ww:iterator>
    </optgroup>
    <optgroup class="labels-new" label="<ww:property value="text('common.words.create')"/>"></optgroup>
    <optgroup class="labels-suggested" label="<ww:property value="text('common.words.suggestions')"/>"></optgroup>
</select>

<div class="description"><ww:property value="text('label.edit.start.typing')"/></div>
<div class="description"><%-- TODO Need the field layout item if we want to display the field description. --%></div>

<ww:if test="parameters['labelAfter'] == true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>

<jsp:include page="/template/aui/formFieldIcon.jsp" />
<jsp:include page="/template/aui/formFieldError.jsp" />
