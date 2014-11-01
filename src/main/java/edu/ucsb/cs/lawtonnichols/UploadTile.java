package edu.ucsb.cs.lawtonnichols;

// a large portion of the code taken from https://cloud.google.com/appengine/docs/java/blobstore/
import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.*;


public class UploadTile extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
        String row = req.getParameter("row");
        String col = req.getParameter("col");
        BlobKey blobKey = blobs.get("file").get(0);

        if (blobKey == null) {
            res.sendRedirect("/");
        } else {
        	// sanitize input
        	int r=1, c=1;
        	if (row.equals("1") || row.equals("2") || row.equals("3"))
        		r = Integer.parseInt(row);
        	if (col.equals("1") || col.equals("2") || col.equals("3"))
        		c = Integer.parseInt(col);
        	NineTiles.AddImageToTaskQueue(r, c, blobKey);
            res.sendRedirect("/");
        }
    }
}

