package edu.ucsb.cs.lawtonnichols;

import java.util.Random;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.taskqueue.*;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;


public class NineTiles {
	public static void AddImageToTaskQueue(int row, int col, BlobKey b) {
		// add it to the task queue to be converted and added
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(withUrl("/worker").param("type", "Convert").param("row", row + "").param("col", col + "").param("blobKey", b.getKeyString()));
	}
	
	public static void AddImageToTileQueue(String row, String col, String base64Image) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity main = GetMainEntity();
		
		// get the size of the queue for the current row & column
		Integer size = (Integer) main.getProperty("queueSize-"+row+"-"+col);
		
		// make sure adding 1 wouldn't make it >= 100
		// if not, add it to the queue at the next index
		if (size+1 < 100) {
			Entity queue = GetTileQueueAtIndex(row, col, size+1);
			
			// set size = size + 1
			main.setProperty("queueSize-"+row+"-"+col, size+1);
			
			// add in the image
			queue.setProperty("image", base64Image);
			
			// update everything
			datastore.put(main);
			datastore.put(queue);
		}
		// otherwise, replace one of the others (except the first)
		else {
			int index = (new Random().nextInt(99)) + 1;
			Entity queue = GetTileQueueAtIndex(row, col, index);
			queue.setProperty("image", base64Image);
			datastore.put(queue);
		}
	}
	
	public static Entity GetMainEntity() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter mainFilter = new FilterPredicate("main", FilterOperator.EQUAL, "main");
		Query q = new Query("Main").setFilter(mainFilter);
		return datastore.prepare(q).asSingleEntity();
	}
	
	public static Entity GetTileQueueAtIndex(String row, String col, int index) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter rowFilter = new FilterPredicate("row", FilterOperator.EQUAL, row);
		Filter colFilter = new FilterPredicate("col", FilterOperator.EQUAL, col);
		Filter indexFilter = new FilterPredicate("index", FilterOperator.EQUAL, index);
		Query q = new Query("TileQueues").setFilter(CompositeFilterOperator.and(rowFilter, colFilter, indexFilter));
		return datastore.prepare(q).asSingleEntity();
	}
}
