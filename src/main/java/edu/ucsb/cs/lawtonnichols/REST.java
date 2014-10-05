package edu.ucsb.cs.lawtonnichols;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
public class REST {
	@GET
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "Hello, world!";
	}
}
