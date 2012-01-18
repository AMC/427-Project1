package bufmgr;
import chainexception.*;

//InvalidFrameNumberException if there is an invalid frame number 
public class InvalidFrameNumberException extends ChainException{
  public InvalidFrameNumberException(Exception e, String name){ 
    super(e, name); 
  }
}
