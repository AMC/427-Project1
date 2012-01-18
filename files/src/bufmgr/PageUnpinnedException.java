package bufmgr;
import chainexception.*;

//PageUnpinnedException if there is a page that is already unpinned
public class PageUnpinnedException extends ChainException{
  public PageUnpinnedException(Exception e, String name){ 
    super(e, name); 
  }
}
