package edu.ucsb.cs.lawtonnichols;

import java.util.Random;
import java.util.logging.Level;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.*;
import com.google.appengine.api.images.*;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;


public class NineTiles {
	public static void AddImageToTaskQueue(int row, int col, BlobKey b) {
		// add it to the task queue to be converted and added
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl("/worker").param("type", "Convert").param("row", row + "").param("col", col + "").param("blobKey", b.getKeyString()));
	}
	
	public static String GetImageForTile(int t) {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        String image = (String) syncCache.get("Image-" + t);
        if (image == null) {
        	// get the image from the datastore
        	try {
        		Entity main = NineTiles.GetMainEntity();
        		image = (String) main.getProperty("Image-"+t);
        		syncCache.put("Image-"+t, image);
        		if (image == null) // this probably means that setup hasn't been run
        			return "defaultTile.png?thisisaproblem";
        	} catch (EntityNotFoundException e) {
        		return "/defaultTile.png?wasnull";
        	}
        }
        
        if (image.equals("default")) {
        	return "/defaultTile.png?wasdefault";
        } else {
        	BlobKey blobKey = new BlobKey(image);
        	ImagesService imagesService = ImagesServiceFactory.getImagesService();
            ServingUrlOptions s = ServingUrlOptions.Builder.withBlobKey(blobKey).imageSize(1000);
            try {
            	return imagesService.getServingUrl(s);
            } catch (Exception e) {
            	return "defaultTile.png?"+image;
            }
        }
	}
	
	public static void AddImageToTileQueue(String row, String col, String blobKey) throws EntityNotFoundException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		Entity main = GetMainEntity();
		
		// get the size of the queue for the current row & column
		int r = Integer.parseInt(row);
		int c = Integer.parseInt(col);
		int i = ((r-1) * 3 + c);
		int size = ((Long) main.getProperty("QueueSize-"+i)).intValue();
		
		// make sure adding 1 wouldn't make it >= 101
		// if not, add it to the queue at the next index
		if (size+1 < 101) {
			Entity queue = GetTileQueueAtIndex(i, size+1);
			
			// set size = size + 1
			main.setProperty("QueueSize-"+i, size+1);
			
			// add in the image
			queue.setProperty("image", blobKey);
			
			// change the tile if this is the first one added
			if (size + 1 == 1) {
				main.setProperty("Image-"+i, blobKey);
		        syncCache.put("Image-"+i, blobKey);
			}
			
			// update everything
			datastore.put(main);
			datastore.put(queue);
		}
		// otherwise, replace one of the others (except the first)
		else {
			int index = (new Random().nextInt(99)) + 2;
			Entity queue = GetTileQueueAtIndex(i, index);
			queue.setProperty("image", blobKey);
			datastore.put(queue);
		}
	}
	
	public static Entity GetMainEntity() throws EntityNotFoundException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return datastore.get(KeyFactory.createKey("Main", "main"));
	}
	
	public static Entity GetTileQueueAtIndex(int rowcol, int index) throws EntityNotFoundException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return datastore.get(KeyFactory.createKey("ImageQueue-"+rowcol, index+""));
	}
}
