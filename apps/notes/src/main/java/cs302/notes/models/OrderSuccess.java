package cs302.notes.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderSuccess {
    // _id stores the monitoring correlation email
    private ObjectId _id;

    // orderId stores the id of the orders
    private String buyerId;

    // notesId stores the id of the notes
    private String noteId;
}
