package nl.yellowbrick.admin.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InconsistentDataException extends RuntimeException {

    public InconsistentDataException(String error) {
        super(error);
    }
}
