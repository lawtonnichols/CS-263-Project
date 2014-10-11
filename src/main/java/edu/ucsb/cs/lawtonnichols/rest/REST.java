package edu.ucsb.cs.lawtonnichols.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
public class REST {
	@GET
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "Hello, world!2";
	}
	
	//@POST
	@GET
	@Path("updateTile/{row}/{col}")
	//@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTile(@PathParam("row") int row, @PathParam("col") int col) {
		return Response.status(200).entity("row: " + row + ", col: " + col).build();
	}
	
	@GET
	@Path("getAllTiles")
	public Response getAllTiles() {
		return Response.status(200).entity("getAllTiles").build();
	}
}