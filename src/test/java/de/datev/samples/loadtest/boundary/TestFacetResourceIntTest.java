package de.datev.samples.loadtest.boundary;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestFacetResourceIntTest {

    @LocalServerPort
    private int randomServerPort;

    private String URL = "/api/test";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testThat_sleepRequest_isWorking() {

        // act
        ResponseEntity<StringResult> result = this.restTemplate.getForEntity(URL + "/sleep?ms=40", StringResult.class);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResult()).isEqualTo("OK");
    }

    @Test
    public void testThat_fib_isWorking() {

        // act
        ResponseEntity<NumberResult> result = this.restTemplate.getForEntity(URL + "/fib?input=10", NumberResult.class);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResult()).isEqualTo(55);
    }

    @Test
    public void testThat_remoteFib_isWorking() {

        // act
        ResponseEntity<NumberResult> result = this.restTemplate.getForEntity(URL + "/remote-fib?input=7", NumberResult.class);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResult()).isEqualTo(13);
    }

    @Test
    public void testThat_remoteRequest_isWorking_with_sleep() {

        // arrange
        String remoteUrl = "http://localhost:" + randomServerPort + URL + "/sleep?ms=10";
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<StatusResult<StringResult>>(){};

        // act
        ResponseEntity<StatusResult<StringResult>> result = this.restTemplate.exchange(
                URL + "/remote?url=" + remoteUrl, HttpMethod.GET, null, typeReference);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo(200);
        assertThat(result.getBody().getResult().getResult()).isEqualTo("OK");
    }

    @Test
    public void testThat_remoteRequest_isWorking_with_fib() {

        // arrange
        String remoteUrl = "http://localhost:" + randomServerPort + URL + "/fib?input=5";
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<StatusResult<NumberResult>>(){};

        // act
        ResponseEntity<StatusResult<NumberResult>> result = this.restTemplate.exchange(
                URL + "/remote?url=" + remoteUrl, HttpMethod.GET, null, typeReference);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo(200);
        assertThat(result.getBody().getResult().getResult()).isEqualTo(5);
    }

    @Ignore
    @Test
    public void testThat_streamSseEventsEachSecond_isWorking() {

        // arrange
        ParameterizedTypeReference<ServerSentEvent<String>> type = new ParameterizedTypeReference<ServerSentEvent<String>>() {};

        // TODO!
    }

    @Test
    public void testThat_getRequestUrl_isWorking() {

        // act
        ResponseEntity<StringResult> result = this.restTemplate.getForEntity(
                URL + "/echo-url", StringResult.class);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResult()).isEqualTo("http://localhost:" + this.randomServerPort + URL + "/echo-url");
    }

    @Test
    public void testThat_getRequestHeader_isWorking() {

        // act
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<Map<String,String>>(){};
        ResponseEntity<Map<String,String>> result = this.restTemplate.exchange(
                URL + "/echo-header", HttpMethod.GET, null, typeReference);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().entrySet().size()).isGreaterThanOrEqualTo(1);
        assertThat(result.getBody().get("user-agent")).contains("Java");
    }

    @Test
    public void testThat_getSystemInfo_isWorking() {

        // act
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<Map<String,String>>(){};
        ResponseEntity<Map<String,String>> result = this.restTemplate.exchange(
                URL + "/system-info", HttpMethod.GET, null, typeReference);

        // assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().entrySet().size()).isGreaterThanOrEqualTo(1);
        assertThat(result.getBody().get("file.separator")).isNotNull();
    }
}
