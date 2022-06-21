package ApiTests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class OwnerControllerTest {

	private static Connection connection;

	@BeforeAll
	public static void connect() throws SQLException {
		connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/petclinic", "petclinic",
				"petclinic");
	}

	@AfterAll
	public static void disconnect() throws SQLException {
		connection.close();
	}

	// processFindForm

	@Test
	public void shouldReturnPetsWhenGet() {
		when().get("/owners").then().statusCode(200).body("address", not(empty()));
	}

	@Test
	public void shouldReturnPetsWhenGetWithLastName() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
		sql.setInt(1, 555);
		sql.setString(2, "Testname");
		sql.setString(3, "Testlastname");
		sql.setString(4, "2698 Helloworld St.");
		sql.setString(5, "Garden");
		sql.setString(6, "6085539554");
		sql.executeUpdate();

		when().get("/owners/Testlastname").then().statusCode(200);

		PreparedStatement sqlDelete = connection.prepareStatement("DELETE FROM country WHERE id = 555");
		sqlDelete.executeUpdate();
	}

	// processCreationForm

	@Test
	public void shouldReturnOkWhenCreateNewUser() throws SQLException {
		given().contentType("application/json")
				.body("{\n" + "  \"address\": \"testAddress\",\n" + "  \"city\": \"testcity\",\n"
						+ "  \"firstName\": \"stringFirstName\",\n" + "  \"id\": 999,\n"
						+ "  \"lastName\": \"stringLastName\",\n" + "  \"pets\": [\n" + "    {\n"
						+ "      \"birthDate\": \"2000-06-20\",\n" + "      \"id\": 88,\n"
						+ "      \"name\": \"stringName\",\n" + "      \"visits\": [\n" + "        {\n"
						+ "          \"date\": \"2000-06-20\",\n" + "          \"description\": \"stringName\",\n"
						+ "          \"id\": 77\n" + "        }\n" + "      ]\n" + "    }\n" + "  ],\n"
						+ "  \"telephone\": \"2345363\"\n" + "}")
				.when().post("/api/countries").then().statusCode(201).body("id", not(empty()));

		PreparedStatement sqlDelete = connection.prepareStatement("DELETE FROM country WHERE id = 999");
		sqlDelete.executeUpdate();
	}

	@Test
	public void shouldReturnErrorWhenCreateWithoutBody() {
		given().contentType("application/json").body("{}").when().post("/api/countries").then().statusCode(400)
				.body("error", not(empty()));
	}

	// showOwner

	@Test
	public void shouldReturnPetsWhenGetShowOwnerWithId() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
		sql.setInt(1, 666);
		sql.setString(2, "Testname");
		sql.setString(3, "Testlastname");
		sql.setString(4, "2698 Helloworld St.");
		sql.setString(5, "Garden");
		sql.setString(6, "7485539554");
		sql.executeUpdate();

		when().get("/owners/666").then().statusCode(200).body("id", not(empty()));

		PreparedStatement sqlDelete = connection.prepareStatement("DELETE FROM country WHERE id = 666");
		sqlDelete.executeUpdate();
	}

	@Test
	public void shouldReturnPetsWhenGetShowOwnerWithoutId() {
		when().get("/owners/").then().statusCode(500);
	}

	@Test
	@Disabled
	public void shouldReturnErrorWhenGetWithWringId() {
		when().get("/owners/123456").then().statusCode(400);
	}

	// processUpdateOwnerForm
	@Test
	public void shouldChangeDataWhenUpdate() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
		sql.setInt(1, 777);
		sql.setString(2, "Testname");
		sql.setString(3, "Testlastname");
		sql.setString(4, "2698 Helloworld St.");
		sql.setString(5, "Garden");
		sql.setString(6, "7485539554");
		sql.executeUpdate();

		given().contentType("application/json").body("{\n" + "  \"address\": \"test\"\n" + "}").when()
				.patch("/api/countries/777").then().statusCode(200).body("countryName", equalTo("test"));

		PreparedStatement sqlDelete = connection.prepareStatement("DELETE FROM country WHERE id = 777");
		sqlDelete.executeUpdate();
	}

	@Test
	public void shouldReturnErrorWhenUpdateWithoutBody() throws SQLException {

		PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
		sql.setInt(1, 888);
		sql.setString(2, "Testname");
		sql.setString(3, "Testlastname");
		sql.setString(4, "2698 Helloworld St.");
		sql.setString(5, "Garden");
		sql.setString(6, "7485539554");
		sql.executeUpdate();

		given().contentType("application/json").body("{}").when().post("/api/countries/888").then().statusCode(200)
				.body("error", not(empty()));

		PreparedStatement sqlDelete = connection.prepareStatement("DELETE FROM country WHERE id = 888");
		sqlDelete.executeUpdate();
	}

}
