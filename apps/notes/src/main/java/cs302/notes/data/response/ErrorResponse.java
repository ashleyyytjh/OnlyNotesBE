package cs302.notes.data.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ErrorResponse implements Response {
    private int statusCode;
    public Map<String, String> errors;
}
