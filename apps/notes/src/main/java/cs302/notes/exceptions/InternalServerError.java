package cs302.notes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Error
public class InternalServerError extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InternalServerError(String message) {
        super(message);
    }
}