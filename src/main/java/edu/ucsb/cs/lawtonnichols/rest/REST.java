package edu.ucsb.cs.lawtonnichols.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import edu.ucsb.cs.lawtonnichols.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import org.glassfish.jersey.internal.util.Base64;
import org.json.*;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.files.*;


@Path("/")
public class REST {
	@GET
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "Hello, world! GET";
	}
	
	@POST
	@Path("posttest")
	@Produces(MediaType.TEXT_PLAIN)
	public String posttest(@FormParam("arg") String arg) {
		return "Hello, world! POST\narg: " + arg + "\n";
	}
	
	@POST
	@Path("downvote")
	@Produces(MediaType.APPLICATION_JSON)
	public Response downvote(@FormParam("jsonData") String json) {
		// convert the string to a json object
		JSONObject obj = new JSONObject(json);
		// get the row and column from the json
		int row = obj.getInt("row");
		int col = obj.getInt("col");
		String ret = "{\"action\": \"downvoted\", \"row\": \"" + row + "\", \"col\": \"" + col + "\"}";
		int index = (row - 1) * 3 + col;
		// only do stuff if we got a valid index
		if (1 <= index && index <= 9 )
			NineTiles.PopFront(index);
		return Response.status(200).entity(ret).build();
	}
	
	@POST
	@Path("postImage")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postImage(@FormParam("imageData") String imageData, @FormParam("row") int row, @FormParam("col") int col) {
		byte[] image = Base64.decode(imageData.getBytes());
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		
		// thank you to http://stackoverflow.com/questions/12328622/writing-byte-array-to-gae-blobstore
		try {
			FileService fileService = FileServiceFactory.getFileService();
			AppEngineFile file = fileService.createNewBlobFile("image/png"); 
			FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
			writeChannel.write(java.nio.ByteBuffer.wrap(image));
			writeChannel.closeFinally();
			BlobKey blobKey = fileService.getBlobKey(file);
			// sanitize input
			if (1 <= row && row <= 3 && 1 <= col && col <= 3)
				NineTiles.AddImageToTaskQueue(row, col, blobKey);
			
		} catch (Exception e) {
			
		}
		
		return Response.status(200).entity("{\"success\": \"true\"}").build();
	}
	
	@GET
	@Path("getAllTiles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTiles() {		
		String ret = "{\n";

		// ask the memcache for all the images
		for (int i = 1; i <= 9; i++) {
			String tileImage = NineTiles.GetImageForTile(i);
			ret += "    \"" + i + "\": \"" + tileImage + "\"";
			if (i < 9) ret += ",\n";
			else ret += "\n";
		}
		
		ret += "}\n";
		
		
		return Response.status(200).entity(ret).build();
	}
}
