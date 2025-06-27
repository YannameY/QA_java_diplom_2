import io.qameta.allure.Description;
import methods.RequestSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import methods.UserRequests;
import io.qameta.allure.Step;

import static constants.ApiConstants.BURGERS_URL;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class CreateUserTest {
    private final String email;
    private final String password;
    private final String name;
    private User user;
    private UserRequests userRequests;
    private Response createUserResponse;

    public CreateUserTest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
                {faker.internet().emailAddress(), faker.internet().password(), faker.name().firstName()},
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        user = new User(email, password, name);
        userRequests = new UserRequests();
        createUserResponse = null;
    }

    @Test
    @Step("Создание пользователя")
    @DisplayName("Проверка создания пользователя")
    public void createUser() {
        createUserResponse = userRequests.createUser(user);
        createUserResponse.then()
                .statusCode(SC_OK)
                .assertThat()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Попытка создания пользователя с существующим email")
    @Description("API должно возвращать ошибку 403 при попытке создать пользователя с уже занятым email")
    public void testCreateUserWithDuplicateEmail() {
        // Сначала создаем пользователя
        createUserResponse = userRequests.createUser(user);

        // Пытаемся создать такого же пользователя еще раз
        Response duplicateResponse = userRequests.createUser(user);

        // Проверяем ответ сервера
        duplicateResponse.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @After
    public void deleteUser() {
        try {
            // Получаем токен из ответа, если пользователь был создан
            if (createUserResponse != null) {
                String accessToken = createUserResponse.then().extract().path("accessToken");
                if (accessToken != null) {
                    Response responseDelete = userRequests.deleteUser(accessToken);
                    responseDelete.then()
                            .statusCode(202)
                            .body("success", equalTo(true));
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
        }
    }
}