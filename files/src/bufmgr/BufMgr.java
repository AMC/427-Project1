/*  File BufMgr,java */

package bufmgr;

import java.io.*;
import java.util.*;
import diskmgr.*;
import global.*;
 

/** A frame description class. It describes each page in the buffer
 * pool, the page number in the file, whether it is dirty or not,
 * its pin count, and the pin count change when pinning or unpinning 
 * a page.
 */
class FrameDesc implements GlobalConst{
  
  /** The page within file, or INVALID_PAGE if the frame is empty. */
  public PageId pageNo;     
  
  /** the dirty bit, 1 (TRUE) stands for this frame is altered,
   *0 (FALSE) for clean frames.
   */
  public boolean dirty;     
                         
  /** The pin count for the page in this frame */
  public int pin_cnt;   

  /** Creates a FrameDesc object, initialize pageNo, dirty and 
   * pin_count.
   */
  public FrameDesc() 
  {
    
  }
  
  
  
  /** Returns the pin count of a certain frame page. 
   *
   * @return the pin count number.
   */
  public int pin_count() 
  {  
  
  }
  
  /** Increments the pin count of a certain frame page when the
   * page is pinned.
   *
   * @return the incremented pin count.
   */
  public int pin() 
  {  
  
  }
  
  /** Decrements the pin count of a frame when the page is 
   * unpinned.  If the pin count is equal to or less than
   * zero, the pin count will be zero.
   *
   * @return the decremented pin count.
   */
  public int unpin() {

  }
}


// *****************************************************

/** A buffer hashtable entry description class. It describes 
 * each entry for the buffer hash table, the page number and 
 * frame number for that page, the pointer points to the next
 * hash table entry.
 */
class BufHTEntry {
  /** The next entry in this hashtable bucket. */
  public BufHTEntry next;     
  
  /** This page number. */
  public PageId pageNo = new PageId(); 
  
  /** The frame we are stored in. */
  public int frameNo;  
}


// *****************************************************

/** A buffer hashtable to keep track of pages in the buffer pool. 
 * It inserts, retrieves and removes pages from the h ash table. 
 */
class BufHashTbl implements GlobalConst{
  
  
  /** Hash Table size, small number for debugging. */
  private static final int HTSIZE = 20;   
  
  
  /** Each slot holds a linked list of BufHTEntrys, NULL means 
   * none. 
   */
  private BufHTEntry ht[] = new BufHTEntry[HTSIZE];       
  
  
  /** Returns the number of hash bucket used, value between 0 and HTSIZE-1
   *
   * @param pageNo the page number for the page in file.
   * @return the bucket number in the hash table.
   */
  private int hash(PageId pageNo) 
    {

    }
  
  /** Creates a buffer hash table object. */
  public BufHashTbl()
   {

    }
  
  
  /** Insert association between page pageNo and frame frameNo 
   * into the hash table.
   *
   * @param pageNo page number in the bucket.
   * @param frameNo frame number in the bucket.
   * @return true if successful.
   */
  public boolean insert(PageId pageNo, int frameNo) 
   {

    }
  
  
  /** Find a page in the hashtable, return INVALID_PAGE
   * on failure, otherwise the frame number.
   * @param pageNo page number in the bucket.
   */
  public int lookup(PageId pageNo)
   {

    }
  
  /** Remove the page from the hashtable.
   * @param pageNo page number of the bucket.
   */
  public boolean remove(PageId pageNo) 
   {
      
    }
  
  /** Show hashtable contents. */
  public void display() {
    BufHTEntry cur;
    
    System.out.println("HASH Table contents :FrameNo[PageNo]");
    
    for (int i=0; i < HTSIZE; i++) {
      //   System.out.println ( "\nindex: " + i + "-" );
      if (ht[i] != null) {
	
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

// *****************************************************

/** A clock algorithm for buffer pool replacement policy. 
 * It picks up the frame in the buffer pool to be replaced. 
 * This is the default replacement policy.
 */
class Clock extends Replacer {
  
  /** Creates a clock object. */
  public Clock(BufMgr javamgr)
    {
      super(javamgr);
    }
  
  /** Picks up the victim frame to be replaced according to
   * the clock algorithm.  Pin the victim so that other
   * process can not pick it as a victim.
   *
   * @return -1 if no frame is available.
   *         head of the list otherwise.
   * @throws BufferPoolExceededException.
   */
  public int pick_victim() 
    throws BufferPoolExceededException, 
	   PagePinnedException 
    {
      
    }
  
  /** Returns the name of the clock algorithm as a string.
   *
   * @return "Clock", the name of the algorithm.
   */
  public final String name() { return "Clock"; }
  
  /** Displays information from clock replacement algorithm. */ 
  public void info()
    {

    }
  
} // end of Clock


// *****************************************************

/** The buffer manager class, it allocates new pages for the
 * buffer pool, pins and unpins the frame, frees the frame 
 * page, and uses the replacement algorithm to replace the 
 * page.
 */
public class BufMgr implements GlobalConst{
  
  /** The hash table, only allocated once. */
  private BufHashTbl hashTable = new BufHashTbl(); 
  
  /** Total number of buffer frames in the buffer pool. */
  private int  numBuffers;	
  
  /** physical buffer pool. */
  private byte[][] bufPool;  // default = byte[NUMBUF][MAX_SPACE];
                         
  /** An array of Descriptors one per frame. */
  private FrameDesc[] frmeTable;  // default = new FrameDesc[NUMBUF];
  
  /** The replacer object, which is only used in this class. */
  private Replacer replacer;
  
  
  /** 
   * Create a buffer manager object.
   *
   * @param numbufs number of buffers in the buffer pool.
   * @param replacerArg name of the buffer replacement policy.
   */
  public BufMgr( int numbufs, String replacerArg )
  	
    {
      
    }
  
  
  // Debug use only   
  private void bmhashdisplay()
    {
      hashTable.display();
    }
  
  
  /** Check if this page is in buffer pool, otherwise
   * find a frame for this page, read in and pin it.
   * Also write out the old page if it's dirty before reading
   * if emptyPage==TRUE, then actually no read is done to bring
   * the page in.
   *
   * @param Page_Id_in_a_DB page number in the minibase.
   * @param page the pointer poit to the page.       
   * @param emptyPage true (empty page); false (non-empty page)
   *
   * @exception ReplacerException if there is a replacer error.
   * @exception HashOperationException if there is a hashtable error.
   * @exception PageUnpinnedException if there is a page that is already unpinned.
   * @exception InvalidFrameNumberException if there is an invalid frame number .
   * @exception PageNotReadException if a page cannot be read.
   * @exception BufferPoolExceededException if the buffer pool is full.
   * @exception PagePinnedException if a page is left pinned .
   * @exception BufMgrException other error occured in bufmgr layer
   * @exception IOException if there is other kinds of I/O error. 
   */

  public void pinPage(PageId pin_pgid, Page page, boolean emptyPage) 
    throws ReplacerException, 
	   HashOperationException, 
	   PageUnpinnedException, 
	   InvalidFrameNumberException, 
	   PageNotReadException, 
	   BufferPoolExceededException, 
	   PagePinnedException, 
	   BufMgrException,
	   IOException
    { 
      
    }
  
  /** 
   * To unpin a page specified by a pageId.
   *If pincount>0, decrement it and if it becomes zero,
   * put it in a group of replacement candidates.
   * if pincount=0 before this call, return error.
   *
   * @param globalPageId_in_a_DB page number in the minibase.
   * @param dirty the dirty bit of the frame
   *
   * @exception ReplacerException if there is a replacer error. 
   * @exception PageUnpinnedException if there is a page that is already unpinned. 
   * @exception InvalidFrameNumberException if there is an invalid frame number . 
   * @exception HashEntryNotFoundException if there is no entry of page in the hash table. 
   */
  public void unpinPage(PageId PageId_in_a_DB, boolean dirty) 
    throws ReplacerException, 
	   PageUnpinnedException, 
	   HashEntryNotFoundException, 
	   InvalidFrameNumberException
    {
       
    }
  
  
  /** Call DB object to allocate a run of new pages and 
   * find a frame in the buffer pool for the first page
   * and pin it. If buffer is full, ask DB to deallocate 
   * all these pages and return error (null if error).
   *
   * @param firstpage the address of the first page.
   * @param howmany total number of allocated new pages.
   * @return the first page id of the new pages. 
   *
   * @exception BufferPoolExceededException if the buffer pool is full. 
   * @exception HashOperationException if there is a hashtable error. 
   * @exception ReplacerException if there is a replacer error. 
   * @exception HashEntryNotFoundException if there is no entry of page in the hash table. 
   * @exception InvalidFrameNumberException if there is an invalid frame number. 
   * @exception PageUnpinnedException if there is a page that is already unpinned. 
   * @exception PagePinnedException if a page is left pinned. 
   * @exception PageNotReadException if a page cannot be read. 
   * @exception IOException if there is other kinds of I/O error.  
   * @exception BufMgrException other error occured in bufmgr layer
   * @exception DiskMgrException other error occured in diskmgr layer
   */
  public PageId newPage(Page firstpage, int howmany)
    throws BufferPoolExceededException, 
	   HashOperationException, 
	   ReplacerException,
	   HashEntryNotFoundException,
	   InvalidFrameNumberException, 
	   PagePinnedException, 
	   PageUnpinnedException,
	   PageNotReadException,
	   BufMgrException,
	   DiskMgrException,
	   IOException 
   { 
     
   }
  
  
  /** User should call this method if she needs to delete a page.
   * this routine will call DB to deallocate the page.
   * 
   * @param globalPageId the page number in the data base.
   * @exception InvalidBufferException if buffer pool corrupted.
   * @exception ReplacerException if there is a replacer error.
   * @exception HashOperationException if there is a hash table error.
   * @exception InvalidFrameNumberException if there is an invalid frame number.  
   * @exception PageNotReadException if a page cannot be read.  
   * @exception BufferPoolExceededException if the buffer pool is already full.  
   * @exception PagePinnedException if a page is left pinned.  
   * @exception PageUnpinnedException if there is a page that is already unpinned.  
   * @exception HashEntryNotFoundException if there is no entry 
   *            of page in the hash table.  
   * @exception IOException if there is other kinds of I/O error.   
   * @exception BufMgrException other error occured in bufmgr layer
   * @exception DiskMgrException other error occured in diskmgr layer
   */
  public void freePage(PageId globalPageId) 
       throws InvalidBufferException, 
	      ReplacerException, 
	      HashOperationException,
	      InvalidFrameNumberException,
	      PageNotReadException,
	      BufferPoolExceededException, 
	      PagePinnedException, 
	      PageUnpinnedException, 
	      HashEntryNotFoundException, 
	      BufMgrException,
	      DiskMgrException,
	      IOException
    {

    }
  
    
    /** Factor out the common code for the two versions of Flush 
     *
     * @param pageid the page number of the page which needs 
     *        to be flushed.
     * @param all_pages the total number of page to be flushed.
     *
     * @exception HashOperationException if there is a hashtable error.
     * @exception PageUnpinnedException when unpinning an unpinned page
     * @exception PagePinnedException when trying to free a pinned page
     * @exception PageNotFoundException when the page could not be found
     * @exception InvalidPageNumberException when the page number is invalid 
     * @exception FileIOException File I/O  error
     * @exception IOException Other I/O errors
     */
    private void privFlushPages(PageId pageid, int all_pages)
    throws HashOperationException, 
    PageUnpinnedException,  
    PagePinnedException, 
    PageNotFoundException,
    BufMgrException,
    IOException
    {
        
    }  
    
  
  /** Added to flush a particular page of the buffer pool to disk
   * @param pageid the page number in the database. 
   *
   * @exception HashOperationException if there is a hashtable error.  
   * @exception PageUnpinnedException if there is a page that is already unpinned.  
   * @exception PagePinnedException if a page is left pinned.  
   * @exception PageNotFoundException if a page is not found.  
   * @exception BufMgrException other error occured in bufmgr layer
   * @exception IOException if there is other kinds of I/O error.   
   */
  public void flushPage(PageId pageid)
    throws HashOperationException, 
	   PageUnpinnedException,  
	   PagePinnedException, 
	   PageNotFoundException,
	   BufMgrException,
	   IOException
    {
      privFlushPages(pageid, 0);	
    }
  
  
  /** Flushes all pages of the buffer pool to disk 
   * @exception HashOperationException if there is a hashtable error.  
   * @exception PageUnpinnedException if there is a page that is already unpinned.  
   * @exception PagePinnedException if a page is left pinned.  
   * @exception PageNotFoundException if a page is not found.  
   * @exception BufMgrException other error occured in bufmgr layer
   * @exception IOException if there is other kinds of I/O error.   
   */
  public void flushAllPages()
    throws HashOperationException, 
	   PageUnpinnedException,  
	   PagePinnedException, 
	   PageNotFoundException,
	   BufMgrException,
	   IOException
    {

    }
  
  
  /** Gets the total number of buffers.
   *
   * @return total number of buffer frames.
   */
  public int getNumBuffers() 
    {
  
    }
  
  
  /** Gets the total number of unpinned buffer frames.
   * 
   * @return total number of unpinned buffer frames.
   */
  public int getNumUnpinnedBuffers()
    {

    }
    
    
 
    /** A few routines currently need direct access to the FrameTable. */
    public   FrameDesc[] frameTable() { return frmeTable; }
    
    private void write_page (PageId pageno, Page page)
    throws BufMgrException {
        
        try {
            SystemDefs.JavabaseDB.write_page(pageno, page);
        }
        catch (Exception e) {
            throw new BufMgrException(e,"BufMgr.java: write_page() failed");
        }
        
    } // end of write_page
    
    private void read_page (PageId pageno, Page page)
    throws BufMgrException {
        
        try {
            SystemDefs.JavabaseDB.read_page(pageno, page);
        }
        catch (Exception e) {
            throw new BufMgrException(e,"BufMgr.java: read_page() failed");
        }
        
    } // end of read_page 
    
    private void allocate_page (PageId pageno, int num)
    throws BufMgrException {
        
        try {
            SystemDefs.JavabaseDB.allocate_page(pageno, num);
        }
        catch (Exception e) {
            throw new BufMgrException(e,"BufMgr.java: allocate_page() failed");
        }
        
    } // end of allocate_page 
    
    private void deallocate_page (PageId pageno)
    throws BufMgrException {
        
        try {
            SystemDefs.JavabaseDB.deallocate_page(pageno);
        }
        catch (Exception e) {
            throw new BufMgrException(e,"BufMgr.java: deallocate_page() failed");
        }
        
    } // end of deallocate_page 
    

}


/** A class describes the victim data, its frame number and page
 * number.
 */
class victim_data {

  public int frame_num;
  public int page_id;
   
}
