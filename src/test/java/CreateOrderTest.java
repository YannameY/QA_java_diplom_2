import io.qameta.allure.Step;
import methods.RequestSpec;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import models.User;
import methods.OrderRequests;
import methods.UserRequests;
import com.github.javafaker.Faker;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private final int fromIndex;
    private final int toIndex;
    private final int statusCode;
    private OrderRequests orderRequests;
    private UserRequests userRequests;
    private String accessToken;
    private User user;

    public CreateOrderTest(int fromIndex, int toIndex, int statusCode) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.statusCode = statusCode;
    }

    @Parameterized.Parameters(name = "Test case: ingredients from {0} to {1}, expected status {2}")
    public static Object[][] getData() {
        return new Object[][]{
                {0, 1, SC_OK},
                {0, 8, SC_OK},
                {10, 15, SC_OK},
                {0, 0, SC_BAD_REQUEST},
        };
    }

    @Before
    @Step("Подготовка тестовых данных")
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();

        Faker faker = new Faker();
        user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );

        userRequests = new UserRequests();
        Response response = userRequests.createUser(user);
        accessToken = response.then().extract().path("accessToken");

        orderRequests = new OrderRequests();
    }

    @Test
    @DisplayName("Проверка создания заказа авторизованным пользователем")
    @Description("Тест проверяет создание заказов с различными комбинациями ингредиентов")
    public void createOrderWithAuthorizationTest() {
        Response responseGetIngredient = orderRequests.getIngredient();
        List<String> ingredients = responseGetIngredient.then()
                .statusCode(SC_OK)
                .extract().path("data._id");

        if (toIndex > ingredients.size()) {
            throw new AssertionError("Недостаточно ингредиентов для теста");
        }

        Order order = new Order(ingredients.subList(fromIndex, toIndex));
        Response responseCreate = orderRequests.createOrder(order, accessToken);

        if (statusCode == SC_OK) {
            responseCreate.then()
                    .statusCode(SC_OK)
                    .body("order.number", notNullValue())
                    .body("success", equalTo(true));
        } else {
            responseCreate.then()
                    .statusCode(SC_BAD_REQUEST)
                    .body("success", equalTo(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        }
    }

    @After
    @Step("Удаление тестового пользователя")
    public void tearDown() {
        if (accessToken != null) {
            try {
                Response deleteResponse = userRequests.deleteUser(accessToken);
                deleteResponse.then()
                        .statusCode(SC_ACCEPTED)
                        .body("success", equalTo(true));
            } catch (Exception e) {
                System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
            }
        }
    }
}