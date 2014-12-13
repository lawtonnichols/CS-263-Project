CS 263 Project — NineTiles 
==========================

Summary
-------

NineTiles is a web application developed in Java for Google App Engine. It consists of nine image tiles that users can upload to and downvote.

Selenium Scripts
----------------

 - test_face.selenium: Uploads an image file of into each of the nine tiles. Together they hold a face. All the images are expected to be in a folder called "test pictures" on the Desktop of my computer.
 - test_sentence.selenium: Uploads an image file of into each of the nine tiles. Each image is an individual word, and together they make a sentence.
 - test_100_tiles.selenium: Uploads more than 100 images (which is the max queue size) into one tile to test that the cutoff is working.
 - test_100_downvotes.selenium: Downvotes those 100 images to test that nothing horrible went wrong.
 - entire_test_suite.selenium: Holds the previous four test cases.
