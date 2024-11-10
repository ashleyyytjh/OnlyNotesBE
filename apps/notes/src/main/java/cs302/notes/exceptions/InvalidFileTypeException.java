package cs302.notes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Error
public class InvalidFileTypeException extends BadRequestException{
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidFileTypeException(String fileType) {
        super(String.format("Filetype %s is not accepted", fileType));
    }
}
