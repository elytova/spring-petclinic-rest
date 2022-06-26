package ApiTests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static Helpers.APIMethods.*;
import static Helpers.RandomHelper.randomNumeric;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@DisplayName("Тестирование OwnerControllerTest")
public class OwnerControllerTest {

	private static Connection connection;
	final static int TEST_OWNER_ID = randomNumeric();
	final static String TEST_OWNER_NAME = "Testname";
	final static String TEST_OWNER_LASTNAME = "Testlastname";

	@BeforeAll
	public static void connect() throws SQLException {
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost:5432/petclinic",
			"petclinic",
			"petclinic");
	}

	@AfterAll
	public static void disconnect() throws SQLException {
		connection.close();
	}

	@Nested
	@DisplayName("processFindForm")
	class CheckProcessFindForm {
		@Test
		public void shouldReturnPetsWhenGet() {

			when()
				.get(METHOD_OWNER_WITHOUT_OWNERID)
				.then()
				.statusCode(200)
				.body("address", not(empty()));
		}

		@Test
		public void shouldReturnPetsWhenGetWithLastName() throws SQLException {
			PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
			sql.setInt(1, TEST_OWNER_ID);
			sql.setString(2, TEST_OWNER_NAME);
			sql.setString(3, TEST_OWNER_LASTNAME);
			sql.setString(4, "2698 Helloworld St.");
			sql.setString(5, "Garden");
			sql.setString(6, "6085539554");
			sql.executeUpdate();
			given()
				.queryParam("lastName", TEST_OWNER_LASTNAME).queryParam("page", 1)
			.when().get(METHOD_OWNER_WITHOUT_OWNERID).then().statusCode(200);
		}
	}

	@Nested
	@DisplayName("processCreationForm")
	class CheckProcessCreationForm {
		@Test
		public void shouldReturnOkWhenCreateNewOwner() {
			given().contentType("application/json")
				.body("{\n" + "  \"address\": \"testAddress\",\n" + "  \"city\": \"testcity\",\n"
					+ "  \"firstName\": \"stringFirstName\",\n" + "  \"id\": " + TEST_OWNER_ID + ",\n"
					+ "  \"lastName\": \"stringLastName\",\n" + "  \"pets\": [\n" + "    {\n"
					+ "      \"birthDate\": \"2000-06-20\",\n" + "      \"id\": 88,\n"
					+ "      \"name\": \"stringName\",\n" + "      \"visits\": [\n" + "        {\n"
					+ "          \"date\": \"2000-06-20\",\n" + "          \"description\": \"stringName\",\n"
					+ "          \"id\": 77\n" + "        }\n" + "      ]\n" + "    }\n" + "  ],\n"
					+ "  \"telephone\": \"2345363\"\n" + "}")
				.when().post(METHOD_OWNER_WITHOUT_OWNERID).then().statusCode(201).body("id", not(empty()));
		}

		@Test
		public void shouldReturnErrorWhenCreateOwnerWithoutBody() {
			given()
				.contentType("application/json")
				.body("{}")
				.when()
				.post(METHOD_OWNER_WITHOUT_OWNERID)
				.then()
				.statusCode(400)
				.body("error", not(empty()));
		}
	}

	@Nested
	@DisplayName("showOwner")
	class CheckShowOwner {
		@Test
		public void shouldReturnPetsWhenGetShowOwnerWithId() throws SQLException {
			PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
			sql.setInt(1, TEST_OWNER_ID);
			sql.setString(2, TEST_OWNER_NAME);
			sql.setString(3, TEST_OWNER_LASTNAME);
			sql.setString(4, "2698 Helloworld St.");
			sql.setString(5, "Garden");
			sql.setString(6, "7485539554");
			sql.executeUpdate();

			given()
				.contentType(ContentType.JSON).pathParam("ownerId", TEST_OWNER_ID)
			.when().get(METHOD_OWNER_WITH_OWNERID)
			.then().statusCode(200)
				.body("isEmpty()", Matchers.is(false)).body("id", not(empty()));
		}

		@Test
		public void shouldReturnPetsWhenGetShowOwnerWithoutId() {
			when().get(METHOD_OWNER_WITHOUT_OWNERID).then().statusCode(200);
		}

		@Test
		@Disabled
		public void shouldReturnErrorWhenGetWithWringId() {
			when().get("/owners/123456")
			.then().statusCode(200).body("isEmpty()", Matchers.is(true));
		}
	}

	@Nested
	@DisplayName("processUpdateOwnerForm")
	class CheckProcessUpdateOwnerForm {
		@Test
		public void shouldChangeDataWhenUpdate() throws SQLException {
			PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
			sql.setInt(1, TEST_OWNER_ID);
			sql.setString(2, TEST_OWNER_NAME);
			sql.setString(3, TEST_OWNER_LASTNAME);
			sql.setString(4, "2698 Helloworld St.");
			sql.setString(5, "Garden");
			sql.setString(6, "7485539554");
			sql.executeUpdate();

			given().contentType("application/json").pathParam("ownerId", TEST_OWNER_ID)
				.body("{\n" + "  \"address\": \"test\"\n" + "}")
				.when().post(METHOD_OWNER_WITH_OWNERID)
				.then().statusCode(200)
				.body("address", equalTo("test"));
		}

		@Test
		public void shouldReturnErrorWhenUpdateWithoutBody() throws SQLException {

			PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
			sql.setInt(1, 888);
			sql.setString(2, TEST_OWNER_NAME);
			sql.setString(3, TEST_OWNER_LASTNAME);
			sql.setString(4, "2698 Helloworld St.");
			sql.setString(5, "Garden");
			sql.setString(6, "7485539554");
			sql.executeUpdate();

			given().contentType("application/json").pathParam("ownerId", 888)
				.body("{}")
				.when().post(METHOD_OWNER_WITH_OWNERID)
				.then().statusCode(400)
				.body("error", not(empty()));
		}
	}

	@AfterEach
	public void deleteDataFromDB() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
			"DELETE FROM OWNERS WHERE ID=? OR FIRST_NAME=?"
		);
		sql.setInt(1, TEST_OWNER_ID);
		sql.setString(2, TEST_OWNER_NAME);
		sql.executeUpdate();
	}
}
