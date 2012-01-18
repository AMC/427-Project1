package bufmgr;
import chainexception.*;

//HashEntryNotFoundException if there is no entry of page in the hash table. 
public class HashEntryNotFoundException extends ChainException {
  public HashEntryNotFoundException(Exception e, String name){ 
    super(e, name); 
  }
}
