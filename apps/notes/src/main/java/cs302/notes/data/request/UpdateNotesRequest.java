package cs302.notes.data.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateNotesRequest {

    //title stores the notes title to be displayed
    @JsonProperty("title")
    @Size(min=1, max=50, message="Title must be between 1-50 characters.")
    private String title;

    //description stores the string data describing the notes
    @JsonProperty("description")
    @Size(min=1, max=300, message="Description must be between 1-300 characters.")
    private String description;

    //categoryCode stores the module number of the notes
    @JsonProperty("categoryCode")
    @Size(min=1, max=50, message = "Category Code must be between 1-50 characters")
    private String categoryCode;

    //price stores the notes' price in cents
    @JsonProperty("price")
    @Min(value = 0, message = "Price cannot be negative.")
    private Integer price;
}
