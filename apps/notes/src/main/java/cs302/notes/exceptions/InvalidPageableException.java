package cs302.notes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Error
public class InvalidPageableException extends BadRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPageableException() {
        super("Page number / limit cannot be less than 0.");
    }
}
