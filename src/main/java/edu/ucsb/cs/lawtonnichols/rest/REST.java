package edu.ucsb.cs.lawtonnichols.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import edu.ucsb.cs.lawtonnichols.*;

import java.util.*;
import org.json.*;

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
	
	@GET
	@Path("getAllTiles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTiles() {
		ArrayList<String> images = new ArrayList<String>();
		
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
