package com.epam.esm.listner;

import com.epam.esm.model.Message;
import com.epam.esm.model.SaveResponse;
import com.epam.esm.service.SongProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageReceiver {

    private final SongProcessingService songProcessingService;

    public MessageReceiver(SongProcessingService songProcessingService) {
        this.songProcessingService = songProcessingService;
    }

    @RabbitListener(queues = "${rabbitmq.queueName}")
    private void receive(Message message) {
        log.info("received :" + message);
        MDC.put("traceId",message.getTraceId());
        try {
            songProcessingService.process(message.getId(), message.getTraceId());
        }finally {
            MDC.remove("traceId");
        }
    }

}
