package cs302.notes.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
@Builder
public class ErrorResponse implements Response {
    private int statusCode;
    public Map<String, String> errors;
}
