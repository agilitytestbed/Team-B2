package io.DPA.tests;

import static org.hamcrest.CoreMatchers.*;
import static io.restassured.RestAssured.*;
import org.junit.jupiter.api.Test;


public class ControllerTest {

	@Test
	public void testGetTransactions() {
		when().
			get("/transactions").
		then().
			assertThat().	
			statusCode(200);
	}
	
}
