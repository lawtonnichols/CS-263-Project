package edu.ucsb.cs.lawtonnichols;

import java.util.Date;
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
	/**
	 * Sends an image blob to the task queue to be inserted in the proper location in a tile's queue 
	 * @param row
	 * @param col
	 * @param b the blobkey for the image blob
	 */
	public static void AddImageToTaskQueue(int row, int col, BlobKey b) {
		// add it to the task queue to be converted and added
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl("/worker").param("type", "Convert").param("row", row + "").param("col", col + "").param("blobKey", b.getKeyString()));
	}
	
	/**
	 * Returns the URL for the current image in the given tile. Crops the 
	 * picture into a square, too.
	 * @param t tile index (1-9)
	 * @return the URL for the current image in that tile
	 */
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
        		if (image == null) // this probably means that /setup hasn't been run
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
            ServingUrlOptions s = ServingUrlOptions.Builder.withBlobKey(blobKey).imageSize(300).crop(true);
            try {
            	return imagesService.getServingUrl(s);
            } catch (Exception e) {
            	return "defaultTile.png?"+image;
            }
        }
	}
	
	/**
	 * Updates the page view count, or resets it to zero if five seconds
	 * have passed since the last time we counted.
	 */
	public static void IncrementPageViewCount() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity main = null;
		try {
			main = GetMainEntity();
		} catch (EntityNotFoundException e) {
			// something is horribly wrong if we get here
			// /setup was most likely not run
			e.printStackTrace();
		}
		int count;
		Date lastReset = (Date) main.getProperty("PageViewCountResetTime");
		if ((new Date().getTime() - lastReset.getTime()) >= 5000) {
			// reset count every five seconds
			count = 0;
			main.setProperty("PageViewCountResetTime", new Date());
			
			// reset all the downvote counts too
			main.setProperty("DownvoteCount-1", 0);
			main.setProperty("DownvoteCount-2", 0);
			main.setProperty("DownvoteCount-3", 0);
			main.setProperty("DownvoteCount-4", 0);
			main.setProperty("DownvoteCount-5", 0);
			main.setProperty("DownvoteCount-6", 0);
			main.setProperty("DownvoteCount-7", 0);
			main.setProperty("DownvoteCount-8", 0);
			main.setProperty("DownvoteCount-9", 0);
		}
		else {
			count = ((Long) main.getProperty("PageViewCount")).intValue();
			count++;
		}
		main.setProperty("PageViewCount", count);
		
		datastore.put(main);
	}
	
	/**
	 * Handle a downvote on the given tile index. If we received enough
	 * downvote requests in 5 seconds, we will then update the current
	 * image with the next in line.
	 * @param index (1-9)
	 */
	public static void TryToPopFront(int index) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		// get the current downvote count and add one
		Entity main = null;
		try {
			main = GetMainEntity();
		} catch (EntityNotFoundException e) {
			// something is horribly wrong if we get here
			// (it means we didn't run /setup)
			e.printStackTrace();
		}
		int pageviewcount = ((Long) main.getProperty("PageViewCount")).intValue();
		int downvotecount = ((Long) main.getProperty("DownvoteCount-"+index)).intValue();
		// can never divide by 0 if downvotecount is incremented here
		downvotecount++;
		
		Date imageResetTime = (Date) main.getProperty("ImageResetTime-"+index);
		
		// only pop the front if we have the right pageview/downvote ratio,
		// or if the image has been up there for over an hour
		// 2 is a heuristic
		if (pageviewcount / downvotecount <= 2 || imageResetTime.before(new Date(System.currentTimeMillis() - 1000 * 60 * 60))) {
			// set the downvote count back to zero now
			downvotecount = 0;
			main.setProperty("DownvoteCount-"+index, downvotecount);
			datastore.put(main);
			//NineTiles.PopFront(index);
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(withUrl("/worker").param("type", "Downvote").param("index", index + ""));
			return;
		}
		main.setProperty("DownvoteCount-"+index, downvotecount);
		datastore.put(main);
	}
	
	/**
	 * Removes the current image tile at the given index, and replaces
	 * it with the next image waiting in the queue for that tile.
	 * @param index (1-9)
	 */
	public static void PopFront(int index) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		Entity main = null;
		try {
			main = GetMainEntity();
		} catch (EntityNotFoundException e) {
			// something is horribly wrong if we get here
			// (it means we didn't run /setup)
			e.printStackTrace();
		}
		
		// decrement the queue size
		int size = ((Long) main.getProperty("QueueSize-"+index)).intValue();
		if (size == 0) // don't do anything if the size is already 0
			return;
		size--;
		main.setProperty("QueueSize-"+index, size);
		
		try {
			// move everything over one
			for (int i = 2; i <= size + 1 && i <= 100; i++) {
				Entity queueToUpdate = GetTileQueueAtIndex(index, i-1);
				Entity queueToUse = GetTileQueueAtIndex(index, i);
				
				String img = (String) queueToUse.getProperty("image");
				queueToUpdate.setProperty("image", img);
				datastore.put(queueToUpdate);
			}
			// set the now-empty queue slot to a default value
			Entity queueToUpdate = GetTileQueueAtIndex(index, size+1);
			queueToUpdate.setProperty("image", "default");
			datastore.put(queueToUpdate);
			
			// update the first image in the memcache & main datastore entry
			Entity firstQueueIndex = GetTileQueueAtIndex(index, 1);
			String img = (String) firstQueueIndex.getProperty("image");
			main.setProperty("Image-"+index, img);
	        syncCache.put("Image-"+index, img);
	        
	        // update the image reset time
	        main.setProperty("ImageResetTime-"+index, new Date());
	        
	        datastore.put(main);
			
		} catch (Exception e) {
			java.util.logging.Logger.getGlobal().warning(e.toString());
		}
	}
	
	/**
	 * Adds a reference to the image into the queue for a given tile.
	 * Used in conjunction with AddImageToTaskQueue
	 * @param row
	 * @param col
	 * @param blobKey the blobKey for the uploaded image
	 * @throws EntityNotFoundException
	 */
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
				
				// update the image reset time
				main.setProperty("ImageResetTime-"+i, new Date());
				
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
	
	/**
	 * Gets the datastore Entity named "main"
	 * @return the main entity
	 * @throws EntityNotFoundException
	 */
	public static Entity GetMainEntity() throws EntityNotFoundException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return datastore.get(KeyFactory.createKey("Main", "main"));
	}
	
	/**
	 * Gets the datastore Entity for the given tile at a given queue index.
	 * @param rowcol which tile (1–9)
	 * @param index the queue index
	 * @return the tile queue entity for that index
	 * @throws EntityNotFoundException
	 */
	public static Entity GetTileQueueAtIndex(int rowcol, int index) throws EntityNotFoundException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return datastore.get(KeyFactory.createKey("ImageQueue-"+rowcol, index+""));
	}
}
