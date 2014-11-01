<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="edu.ucsb.cs.lawtonnichols.*" %>
<% 
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>
<html>
<body>
	<h1>NineTiles</h1>
    <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
        <p>Row: 
	        <select name="row">
	        	<option value="1">1</option>
	        	<option value="2">2</option>
	        	<option value="3">3</option>
	        </select>
	    </p>
        <p> Column: 
	        <select name="col">
	        	<option value="1">1</option>
	        	<option value="2">2</option>
	        	<option value="3">3</option>
	        </select>
	    </p>
	    <p>
	        <input type="file" name="file">
	        <input type="submit" value="Submit">
	    </p>
    </form>
    <table>
        <tr>
            <td><img src="<%= NineTiles.GetImageForTile(1) %>" height="150px" width="150px" /></td>
            <td><img src="<%= NineTiles.GetImageForTile(2) %>" height="150px" width="150px" /></td>
            <td><img src="<%= NineTiles.GetImageForTile(3) %>" height="150px" width="150px" /></td>
        </tr>
        <tr>
            <td><img src="<%= NineTiles.GetImageForTile(4) %>" height="150px" width="150px" /></td>
            <td><img src="<%= NineTiles.GetImageForTile(5) %>" height="150px" width="150px" /></td>
            <td><img src="<%= NineTiles.GetImageForTile(6) %>" height="150px" width="150px" /></td>
        </tr>
        <tr>
            <td><img src="<%= NineTiles.GetImageForTile(7) %>" height="150px" width="150px" /></td>
            <td><img src="<%= NineTiles.GetImageForTile(8) %>" height="150px" width="150px" /></td>
            <td><img src="<%= NineTiles.GetImageForTile(9) %>" height="150px" width="150px" /></td>
        </tr>
    </table>
</body>
</html>