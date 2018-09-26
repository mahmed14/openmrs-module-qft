<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="qft.replace.this.link.name" /></h2>

<h4>   <c:out value="${status}"/></h4>
<br/>
<h3>File Upload:</h3>
Select a file to upload: <br />
<form method = "post"
      enctype = "multipart/form-data">
    <input type = "file" name = "file" size = "50" />
    <br /> <br />
    <input type = "submit" value = "Upload File" />
</form>
<br/>
<br/>
<c:if test="${not empty errorsList}">
<h3> Unsuccessfull Results</h3>
<c:forEach  var="listVar" items="${errorsList}">
   <c:out value="${listVar}"/>
   <br/>
    </c:forEach>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>
