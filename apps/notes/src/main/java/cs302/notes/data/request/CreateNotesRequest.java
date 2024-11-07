package cs302.notes.data.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateNotesRequest {

    //title stores the notes title to be displayed
    @JsonProperty("title")
    @NotEmpty(message = "Title cannot be empty.")
    @Size(min=1, max=50, message="Title must be between 1-50 characters.")
    private String title;

    //description stores the string data describing the notes
    @JsonProperty("description")
    @NotEmpty(message = "Description cannot be empty.")
    @Size(min=1, max=300, message="Description must be between 1-300 characters.")
    private String description;

    //categoryCode stores the module number of the notes
    @JsonProperty("categoryCode")
    @NotEmpty(message = "Category Code cannot be empty.")
    @Size(min=1, max=50, message = "Category Code must be between 1-50 characters")
    private String categoryCode;

    //price stores the notes' price in cents
    @JsonProperty("price")
    @NotNull(message = "Price cannot be empty.")
    @Min(value = 0, message = "Price cannot be negative.")
    private Integer price;

    //file stores the File to be uploaded
    @NotNull(message = "File must be attached.")
    private MultipartFile file;

    //url stores the S3 bucket's URL
    private String url;
}
