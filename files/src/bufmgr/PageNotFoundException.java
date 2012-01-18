package bufmgr;
import chainexception.*;

//PageNotFoundException when the page could not be found
public class PageNotFoundException extends ChainException {
  public PageNotFoundException(Exception e, String name){ 
    super(e, name); 
  }
}
