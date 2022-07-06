package ApiTests;

import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static Helpers.APIMethods.*;
import static Helpers.RandomHelper.randomAlphabetString;
import static Helpers.RandomHelper.randomNumeric;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Тестирование VisitControllerTest")
public class VisitControllerTest {

	private static Connection connection;
	final static int TEST_PET_ID = randomNumeric();
	final static String TEST_PET_NAME = randomAlphabetString(5);
	final static int OWNER_ID = 1;
	final static String TEST_VISIT_DESCRIPTION = "Test doctor visit";
	final static int TEST_VISIT_ID = randomNumeric();
	LocalDate CURRENT_DATE = java.time.LocalDate.now();
	LocalDate TEST_BIRTH_DATE = CURRENT_DATE.minusMonths(1);

	@BeforeAll
	public static void connectDb() throws SQLException {
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost/petclinic",
			"petclinic",
			"petclinic");
	}

	@BeforeEach
	public void createDataInDB() throws SQLException {
		PreparedStatement sqlAddPet = connection
			.prepareStatement("INSERT INTO PETS(id, name, birth_date, owner_id) VALUES(?,?,?,?)");
		sqlAddPet.setInt(1, TEST_PET_ID);
		sqlAddPet.setString(2, TEST_PET_NAME);
		sqlAddPet.setDate(3, java.sql.Date.valueOf(TEST_BIRTH_DATE));
		sqlAddPet.setInt(4, OWNER_ID);
		sqlAddPet.executeUpdate();

		PreparedStatement sqlAddVisit = connection
			.prepareStatement("INSERT INTO VISITS(ID, PET_ID, VISIT_DATE, DESCRIPTION) VALUES(?,?,?,?)");
		sqlAddVisit.setInt(1, TEST_VISIT_ID);
		sqlAddVisit.setInt(2, TEST_PET_ID);
		sqlAddVisit.setDate(3, java.sql.Date.valueOf(CURRENT_DATE));
		sqlAddVisit.setString(4, TEST_VISIT_DESCRIPTION);
		sqlAddVisit.executeUpdate();
	}

	@Nested
	@DisplayName("Метод Get")
	class CheckGetMethod {
		@Test
		@DisplayName("Проверка на успешное получение данных о визите")
		public void ShouldReturnVisitWhenCorrectPath() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", TEST_PET_ID)
				.when()
				.get(METHOD_VISIT)
				.then()
				.statusCode(200)
				.body("isEmpty()", Matchers.is(false))
				.body("", hasItem(hasEntry("description", "Test doctor visit")))
				.body("", hasItem(hasEntry("date", java.sql.Date.valueOf(CURRENT_DATE).toString())));
		}

		@Test
		@DisplayName("Проверка на получение пустого visits, если данных в БД нет")
		public void ShouldReturnEmptyResponseWhenDataDoesntExist() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", 1)
				.when()
				.get(METHOD_VISIT)
				.then()
				.statusCode(200)
				.body("isEmpty()", Matchers.is(true));
		}
	}

	@Nested
	@DisplayName("Метод Post")
	class CheckPostMethod {
		@Test
		@DisplayName("Проверка на успешное добавление visit")
		public void ShouldAddVisitWhenBodyIsCorrect() {
			final int TEST_NEW_VISIT_ID = randomNumeric();
			final String TEST_NEW_VISIT_DESCRIPTION = "Check,that visit is being added";
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", TEST_PET_ID)
				.body("{" +
					"\"date\": \"" + CURRENT_DATE + "\"," +
					"\"description\": \"" + TEST_NEW_VISIT_DESCRIPTION + "\"," +
					"\"id\": " + TEST_NEW_VISIT_ID + "}")
				.when()
				.post(METHOD_VISIT)
				.then()
				.statusCode(201)
				//Возможно, тут проверка может быть неверной: 500код ответа вместо 201 не дал проверить
				.body("pets.visits", hasItem(hasEntry("description", TEST_NEW_VISIT_DESCRIPTION)))
				.body("pets.visits", hasItem(hasEntry("id", TEST_NEW_VISIT_ID)));
		}

		@Test
		@DisplayName("Проверка на успешное изменение visit")
		public void ShouldChangeVisitWhenBodyIsCorrect() {
			final String TEST_NEW_VISIT_DESCRIPTION = "Check,that visit is being changed";
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", TEST_PET_ID)
				.body("{" +
					"\"date\": \"" + CURRENT_DATE + "\"," +
					"\"description\": \"" + TEST_NEW_VISIT_DESCRIPTION + "\"," +
					"\"id\": " + TEST_VISIT_ID + "}")
				.when()
				.post(METHOD_VISIT)
				.then()
				.statusCode(201)
				.body("pets.visits", hasItem(hasEntry("description", TEST_NEW_VISIT_DESCRIPTION)));
		}

		@Test
		@DisplayName("Проверка на получение ошибки, если Pet не привязан к OwnerId")
		public void ShouldReturnErrorWhenOwnerDoesntHaveSpecifiedPet() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", 1)
				.when()
				.post(METHOD_VISIT)
				.then()
				.statusCode(400);
		}
	}

	@AfterEach
	public void deleteDataFromDB() throws SQLException {
		PreparedStatement sqlDeleteVisit = connection.prepareStatement(
			"DELETE FROM VISITS WHERE PET_ID=?");
		sqlDeleteVisit.setInt(1, TEST_PET_ID);
		sqlDeleteVisit.executeUpdate();

		PreparedStatement sqlDeletePet = connection.prepareStatement(
			"DELETE FROM PETS WHERE ID=?");
		sqlDeletePet.setInt(1, TEST_PET_ID);
		sqlDeletePet.executeUpdate();
	}

	@AfterAll
	static void closeDBConnection() throws SQLException {
		connection.close();
	}

}

