package cs302.notes.utils;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
public class AcceptedValues {
    public List<String> mediaList = new ArrayList<>() {
        { add(".pdf"); }
        { add(".jpg"); }
        { add(".jpeg"); }
        { add(".txt"); }
        { add(".doc"); }
        { add(".docx"); }
        { add(".pptx"); }
    };
}
