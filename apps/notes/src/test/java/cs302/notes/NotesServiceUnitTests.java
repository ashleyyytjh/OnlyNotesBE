package cs302.notes;

import cs302.notes.data.request.CreateNotesRequest;
import cs302.notes.data.request.UpdateNotesRequest;
import cs302.notes.data.response.MultiNotesResponse;
import cs302.notes.data.response.MultiStringResponse;
import cs302.notes.data.response.SingleNotesResponse;
import cs302.notes.exceptions.ForbiddenException;
import cs302.notes.exceptions.InternalServerError;
import cs302.notes.exceptions.InvalidPageableException;
import cs302.notes.exceptions.NotesNotFoundException;
import cs302.notes.models.ListingStatus;
import cs302.notes.models.Notes;
import cs302.notes.repository.NotesRepository;
import cs302.notes.service.serviceImpl.MessageSender;
import cs302.notes.service.serviceImpl.NotesServiceImpl;
import cs302.notes.service.services.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotesServiceUnitTests {
    @Mock
    private NotesRepository notesRepository;
    @Mock
    private MessageSender messageSender;
    @Mock
    private StorageService storageService;
    @InjectMocks
    private NotesServiceImpl notesService;

    // Attributes
    private final String fkAccountOwner = "123456";
    private final String testFileName = "testFile.jpeg";
    private final Path path = Paths.get("./resources/" + testFileName);

    // Mock data
    private final Notes mockNotes = new Notes("1", fkAccountOwner, "CS101 notes", "Take a step into programming fundamentals 1. Explore the world of C", "https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf", "CS101", 500, "Pending");
    private final Notes mockUpdatedNotes = new Notes("1", fkAccountOwner, "CS102 notes", "Take a step into programming fundamentals 2. Explore the world of Java", "https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf", "CS102", 300, "Pending");

    // Mock Requests
    private final UpdateNotesRequest updateRequest = new UpdateNotesRequest("CS102 notes", "Take a step into programming fundamentals 2. Explore the world of Java", "CS102", 300);

    @AfterEach
    void tearDown() {
        notesRepository.deleteAll();
    }

    @Test
    void getAllNotesByAccountId_Successful_ReturnNotes() {
        // Arrange
        int pageNum = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(pageNum, limit);
        Page<Notes> page = new PageImpl<>(List.of(mockNotes));
        when(notesRepository.findByFkAccountOwnerOrderByStatus(fkAccountOwner, pageable)).thenReturn(page);

        // Act
        MultiNotesResponse response = (MultiNotesResponse) notesService.getAllNotesByAccountId(fkAccountOwner, pageNum, limit);
        List<Notes> notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes.size(), 1);
        verify(notesRepository).findByFkAccountOwnerOrderByStatus(any(String.class), any(Pageable.class));
    }

    @Test
    void getAllNotesByAccountId_InvalidPageNum_ThrowException() {
        // Arrange
        int pageNum = -1;
        int limit = 10;

        // Act and assert
        assertThrows(InvalidPageableException.class, () -> notesService.getAllNotesByAccountId(fkAccountOwner, pageNum, limit));
    }

    @Test
    void getAllNotesByAccountId_InvalidLimit_ThrowException() {
        // Arrange
        int pageNum = 0;
        int limit = 0;

        // Act and assert
        assertThrows(InvalidPageableException.class, () -> notesService.getAllNotesByAccountId(fkAccountOwner, pageNum, limit));
    }

    @Test
    void getAllNotesByAccountId_NoNotes_ReturnNotes() {
        // Arrange
        int pageNum = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(pageNum, limit);
        Page<Notes> page = new PageImpl<>(List.of());
        when(notesRepository.findByFkAccountOwnerOrderByStatus(fkAccountOwner, pageable)).thenReturn(page);

        // Act
        MultiNotesResponse response = (MultiNotesResponse) notesService.getAllNotesByAccountId(fkAccountOwner, pageNum, limit);
        List<Notes> notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes.size(), 0);
        verify(notesRepository).findByFkAccountOwnerOrderByStatus(any(String.class), any(Pageable.class));
    }

    @Test
    void getAllNotesByStatusIn_Successful_ReturnNotes() {
        // Arrange
        int pageNum = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(pageNum, limit);
        Page<Notes> page = new PageImpl<>(List.of(mockNotes));
        when(notesRepository.findByStatusIn(List.of("Pending"), pageable)).thenReturn(page);

        // Act
        MultiNotesResponse response = (MultiNotesResponse) notesService.getAllNotesByStatusIn(List.of("Pending"), pageNum, limit);
        List<Notes> notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes.size(), 1);
        verify(notesRepository).findByStatusIn(any(List.class), any(Pageable.class));
    }

    @Test
    void getAllNotesByStatusIn_InvalidPageNum_ThrowException() {
        // Arrange
        int pageNum = -1;
        int limit = 10;

        // Act and assert
        assertThrows(InvalidPageableException.class, () -> notesService.getAllNotesByStatusIn(List.of("Pending"), pageNum, limit));
    }

    @Test
    void getAllNotesByStatusIn_InvalidLimit_ThrowException() {
        // Arrange
        int pageNum = 0;
        int limit = 0;

        // Act and assert
        assertThrows(InvalidPageableException.class, () -> notesService.getAllNotesByStatusIn(List.of("Pending"), pageNum, limit));
    }

    @Test
    void getAllNotesByStatusIn_NoNotes_ReturnNotes() {
        // Arrange
        int pageNum = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(pageNum, limit);
        Page<Notes> page = new PageImpl<>(List.of());
        when(notesRepository.findByStatusIn(List.of("Pending"), pageable)).thenReturn(page);

        // Act
        MultiNotesResponse response = (MultiNotesResponse) notesService.getAllNotesByStatusIn(List.of("Pending"), pageNum, limit);
        List<Notes> notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes.size(), 0);
        verify(notesRepository).findByStatusIn(any(List.class), any(Pageable.class));
    }

    @Test
    void getAllNotesByCategoryCodeAndStatusIn_Successful_ReturnNotes() {
        // Arrange
        int pageNum = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(pageNum, limit);
        Page<Notes> page = new PageImpl<>(List.of(mockNotes));
        when(notesRepository.findByStatusInAndCategoryCode(List.of("Pending"), "CS101", pageable)).thenReturn(page);

        // Act
        MultiNotesResponse response = (MultiNotesResponse) notesService.getAllNotesByCategoryCodeAndStatusIn("CS101", List.of("Pending"), pageNum, limit);
        List<Notes> notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes.size(), 1);
        verify(notesRepository).findByStatusInAndCategoryCode(any(List.class), any(String.class), any(Pageable.class));
    }

    @Test
    void getAllNotesByCategoryCodeAndStatusIn_InvalidPageNum_ThrowException() {
        // Arrange
        int pageNum = -1;
        int limit = 10;

        // Act and assert
        assertThrows(InvalidPageableException.class, () -> notesService.getAllNotesByCategoryCodeAndStatusIn("CS101", List.of("Pending"), pageNum, limit));
    }

    @Test
    void getAllNotesByCategoryCodeAndStatusIn_InvalidLimit_ThrowException() {
        // Arrange
        int pageNum = 0;
        int limit = 0;

        // Act and assert
        assertThrows(InvalidPageableException.class, () -> notesService.getAllNotesByCategoryCodeAndStatusIn("CS101", List.of("Pending"), pageNum, limit));
    }

    @Test
    void getAllNotesByCategoryCodeAndStatusIn_NoNotes_ReturnNotes() {
        // Arrange
        int pageNum = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(pageNum, limit);
        Page<Notes> page = new PageImpl<>(List.of());
        when(notesRepository.findByStatusInAndCategoryCode(List.of("Pending"), "CS101", pageable)).thenReturn(page);

        // Act
        MultiNotesResponse response = (MultiNotesResponse) notesService.getAllNotesByCategoryCodeAndStatusIn("CS101", List.of("Pending"), pageNum, limit);
        List<Notes> notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes.size(), 0);
        verify(notesRepository).findByStatusInAndCategoryCode(any(List.class), any(String.class), any(Pageable.class));
    }

    @Test
    void getNotesById_Successful_ReturnNotes() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));

        // Act
        SingleNotesResponse response = (SingleNotesResponse) notesService.getNotesById("1");
        Notes notes = response.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, mockNotes);
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void getNotesById_InvalidNotesId_ThrowException() {
        // Arrange
        when(notesRepository.findBy_id(any(String.class))).thenReturn(Optional.empty());

        // Act
        assertThrows(NotesNotFoundException.class, () -> notesService.getNotesById("1"));

        // Assert
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void getAllDistinctCategories_Successful_ReturnDistinctCategories() {
        // Arrange
        List<String> categories = List.of("CS101", "CS102", "CS103", "CS104");
        when(notesRepository.findDistinctCategoryCode()).thenReturn(categories);

        // Act
        MultiStringResponse response = (MultiStringResponse) notesService.getAllDistinctCategories();
        List<String> distinctCategories = response.getResponse();

        // Assert
        assertNotNull(distinctCategories);
        assertEquals(distinctCategories.size(), 4);
        verify(notesRepository).findDistinctCategoryCode();
    }

    @Test
    void getAllDistinctCategories_NoCategories_ReturnDistinctCategories() {
        // Arrange
        List<String> categories = List.of();
        when(notesRepository.findDistinctCategoryCode()).thenReturn(categories);

        // Act
        MultiStringResponse response = (MultiStringResponse) notesService.getAllDistinctCategories();
        List<String> distinctCategories = response.getResponse();

        // Assert
        assertNotNull(distinctCategories);
        assertEquals(distinctCategories.size(), 0);
        verify(notesRepository).findDistinctCategoryCode();
    }

    @Test
    void createNotes_Successful_ReturnNotes() {
        // Arrange
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        MultipartFile multipartFile = new MockMultipartFile(testFileName, testFileName, "image/jpeg", content);
        CreateNotesRequest createRequest = new CreateNotesRequest("CS101 notes", "Take a step into programming fundamentals 1. Explore the world of C", "CS101", 500, multipartFile, null);
        when(storageService.uploadFile(multipartFile, fkAccountOwner)).thenReturn("https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf");
        when(notesRepository.insert(any(Notes.class))).thenReturn(mockNotes);

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.createNotes(createRequest, fkAccountOwner);
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, mockNotes);
        verify(storageService).uploadFile(any(MultipartFile.class), any(String.class));
        verify(notesRepository).insert(any(Notes.class));
        verify(messageSender).publishListingUploaded(any(ListingStatus.class));
    }

    @Test
    void createNotes_InternalServerError_ThrowException() {
        // Arrange
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        MultipartFile multipartFile = new MockMultipartFile(testFileName, testFileName, "image/jpeg", content);
        CreateNotesRequest createRequest = new CreateNotesRequest("CS101 notes", "Take a step into programming fundamentals 1. Explore the world of C", "CS101", 500, multipartFile, null);
        when(storageService.uploadFile(multipartFile, fkAccountOwner)).thenThrow(new InternalServerError("Internal Server Error when uploading file"));

        // Act
        assertThrows(InternalServerError.class, () -> notesService.createNotes(createRequest, fkAccountOwner));

        // Assert
        verify(storageService).uploadFile(any(MultipartFile.class), any(String.class));
    }

    @Test
    void updateNotes_Successful_ReturnNotes() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));
        when(notesRepository.save(mockUpdatedNotes)).thenReturn(mockUpdatedNotes);

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.updateNotes(fkAccountOwner, "1", updateRequest);
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, mockUpdatedNotes);
        verify(notesRepository).findBy_id(any(String.class));
        verify(notesRepository).save(any(Notes.class));
    }

    @Test
    void updateNotes_NotesNotFound_ThrowException() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.empty());

        // Act
        assertThrows(NotesNotFoundException.class, () -> notesService.updateNotes(fkAccountOwner, "1", updateRequest));

        // Assert
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void updateNotes_Forbidden_ThrowException() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));

        // Act
        assertThrows(ForbiddenException.class, () -> notesService.updateNotes("1234", "1", updateRequest));

        // Assert
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void updateNotes_ModifyTitle_ReturnNotes() {
        // Arrange
        UpdateNotesRequest modifiedRequest = UpdateNotesRequest.builder().title("CS102 notes").build();
        Notes modifiedNotes = new Notes("1", fkAccountOwner, "CS102 notes", "Take a step into programming fundamentals 1. Explore the world of C", "https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf", "CS101", 500, "Pending");
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));
        when(notesRepository.save(modifiedNotes)).thenReturn(modifiedNotes);

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.updateNotes(fkAccountOwner, "1", modifiedRequest);
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, modifiedNotes);
        verify(notesRepository).findBy_id(any(String.class));
        verify(notesRepository).save(any(Notes.class));
    }

    @Test
    void updateNotes_ModifyDescription_ReturnNotes() {
        // Arrange
        UpdateNotesRequest modifiedRequest = UpdateNotesRequest.builder().description("Take a step into programming fundamentals 2. Explore the world of Java").build();
        Notes modifiedNotes = new Notes("1", fkAccountOwner, "CS101 notes", "Take a step into programming fundamentals 2. Explore the world of Java", "https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf", "CS101", 500, "Pending");
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));
        when(notesRepository.save(modifiedNotes)).thenReturn(modifiedNotes);

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.updateNotes(fkAccountOwner, "1", modifiedRequest);
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, modifiedNotes);
        verify(notesRepository).findBy_id(any(String.class));
        verify(notesRepository).save(any(Notes.class));
    }

    @Test
    void updateNotes_ModifyCategoryCode_ReturnNotes() {
        // Arrange
        UpdateNotesRequest modifiedRequest = UpdateNotesRequest.builder().categoryCode("CS102").build();
        Notes modifiedNotes = new Notes("1", fkAccountOwner, "CS101 notes", "Take a step into programming fundamentals 1. Explore the world of C", "https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf", "CS102", 500, "Pending");
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));
        when(notesRepository.save(modifiedNotes)).thenReturn(modifiedNotes);

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.updateNotes(fkAccountOwner, "1", modifiedRequest);
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, modifiedNotes);
        verify(notesRepository).findBy_id(any(String.class));
        verify(notesRepository).save(any(Notes.class));
    }

    @Test
    void updateNotes_ModifyPrice_ReturnNotes() {
        // Arrange
        UpdateNotesRequest modifiedRequest = UpdateNotesRequest.builder().price(300).build();
        Notes modifiedNotes = new Notes("1", fkAccountOwner, "CS101 notes", "Take a step into programming fundamentals 1. Explore the world of C", "https://only-notes-bucket.s3.ap-southeast-1.amazonaws.com/123456_1729680218630_EvenmoreExercisessolutions.pdf", "CS101", 300, "Pending");
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));
        when(notesRepository.save(modifiedNotes)).thenReturn(modifiedNotes);

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.updateNotes(fkAccountOwner, "1", modifiedRequest);
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, modifiedNotes);
        verify(notesRepository).findBy_id(any(String.class));
        verify(notesRepository).save(any(Notes.class));
    }

    @Test
    void deleteNotes_Successful_ReturnNotes() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));

        // Act
        SingleNotesResponse singleNotesResponse = (SingleNotesResponse) notesService.deleteNotes(fkAccountOwner, "1");
        Notes notes = singleNotesResponse.getResponse();

        // Assert
        assertNotNull(notes);
        assertEquals(notes, mockNotes);
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void deleteNotes_NoNotes_ThrowException() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.empty());

        // Act
        assertThrows(NotesNotFoundException.class, () -> notesService.deleteNotes(fkAccountOwner, "1"));

        // Assert
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void deleteNotes_Forbidden_ThrowException() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));

        // Act
        assertThrows(ForbiddenException.class, () -> notesService.deleteNotes("1234", "1"));

        // Assert
        verify(notesRepository).findBy_id(any(String.class));
    }

    @Test
    void deleteNotes_InternalServerError_ThrowException() {
        // Arrange
        when(notesRepository.findBy_id("1")).thenReturn(Optional.of(mockNotes));
        doThrow(InternalServerError.class).when(storageService).deleteFile(any(String.class), any(String.class));

        // Act
        assertThrows(InternalServerError.class, () -> notesService.deleteNotes(fkAccountOwner, "1"));

        // Assert
        verify(notesRepository).findBy_id(any(String.class));
    }
}
