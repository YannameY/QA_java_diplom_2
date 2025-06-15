import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import models.User;
import io.qameta.allure.Step;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import methods.UserRequests;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class UpdateUserDataTest {
    private Login login;
    private final String oldEmail;
    private final String newEmail;
    private final String oldPassword;
    private final String newPassword;
    private final String oldName;
    private final String newName;
    private UserRequests userRequests;
    private String accessToken;

    public UpdateUserDataTest(String oldEmail, String newEmail, String oldPassword, String newPassword, String oldName, String newName) {
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.oldName = oldName;
        this.newName = newName;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                // Обновляются все данные
                {"риван@яндекс.com", faker.internet().emailAddress(), "ponpon", faker.internet().password(), "Pavel", faker.name().firstName()},
                // Обновляется только email
                {"риван@яндекс.com", faker.internet().emailAddress(), "ponpon", "ponpon", "Pavel", "Pavel"},
                // Обновляется только пароль
                {"риван@яндекс.com", "риван@яндекс.com", "ponpon", faker.internet().password(), "Pavel", "Pavel"},
                // Обновляется только имя
                {"риван@яндекс.com", "ponpon@yandex.com", "ponpon", "ponpon", "Pavel", faker.name().firstName()}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BURGERS_URL;
        User user = new User(oldEmail, oldPassword, oldName);
        userRequests = new UserRequests();
        login = new Login(newEmail, newPassword);
        Response responseCreate = userRequests.createUser(user);
        accessToken = responseCreate.then().log().all().extract().path("accessToken");
    }

    @Test
    @DisplayName("Проверка изменения данных авторизованного пользователя")
    public void modifyUserDataTest() {
        Response responseGetUser = userRequests.getUser(accessToken);
        responseGetUser.then().log().all().statusCode(200).body("success", equalTo(true)).body("user.email", equalTo(oldEmail)).body("user.name", equalTo(oldName));
        Response responseUpdate = userRequests.updateUser(new User(newEmail, newPassword, newName), accessToken);
        responseUpdate.then().log().all().statusCode(200).body("success", equalTo(true)).body("user.email", equalTo(newEmail)).body("user.name", equalTo(newName));
        Response responseGetUserAfterUpdate = userRequests.getUser(accessToken);
        responseGetUserAfterUpdate.then().log().all().statusCode(200).body("success", equalTo(true)).body("user.email", equalTo(newEmail)).body("user.name", equalTo(newName));
        Response responseLogin = userRequests.loginUser(login);
        responseLogin.then().log().all().statusCode(200);
    }

    @Test
    @DisplayName("Проверка изменения данных неавторизованного пользователя")
    public void modifyUserDataWithoutAuthorizationTest() {
        Response responseUpdateWithoutAuthorization = userRequests.updateUserWithoutAuthorization(new User(newEmail, newPassword, newName));
        responseUpdateWithoutAuthorization.then().log().all().statusCode(401).body("success", equalTo(false)).body("message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUser() {
        Response responseDelete = userRequests.deleteUser(accessToken);
        responseDelete.then().log().all().statusCode(202).body("success", equalTo(true));
    }
}