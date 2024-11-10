package cs302.notes.controller;

import cs302.notes.data.request.CreateNotesRequest;
import cs302.notes.data.request.UpdateNotesRequest;
import cs302.notes.data.response.DefaultResponse;
import cs302.notes.data.response.Response;
import cs302.notes.service.services.NotesService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotesController {

    private final Logger logger = LoggerFactory.getLogger(NotesController.class);
    private final NotesService notesService;

    //Setter Injection
    @Autowired
    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping("/health")
    public ResponseEntity<Response> healthCheck() {
        Response response = DefaultResponse.builder().message("Notes Service is running").build();
        logger.info("GET /health 200");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("${currentApiPrefix}/notes/categories")
    public ResponseEntity<Response> getAllDistinctCategories() {
        Response response = notesService.getAllDistinctCategories();
        logger.info("GET /notes/categories 200");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("${currentApiPrefix}/notes/account")
    public ResponseEntity<Response> getAllNotesByAccount(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int limit,
                                                         @RequestAttribute("id") String id) {
        Response response = notesService.getAllNotesByAccountId(id, page, limit);
        logger.info("GET /notes/account 200");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("${currentApiPrefix}/notes")
    public ResponseEntity<Response> getAllVerifiedNotes(@RequestParam(defaultValue = "") String categoryCode,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int limit) {
        Response response = "".equals(categoryCode) ? notesService.getAllNotesByStatusIn(List.of("Verified"), page, limit)
                : notesService.getAllNotesByCategoryCodeAndStatusIn(categoryCode, List.of("Verified"), page, limit);
        logger.info("GET /notes 200");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "${currentApiPrefix}/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> createNotes(@Valid @ModelAttribute CreateNotesRequest request,
                                                @RequestAttribute("id") String id) {
        Response notesResponse = notesService.createNotes(request, id);
        logger.info("POST /notes 201");
        return new ResponseEntity<>(notesResponse, HttpStatus.CREATED);
    }

    @GetMapping("${currentApiPrefix}/notes/{notesId}")
    public ResponseEntity<Response> getNotesById(@PathVariable("notesId") String notesId) {
        Response response = notesService.getNotesById(notesId);
        logger.info(String.format("GET /notes/%s 200", notesId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("${currentApiPrefix}/notes/{notesId}")
    public ResponseEntity<Response> updateNotesById(@PathVariable("notesId") String notesId,
                                                    @Valid @RequestBody UpdateNotesRequest request,
                                                    @RequestAttribute("id") String id) {
        Response response = notesService.updateNotes(id, notesId, request);
        logger.info(String.format("PUT /notes/%s 200", notesId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("${currentApiPrefix}/notes/{notesId}")
    public ResponseEntity<Response> deleteNotesById(@PathVariable("notesId") String notesId,
                                                    @RequestAttribute("id") String id) {
        Response response = notesService.deleteNotes(id, notesId);
        logger.info(String.format("DELETE /notes/%s 200", notesId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
