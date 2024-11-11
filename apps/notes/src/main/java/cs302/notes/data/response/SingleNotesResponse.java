package cs302.notes.data.response;

import cs302.notes.models.Notes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class SingleNotesResponse implements Response {
    public Notes response;
}
