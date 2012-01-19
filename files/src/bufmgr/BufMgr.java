package bufmgr;
import java.io.*;
import java.util.*;
import diskmgr.*;
import global.*


/** A frame description class. It describes each page in the buffer
 * pool, the page number in the file, whether it is dirty or not,
 * its pin count, and the pin count change when pinning or unpinning 
 * a page.
 */
class FrameDesc implements GlobalConst{

	// The page within file, or INVALID_PAGE if the frame is empty. 
	public PageId pageNo;     

	// the dirty bit, 1 (TRUE) stands for this frame is altered, else 0 (FALSE) for clean frames.
	public boolean dirty;     

	//The pin count for the page in this frame
	public int pin_cnt;   

	//Creates a FrameDesc object, initialize pageNo, dirty and pin_count.
	public FrameDesc() {
		pageNo = new PageId();
		dirty = false;
		pin_cnt = 0; 			//initialized to none pinned
		pageNo.pid = INVALID_PAGE;  	//since the frame is empty (as specified above)

	}

	//Returns the pin count of a certain frame page. 
	public int pin_count() {  
		return pin_cnt;
	}

	// Increments the pin count of a certain frame page when the page is pinned.
	public int pin() {  
		return pin_cnt++;
	}

	//Decrements the pin count of a frame when the page is unpinned. Not less than 0!
	public int unpin() {
		pin_cnt = pin_cnt > 0 ? pin_cnt-- :  0;
		return pin_cnt;	  	
	}
}



/** A buffer HASTABLE ENTRY description class. It describes 
 * each entry for the buffer hash table, the page number and 
 * frame number for that page, the pointer points to the next
 * hash table entry.
 */
class BufHTEntry {

	// The next entry in this hashtable bucket.
	public BufHTEntry next;     

	// This page number. 
	public PageId pageNo = new PageId(); 

	// The frame we are stored in.
	public int frameNo;  
}




/** A buffer hashtable to keep track of pages in the buffer pool. 
 * It inserts, retrieves and removes pages from the hash table. 
 */
class BufHashTbl implements GlobalConst{

	// Hash Table size, small number for debugging.
	private static final int HTSIZE = 20;   

	// Each slot holds a linked list of BufHTEntrys, NULL means none
	private BufHTEntry ht[] = new BufHTEntry[HTSIZE];       

	//Returns the number of hash bucket used, value between 0 and hash table size-1
	private int hash(PageId pageNo) {
		//return the bucket number in the hash table
		//Dan Li mentioned in lecture an acceptable hash function is the page id mod hash table size
		return (pageNo.pid % HTSIZE);
	}

	// Creates a buffer hash table object.
	public BufHashTbl(){
		//for each slot of the linked list BufTHEntry, intialize its value in preparation of hashing to come
		for(int i = 0; i < HTSIZE; i++){
			ht[i] = null;
		}
	}

	//Insert association between page pageNo and frame frameNo into the hash table.
	//TODO: Is this the way we want to insert?
	public boolean insert(PageId pageNo, int frameNo) {
		BufHTEntry bhtEntry = new BufHTEntry();

		//give the data to insert to the new bhtEntry
		bhtEntry.pageNo.pid = pageNo.pid;
		bhtEntry.frameNo = frameNo;
		
		//place the entry in the array
		ht[hash(pageNo)] = bhtEntry;

		//insert the page into the buffer hash table
		bhtEntry.next = ht[hash(pageNo)];

		//return success
		return true;
	}

	//Find a page in the hashtable, return INVALID_PAGE on failure, otherwise the frame number.
	public int lookup(PageId pageNo){
	      
	      BufHTEntry entryToFind = ht[hash(pageNo)];
	      
	      while(entryToFind != null) {
	    	  if (entryToFind.pageNo.pid == pageNo.pid) {
	    		  return(entryToFind.frameNo);
	    	  }
	    	  entryToFind = entryToFind.next;
	      }
	      
	      return INVALID_PAGE;
	      
	}

	// Remove the page from the hashtable.
	public boolean remove(PageId pageNo) {

	      //hash the page number to find the entry we want to remove
	      int i = hash(pageNo);
	     
	      //determine initial cur and prev
	      BufHTEntry cur = ht[i];
	      BufHTEntry prev = null;
	      
	      while (cur != null) {	
	    	  if (cur.pageNo.pid == pageNo.pid){
	    		  //then stop looking we have found it
	    		  break;
	    	  }
	    	  //update prev and make cur next in line
	    	  prev = cur;
	    	  cur = cur.next;
	      }
	      
	      //unlink the found page so that it is "removed"
		  prev.next = cur.next;
		
	      if(pageNo.pid != ht[i].pageNo.pid){
	        System.err.println ("ERROR: The page " + pageNo.pid + " was not found in the hashtable.\n");       
	        return false;
	      }
	      
	      //return success of removal
	      return true;
	}

	// Show the hashtable contents.
	public void display() {
		BufHTEntry cur;

		System.out.println("HASH Table contents :FrameNo[PageNo]");

		for (int i=0; i < HTSIZE; i++) {
			//System.out.println ( "\nindex: " + i + "-" );
			if (ht[i] != null){
				for (cur=ht[i]; cur!=null; cur=cur.next) {
					System.out.println(cur.frameNo + "[" + cur.pageNo.pid + "]-");
				}
				System.out.println("\t\t");
			} 
			else {
				System.out.println("NONE\t");
			}
		}
		System.out.println("");    
	} 
}




/** A clock algorithm for buffer pool replacement policy. 
 * It picks up the frame in the buffer pool to be replaced. 
 */
class Clock extends Replacer {

	// Creates a clock object
	// Two constructors are needed for Replacer. One takes BufMgr, one a string.
	public Clock(BufMgr javamgr){
		super(javamgr);
	}

	// Picks up the frame to be replaced according to the clock algorithm.  
	// Pin the frame so that other processes can not pick it.
	public int pick_victim() throws BufferPoolExceededException, PagePinnedException {

		//return -1 if no frame is available. head of the list otherwise.	
		return -1;
	}

	//Returns the name of the clock algorithm as a string.
	public final String name() { return "Clock"; }

	// Displays information from clock replacement algorithm. 
	public void info()
	{

	}

} 






/** The buffer manager class, it allocates new pages for the
 * buffer pool, pins and unpins the frame, frees the frame 
 * page, and uses the replacement algorithm to replace the 
 * page.
 */
public class BufMgr implements GlobalConst{

	//The hash table, only allocated once.
	private BufHashTbl hashTable = new BufHashTbl(); 

	// Total number of buffer frames in the buffer pool.
	private int  numBuffers;	

	// physical buffer pool has default = byte[NUMBUF][MAX_SPACE];
	private byte[][] bufPool;    

	// An array of Descriptors one per frame has default = new FrameDesc[NUMBUF];
	private FrameDesc[] frmeTable;  

	// The replacer object, which is only used in this class.
	private Replacer replacer;

	//Constructor. Create a buffer manager object. 
	public BufMgr( int numbufs, String replacerArg ){
		
		//intialize the needed variables
		numBuffers = numbufs;
		bufPool = new byte[numBuffers][MAX_SPACE];
		frmeTable = new FrameDesc[numBuffers];
	    
		//setup the frmeTable
		for (int i=0; i<numBuffers; i++){  
			frmeTable[i] = new FrameDesc();
		}

		//set the replacement policy
		replacer = new Clock(this);
	}

	// Debug use only   
	private void bmhashdisplay(){
		hashTable.display();
	}

	public void pinPage(PageId pin_pgid, Page page, boolean emptyPage) throws ReplacerException, 
	HashOperationException, PageUnpinnedException, InvalidFrameNumberException, PageNotReadException, 
	BufferPoolExceededException, PagePinnedException, BufMgrException,IOException { 
		// Check if this page is in buffer pool, otherwise
		// find a frame for this page, read in and pin it.
		// Also write out the old page if it's dirty before reading
		// if emptyPage==TRUE, then actually no read is done to bring the page in.	
		
	}

	public void unpinPage(PageId PageId_in_a_DB, boolean dirty) throws ReplacerException, 
	PageUnpinnedException,HashEntryNotFoundException,InvalidFrameNumberException{
		// To unpin a page specified by a pageId.
		// If pincount>0, decrement it and if it becomes zero
		// put it in a group of replacement candidates.
		// If pincount=0 before this call, return error.

	}

	public PageId newPage(Page firstpage, int howmany) throws BufferPoolExceededException, 
	HashOperationException, ReplacerException, HashEntryNotFoundException, InvalidFrameNumberException, 
	PagePinnedException, PageUnpinnedException,PageNotReadException,BufMgrException,
	DiskMgrException,IOException { 
		//Call DB object to allocate a run of new pages and 
		// find a frame in the buffer pool for the first page
		// and pin it. If buffer is full, ask DB to deallocate 
		// all these pages and return error (null if error).
		//Note: howmany refers to the total number of allocated new pages

	     PageId pageid = new PageId();

	     //find space for the page
	     allocate_page(pageid,howmany);
	     
	     //try to pin the page
	     try{pinPage(pageid,firstpage,true);}
	     catch (Exception e) {
	       //the buffer must be full, so deallocaate and return null for error
	       for (int i=0; i < howmany; i++){ 
	    	   	pageid.pid  = pageid.pid + i;
	    	   	deallocate_page(pageid);
	       }
	       return  null;
	    }   
	     //return success
	     return pageid;
	}

	// User should call this method if she needs to delete a page.
	public void freePage(PageId globalPageId) throws InvalidBufferException, 
	ReplacerException, HashOperationException,InvalidFrameNumberException,
	PageNotReadException,BufferPoolExceededException, PagePinnedException, 
	PageUnpinnedException, HashEntryNotFoundException, BufMgrException,
	DiskMgrException,IOException{
	      
		int frameNo = hashTable.lookup(globalPageId); 

		//set the page id to empty
	    frmeTable[frameNo].pageNo.pid = INVALID_PAGE;
	    
	    //this page is now clean and clear
	    frmeTable[frameNo].dirty = false;

	    deallocate_page(globalPageId);
	}

	// Factor out the common code for the two versions of Flush 
	// pageid is the page number of the page which needs to be flushed.
	// all_pages is the total number of page to be flushed.
	private void privFlushPages(PageId pageid, int all_pages) throws HashOperationException, 
	PageUnpinnedException,PagePinnedException, PageNotFoundException,
	BufMgrException,IOException{

		//keep track of pages unpinned for exception handling
		int numUnpinnedPages = 0;

//		//before flushing, decide if which pages are dirty and need writing
//		for(int i = 0; i < numBuffers; i++){
//			if((all_pages == 1) || (pageid.pid == frmeTable[i].pageNo.pid)){
//
//				if(frmeTable[i].dirty){		  
//					//create a page to write to the hard drive
//					Page pageToWrite = new Page(bufPool[i]);
//					//write the page 
//					write_page(pageid,pageToWrite);
//					//remove the page from the buffer pool
//					hashTable.remove(pageid);
//					//set the frame location as empty
//					frmeTable[i].pageNo.pid = INVALID_PAGE;
//					//its clean!
//					frmeTable[i].dirty = false;
//				}
//			}
//		}  
	}

		// Added to flush a particular page of the buffer pool to disk.
		// pageid the page number in the database. 
		public void flushPage(PageId pageid) throws HashOperationException, 
		PageUnpinnedException,PagePinnedException,PageNotFoundException,
		BufMgrException,IOException {
			privFlushPages(pageid, 0);	//use 0 to signify false on all_pages parameter
		}

		// Flushes all pages of the buffer pool to disk 
		public void flushAllPages() throws HashOperationException, 
		PageUnpinnedException,PagePinnedException,PageNotFoundException,
		BufMgrException,IOException {
			//Flushing all pages is equivent to using the intial settings
			PageId pageId = new PageId(INVALID_PAGE);
			privFlushPages(pageId,1);		//use 1 to signify true on all_pages parameter
		}

		// Gets the total number of buffers.
		public int getNumBuffers(){
			return numBuffers;
		}

		// Gets the total number of unpinned buffer frames.
		public int getNumUnpinnedBuffers(){
			//TODO: get the number of unpinned buffers
			return 0;
		}

		// A few routines currently need direct access to the FrameTable.
		public   FrameDesc[] frameTable() { return frmeTable; }

		// Try to write the page
		private void write_page (PageId pageno, Page page)throws BufMgrException {
			try {SystemDefs.JavabaseDB.write_page(pageno, page);}
			catch (Exception e) {
				throw new BufMgrException(e,"BufMgr.java: write_page() failed");
			}
		} 

		private void read_page (PageId pageno, Page page)throws BufMgrException {        
			try {SystemDefs.JavabaseDB.read_page(pageno, page);}
			catch (Exception e) {
				throw new BufMgrException(e,"BufMgr.java: read_page() failed");
			}   
		}  

		private void allocate_page (PageId pageno, int num)throws BufMgrException {      
			try {SystemDefs.JavabaseDB.allocate_page(pageno, num);}
			catch (Exception e) {
				throw new BufMgrException(e,"BufMgr.java: allocate_page() failed");
			} 
		} 

		private void deallocate_page (PageId pageno)throws BufMgrException {
			try {SystemDefs.JavabaseDB.deallocate_page(pageno);}
			catch (Exception e) {
				throw new BufMgrException(e,"BufMgr.java: deallocate_page() failed");
			}
		} 
	}



	// A class describes the victim frames data, its frame number and page number.
	class victim_data {
		public int frame_num;
		public int page_id;
	}
