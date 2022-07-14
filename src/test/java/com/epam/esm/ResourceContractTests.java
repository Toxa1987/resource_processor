package com.epam.esm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@AutoConfigureStubRunner(
        ids = "com.epam.esm:resource-service:0.0.1-SNAPSHOT:stubs:8081",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class ResourceContractTests {
    @Test
    void get_song_from_resource_service_contract() throws IOException {
        // given:
        RestTemplate restTemplate = new RestTemplate();
        byte[] expected = new FileInputStream(ResourceUtils.getFile(String.format("classpath:%s", "file.mp3"))).readAllBytes();
        // when:
        ResponseEntity<byte[]> song = restTemplate.getForEntity("http://localhost:8081/resources/1", byte[].class);
        // then:
        Assertions.assertEquals(HttpStatus.OK, song.getStatusCodeValue());
        Assertions.assertArrayEquals(expected,song.getBody());
    }
}
