import methods.RequestSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import methods.UserRequests;

import static constants.ApiConstants.BURGERS_URL;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class CreateInvalidUserTest {

    private final String email;
    private final String password;
    private final String name;
    private final int statusCode;
    private final boolean success;
    private final String message;

    private User user;
    private UserRequests userRequests;
    private String accessToken;

    public CreateInvalidUserTest(String email, String password, String name,
                                 int statusCode, boolean success, String message) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
    }

    @Parameterized.Parameters(name = "Test case: email={0}, password={1}, name={2}, expectedStatus={3}")
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                // Создание пользователя без email
                {null, faker.internet().password(), faker.name().firstName(),
                        SC_FORBIDDEN, false, "Email, password and name are required fields"},
                // Создание пользователя без пароля
                {faker.internet().emailAddress(), null, faker.name().firstName(),
                        SC_FORBIDDEN, false, "Email, password and name are required fields"},
                // Создание пользователя без имени
                {faker.internet().emailAddress(), faker.internet().password(), null,
                        SC_FORBIDDEN, false, "Email, password and name are required fields"},
                // Создание дубликата пользователя
                {faker.internet().emailAddress(), faker.internet().password(),
                        faker.name().firstName(), SC_OK, true, null}
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        user = new User(email, password, name);
        userRequests = new UserRequests();
    }

    @Test
    @DisplayName("Проверка создания пользователя с невалидными данными")
    @Description("Тест проверяет различные негативные сценарии создания пользователя: " +
            "без email, без пароля, без имени, а также попытку дублирования пользователя")
    public void createInvalidUserTest() {
        Response responseCreate = userRequests.createUser(user);
        responseCreate.then()
                .log().all()
                .statusCode(statusCode)
                .body("success", equalTo(success))
                .body("message", equalTo(message));

        if (responseCreate.statusCode() == SC_OK) {
            accessToken = responseCreate.then().extract().path("accessToken");
            Response responseCreateDouble = userRequests.createUser(user);
            responseCreateDouble.then()
                    .log().all()
                    .statusCode(SC_FORBIDDEN)
                    .body("success", equalTo(false))
                    .body("message", equalTo("User already exists"));
        }
    }

    @After
    public void deleteUserAfterTest() {
        if (statusCode == SC_OK) {
            userRequests.deleteUser(accessToken);
        }
    }
}