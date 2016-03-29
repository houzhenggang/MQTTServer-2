package com.vmanolache.mqttserver;
 
import java.util.ArrayList;
import java.util.Collection; 

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.vmanolache.mqttserver.entity.Notification;
import com.vmanolache.mqttserver.entity.NotificationRepository; 
import com.vmanolache.mqttserver.entity.NotificationDTO;
import com.vmanolache.mqttserver.entity.QNotification;

/**
 *
 * @author zakyalvan
 */
@SpringBootApplication
@IntegrationComponentScan
public class MqttNotificationApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttNotificationApplication.class);

    public static void main(String[] args) {
        new SpringApplication(MqttNotificationApplication.class).run(args);
    }

    @Autowired
    private NotificationRepository notifRepository;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public MessageSource<Collection<Notification>> notificationSource() {
        return () -> {
//			LOGGER.debug("Loading unpublished notifications");
            QNotification notifRoot = QNotification.notification;
            Collection<Notification> notifications = new ArrayList<>();
            notifRepository.findAll(notifRoot.published.isFalse())
                    .forEach(notification -> notifications.add(notification));
//			LOGGER.debug("Loaded {} unpublished notifications ({})", notifications.size(), notifications);
            return MessageBuilder.withPayload(notifications).build();
        };
    }

    /**
     * Main flow of publish notification.
     *
     * @return
     */
    @Bean
    public IntegrationFlow notificationFlow() {
        return IntegrationFlows
                .from(notificationSource(), spec -> spec.poller(Pollers.fixedRate(5000l)))
                .<Collection<Notification>>filter(
                        payload -> payload.size() > 0,
                        filterSpec -> filterSpec.discardFlow(discardFlow -> discardFlow
//                                .handle(message -> LOGGER.debug("Publish notifications discarded, no unpublished notifications found, received message payload {}", message.getPayload()))
                                .handle(message -> {})  // No-op handler. 

                        )
                )
                .split()
                .<Notification, Boolean>route(Notification::isTargeted, mapping -> mapping
                        .subFlowMapping("true", flow -> flow
                                .publishSubscribeChannel(spec -> spec
                                        .id("notif.targeted.pubsub")
                                        .subscribe(subscribeFlow -> subscribeFlow
                                                .channel(channelSpec -> channelSpec.queue())
                                                .<Notification, Collection<Message<NotificationDTO>>>transform(notification -> {
                                                    NotificationDTO notificationDto
                                                            = new NotificationDTO(notification.getSubject(), notification.getContent());

                                                    Collection<Message<NotificationDTO>> dtoMessages = new ArrayList<>();
                                                    notification.getTargets().forEach(target -> {
                                                        Message<NotificationDTO> dtoMessage = MessageBuilder.withPayload(notificationDto)
                                                                .setHeader("target.user", target)
                                                                .build();
                                                        dtoMessages.add(dtoMessage);
                                                    });
                                                    return dtoMessages;
                                                }, transformSpec -> transformSpec.poller(Pollers.fixedRate(1000l)))
                                                .split()
                                                .transform(Transformers.toJson())
                                                .handle(Jms.outboundAdapter(connectionFactory).destination(message -> "user.notification.".concat(message.getHeaders().get("target.user", String.class))).configureJmsTemplate(templateSpec -> templateSpec.pubSubDomain(true)))
                                        )
                                )
                        )
                        .subFlowMapping("false", flow -> flow
                                .publishSubscribeChannel(spec -> spec
                                        .id("notif.nontargeted.pubsub")
                                        .subscribe(subscribeFlow -> subscribeFlow
                                                .channel(channelSpec -> channelSpec.queue())
                                                .<Notification, NotificationDTO>transform(Notification.class, notification -> new NotificationDTO(notification.getSubject(), notification.getContent()), transformSpec -> transformSpec.poller(Pollers.fixedRate(1000l)))
                                                .transform(Transformers.toJson())
                                                .handle(Jms.outboundAdapter(connectionFactory).destination(message -> "user.notification.*").configureJmsTemplate(templateSpec -> templateSpec.pubSubDomain(true)))
                                        )
                                )
                        )
                )
                .resequence(spec -> spec.id("notif.resequencer"))
                .aggregate(spec -> spec.id("notif.aggregator"))
                .handle(message -> {
                    Collection<Notification> payload = (Collection<Notification>) message.getPayload();
                    payload.forEach(notification -> notification.setPublished(true));
                    notifRepository.save(payload);
                })
                .get();
    }
}
