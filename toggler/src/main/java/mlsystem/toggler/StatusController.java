package mlsystem.toggler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController
{
    private boolean statusOK = true;
    private String error = null;
    private final byte OK = 1;
    private final byte ERROR = 0;

    public void RaiseError(String error)
    {
        statusOK = false;
        this.error = error;
    }
    public void CancelError()
    {
        statusOK = true;
        error = null;
    }
    
    @PostMapping("/status")
    public ResponseEntity<byte[]> Status()
    {
        return new ResponseEntity<byte[]>(new byte[]{ (statusOK ? OK : ERROR) }, HttpStatus.OK);
    }
    @PostMapping("/errormessage")
    public ResponseEntity<String> Error()
    {
        return new ResponseEntity<String>(error, HttpStatus.OK);
    }
}
