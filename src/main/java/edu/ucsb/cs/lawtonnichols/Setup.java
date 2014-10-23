package edu.ucsb.cs.lawtonnichols;

// used https://cloud.google.com/appengine/docs/java/taskqueue/overview-push as a guide

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.Base64;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.images.*;

public class Setup extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
    	// clear the blobstore
    	
    	// add a default image blob
    	BlobKey defaultImageBlob = null;
    	
    	
    	// initialize the main tiles
    	Entity e = new Entity("Main", "main");
    	e.setProperty("QueueSize-1", 0);
    	e.setProperty("QueueSize-2", 0);
    	e.setProperty("QueueSize-3", 0);
    	e.setProperty("QueueSize-4", 0);
    	e.setProperty("QueueSize-5", 0);
    	e.setProperty("QueueSize-6", 0);
    	e.setProperty("QueueSize-7", 0);
    	e.setProperty("QueueSize-8", 0);
    	e.setProperty("QueueSize-9", 0);
    	datastore.put(e);
    	
    	// initialize the queues (ImageQueue-1, ImageQueue-2, ...)
    	for (int queue = 1; queue <= 9; queue++) {
    		for (int index = 1; index <= 100; index++) {
    			Entity q = new Entity("ImageQueue-" + queue, index + "");
    	        q.setProperty("image", defaultImageBlob);
    	        datastore.put(q);
    		}
    	}
    	
    	// initialize the memcache to point to the current queue sizes & current image blob references
    	
        response.sendRedirect("/");
    }

}