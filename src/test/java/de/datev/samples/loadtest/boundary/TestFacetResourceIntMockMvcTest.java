package de.datev.samples.loadtest.boundary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary tests using MockMvc.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TestFacetResourceIntMockMvcTest {

	private static final String URL = "/api/test";

	@Autowired
	private WebApplicationContext wac;
	@Autowired
	private MockMvc mockLoadTestResource;

	private ObjectMapper objectMapper;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);
		this.mockLoadTestResource = MockMvcBuilders
				.webAppContextSetup(wac)
				//.alwaysDo(print())
				//.apply(springSecurity())
				.build();
		this.objectMapper = createObjectMapper();
	}

	@Test
	public void testThat_sleepRequest_isWorking() throws Exception {

		// arrange
		long start = System.currentTimeMillis();

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/sleep?ms=40"));

		// assert
		long end = System.currentTimeMillis();
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value("OK"));
		assertThat((end-start)).isBetween(30L, 200L);
	}

	@Test
	public void testThat_fibonacciRequest_3_isWorking() throws Exception {

		// arrange

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/fib?input=3"));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value(2));
	}

	@Test
	public void testThat_fibonacciRequest_5_isWorking() throws Exception {

		// arrange

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/fib?input=5"));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value(5));
	}

	@Test
	public void testThat_fibonacciRequest_10_isWorking() throws Exception {

		// arrange

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/fib?input=10"));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value(55));
	}

	@Test
	public void testThat_returnRequest_isWorking() throws Exception {

		// arrange

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/return?size=10"));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value("aaaaaaaaaa"));
	}

	@Test
	public void testThat_returnBlocksRequest_isWorking() throws Exception {

		// arrange

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/return-blocks?numberOfKiloByteBlocks=10"));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(header().longValue("Content-Length", 10240))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(content().string(length(is(10240))))
				.andExpect(content().string(containsString("aaaaaaaaaaaaaaaaaaaaaa")));
	}

	@Test
	public void testThat_receiveRequest_isWorking() throws Exception {

		// arrange
		String aLongString = "aaaaaaaaaa";
		StringResult body = new StringResult(aLongString);

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(post(URL + "/receive")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsBytes(body)));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value(aLongString.length()));
	}

	@Test
	public void testThat_memoryRequest_isWorking() throws Exception {

		// arrange

		// act
		ResultActions resultActions = this.mockLoadTestResource.perform(get(URL + "/memory?factor=100"));

		// assert
		resultActions
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.result").value(1051501));
	}

	private static ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	public static Matcher<String> length(Matcher<? super Integer> matcher) {
		return new FeatureMatcher<String, Integer>(matcher, "a String of length that", "length") {
			@Override
			protected Integer featureValueOf(String actual) {
				return actual.length();
			}
		};
	}
}
