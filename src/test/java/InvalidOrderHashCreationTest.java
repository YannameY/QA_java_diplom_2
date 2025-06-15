import methods.RequestSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import io.qameta.allure.Step;

import org.junit.runners.Parameterized;
import methods.OrderRequests;

import java.util.List;

@RunWith(Parameterized.class)
public class InvalidOrderHashCreationTest {
    private final List<String> ingredients;
    private OrderRequests orderRequests;
    private Order order;

    public InvalidOrderHashCreationTest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @Parameterized.Parameters
    public static Object[][] getData(){
        Faker faker = new Faker();
        return new Object[][]{
                {List.of(faker.number().digits(7))}, // Не валидный хэш (7 символов)
                {List.of(faker.number().digits(23))}, // Не валидный хэш (23 символа)
                {List.of(faker.number().digits(25))} // Не валидный хэш (25 символов)
        };
    }

    @Before
    public void setUp()  {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
        order = new Order(ingredients);
    }

    @Test
    @DisplayName("Проверка отправки невалидного хэша при создании заказа")
    public void createOrderInvalidHash(){
        // Отправка запроса на создание заказа с не валидным хэшем и ожидание статуса 500 (внутренняя ошибка сервера)
        Response responseCreate = orderRequests.createOrder(order);
        responseCreate.then().log().all().statusCode(500);
    }
}