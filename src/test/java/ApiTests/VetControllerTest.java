package ApiTests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static Helpers.APIMethods.*;
import static Helpers.RandomHelper.randomAlphabetString;
import static Helpers.RandomHelper.randomNumeric;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItem;

@DisplayName("Тестирование VetControllerTest")
public class VetControllerTest {

	private static Connection connection;
	final static int TEST_VETERINARY_ID = randomNumeric();
	final static String TEST_VETERINARY_NAME = randomAlphabetString(5);
	final static String TEST_VETERINARY_SURNAME = randomAlphabetString(10);
	final static int TEST_SPECIALTY_ID = randomNumeric();
	final static String TEST_SPECIALTY_NAME = "therapy";

	@BeforeAll
	public static void connectDb() throws SQLException {
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost/petclinic",
			"petclinic",
			"petclinic");
	}

	@BeforeEach
	public void createDataInDB() throws SQLException {
		PreparedStatement sqlVet = connection
				.prepareStatement("INSERT INTO VETS(id, first_name, last_name) VALUES(?,?,?)");
		sqlVet.setInt(1, TEST_VETERINARY_ID);
		sqlVet.setString(2, TEST_VETERINARY_NAME);
		sqlVet.setString(3, TEST_VETERINARY_SURNAME);
		sqlVet.executeUpdate();

		PreparedStatement sqlSpecialty = connection
			.prepareStatement("INSERT INTO SPECIALTIES(id, name) VALUES(?,?)");
		sqlSpecialty.setInt(1, TEST_SPECIALTY_ID);
		sqlSpecialty.setString(2, TEST_SPECIALTY_NAME);
		sqlSpecialty.executeUpdate();

		PreparedStatement sqlVetSpecialty = connection
			.prepareStatement("INSERT INTO VET_SPECIALTIES(vet_id, specialty_id) VALUES(?,?),(?,?)");
		sqlVetSpecialty.setInt(1, TEST_VETERINARY_ID);
		sqlVetSpecialty.setInt(2, TEST_SPECIALTY_ID);
		sqlVetSpecialty.setInt(3, TEST_VETERINARY_ID);
		sqlVetSpecialty.setInt(4, 1);
		sqlVetSpecialty.executeUpdate();
	}

	@Test
	@DisplayName("Простая проверка на получение всех ветеринаров")
	public void ShouldReturnCreatedVet() throws SQLException {
		Collection<String> COUNT_VETS = new ArrayList<>();
		PreparedStatement sqlCntVets = connection.prepareStatement("SELECT COUNT(*) FROM VETS");
		ResultSet resultSet = sqlCntVets.executeQuery();
		while (resultSet.next()) {
			COUNT_VETS.add(resultSet.getString(1));
		}

		given()
			.contentType(ContentType.JSON)
			.when()
			.get(METHOD_VETS)
			.then()
			.statusCode(200)
			.body("vetList.size()", is(COUNT_VETS))
			.body("", hasItem(hasEntry("firstName", "TEST_VETERINARY_NAME")))
			.body("", hasItem(hasEntry("lastName", "TEST_VETERINARY_SURNAME")));
	}

	@Nested
	@DisplayName("Связка данных ветеринаров с их специальностями")
	class CheckGetVetMethod {
		@Test
		@DisplayName("Проверка на выгрузку в ответе специальности добавленного врача")
		public void ShouldReturnVetsAndSpecialtiesWhenItExists(){
			given()
				.contentType(ContentType.JSON)
				.when()
				.get(METHOD_VETS)
				.then()
				.statusCode(200)
				.body("vetList.specialties", hasItem(hasEntry("id", TEST_SPECIALTY_ID)))
				.body("vetList.specialties", hasItem(hasEntry("name", TEST_SPECIALTY_NAME)));
		}
	}

	@AfterEach
	public void deleteDataFromDB() throws SQLException {
		PreparedStatement sqlDeleteVetSpecialty = connection.prepareStatement(
			"DELETE FROM VET_SPECIALTIES WHERE VET_ID IN(?)");
		sqlDeleteVetSpecialty.setInt(1, TEST_VETERINARY_ID);
		sqlDeleteVetSpecialty.executeUpdate();

		PreparedStatement sqlDeleteVet = connection.prepareStatement(
			"DELETE FROM VETS WHERE ID IN(?)");
		sqlDeleteVet.setInt(1, TEST_VETERINARY_ID);
		sqlDeleteVet.executeUpdate();

		PreparedStatement sqlDeleteSpecialty = connection.prepareStatement(
			"DELETE FROM SPECIALTIES WHERE ID IN(?)");
		sqlDeleteSpecialty.setInt(1, TEST_SPECIALTY_ID);
		sqlDeleteSpecialty.executeUpdate();
	}

	@AfterAll
	static void closeDBConnection() throws SQLException {
		connection.close();
	}

}
