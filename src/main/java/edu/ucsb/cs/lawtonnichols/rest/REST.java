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
import com.google.appengine.api.log.*;



@Path("/")
public class REST {
	/**
	 * A test of REST GET functionality 
	 */
	@GET
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "Hello, world! GET";
	}
	
	/**
	 * A test of REST POST functionality 
	 */
	@POST
	@Path("posttest")
	@Produces(MediaType.TEXT_PLAIN)
	public String posttest(@FormParam("arg") String arg) {
		return "Hello, world! POST\narg: " + arg + "\n";
	}
	
	/**
	 * Handles a POST request to downvote a certain tile. 
	 * @param json The tile to downvote
	 * @return A confirmation that the downvote request was received
	 */
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
			NineTiles.TryToPopFront(index);
		return Response.status(200).entity(ret).build();
	}
	
	/**
	 * Handles the uploading of a webcam image.
	 */
	@POST
	@Path("postImage")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postImage(@FormParam("imageData") String imageData, @FormParam("row") int row, @FormParam("col") int col) {
		byte[] image = Base64.decode(imageData.getBytes());
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		
		// thank you to http://stackoverflow.com/questions/12328622/writing-byte-array-to-gae-blobstore
		// these deprecated features are necessary--there doesn't seem to be any other way to do what I want to do
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
			java.util.logging.Logger.getGlobal().warning(e.toString());
			return Response.status(200).entity("{\"error\": \"" + e + "\"}").build();
		}
		
		return Response.status(200).entity("{\"success\": \"true\"}").build();
	}
	
	/**
	 * Retrieves the URLs for the current tile
	 * @return A JSON object with links for each current tile image.
	 */
	@GET
	@Path("getAllTiles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTiles() {	
		// update page view count
		NineTiles.IncrementPageViewCount();
		
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
