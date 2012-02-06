/*  File BufMgr,java */

package bufmgr;

import java.io.*;
import java.util.*;
import diskmgr.*;
import global.*;
 

// A frame description class. It describes each page in the buffer
// pool, the page number in the file, whether it is dirty or not,
// its pin count, and the pin count change when pinning or unpinning 
// a page.

// The Buffer Table
class FrameDesc implements GlobalConst {
  
  // The page within file, or INVALID_PAGE if the frame is empty. 
  public PageId pageNo;     
  
  // true if altered since last write
  public boolean dirty;     
                         
  // pin count 
  public int pin_cnt;   


  // Constructor
  public FrameDesc() {
    pageNo = new PageId();
    pageNo.pid = INVALID_PAGE;
    dirty   = false;
    pin_cnt = 0;
  } // end constructor


  // getPinCount
  public int pin_count() { 
    return(pin_cnt); 
  } // end pin_count()

  
  // increments pin count
  public int pin() { 
    return(++pin_cnt); 
  } // end pin()

  
  // decrements pin count
  public int unpin() {
    pin_cnt = (pin_cnt <= 0) ? 0 : pin_cnt - 1;
    return(pin_cnt);
  } // end unpin()

} // end FrameDesc




// A buffer hashtable entry description class. It describes 
// each entry for the buffer hash table, the page number and 
// frame number for that page, the pointer points to the next
// hash table entry.

// An Entry node in the page table linked list
class BufHTEntry {

  // The next entry in this hashtable bucket.
  public BufHTEntry next;     
  
  // Page number 
  public PageId pageNo = new PageId(); 
  
  // The buffer pool frame the page is stored in
  public int frameNo;  

} // end BufHTEntry




// A buffer hashtable to keep track of pages in the buffer pool. 
// It inserts, retrieves and removes pages from the h ash table. 

// The Page Table array containing the head of the entry linked list
class BufHashTbl implements GlobalConst {
    
  // Size of the hashtable
  // NOTE: Using a non-prime number as the hash table size is not optimal 
  private static final int HTSIZE = 20;   
  
  // Page table array
  private BufHTEntry ht[] = new BufHTEntry[HTSIZE];       


  // Contructor  
  public BufHashTbl() {
    // initializes all entries in the Page Table to null
    for (int i=0; i < HTSIZE; i++)
      ht[i] = null;
  } // end constructor

  
  // The hash function for placement into the page table
  // NOTE: This is not a very effective hash function
  private int hash(PageId pageNo) {
    return (pageNo.pid % HTSIZE);
  } // end hash()
  

  // Inserts the association between page's pageNo and frame's frameNo 
  public boolean insert(PageId pageNo, int frameNo) {
      
    // Instantiate a new entry
    BufHTEntry entry = new BufHTEntry();
      
    // Set entry properties
    // NOTE: this should be part of the constructor or through a setter method
    entry.pageNo.pid = pageNo.pid;
    entry.frameNo = frameNo;
      
    // Determine the pageNo's bucket using the hash function
    int index = hash(pageNo);
      
    // Insert entry into top of linked list for a time complexity of O(1)
    entry.next = ht[index];
    ht[index] = entry;
      
    return true;
    
  } // end insert()
  
  
  // Removes the association to the page's pageNo
  public boolean remove(PageId pageNo) {
      
    // Instantiate current and previous entries
    BufHTEntry cur, prev = null;
      
    // Removes INVALID_PAGE
    if (pageNo.pid == INVALID_PAGE)
      return true;
    
    // Determine the pageNo's bucket using the hash function  
    int index = hash(pageNo);
    
    // Iterate through the Page Table linked list until the pageNo is found
    // or the end of the linked list is found
    for (cur = ht[index]; cur != null; cur = cur.next) {
      if (cur.pageNo.pid == pageNo.pid)
        break;
      prev = cur;
    }
    
    // If the current entry is not null, remove the current entry
    if (cur != null) {
      // If the there is more than one entry in this bucket
      if (prev != null)
        prev.next = cur.next;
      // There is only one entry in this bucket 
      else
        ht[index] = cur.next;
    // The current entry is null meaning the pageNo is not in the Page Table
    } else {
      System.out.println ("ERROR: Page " + pageNo.pid + " was not found in hashtable.");
      // The pageNo was not removed from the Page Table
      return false;
    }
    
    // The pageNo was removed from the Page Table
    return true;
    
  } //end remove()


  // Lookup the index of the page in the hashtable
  // If the page does not exist return an INVALID_PAGE
  public int lookup(PageId pageNo) {

    // Instantiate a new entry
    BufHTEntry entry;
    int index;

    // If the page does not exist return an INVALID_PAGE
    if (pageNo.pid == INVALID_PAGE)
      return INVALID_PAGE;

    // Determine which bucket in Page Table array that the pageNo would be in
    index = hash(pageNo);

    // Iterate through Page Table linked list searching for the pageNo specified
    // If found, return index in array, else return an INVALID_PAGE
    for (entry = ht[index]; entry != null; entry = entry.next) {
      if (entry.pageNo.pid == pageNo.pid) {
        return(entry.frameNo);
      }
    }

    // pageNo was not in the Page Table
    return(INVALID_PAGE);

  } // end lookup()


  // Display the contents of the entire Page Table (array and linked lists)
  public void display() {
    
    // Instantiate an Entry
    BufHTEntry cur;
    
    System.out.println("HASH Table contents :FrameNo[PageNo]");
    
    // Interate through the Page Table array
    for (int i = 0; i < HTSIZE; i++) {
      System.out.println("Array Index: " + i);
      if (ht[i] != null) {
        // Iterate through the linked list in the current bucket
        for (cur = ht[i]; cur != null; cur = cur.next) 
          System.out.println("\t" + cur.frameNo + "[" + cur.pageNo.pid + "]");
      } else {
        System.out.println("\t EMPTY");
      }
    }
    System.out.println("");
  } // end display()
  
}  // end BufHasTbl




// A clock algorithm for buffer pool replacement policy. 
// It picks up the frame in the buffer pool to be replaced. 
// This is the default replacement policy.

// NOTE: Storing the state in the seperate replacer class decouples the replacer
// from the buffer manager and enables the program to change replacers on the fly 
class Clock extends Replacer {
  
  // Constructor
  // NOTE: javamgr is stored as the public internal variable mgr
  public Clock(BufMgr javamgr) {
      super(javamgr);
  }
  
  // Determine which frame will be replaced
  public int pick_victim() throws 
    BufferPoolExceededException, 
    PagePinnedException {
    
    // The current frame in the Buffer Table
    FrameDesc frame;
      
    // num is used to determine how many times we have iterated through the state array
    int num = 0;
      
    // numBuffers is the size of the state array
    int numBuffers = mgr.getNumBuffers();
      
    // Mod by the numBuffers to ensure the head (aka clock hand) is within the array 
    // NOTE: head is a member variable of replacer class
    head = ( head + 1) % numBuffers;
      
    // Iterate through the state array to find an available frame in the buffer pool
    while ( state_bit[head].state != Available ) {
        
      // On first pass, set all referenced states to available
      if ( state_bit[head].state == Referenced )
        state_bit[head].state = Available;
  
      // If you have iterated through the array twice without an available frame
      // then there are no available frames in the buffer pool. 
      if ( num == numBuffers * 2 ) 
        throw new BufferPoolExceededException (null, "CLOCK: The Buffer Pool is full.");
        
      // Increment num
      num++;
        
      // Move to the next position in the array
      head = (head+1) % numBuffers;
    } // end while
    
    // Set the current frame
    frame = (mgr.frameTable())[head];
    
    // Confirm the page is not pinned
    if (frame.pin_count() != 0) {
      throw new PagePinnedException (null, "CLOCK: Current page is still pinned");
    } // end if
      
    // Pin the current frame to ensure other processes don't try to use it
    state_bit[head].state = Pinned;        
    frame.pin();    
      
    return head;
  } // end pick_victim()
  
  // Returns the replacer being used
  public final String name() { 
    return "Clock"; 
  } // end name()
  
  // Displays the information from the current replacer subclass
  public void info() {
    super.info();
    System.out.println ("head:\t" + head);
  } // end info()
  
} // end of Clock


// The buffer manager class, it allocates new pages for the
// buffer pool, pins and unpins the frame, frees the frame 
// page, and uses the replacement algorithm to replace the 
// page.
 
public class BufMgr implements GlobalConst {
  
  // Instantiates the Page Table
  private BufHashTbl hashTable = new BufHashTbl(); 
  
  // Number of frames in the Buffer Pool
  private int  numBuffers;  
  
  // The Buffer Pool stored as a two-dimensional array [frameNo][pageContents]
  // NOTE: Must be byte array due to other methods within MINIBASE
  private byte[][] bufPool;  

  // The Buffer Table stored as an array where size = numBuffers
  // NOTE: frmeTable is misspelled to allow for the frameTable() method
  // which should actually be getFrameTable(), but cannot be changed as
  // the method is per specifications 
  private FrameDesc[] frmeTable;  
  
  // The replacer algorithm
  private Replacer replacer;


  // constructor
  public BufMgr( int numbufs, String replacerArg ) {
    
    numBuffers = numbufs;  
    frmeTable = new FrameDesc[numBuffers];
    bufPool = new byte[numBuffers][MAX_SPACE];
    frmeTable = new FrameDesc[numBuffers];
    
    // Initialize the Buffer Table with empty frame descriptions
    for (int i = 0; i < numBuffers; i++)
      frmeTable[i] = new FrameDesc();
    
    replacer = new Clock(this);
    
    // You must setBufferManager or the state array is not initialized
    // NOTE: This seems redundant and should be part of the replacer constructor
    replacer.setBufferManager( this );
      
  } // end constructor
  
  
  // Returns the number of frames in the buffer pool

  public int getNumBuffers() { 
    return numBuffers; 
  } // end getNumBuffers()
  
  
  // Returns the number of unpinned buffers from the replacer
  public int getNumUnpinnedBuffers() {
    return replacer.getNumUnpinnedBuffers();
  } // end getNumUnpinnedBuffers()
  
  // returns the private member variable frmeTable
  // NOTE: should be getFrameTable()
  public FrameDesc[] frameTable() { 
    return frmeTable; 
  } // end frameTable()


  // Check if this page is in buffer pool, otherwise
  // find a frame for this page, read in and pin it.
  // Also write out the old page if it's dirty before reading
  // if emptyPage==TRUE, then actually no read is done to bring
  // the page in.

  public void pinPage(PageId pin_pgid, Page page, boolean emptyPage) throws 
    ReplacerException, 
    HashOperationException, 
    PageUnpinnedException, 
    InvalidFrameNumberException, 
    PageNotReadException, 
    BufferPoolExceededException, 
    PagePinnedException, 
    BufMgrException,
    IOException {
       
    // Current frame descriptor index
    int frameNo = hashTable.lookup(pin_pgid);
    
    // Current frame descriptor
    FrameDesc frame;
    
    // Current page
    Page curPage;
    
    // Page being replaced
    PageId oldpageNo = new PageId(-1);
    
    // Determines if oldPageNo needs to be written to disk
    boolean dirty = false;

    // Frame is not in the buffer pool
    if (frameNo < 0) {
      // pick a frame in the buffer pool to store the            
      frameNo = replacer.pick_victim(); 
      
      // No frames available in the buffer pool
      if (frameNo < 0)  
        throw new ReplacerException (null, "BUFMGR: Buffer Pool is full.");  
    
      // Set current frame descriptor
      frame = frmeTable[frameNo];
    
      // If the old page is dirty, remember to write it to disk
      // NOTE: Cannot write until new page has been inserted
      if ((frame.pageNo.pid != INVALID_PAGE) && (frame.dirty == true) ) {
        dirty = true;
        oldpageNo.pid = frame.pageNo.pid;
      }
  
      // Remove the old page
      boolean removed = hashTable.remove(frame.pageNo);
      if (removed == false) 
        throw new HashOperationException (null, "BUFMGR: Cannot remove old page from Page Table");
  
      // Reset the frame descriptor
      // NOTE: this should be a FrameDesc method 
      frame.pageNo.pid = pin_pgid.pid;
      frame.dirty = false;             
  
      // Insert the new page
      boolean inserted = hashTable.insert(pin_pgid,frameNo);
      if (inserted == false)  
        throw new HashOperationException (null, "BUFMGR: Unable to insert page into Page Table");
      
      // Set the curPage
      curPage = new Page(bufPool[frameNo]);
  
      // Write the oldPage
      if (dirty)  
        write_page(oldpageNo, curPage);   
  
      // If the curPage is not empty, read page into the buffer pool
      if (emptyPage == false) {
        try {
          curPage.setpage(bufPool[frameNo]);
          read_page(pin_pgid, curPage);
          
        // If read_page fails, reset the Page Table and frame descriptor
        } catch (Exception e) {
          
          removed = hashTable.remove(frame.pageNo);
          if (removed == false)
            throw new HashOperationException (e, "BUFMGR: Unable to remove page from Page Table.");
      
          // Reset the frame descriptor
          // NOTE: this should be a FrameDesc method
          frame.pageNo.pid = INVALID_PAGE;
          frame.dirty = false;
      
          boolean unpinned = replacer.unpin(frameNo);
      
          if (unpinned == false)
            throw new ReplacerException (e, "BUFMGR: Unable to unpin page in Buffer Table.");
      
          throw new PageNotReadException (e, "BUFMGR: Could not read page from database.");
        } // end try 
    
      } // end if
  
      // load page into the buffer pool
      page.setpage(bufPool[frameNo]);
  
    // The page is in the buffer pool
    } else {    
  
      // load the page into the buffer pool
      page.setpage(bufPool[frameNo]);
      replacer.pin(frameNo);
  
    } // end if
  } // end pinPage()
  
  // To unpin a page specified by a pageId.
  // If pincount>0, decrement it and if it becomes zero,
  // put it in a group of replacement candidates.
  // if pincount=0 before this call, return error.
  public void unpinPage(PageId PageId_in_a_DB, boolean dirty) throws 
    ReplacerException, 
    PageUnpinnedException, 
    HashEntryNotFoundException, 
    InvalidFrameNumberException {
      
    // Current frame descriptor index
    int frameNo = hashTable.lookup(PageId_in_a_DB);
    
    // Current frame descriptor
    FrameDesc frame; 
    
    // If the pageId is not found in the Page Table
    if (frameNo < 0) 
      throw new HashEntryNotFoundException (null, "BUFMGR: Hash entry not found");
    
    // Set the frame descriptor  
    frame = frmeTable[frameNo];
      
    // If the page returned is an INVALID_PAGE
    if (frame.pageNo.pid == INVALID_PAGE)
      throw new InvalidFrameNumberException (null, "BUFMGR: Invalid page no.");
    
    // If we are unable to unpin the frame
    if ((replacer.unpin(frameNo)) == false) 
      throw new ReplacerException (null, "BUFMGR: Unable to unpin page in the replacer.");
    
    // Sets the dirty bit in the frame descriptor
    if (dirty == true)
      frame.dirty = dirty;
      
  }  // end unpinPage()
  
  
  
  // private Flush Pages method used to factor out the common code for flushPage() 
  // and flushAllPages()
  private void privFlushPages(PageId pageid, int all_pages) throws 
    HashOperationException, 
    PageUnpinnedException,  
    PagePinnedException, 
    PageNotFoundException,
    BufMgrException,
    IOException {
    
    // The current page
    Page curPage;
    
    // The current frame descriptor
    FrameDesc frame;
      
    // Iterate through the Buffer Pool
    for (int i=0; i < numBuffers; i++) {
      
      // If flushAllPages() or pageId specified in flushPage()
      if ( (all_pages == 1) || (frmeTable[i].pageNo.pid == pageid.pid)) {
    
        // If the pin_count isn't 0, the page is still in use and can't be flushed
        if ( frmeTable[i].pin_count() != 0 )
          throw new PagePinnedException (null, "BUFMGR: Page is still pinned.");
    
        // If page is dirty
        if (frmeTable[i].dirty) {
          
          // You cannot write an INVALID_PAGE
          if (frmeTable[i].pageNo.pid == INVALID_PAGE)
            throw new PageNotFoundException( null, "BUFMGR: Page not found");
          
          // Set the current frame and current page
          frame = frmeTable[i];
          curPage = new Page(bufPool[i]);

          pageid.pid = frame.pageNo.pid;
      
          // Write the dirty page
          write_page(pageid, curPage);
      
          // Remove the page from the Page Table
          hashTable.remove(pageid);
      
          // Reset the Buffer Table
          // NOTE: this should be a FrameDesc method
          frmeTable[i].pageNo.pid = INVALID_PAGE; // frame is empty
          frmeTable[i].dirty = false ;
        } // end if
      } // end if
    } // end for
  } // end privFlushPages()

  // Added to flush a particular page of the buffer pool to disk
  // @param pageid the page number in the database. 

  public void flushPage(PageId pageid) throws 
    HashOperationException, 
    PageUnpinnedException,  
    PagePinnedException, 
    PageNotFoundException,
    BufMgrException,
    IOException {
    
    // send to private method with the common code for all flush methods
    privFlushPages(pageid, 0);  
  } // end flushPage()
  
  
  // Flushes all pages of the buffer pool to disk 
  public void flushAllPages() throws 
    HashOperationException, 
    PageUnpinnedException,  
    PagePinnedException, 
    PageNotFoundException,
    BufMgrException,
    IOException {
      
      // send to the private method with the common coe for all flush methods
      PageId pageId = new PageId(INVALID_PAGE);
      privFlushPages(pageId ,1); 
    } // end flushAllPages()
  
  
  // Call DB object to allocate a run of new pages and 
  // find a frame in the buffer pool for the first page
  // and pin it. If buffer is full, ask DB to deallocate 
  // all these pages and return error (null if error).
  
  public PageId newPage(Page firstpage, int pages) throws 
    BufferPoolExceededException, 
    HashOperationException, 
    ReplacerException,
    HashEntryNotFoundException,
    InvalidFrameNumberException, 
    PagePinnedException, 
    PageUnpinnedException,
    PageNotReadException,
    BufMgrException,
    DiskMgrException,
    IOException {
       
     PageId firstPageId = new PageId();
     allocate_page(firstPageId, pages);
     
     try {
       pinPage(firstPageId,firstpage,true);
    
     // Roll back the pages
     } catch (Exception e) {
       for (int i = 0; i < pages; i++) {
            firstPageId.pid += i;
            deallocate_page(firstPageId);
       } // end for
       return  null;
     } // end try
     
     return firstPageId;
   } // end newPage()
  
  
  // User should call this method if she needs to delete a page.
  // this routine will call DB to deallocate the page.

  public void freePage(PageId globalPageId) throws 
    InvalidBufferException, 
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
    IOException {

    // Current frame descriptor index
    int frameNo = hashTable.lookup(globalPageId); 

    // The current frame descriptor
    FrameDesc frame;
    
    // if globalPageId is not in pool, frameNo < 0 then deallocate 
    if (frameNo < 0) {
      deallocate_page(globalPageId);
      return;
    } // end if
    
    if (frameNo >= numBuffers) {
      throw new InvalidBufferException(null, "BUFMGR: Invalid buffer."); 
    } // end if
    
    frame = frmeTable[frameNo];
      
    try {
      replacer.free(frameNo);
    } catch(Exception e) {
      throw new ReplacerException(e, "BUFMGR: Unable to free from replacer.");
    } // end try
    
    try {
      hashTable.remove(frame.pageNo);
    } catch (Exception e) {
      throw new HashOperationException(e, "BUFMGR, Unable to remove from Page Table");
    } // end try
    
    // Reset frame descriptor
    // NOTE: this should be a FrameDesc method
    frame.pageNo.pid = INVALID_PAGE; 
    frame.dirty = false;
      
    deallocate_page(globalPageId);
      
  } // end freePage()
  

  private void allocate_page (PageId pageno, int num) throws 
    BufMgrException {
    
    try {
      SystemDefs.JavabaseDB.allocate_page(pageno, num);
    } catch (Exception e) {
      throw new BufMgrException(e, "BUFMGR: allocate_page() failed");
    } // end try
  } // end allocate_page()

  private void deallocate_page (PageId pageno) throws 
    BufMgrException {
    
    try {
      SystemDefs.JavabaseDB.deallocate_page(pageno);
    } catch (Exception e) {
      throw new BufMgrException(e, "BUFMGR: deallocate_page() failed");
    } // end try
    
  } // end deallocate_page()
  
  
  private void write_page (PageId pageno, Page page) throws 
    BufMgrException {
    
    try {
      SystemDefs.JavabaseDB.write_page(pageno, page);
    } catch (Exception e) {
      throw new BufMgrException(e, "BUFMGR: write_page() failed");
    } // end try
    
  } // end write_page()


  private void read_page (PageId pageno, Page page) throws 
    BufMgrException {
    
    try {
      SystemDefs.JavabaseDB.read_page(pageno, page);
    } catch (Exception e) {
      throw new BufMgrException(e, "BUFMGR: read_page() failed");
    } // end try
    
  } // end read_page()

  
  // Display the Page Table
  private void bmhashdisplay() {
    hashTable.display();
  } // end dmhashdisplay()

}


// A class describes the victim data, its frame number and page number.

class victim_data {

  public int frame_num;
  public int page_id;
}