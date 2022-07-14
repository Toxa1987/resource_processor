package com.epam.esm.listner;

import com.epam.esm.model.SaveResponse;
import com.epam.esm.service.SongProcessingService;
import lombok.extern.slf4j.Slf4j;
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
    private void receive(SaveResponse response) {
        log.info("received :" + response);
        songProcessingService.process(response.getId());
    }







  /*  private void sdfsf(){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
        Mono<byte[]> songMono = client
                .get()
                .uri("/resources/{id}", 1)
                .exchangeToMono(
                        res -> res.bodyToMono(ByteArrayResource.class)
                                .map(ByteArrayResource::getByteArray));

    }*/
}
