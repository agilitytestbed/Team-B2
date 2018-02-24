package io.DPA.tests;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;
import org.junit.jupiter.api.Test;


public class ControllerTest {

	@Test
	public void testGetTransactions() {
		when().
			get("/data/transactions").
		then().
			assertThat().	
			statusCode(200);
	}
	
}
