import methods.RequestSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import io.qameta.allure.Step;

import org.junit.runners.Parameterized;
import methods.OrderRequests;

import java.util.ArrayList;
import java.util.List;

import static constants.ApiConstants.BURGERS_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private final int fromIndex;
    private final int toIndex;
    private final int statusCode;
    private OrderRequests orderRequests;


    public CreateOrderTest(int fromIndex, int toIndex, int statusCode) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.statusCode = statusCode;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        return new Object[][]{
                // Добавить один ингредиент
                {0, 1, 200},
                // Добавить несколько ингредиентов
                {0, 8, 200},
                // Добавить несколько ингредиентов
                {10, 15, 200},
                // Не добавлять ингредиенты
                {0, 0, 400},
        };
    }

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
    }

    @Test
    @DisplayName("Проверка создания заказа")
    public void createOrder() {
        Response responseGetIngredient = orderRequests.getIngredient();
        List<String> ingredients = new ArrayList<>(responseGetIngredient.then().log().all().statusCode(200).extract().path("data._id"));
        Order order = new Order(ingredients.subList(fromIndex, toIndex));
        Response responseCreate = orderRequests.createOrder(order);
        if (statusCode == 200) {
            responseCreate.then().log().all().assertThat().body("order.number", notNullValue()).body("success", equalTo(true));
        } else if (statusCode == 400) {
            responseCreate.then().log().all().assertThat().body("success", equalTo(false)).body("message", equalTo("Ingredient ids must be provided"));
        }
    }
}