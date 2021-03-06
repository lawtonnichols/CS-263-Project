package edu.ucsb.cs.lawtonnichols;

// used https://cloud.google.com/appengine/docs/java/taskqueue/overview-push as a guide

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

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
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Setup extends HttpServlet {
	
	/**
	 * This is meant to be run only once after deploying the app to App Engine.
	 * It resets the datastore, memcache, and blobstore to some default values
	 * that allow the app to function normally.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

		// clear the blobstore (taken from https://groups.google.com/forum/#!topic/google-appengine/whWF95icabI)
		List<BlobInfo> blobsToDelete = new LinkedList<BlobInfo>();
		Iterator<BlobInfo> iterator = new BlobInfoFactory().queryBlobInfos();
		while(iterator.hasNext())
			blobsToDelete.add(iterator.next());
		for(BlobInfo blobInfo : blobsToDelete)
			blobstoreService.delete(blobInfo.getBlobKey());

		// initialize the main tiles in the datastore
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
		e.setProperty("Image-1", "default");
		e.setProperty("Image-2", "default");
		e.setProperty("Image-3", "default");
		e.setProperty("Image-4", "default");
		e.setProperty("Image-5", "default");
		e.setProperty("Image-6", "default");
		e.setProperty("Image-7", "default");
		e.setProperty("Image-8", "default");
		e.setProperty("Image-9", "default");
		e.setProperty("DownvoteCount-1", 0);
		e.setProperty("DownvoteCount-2", 0);
		e.setProperty("DownvoteCount-3", 0);
		e.setProperty("DownvoteCount-4", 0);
		e.setProperty("DownvoteCount-5", 0);
		e.setProperty("DownvoteCount-6", 0);
		e.setProperty("DownvoteCount-7", 0);
		e.setProperty("DownvoteCount-8", 0);
		e.setProperty("DownvoteCount-9", 0);
		e.setProperty("ImageResetTime-1", new Date());
		e.setProperty("ImageResetTime-2", new Date());
		e.setProperty("ImageResetTime-3", new Date());
		e.setProperty("ImageResetTime-4", new Date());
		e.setProperty("ImageResetTime-5", new Date());
		e.setProperty("ImageResetTime-6", new Date());
		e.setProperty("ImageResetTime-7", new Date());
		e.setProperty("ImageResetTime-8", new Date());
		e.setProperty("ImageResetTime-9", new Date());
		e.setProperty("PageViewCount", 0);
		e.setProperty("PageViewCountResetTime", new Date());
		datastore.put(e);

		// initialize the queues (ImageQueue-1, ImageQueue-2, ...) in the datastore
		// and set the images to a maker for the default image
		for (int queue = 1; queue <= 9; queue++) {
			for (int index = 1; index <= 100; index++) {
				Entity q = new Entity("ImageQueue-" + queue, index + "");
				q.setProperty("image", "default");
				datastore.put(q);
			}
		}

		// initialize the memcache to point to the current images (which are defaults)
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        syncCache.clearAll(); // clear the memcache first
        syncCache.put("Image-1", "default");
        syncCache.put("Image-2", "default");
        syncCache.put("Image-3", "default");
        syncCache.put("Image-4", "default");
        syncCache.put("Image-5", "default");
        syncCache.put("Image-6", "default");
        syncCache.put("Image-7", "default");
        syncCache.put("Image-8", "default");
        syncCache.put("Image-9", "default");
		
		response.sendRedirect("/");
	}

}