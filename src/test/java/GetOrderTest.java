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
import methods.OrderRequests;
import methods.UserRequests;
import io.qameta.allure.Step;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class GetOrderTest {
    private final String email;
    private final String password;
    private final String name;
    private UserRequests userRequests;
    private OrderRequests orderRequests;
    private String accessToken;

    public GetOrderTest(String email, String password, String name) {
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
    public void setUp()  {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
        User user = new User(email, password, name);
        userRequests = new UserRequests();
        Response responseCreate = userRequests.createUser(user);
        accessToken = responseCreate.then().log().all().extract().path("accessToken");
    }

    @Test
    @DisplayName("Проверка получения заказов авторизованного пользователя")
    public void getOrderTest(){
        Response responseGetOrder = orderRequests.getOrderUser(accessToken);
        responseGetOrder.then().log().all().statusCode(200).body("success", equalTo(true)).body("orders.total", notNullValue());
    }

    @Test
    @DisplayName("Проверка получения заказов неавторизованного пользователя")
    public void getOrderWithoutAuthorization(){
        Response responseGetOrderWithoutAuthorization = orderRequests.getOrderUserWithoutAuthorization();
        responseGetOrderWithoutAuthorization.then().log().all().statusCode(401).body("success", equalTo(false)).body("message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUser(){
        Response responseDelete = userRequests.deleteUser(accessToken);
        responseDelete.then().log().all().statusCode(202).body("success", equalTo(true));
    }
}