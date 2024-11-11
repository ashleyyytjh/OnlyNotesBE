package cs302.notes.service.serviceImpl;

import cs302.notes.models.*;
import cs302.notes.exceptions.NotesNotFoundException;
import cs302.notes.repository.NotesRepository;
import io.opentelemetry.api.trace.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {

    private final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final NotesRepository repository;
    private final MessageSender messageSender;

    public MessageReceiver(NotesRepository repository, MessageSender messageSender) {
        this.repository = repository;
        this.messageSender = messageSender;
    }

    @RabbitListener(queues = "${rabbitmq.orders.created.queue}")
    public void receiveMessage(final OrderCreated request, Message message) {
        correlateSpan(message);
        logger.info(String.format("Receiving message: %s", request));
        try {
            repository.findBy_id(request.getNoteId()).orElseThrow(() -> new NotesNotFoundException(request.getNoteId()));
            messageSender.publishNotesFound(request);
            System.out.println(request);
        } catch (NotesNotFoundException e) {
            messageSender.publishNotesMissing(request);
        } catch (Exception e) {
            System.out.println("Error receiving verification");
        }
    }

    @RabbitListener(queues = "${rabbitmq.orders.success.queue}")
    public void receiveMessage(final OrderSuccess request, Message message) {
        correlateSpan(message);
        logger.info(String.format("Receiving message: %s", request));
        try {
            // Append stuff for notes and forward to eddy
            Notes notes = repository.findBy_id(request.getNoteId())
                    .orElseThrow(() -> new NotesNotFoundException(request.getNoteId()));
            OrdersNotesSuccess ordersNotesSuccess = new OrdersNotesSuccess(request, notes);
            messageSender.publishEmailClients(ordersNotesSuccess);

        } catch (NotesNotFoundException e) {
            System.out.println("Notes no longer found");
        } catch (Exception e) {
            System.out.println("Failed to send out notification");
        }
    }

    @RabbitListener(queues = "${rabbitmq.listings.verified.queue}")
    public void receiveMessage(final ListingStatus request, Message message) {
        correlateSpan(message);
        logger.info(String.format("Receiving message: %s", request));
        try {
            Notes notes = repository.findBy_id(request.get_id())
                    .orElseThrow(() -> new NotesNotFoundException(request.get_id()));
            notes.setStatus(request.getStatus());
            repository.save(notes);
            messageSender.publishListingCompleted(request);
            logger.info("Notes updated in Database");
        } catch (NotesNotFoundException e) {
            System.out.println("Notes no longer found");
        } catch (Exception e) {
            System.out.println("Error receiving verification");
        }
    }

    public void correlateSpan(Message message) {
        String correlationId = (String) message.getMessageProperties().getHeaders().get("correlation_id");
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.isRecording()) {
            currentSpan.setAttribute("correlation_id", correlationId);
        }
    }
}
