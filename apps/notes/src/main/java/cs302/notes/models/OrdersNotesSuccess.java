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
public class OrdersNotesSuccess {

    // _id stores the monitoring correlation email
    private ObjectId _id;

    // userId stores the id
    private String userId;

    // notesId stores the id of the notes
    private String notesId;

    //fkAccountOwner stores the AWS cognito user id
    private String fkAccountOwner;

    //title stores the notes title to be displayed
    private String title;

    //description stores the string data describing the notes
    private String description;

    //url stores the S3 bucket's URL
    private String url;

    //categoryCode stores the module number of the notes
    private String categoryCode;

    //price stores the notes' price in cents
    private Integer price;

    // Constructor from OrderSuccess object and Notes object
    public OrdersNotesSuccess(OrderSuccess orderSuccess, Notes notes) {
        this._id = orderSuccess.get_id();
        this.userId = orderSuccess.getBuyerId();
        this.notesId = orderSuccess.getNoteId();
        this.fkAccountOwner = notes.getFkAccountOwner();
        this.title = notes.getTitle();
        this.description = notes.getDescription();
        this.url = notes.getUrl();
        this.categoryCode = notes.getCategoryCode();
        this.price = notes.getPrice();
    }
}
