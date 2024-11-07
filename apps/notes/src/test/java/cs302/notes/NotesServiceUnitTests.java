package cs302.notes;

import cs302.notes.repository.NotesRepository;
import cs302.notes.service.serviceImpl.MessageSender;
import cs302.notes.service.serviceImpl.NotesServiceImpl;
import cs302.notes.service.services.NotesService;
import cs302.notes.service.services.StorageService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


    // Mock data

    // Mock Requests

}
