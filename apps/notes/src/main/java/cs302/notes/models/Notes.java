package cs302.notes.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Notes {

    //_id stores the notes' autogenerated id
    private String _id;

    //fk_account_owner stores the AWS cognito user id
    private String fk_account_owner;

    //title stores the notes title to be displayed
    private String title;

    //description stores the string data describing the notes
    private String description;

    //url stores the S3 bucket's URL
    private String url;

    //price stores the notes' price in cents
    private Integer price;

}
