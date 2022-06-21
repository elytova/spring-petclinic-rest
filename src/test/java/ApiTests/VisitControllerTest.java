package ApiTests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class VisitControllerTest {

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

	// initNewVisitForm
	// processNewVisitForm

	@Test
	@Disabled
	public void shouldReturnOkWhenCreateNewVisit() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
				"INSERT INTO owners(id, first_name, last_name, address, city, telephone) VALUES(?,?,?,?,?,?)");
		sql.setInt(1, 888);
		sql.setString(2, "Testname");
		sql.setString(3, "Testlastname");
		sql.setString(4, "2698 Helloworld St.");
		sql.setString(5, "Garden");
		sql.setString(6, "6085539554");
		sql.executeUpdate();

		PreparedStatement sqlPets = connection
				.prepareStatement("INSERT INTO pets(id, name, birth_date, owner_id) VALUES(?,?,?,?)");
		sqlPets.setInt(1, 88);
		sqlPets.setString(2, "Bobik");
		sqlPets.setString(3, "2000-09-07");
		sqlPets.setString(4, "888");

		sql.executeUpdate();

		given().contentType("application/json").body("{}").when().post("/owners/888/pets/88/visits").then()
				.statusCode(200).body("id", not(empty()));

		PreparedStatement sqlDelete = connection.prepareStatement("DELETE FROM owners WHERE id = 888");
		sqlDelete.executeUpdate();
		PreparedStatement sqlDeletePets = connection.prepareStatement("DELETE FROM pets WHERE id = 88");
		sqlDeletePets.executeUpdate();

	}

}
