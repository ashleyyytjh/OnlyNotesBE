package cs302.notes.service.serviceImpl;

import cs302.notes.data.request.CreateNotesRequest;
import cs302.notes.data.request.UpdateNotesRequest;
import cs302.notes.data.response.MultiNotesResponse;
import cs302.notes.data.response.MultiStringResponse;
import cs302.notes.data.response.Response;
import cs302.notes.data.response.SingleNotesResponse;
import cs302.notes.exceptions.ForbiddenException;
import cs302.notes.exceptions.NotesNotFoundException;
import cs302.notes.models.ListingStatus;
import cs302.notes.models.Notes;
import cs302.notes.repository.NotesRepository;
import cs302.notes.service.services.NotesService;
import cs302.notes.service.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotesServiceImpl implements NotesService {

    private final Logger logger = LoggerFactory.getLogger(NotesServiceImpl.class);

    private final NotesRepository notesRepository;
    private final MessageSender messageSender;
    private final StorageService storageService;

    //Setter Injection
    @Autowired
    public NotesServiceImpl(NotesRepository notesRepository, MessageSender messageSender, StorageService storageService) {
        this.notesRepository = notesRepository;
        this.messageSender = messageSender;
        this.storageService = storageService;
    }

    /**
     * Service Implementation allowing getting of notes by notesID
     */
    @Override
    public Response getNotesById(String id) {
        Notes notes = notesRepository.findBy_id(id).orElseThrow(() -> {
            logger.warn(String.format("Notes with id %s not found", id));
            return new NotesNotFoundException(id);
        });
        return SingleNotesResponse.builder().response(notes).build();
    }

    @Override
    public Response getAllNotesByAccountId(String account_num, int pageNum, int limit) {
        Pageable paging = PageRequest.of(pageNum, limit);
        Page<Notes> page = notesRepository.findByFkAccountOwnerOrderByStatus(account_num, paging);
        return MultiNotesResponse.builder()
                .totalItems(page.getTotalElements())
                .response(page.getContent())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }

    @Override
    public Response getAllNotesByStatusIn(List<String> status, int pageNum, int limit) {
        Pageable paging = PageRequest.of(pageNum, limit);
        Page<Notes> page = notesRepository.findByStatusIn(status, paging);
        return MultiNotesResponse.builder()
                .totalItems(page.getTotalElements())
                .response(page.getContent())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }

    @Override
    public Response getAllNotesByCategoryCodeAndStatusIn(String categoryCode, List<String> status, int pageNum, int limit) {
        Pageable paging = PageRequest.of(pageNum, limit);
        Page<Notes> page = notesRepository.findByStatusInAndCategoryCode(status, categoryCode, paging);
        return MultiNotesResponse.builder()
                .totalItems(page.getTotalElements())
                .response(page.getContent())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }

    @Override
    public Response getAllDistinctCategories() {
        List<String> categories = notesRepository.findDistinctCategoryCode();
        return MultiStringResponse.builder().response(categories).build();
    }

    private Notes getNotesFromCreateRequest(CreateNotesRequest request, String fkAccountOwner) {
        return Notes.builder()
                .fkAccountOwner(fkAccountOwner)
                .title(request.getTitle())
                .description(request.getDescription())
                .url(request.getUrl())
                .categoryCode(request.getCategoryCode())
                .price(request.getPrice())
                .status("Pending")
                .build();
    }

    @Override
    public Response createNotes(CreateNotesRequest request, String fkAccountOwner) {
        // Uncomment once testing is done
        String s3Url = storageService.uploadFile(request.getFile(), fkAccountOwner);
        // Hardcoded
        request.setUrl(s3Url);
        Notes notes = getNotesFromCreateRequest(request, fkAccountOwner);
        Notes createdNotes = notesRepository.insert(notes);
        // Send notes to listing uploaded channel
        messageSender.publishListingUploaded(ListingStatus.builder()
                ._id(createdNotes.get_id())
                .status("Pending")
                .price(createdNotes.getPrice())
                .categoryCode(createdNotes.getCategoryCode())
                .url(createdNotes.getUrl())
                .build());
        return SingleNotesResponse.builder().response(createdNotes).build();
    }

    private Notes validateNotesAndOwner(String ownerId,String notesId) throws ForbiddenException, NotesNotFoundException {
        Notes notes = notesRepository.findBy_id(notesId).orElseThrow(() -> {
            logger.warn(String.format("Notes with id %s not found", notesId));
            return new NotesNotFoundException(notesId);
        });
        if (notes.getFkAccountOwner().equals(ownerId)) {
            logger.warn("User is not permitted to complete the following action.");
            throw new ForbiddenException();
        }
        return notes;
    }

    private Notes getNotesFromUpdateRequest(Notes notes, UpdateNotesRequest request) {
        if (request.getTitle() != null && !notes.getTitle().equals(request.getTitle())) {
            logger.info("Update Notes 'title': %s", request.getTitle());
            notes.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !notes.getDescription().equals(request.getDescription())) {
            logger.info("Update Notes 'description': %s", request.getDescription());
            notes.setDescription(request.getDescription());
        }
        if (request.getCategoryCode() != null && !notes.getCategoryCode().equals(request.getCategoryCode())) {
            logger.info("Update Notes 'categoryCode': %s", request.getCategoryCode());
            notes.setCategoryCode(request.getCategoryCode());
        }
        if (request.getPrice() != null && !notes.getPrice().equals(request.getPrice())) {
            logger.info("Update Notes 'price': %s", request.getPrice());
            notes.setPrice(request.getPrice());
        }
        return notes;
    }

    @Override
    public Response updateNotes(String fkAccountOwner, String notesId, UpdateNotesRequest request) {
        Notes foundNotes = validateNotesAndOwner(fkAccountOwner, notesId);
        Notes updatedNotes = getNotesFromUpdateRequest(foundNotes, request);
        Notes savedNotes = notesRepository.save(updatedNotes);
        return SingleNotesResponse.builder().response(savedNotes).build();
    }

    @Override
    public Response deleteNotes(String fkAccountOwner, String id) {
        Notes notes = validateNotesAndOwner(fkAccountOwner, id);
        // Delete notes from S3
        notesRepository.delete(notes);
        return SingleNotesResponse.builder().response(notes).build();
    }
}
