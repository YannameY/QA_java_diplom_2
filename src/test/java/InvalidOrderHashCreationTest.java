import io.qameta.allure.Step;
import jdk.jfr.Description;
import methods.RequestSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import models.User;

import org.junit.runners.Parameterized;
import methods.OrderRequests;
import methods.UserRequests;

import java.util.List;

@RunWith(Parameterized.class)
@DisplayName("Тесты создания заказа с невалидными хэшами ингредиентов")
@Description("Проверка обработки API запросов с различными невалидными хэшами ингредиентов")
public class InvalidOrderHashCreationTest {
    private final List<String> ingredients;
    private OrderRequests orderRequests;
    private UserRequests userRequests;
    private Order order;
    private String accessToken;
    private User user;

    public InvalidOrderHashCreationTest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @Parameterized.Parameters
    public static Object[][] getData() {
        Faker faker = new Faker();
        return new Object[][]{
                {List.of(faker.number().digits(7))},   // Невалидный хэш (7 символов)
                {List.of(faker.number().digits(23))},  // Невалидный хэш (23 символа)
                {List.of(faker.number().digits(25))}   // Невалидный хэш (25 символов)
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
        order = new Order(ingredients);
    }

    @Test
    @DisplayName("Проверка создания заказа с невалидным хэшем ингредиентов")
    @Description("Проверка что система корректно обрабатывает невалидные хэши ингредиентов")
    public void createOrderInvalidHash() {
        Response responseCreate = orderRequests.createOrder(order, accessToken);
        responseCreate.then().log().all().statusCode(500); // Ожидаем 500, так как хэш невалидный
    }
}