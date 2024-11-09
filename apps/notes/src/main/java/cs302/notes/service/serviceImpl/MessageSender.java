package cs302.notes.service.serviceImpl;

import cs302.notes.models.ListingStatus;
import cs302.notes.models.OrderCreated;
import cs302.notes.models.OrdersNotesSuccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    private final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.orders.exchange}")
    private String ordersExchange;

    @Value("${rabbitmq.listings.exchange}")
    private String listingsExchange;

    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ORDERS
    public void publishNotesMissing(OrderCreated message) {
        logger.info(String.format("Publishing message: %s", message));
        rabbitTemplate.convertAndSend(ordersExchange, "orders.notes.missing", message);
    }

    public void publishNotesFound(OrderCreated message) {
        logger.info(String.format("Publishing message: %s", message));
        rabbitTemplate.convertAndSend(ordersExchange, "orders.notes.found", message);
    }

    // Take info from Orders service and append information about notes (everything except id)
    public void publishEmailClients(OrdersNotesSuccess message) {
        logger.info(String.format("Publishing message: %s", message));
        rabbitTemplate.convertAndSend(ordersExchange, "orders.email", message);
    }

    // LISTINGS
    public void publishListingUploaded(ListingStatus message) {
        logger.info(String.format("Publishing message: %s", message));
        rabbitTemplate.convertAndSend(listingsExchange, "listings.uploaded", message);
    }

    public void publishListingCompleted(ListingStatus message) {
        logger.info(String.format("Publishing message: %s", message));
        rabbitTemplate.convertAndSend(listingsExchange, "listings.completed", message);
    }
}
