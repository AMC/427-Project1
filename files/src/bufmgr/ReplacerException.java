package bufmgr;
import chainexception.*;

//ReplacerException if there is a replacer error
public class ReplacerException extends ChainException{
  public ReplacerException(Exception e, String name){ 
    super(e, name); 
  }
}
