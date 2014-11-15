<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="edu.ucsb.cs.lawtonnichols.*" %>
<% 
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>
<html>
<head>
<title>NineTiles</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap-theme.min.css">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js"></script>
<script src="https://code.jquery.com/jquery-2.1.1.min.js"></script>
<style>
body {background-color: #fdfdfd;}
div#table {width: 456px; margin: 0 auto;}
div#upload {
    width: 400px;
    padding: 1em;
    margin: 0px auto;
    background-color: #FDFDFD;
    position: absolute;
    left: 0px;
    right: 0px;
    top: 100px;
    border: 1px solid #D0D0D0;
    display: none;
}
input[type="file"] {display: inline;}
/*table td {border-bottom: 1px solid #fdfdfd;}
table tr:last-child td {border-bottom: none;}
table td {border-left: 1px solid #fdfdfd;}
table td:first-child {border-left: none;}*/
table {border-collapse: separate; border-spacing: 3px 3px;}
video#myVideo {display: none;}
canvas#myCanvas {width: 370px; margin: 0 auto;}
</style>
<script>

// taken from http://matthewschrager.com/2013/05/25/how-to-take-webcam-pictures-from-browser-and-store-server-side/
navigator.getUserMedia = ( navigator.getUserMedia ||
                       navigator.webkitGetUserMedia ||
                       navigator.mozGetUserMedia ||
                       navigator.msGetUserMedia);
                       
window.URL = (window.URL || window.mozURL || window.webkitURL);

function showUpload(i) {
	var row = Math.floor((i-1) / 3) + 1;
	var col = Math.floor(i % 3);
	if (col == 0)
		col = 3;
	$("#selectrow").val(row);
	$("#selectcol").val(col);
	$("#upload").show();
}

function downvote(i) {
	var row = Math.floor((i-1) / 3) + 1;
	var col = Math.floor(i % 3);
	if (col == 0)
		col = 3;
	var downvoteObject = JSON.stringify({"row": row, "col": col});
	$.post("/rest/downvote", {"jsonData": downvoteObject});
}

function updateImages(result) {
	document.getElementById("image1").src = result[1];
	document.getElementById("image2").src = result[2];
	document.getElementById("image3").src = result[3];
	document.getElementById("image4").src = result[4];
	document.getElementById("image5").src = result[5];
	document.getElementById("image6").src = result[6];
	document.getElementById("image7").src = result[7];
	document.getElementById("image8").src = result[8];
	document.getElementById("image9").src = result[9];
}

$(document).ready(function () {
	$("#image1").mousedown(function (e) {
		if (e.which == 3)
			showUpload(1);
		else if (e.which == 1)
			downvote(1);
	});
	$("#image2").mousedown(function (e) {
		if (e.which == 3)
			showUpload(2);
		else if (e.which == 1)
			downvote(2);
	});
	$("#image3").mousedown(function (e) {
		if (e.which == 3)
			showUpload(3);
		else if (e.which == 1)
			downvote(3);
	});
	$("#image4").mousedown(function (e) {
		if (e.which == 3)
			showUpload(4);
		else if (e.which == 1)
			downvote(4);
	});
	$("#image5").mousedown(function (e) {
		if (e.which == 3)
			showUpload(5);
		else if (e.which == 1)
			downvote(5);
	});
	$("#image6").mousedown(function (e) {
		if (e.which == 3)
			showUpload(6);
		else if (e.which == 1)
			downvote(6);
	});
	$("#image7").mousedown(function (e) {
		if (e.which == 3)
			showUpload(7);
		else if (e.which == 1)
			downvote(7);
	});
	$("#image8").mousedown(function (e) {
		if (e.which == 3)
			showUpload(8);
		else if (e.which == 1)
			downvote(8);
	});
	$("#image9").mousedown(function (e) {
		if (e.which == 3)
			showUpload(9);
		else if (e.which == 1)
			downvote(9);
	});
	
	$("#closeButton").click(function (e) {
		$("#upload").hide();
	});
	
	$(document).on('contextmenu', function(e) {
	    e.preventDefault();
	});
	
	$("#submitImage").click(function() {
		// extract the image data from the canvas in base 64
		// encoded JPG format
		var imgData = canvas.toDataURL('img/png');
		
		console.log(imgData);

		// remove extraneous data from start of string
		imgData = imgData.replace('data:image/png;base64,', '');
		
		console.log(imgData);
		
		var r = $("#selectrow").val();
		var c = $("#selectcol").val();

		/*// JSON-encode it
		var postData = JSON.stringify({ imageData: imgData, row: r, col: c });

		// Post it to the server
		$.ajax({
			url: '/rest/postImage',
			type: "POST",
			data: postData,
			contentType: "application/json"
		});*/
		
		$.post("/rest/postImage", {"imageData": imgData, "row": r, "col": c});
		
		location.reload();
	});
	
	function drawImage(canvas, video) {
		try {
			canvas.getContext('2d').drawImage(video, 0, 0);
		  } catch (e) {
		    if (e.name == "NS_ERROR_NOT_AVAILABLE") {
		      setTimeout(function () { drawImage(canvas, video); }, 0);
		    } else {
		      throw e;
		    }
  		}
	}
	
	// get the canvas element
	var canvas = document.getElementById("myCanvas");
	
	// get the video element
	var video = document.getElementById("myVideo");
	
	// adapted from https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API/Taking_still_photos
	function takepicture() {
		try {
		    canvas.width = 352;
		    canvas.height = 288;
		    canvas.getContext('2d').drawImage(video, 0, 0, 352, 288);
		    var data = canvas.toDataURL('image/jpeg');
		    photo.setAttribute('src', data);
		} catch (e) {
			setTimeout(takepicture, 0);
		}
	  }
	
	// also taken from http://matthewschrager.com/2013/05/25/how-to-take-webcam-pictures-from-browser-and-store-server-side/
	navigator.getUserMedia({ video: true }, function(stream) {
		// get the video element
		var video = document.getElementById("myVideo");

		// set it to receive input from the webcam
		video.src = window.URL.createObjectURL(stream);

		video.play();
		
		
		

		// Wait 50 milliseconds to allow the webcam to capture some video
		setTimeout(function () {
			// set the canvas height/width to the size of the video
			//canvas.width = video.videoWidth;
			//canvas.height = video.videoHeight;

			// draw the video frame to the canvas
			takepicture();
			
			
			
		}, 50);
	}, function (e) {console.log(e);});
});
</script>
</head>
<body>
	<h1 class="text-center">NineTiles</h1>
	<p class="text-center"><em>Left-click to downvote, Right-click to upload</em></p>
	<div id="upload">
	<h3 class="text-center">Upload</h3>
    <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
        <p class="text-center">Row: 
	        <select name="row" id="selectrow">
	        	<option value="1">1</option>
	        	<option value="2">2</option>
	        	<option value="3">3</option>
	        </select>
	    &nbsp;&nbsp;
        Column: 
	        <select name="col" id="selectcol">
	        	<option value="1">1</option>
	        	<option value="2">2</option>
	        	<option value="3">3</option>
	        </select>
	    </p>
	    <canvas id="myCanvas"></canvas>
	    <video id="myVideo"></video>
	    <p>&nbsp;</p>
	    <p class="text-center">
	        <input type="file" accept="image/*" name="file">
	        <input type="submit" value="Submit File">
	        <input type="button" id="submitImage" value="Submit Current Image">
	    </p>
	    <p class="text-center">
	    	<a href="#" id="closeButton">[close]</a>
	    </p>
    </form>
    </div>
    <div id="table">
    <table>
        <tr>
            <td><img id="image1" src="<%= NineTiles.GetImageForTile(1) %>" height="150px" width="150px" /></td>
            <td><img id="image2" src="<%= NineTiles.GetImageForTile(2) %>" height="150px" width="150px" /></td>
            <td><img id="image3" src="<%= NineTiles.GetImageForTile(3) %>" height="150px" width="150px" /></td>
        </tr>
        <tr>
            <td><img id="image4" src="<%= NineTiles.GetImageForTile(4) %>" height="150px" width="150px" /></td>
            <td><img id="image5" src="<%= NineTiles.GetImageForTile(5) %>" height="150px" width="150px" /></td>
            <td><img id="image6" src="<%= NineTiles.GetImageForTile(6) %>" height="150px" width="150px" /></td>
        </tr>
        <tr>
            <td><img id="image7" src="<%= NineTiles.GetImageForTile(7) %>" height="150px" width="150px" /></td>
            <td><img id="image8" src="<%= NineTiles.GetImageForTile(8) %>" height="150px" width="150px" /></td>
            <td><img id="image9" src="<%= NineTiles.GetImageForTile(9) %>" height="150px" width="150px" /></td>
        </tr>
    </table>
    </div>
    <script>
    setInterval(function(){
  	  $.get("/rest/getAllTiles", function(result) {
  	    updateImages(result);
  	  });
  	}, 2000);
    </script>
</body>
</html>