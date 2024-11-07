package cs302.notes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs302.notes.controller.NotesController;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import cs302.notes.service.services.NotesService;
import cs302.notes.data.request.CreateNotesRequest;
import cs302.notes.data.request.UpdateNotesRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebMvcTest(value = NotesController.class)
@AutoConfigureMockMvc(addFilters = false)
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotesService notesService;

    @Autowired
    private ObjectMapper objectMapper;

    // Attributes
    private final String testFileName = "testFile.jpeg";
    private final Path path = Paths.get("./resources/" + testFileName);
    private final String str_0 = "";
    private final String str_1 = "L";
    private final String str_30 = "Lorem ipsum odor amet, consectetuer adipiscing eli";
    private final String str_31 = "Lorem ipsum odor amet, consectetuer adipiscing elit";
    private final String str_300 = "Lorem ipsum odor amet, consectetuer adipiscing elit. Tincidunt enim pharetra viverra a luctus urna. Turpis imperdiet consectetur fermentum; nisi dolor per. Conubia blandit arcu taciti mus convallis netus mauris. Fames est accumsan lacinia tellus donec habitasse. Ipsum ut integer pulvinar tristique f";
    private final String str_301 = "Lorem ipsum odor amet, consectetuer adipiscing elit. Tincidunt enim pharetra viverra a luctus urna. Turpis imperdiet consectetur fermentum; nisi dolor per. Conubia blandit arcu taciti mus convallis netus mauris. Fames est accumsan lacinia tellus donec habitasse. Ipsum ut integer pulvinar tristique fe";


    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value("Hello World!"));
    }

    @Test
    void getAllDistinctCategories() throws Exception {
        mockMvc.perform(get("/api/v1/notes/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllNotesByAccount() throws Exception {
        mockMvc.perform(get("/api/v1/notes/account")
                        .param("page", "0")
                        .param("limit", "10")
                        .requestAttr("id", "user-id"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllVerifiedNotes() throws Exception {
        mockMvc.perform(get("/api/v1/notes")
                        .param("categoryCode", "CS301")
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    MultipartFile getMultiPartFile() {
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new MockMultipartFile(testFileName, testFileName, "image/jpeg", content);
    }

    @Test
    void createNotes() throws Exception {
        MultipartFile multipartFile = getMultiPartFile();
        CreateNotesRequest request = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        mockMvc.perform(multipart("/api/v1/notes")
                        .file("file", multipartFile.getBytes()) // Assuming a file upload is required
                        .param("title", request.getTitle())
                        .param("description", request.getDescription())
                        .param("categoryCode", request.getCategoryCode())
                        .param("price", String.valueOf(500))
                        .requestAttr("id", "user-id"))
                .andExpect(status().isCreated());
    }

    @Test
    void getNotesById() throws Exception {
        mockMvc.perform(get("/api/v1/notes/{notesId}", "note-id"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotesById() throws Exception {
        UpdateNotesRequest request = UpdateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .build();
        mockMvc.perform(put("/api/v1/notes/{notesId}", "note-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("id", "user-id"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteNotesById() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/{notesId}", "note-id")
                        .requestAttr("id", "user-id"))
                .andExpect(status().isOk());
    }
}
