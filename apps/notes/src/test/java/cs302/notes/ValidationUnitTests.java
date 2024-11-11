package cs302.notes;

import cs302.notes.data.request.CreateNotesRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class ValidationUnitTests {

    private Validator validator;

    // Attributes
    private final String testFileName = "testFile.jpeg";
    private final Path path = Paths.get("./resources/" + testFileName);

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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
    void createNotes_Successful_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_NoAttributes_ValidationFail() {
        // Arrange
        CreateNotesRequest createRequest = CreateNotesRequest.builder().build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(5);
    }

    @Test
    void createNotes_NoTitle_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title cannot be empty.");
    }

    @Test
    void createNotes_NoDescription_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description cannot be empty.");
    }

    @Test
    void createNotes_NoCategoryCode_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category Code cannot be empty.");
    }

    @Test
    void createNotes_NoPrice_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Price cannot be empty.");
    }

    @Test
    void createNotes_NoFile_ValidationFail() {
        // Arrange
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("File must be attached.");
    }

    @Test
    void createNotes_ShortTitle_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title(string)
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title cannot be empty.");
    }

    @Test
    void createNotes_NormalTitle1_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "C";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title(string)
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_NormalTitle2_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "Lorem ipsum odor amet, consectetuer adipiscing eli";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title(string)
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_LongTitle_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "Lorem ipsum odor amet, consectetuer adipiscing elit";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title(string)
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title must be between 1-50 characters.");
    }

    @Test
    void createNotes_ShortDescription_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description(string)
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description cannot be empty.");
    }

    @Test
    void createNotes_NormalDescription1_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "C";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description(string)
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_NormalDescription2_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "Lorem ipsum odor amet, consectetuer adipiscing elit. Tincidunt enim pharetra viverra a luctus urna. Turpis imperdiet consectetur fermentum; nisi dolor per. Conubia blandit arcu taciti mus convallis netus mauris. Fames est accumsan lacinia tellus donec habitasse. Ipsum ut integer pulvinar tristique f";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description(string)
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_LongDescription_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "Lorem ipsum odor amet, consectetuer adipiscing elit. Tincidunt enim pharetra viverra a luctus urna. Turpis imperdiet consectetur fermentum; nisi dolor per. Conubia blandit arcu taciti mus convallis netus mauris. Fames est accumsan lacinia tellus donec habitasse. Ipsum ut integer pulvinar tristique fe";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description(string)
                .categoryCode("CS101")
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description must be between 1-300 characters.");
    }

    @Test
    void createNotes_ShortCategoryCode_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode(string)
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category Code cannot be empty.");
    }

    @Test
    void createNotes_NormalCategoryCode1_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "C";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode(string)
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_NormalCategoryCode2_ValidationPass() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "Lorem ipsum odor amet, consectetuer adipiscing eli";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode(string)
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(0);
    }

    @Test
    void createNotes_LongCategoryCode_ValidationFail() {
        // Arrange
        MultipartFile multipartFile = getMultiPartFile();
        String string = "Lorem ipsum odor amet, consectetuer adipiscing elit";
        CreateNotesRequest createRequest = CreateNotesRequest.builder()
                .title("CS101 notes")
                .description("Take a step into programming fundamentals 1. Explore the world of C")
                .categoryCode(string)
                .price(500)
                .file(multipartFile)
                .build();

        // Act
        Set<ConstraintViolation<CreateNotesRequest>> violations = validator.validate(createRequest);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category Code must be between 1-50 characters.");
    }
}
