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

@DisplayName("Тестирование PetControllerTest")
public class PetControllerTest {

	private static Connection connection;
	final static int TEST_PET_ID = randomNumeric();
	final static String TEST_PET_NAME = randomAlphabetString(5);
	final static int OWNER_ID = 1;
	LocalDate CURRENT_DATE = java.time.LocalDate.now();
	LocalDate TEST_BIRTH_DATE = CURRENT_DATE.minusMonths(1);

	final String BODY_FOR_POST_METHODS = "{" +
		"\"birthDate\": \"" + TEST_BIRTH_DATE + "\"," +
		"\"id\": " + TEST_PET_ID + "," +
		"\"name\": \"" + TEST_PET_NAME + "\"," +
		"	\"visits\": [{" +
		"	\"date\": \"" + CURRENT_DATE + "\"," +
		"	\"description\": \"" + "Test visit" + "\"," +
		"	\"id\": " + 0 + "}]}";


	@BeforeAll
	public static void connectDb() throws SQLException {
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost/petclinic",
			"petclinic",
			"petclinic");
	}

	@BeforeEach
	public void createDataInDB() throws SQLException {
		PreparedStatement sql = connection
				.prepareStatement("INSERT INTO PETS(id, name, birth_date, owner_id) VALUES(?,?,?,?)");
		sql.setInt(1, TEST_PET_ID);
		sql.setString(2, TEST_PET_NAME);
		sql.setDate(3, java.sql.Date.valueOf(TEST_BIRTH_DATE));
		sql.setInt(4, OWNER_ID);
		sql.executeUpdate();
	}

	@Nested
	@DisplayName("Метод Get")
	class CheckGetMethod {
		@Test
		@DisplayName("Проверка на успешное получение pets")
		public void ShouldReturnDataWhenOwnerExists() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.when()
				.get(METHOD_WITHOUT_PETID)
				.then()
				.statusCode(200)
				.body("isEmpty()", Matchers.is(false))
				.body(containsString(TEST_BIRTH_DATE.toString()))
				.body(containsString(TEST_PET_NAME))
				.body("", hasItem(hasEntry("id", TEST_PET_ID)));
		}

		@Test
		@DisplayName("Проверка на получение ошибки, если path пустой")
		public void ShouldReturnErrorWhenPathDoesntExist() {
			given()
				.contentType(ContentType.JSON)
				.when()
				.get(METHOD_WITHOUT_PETID)
				.then()
				.statusCode(400);
		}
	}

	@Nested
	@DisplayName("Метод Post .../pets")
	class CheckPostMethod_WithoutPetId {
		@Test
		@DisplayName("Проверка на успешное добавление визита")
		public void ShouldAddVisitWhenOwnerExistsAndCorrectBody() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.body(BODY_FOR_POST_METHODS)
				.when()
				.post(METHOD_WITHOUT_PETID)
				.then()
				.statusCode(201)
				.body(containsString(TEST_BIRTH_DATE.toString()))
				.body(containsString(TEST_PET_NAME))
				.body("visits", hasItem(hasEntry("description", "Test visit")))
				.body("visits", hasItem(hasEntry("date", CURRENT_DATE)));
		}

		@Test
		@DisplayName("Проверка на получение ошибки, если оунера нет в БД")
		public void ShouldReturnErrorWhenOwnerDoesntExist() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", 0) //Как найти несуществующий ID?
				.body(BODY_FOR_POST_METHODS)
				.when()
				.post(METHOD_WITHOUT_PETID)
				.then()
				.statusCode(500);
		}

		@Test
		@DisplayName("Проверка на получение ошибки, если пустой body")
		public void ShouldReturnErrorWhenBodyIsEmpty() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.body("")
				.when()
				.post(METHOD_WITHOUT_PETID)
				.then()
				.statusCode(400);
		}
	}

	@Nested
	@DisplayName("Метод Post .../pets/{petId}")
	class CheckPostMethod_WithPetId {
		@Test
		@DisplayName("Проверка на успешное добавление визита: метод /pets/{petId}")
		public void ShouldAddVisitWhenOwnerAndPetExistAndCorrectBody() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", TEST_PET_ID)
				.body(BODY_FOR_POST_METHODS)
				.when()
				.post(METHOD_WITH_PETID)
				.then()
				.statusCode(201)
				.body(containsString(TEST_BIRTH_DATE.toString()))
				.body(containsString(TEST_PET_NAME))
				.body("visits", hasItem(hasEntry("description", "Test visit")))
				.body("visits", hasItem(hasEntry("date", CURRENT_DATE)));
		}

		@Test
		@DisplayName("Проверка на получение ошибки, если оунера нет в БД: метод /pets/{petId}")
		public void ShouldReturnErrorWhenOwnerDoesntExist_PetId() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", 0) //Как найти несуществующий ID?
				.pathParam("petId", TEST_PET_ID)
				.body(BODY_FOR_POST_METHODS)
				.when()
				.post(METHOD_WITH_PETID)
				.then()
				.statusCode(500);
		}

		@Test
		@DisplayName("Проверка на получение ошибки, если пустой body: метод /pets/{petId}")
		public void ShouldReturnErrorWhenBodyIsEmpty_PetId() {
			given()
				.contentType(ContentType.JSON)
				.pathParam("ownerId", OWNER_ID)
				.pathParam("petId", TEST_PET_ID)
				.body("")
				.when()
				.post(METHOD_WITH_PETID)
				.then()
				.statusCode(400);
		}
	}

	@AfterEach
	public void deleteDataFromDB() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
			"DELETE FROM PETS WHERE ID=?"
		);
		sql.setInt(1, TEST_PET_ID);
		sql.executeUpdate();
	}

	@AfterAll
	static void closeDBConnection() throws SQLException {
		connection.close();
	}

}
