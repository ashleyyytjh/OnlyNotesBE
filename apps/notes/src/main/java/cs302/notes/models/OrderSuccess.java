package cs302.notes.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderSuccess {
    // _id stores the monitoring correlation email
    private String _id;

    // orderId stores the id of the orders
    private String userId;

    // notesId stores the id of the notes
    private String notesId;
}
