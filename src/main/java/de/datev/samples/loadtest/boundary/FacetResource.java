package de.datev.samples.loadtest.boundary;

import de.datev.samples.loadtest.config.LoadTestConfiguration;
import de.datev.samples.loadtest.control.LoadGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Different test facets. Every test returns an object of type StringResult, NumberResult or StatusResult, so
 * that the object mapper is always involved, which is a more realistic scenario.
 */
@RestController
@RequestMapping("/api/test")
public class FacetResource {

    private final Logger log = LoggerFactory.getLogger(FacetResource.class);

    private static final HashMap<String, Object> PATH_RESULT_CLASS_LOOKUP = new HashMap<>();

    static {
        PATH_RESULT_CLASS_LOOKUP.put("sleep", StringResult.class);
        PATH_RESULT_CLASS_LOOKUP.put("fib", NumberResult.class);
        PATH_RESULT_CLASS_LOOKUP.put("remote-fib", NumberResult.class);
        PATH_RESULT_CLASS_LOOKUP.put("remote", new ParameterizedTypeReference<StatusResult<StringResult>>(){});
        PATH_RESULT_CLASS_LOOKUP.put("return", StringResult.class);
        PATH_RESULT_CLASS_LOOKUP.put("receive", NumberResult.class);
        PATH_RESULT_CLASS_LOOKUP.put("memory", NumberResult.class);
    }

    private LoadTestConfiguration loadTestConfiguration;
    private LoadGeneratorService loadGeneratorService;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public FacetResource(LoadTestConfiguration loadTestConfiguration, LoadGeneratorService loadGeneratorService) {
        this.loadTestConfiguration = loadTestConfiguration;
        this.loadGeneratorService = loadGeneratorService;
    }

    @GetMapping(path = "/sleep")
    public ResponseEntity<StringResult> sleepRequest(@RequestParam(value = "ms", required = false) Long ms) {

        if (ms == null) {
            ms = this.loadTestConfiguration.getDefaultSleepTimeMilliseconds();
        }
        log.debug("sleepRequest: ms={}", ms);
        try {
            Thread.sleep(ms);
            return ResponseEntity.ok().body(new StringResult("OK"));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.ok().body(new StringResult("Interrupted"));
        }
    }

    @GetMapping(path = "/fib")
    public ResponseEntity<NumberResult> fibonacciRequest(@RequestParam(value = "input", required = false) Integer input) {

        if (input == null) {
            input = this.loadTestConfiguration.getDefaultFibInput();
        }
        log.debug("fibonacciRequest: input={}", input);
        if (input < 0 || input > 100) {
            log.warn("fibonacciRequest input was bad (0 <= input < 100): {}", input);
            return ResponseEntity.badRequest().build();
        }
        final int result = this.loadGeneratorService.computeFibonacciRecursive(input);
        return ResponseEntity.ok().body(new NumberResult(result));
    }

    @GetMapping(path = "/remote-fib")
    public ResponseEntity<NumberResult> remoteFibonacciRequest(
            @RequestParam(value = "input", required = false) Integer input,
            HttpServletRequest request) {

        if (input == null) {
            input = this.loadTestConfiguration.getDefaultFibInput();
        }
        log.debug("remoteFibonacciRequest: input={}", input);
        if (input < 0 || input > 100) {
            log.warn("remoteFibonacciRequest input was bad (0 <= input < 100): {}", input);
            return ResponseEntity.badRequest().build();
        }

        if (input == 0) {
            return ResponseEntity.ok().body(new NumberResult(0));
        }
        else if (input == 1) {
            return ResponseEntity.ok().body(new NumberResult(1));
        }
        else {
            String targetUrl = request.getRequestURL().toString();
            if (this.loadTestConfiguration.isForceSsl()) {
                targetUrl = targetUrl.replace("http:", "https:");
            }
            //log.info("remoteFibonacciRequest: targetUrl1={}", targetUrl);
            final long fibMinus1 = restTemplate.getForEntity(
                    targetUrl + "?input=" + (input-1), NumberResult.class).getBody().getResult();
            //log.info("remoteFibonacciRequest: targetUrl2={}", targetUrl);
            final long fibMinus2 = restTemplate.getForEntity(
                    targetUrl + "?input=" + (input-2), NumberResult.class).getBody().getResult();
            return ResponseEntity.ok().body(new NumberResult(fibMinus1 + fibMinus2));
        }
    }

    @GetMapping(path = "/remote")
    public ResponseEntity<StatusResult> remoteRequest(@RequestParam(value = "url", required = false) String url, HttpServletRequest request) {

        if (url == null || url.trim().length() == 0) {
            url = this.loadTestConfiguration.getDefaultRemoteUrl();
        }
        final String targetUrl = this.urlReplace(url, request);
        log.debug("remoteRequest: url={}, targetUrl={}", url, targetUrl);

        final URI targetUri;
        try {
            targetUri = new URI(targetUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new StatusResult<>(-1, e.getMessage()));
        }
        final Object resultClass = lookupResultClass(targetUri);
        final ResponseEntity response;
        if (resultClass instanceof ParameterizedTypeReference) {
            response = restTemplate.exchange(targetUrl, HttpMethod.GET, null, (ParameterizedTypeReference) resultClass);
        } else {
            response = restTemplate.getForEntity(targetUrl, (Class) resultClass);
        }
        final int statusCode = response.getStatusCodeValue();
        final Object result = response.getBody();
        return ResponseEntity.ok().body(new StatusResult<>(statusCode, result));
    }

    @GetMapping(path = "/return", produces = "application/json")
    public ResponseEntity<StringResult> returnRequest(@RequestParam(value = "size", required = false) Integer size) {

        if (size == null) {
            size = this.loadTestConfiguration.getDefaultReturnSize();
        }
        log.debug("returnRequest: size={}", size);
        String result = this.loadGeneratorService.createStringOfSize(size);
        return ResponseEntity.ok().body(new StringResult(result));
    }

    @GetMapping(path = "/return-blocks", produces = "text/plain")
    public void returnBlocksRequest(
            @RequestParam(value = "numberOfKiloByteBlocks", required = false) Integer numberOfKiloByteBlocks,
            HttpServletResponse response) throws Exception {

        if (numberOfKiloByteBlocks == null) {
            numberOfKiloByteBlocks = this.loadTestConfiguration.getDefaultNumberOfKiloByteBlocks();
        }

        long contentLength = 1024 * numberOfKiloByteBlocks;
        log.debug("streamedRequest: numberOfMegaByteBlocks={}, contentLength={}", numberOfKiloByteBlocks, contentLength);
        response.setHeader("Content-Type", "text/plain");
        response.setHeader("Content-Length", Long.toString(contentLength));
        response.setHeader("Content-Disposition", "attachment;filename=download.txt");
        this.loadGeneratorService.stream(response.getOutputStream(), numberOfKiloByteBlocks);
    }

    // A Spring variant of "Streaming"
    @GetMapping(path = "/return-blocks-streamed")
    public StreamingResponseBody returnBlocksRequest2(
            @RequestParam(value = "numberOfKiloByteBlocks", required = false) Integer numberOfKiloByteBlocks,
            HttpServletResponse response) {

        if (numberOfKiloByteBlocks == null) {
            numberOfKiloByteBlocks = this.loadTestConfiguration.getDefaultNumberOfKiloByteBlocks();
        }

        long contentLength = 1024 * numberOfKiloByteBlocks;
        log.debug("streamedRequest: numberOfMegaByteBlocks={}, contentLength={}", numberOfKiloByteBlocks, contentLength);
        response.setHeader("Content-Type", "text/plain");
        response.setHeader("Content-Length", Long.toString(contentLength));
        response.setHeader("Content-Disposition", "attachment;filename=download.txt");

        final int numberOfKiloByteBlocksF = numberOfKiloByteBlocks;
        return outputStream -> loadGeneratorService.stream(outputStream, numberOfKiloByteBlocksF);
    }

    @PostMapping(path = "/receive")
    public ResponseEntity<NumberResult> receiveRequest(@RequestBody StringResult receive) {

        return ResponseEntity.ok().body(new NumberResult(receive.getResult().length()));
    }

    @GetMapping(path = "/memory")
    public ResponseEntity<NumberResult> memoryRequest(
            @RequestParam(value = "factor", required = false) Integer factor) {

        if (factor == null) {
            factor = this.loadTestConfiguration.getDefaultMemoryFactor();
        }

        final Map<String, List<String>> object = this.loadGeneratorService.createLargeObject(factor);
        final int size = this.loadGeneratorService.calculateSizeOfLargeObject(object);
        log.debug("memoryRequest: size={}", size);
        return ResponseEntity.ok().body(new NumberResult(size));
    }

    @GetMapping(path = "/sse-time-by-second")
    public SseEmitter streamSseEventsEachSecond(@RequestParam(value = "nrOfEvents", required = false) Integer nrOfEventsParam) {

        if (nrOfEventsParam == null) {
            nrOfEventsParam = 10;
        }
        final int nrOfEvents = nrOfEventsParam;

        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; i < nrOfEvents; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data(LocalTime.now().toString())
                            .id(String.valueOf(i))
                            .name("sse-time-by-second");
                    emitter.send(event);
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    //------------------------------------------------------------------------------------------------------------------
    //- HELPER OPERATIONS ----------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    @GetMapping(path = "/echo-url")
    public ResponseEntity<StringResult> getRequestUrl(HttpServletRequest request) {

        final String requestUrl = request.getRequestURL().toString();
        log.debug("getRequestUrl: requestUrl={}", requestUrl);
        return ResponseEntity.ok().body(new StringResult(requestUrl));
    }

    @GetMapping(path = "/echo-header")
    public ResponseEntity<Map<String,String>> getRequestHeader(HttpServletRequest request) {

        Map<String,String> ret = new HashMap<>();
        enumerationAsStream(request.getHeaderNames())
                .forEach(name -> ret.put(name, request.getHeader(name)));
        return ResponseEntity.ok().body(ret);
    }

    @GetMapping(path = "/system-info")
    public ResponseEntity<Map<String,String>> getSystemInfo(HttpServletRequest request) {

        Map<String,String> ret = new HashMap<>();
        System.getProperties().stringPropertyNames().stream().forEach(k -> ret.put(k, System.getProperty(k)));
        return ResponseEntity.ok().body(ret);
    }

    //------------------------------------------------------------------------------------------------------------------

    private String urlReplace(String url, HttpServletRequest request) {

        String ret = url;
        if (this.loadTestConfiguration.getUrlReplacementBase() != null) {
            ret = ret.replace("$BASE$", this.loadTestConfiguration.getUrlReplacementBase());
        }
        final String requestUrl = request.getRequestURL().toString();
        ret = urlReplace(ret, requestUrl);
        if (this.loadTestConfiguration.isForceSsl()) {
            ret = ret.replace("http:", "https:");
        }
        log.debug("urlReplace: requestUrl={}, ret={}", requestUrl, ret);
        return ret;
    }

    // Not private to be testable
    String urlReplace(String url, String requestUri) {

        String ret = url;
        try {
            final URL requestUrl = new URL(requestUri);
            ret = ret.replace("$PROTOCOL$", requestUrl.getProtocol());
            ret = ret.replace("$HOST$", requestUrl.getHost());
            int port = requestUrl.getPort();
            if (port > 0) {
                ret = ret.replace("$PORT$", Integer.toString(requestUrl.getPort()));
            } else {
                ret = ret.replace(":$PORT$", ""); // a bit hacky
            }
            String path = requestUrl.getPath();
            String contextRoot = "";
            if (path != null) {
                int i = path.indexOf("/", 1);
                if (i > 0) {
                    contextRoot = path.substring(1, i);
                }
            }
            ret = ret.replace("$CONTEXTROOT$", contextRoot);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private Object lookupResultClass(URI targetUri) {

        String lastPath = targetUri.getPath();
        lastPath = lastPath.substring(lastPath.lastIndexOf('/') + 1);
        // log.debug("lookupResultClass: targetUri={}, lastPath={}", targetUri, lastPath);
        return PATH_RESULT_CLASS_LOOKUP.get(lastPath);
    }

    private static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                new Iterator<T>() {
                    public T next() {
                        return e.nextElement();
                    }
                    public boolean hasNext() {
                        return e.hasMoreElements();
                    }
                    public void forEachRemaining(Consumer<? super T> action) {
                        while(e.hasMoreElements()) action.accept(e.nextElement());
                    }
                },
                Spliterator.ORDERED), false);
    }
}
