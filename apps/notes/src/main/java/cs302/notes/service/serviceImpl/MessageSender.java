package cs302.notes.service.serviceImpl;

import cs302.notes.models.ListingStatus;
import cs302.notes.models.OrderCreated;
import cs302.notes.models.OrdersNotesSuccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
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
        sendMessage(ordersExchange, "orders.notes.missing", message, message.get_id());
        // rabbitTemplate.convertAndSend(ordersExchange, "orders.notes.missing", message);
    }

    public void publishNotesFound(OrderCreated message) {
        logger.info(String.format("Publishing message: %s", message));
        sendMessage(ordersExchange, "orders.notes.found", message, message.get_id());
        // rabbitTemplate.convertAndSend(ordersExchange, "orders.notes.found", message);
    }

    // Take info from Orders service and append information about notes (everything except id)
    public void publishEmailClients(OrdersNotesSuccess message) {
        logger.info(String.format("Publishing message: %s", message));
        sendMessage(ordersExchange, "orders.email", message, message.get_id().toString());
        // rabbitTemplate.convertAndSend(ordersExchange, "orders.email", message);
    }

    // LISTINGS
    public void publishListingUploaded(ListingStatus message) {
        logger.info(String.format("Publishing message: %s", message));
        sendMessage(listingsExchange, "listings.uploaded", message, message.get_id());
        // rabbitTemplate.convertAndSend(listingsExchange, "listings.uploaded", message);
    }

    public void publishListingCompleted(ListingStatus message) {
        logger.info(String.format("Publishing message: %s", message));
        sendMessage(listingsExchange, "listings.completed", message, message.get_id());
        // rabbitTemplate.convertAndSend(listingsExchange, "listings.completed", message);
    }

    public void sendMessage(String exchange, String routingKey, Object message, String correlationId) {
        System.out.println("Sending message HERE");
        MessagePostProcessor processor = msg -> {
            msg.getMessageProperties().setCorrelationId(correlationId);
            return msg;
        };

        rabbitTemplate.convertAndSend(exchange, routingKey, message, processor);
    }
}
