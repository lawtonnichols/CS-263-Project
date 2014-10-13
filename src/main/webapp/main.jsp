<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<% 
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<html>
<body>
    <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
        Row: 
        <select name="row">
        	<option value="1">1</option>
        	<option value="2">2</option>
        	<option value="3">3</option>
        </select>
        Column: 
        <select name="col">
        	<option value="1">1</option>
        	<option value="2">2</option>
        	<option value="3">3</option>
        </select>
        <input type="file" name="file">
        <input type="submit" value="Submit">
    </form>
</body>
</html>