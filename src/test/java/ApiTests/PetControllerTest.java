package ApiTests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static Helpers.RandomHelper.randomAlphabetString;
import static Helpers.RandomHelper.randomNumeric;
import static io.restassured.RestAssured.given;

public class PetControllerTest {

	private static Connection connection;

	final static int TEST_PET_ID = randomNumeric();

	final static String TEST_PET_NAME = randomAlphabetString(2);

	final static int OWNER_ID = 1;

	LocalDate localDate = LocalDate.now(ZoneId.of("Europe/Paris")).minusDays(30);

	Date date = Date.from(Instant.from(localDate.atStartOfDay(ZoneId.of("GMT"))));

	@BeforeAll
	public static void connectDb() throws SQLException {
		connection = DriverManager.getConnection("jdbc:postgresql://localhost/app-db", "app-db-admin", "P@ssw0rd");
	}

	@BeforeEach
	public void createDataInDB() throws SQLException {
		PreparedStatement sql = connection
				.prepareStatement("INSERT INTO PETS(id, name, birth_date, owner_id) VALUES(?,?,?,?)");
		sql.setInt(1, TEST_PET_ID);
		sql.setString(2, TEST_PET_NAME);
		sql.setInt(3, OWNER_ID);
		sql.setDate(4, (java.sql.Date) date);
		sql.executeUpdate();
	}

	@Test
	@DisplayName("Проверка на успешное получение pets")
	public void ShouldReturnDataWhenPetExists() {
		given().contentType(ContentType.JSON).body("{\n" + "  \"ownerId\": \"" + OWNER_ID + "\"\n" + "}").when()
				.get("/owners/{ownerId}pets").then().statusCode(200);
	}

	@AfterAll
	static void closeDBConnection() throws SQLException {
		connection.close();
	}

}
