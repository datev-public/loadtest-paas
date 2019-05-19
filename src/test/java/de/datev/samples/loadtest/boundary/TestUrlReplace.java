package de.datev.samples.loadtest.boundary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUrlReplace {

	@Autowired
	private FacetResource facetResource;

	@Test
	public void testThat_urlReplace_isWorking_with_localhost() {

		// arrange
		String requestUrl = "http://localhost:8080/api/test";
		// act
		String result = this.facetResource.urlReplace("$PROTOCOL$://$HOST$:$PORT$/$CONTEXTROOT$/sleep", requestUrl);
		// assert
		assertThat(result).isEqualTo("http://localhost:8080/api/sleep");
	}

	@Test
	public void testThat_urlReplace_isWorking_with_https_and_context_root() {

		// arrange
		String requestUrl = "https://loadtest1.pcfapps.dev.datev.de/root/api/test";
		// act
		String result = this.facetResource.urlReplace("$PROTOCOL$://$HOST$:$PORT$/$CONTEXTROOT$/api/sleep", requestUrl);
		// assert
		assertThat(result).isEqualTo("https://loadtest1.pcfapps.dev.datev.de/root/api/sleep");
	}
}
