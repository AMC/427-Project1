package bufmgr;
import chainexception.*;

//InvalidBufferException if buffer pool corrupted
public class InvalidBufferException extends ChainException{
  public InvalidBufferException(Exception e, String name){ 
    super(e, name); 
  }
}
