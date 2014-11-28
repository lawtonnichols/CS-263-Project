package edu.ucsb.cs.lawtonnichols;

// used https://cloud.google.com/appengine/docs/java/taskqueue/overview-push as a guide

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.internal.util.Base64;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.images.*;

public class TaskQueueWorker extends HttpServlet {
	
	/**
	 * Processes a newly added image in the background. It adds it to the
	 * tile queue for the given index.
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String row = request.getParameter("row");
        String col = request.getParameter("col");
        String blobKey = request.getParameter("blobKey");
        
        /*ImagesService imagesService = ImagesServiceFactory.getImagesService();
        Image oldImage = ImagesServiceFactory.makeImageFromBlob(new BlobKey(blobKey));
        Transform resize = ImagesServiceFactory.makeResize(500, 500, 0.5, 0.5);
        Image newImage = imagesService.applyTransform(resize, oldImage);
        byte[] newImageData = newImage.getImageData();*/
        
        // TODO: make sure this is actually an image
                
        try {
			NineTiles.AddImageToTileQueue(row, col, blobKey);
		} catch (EntityNotFoundException e) {
			// can't really do anything here
		}
        
    }

}