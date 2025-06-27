import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jdk.jfr.Description;
import methods.RequestSpec;
import models.Order;
import methods.OrderRequests;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

@DisplayName("Тест создания заказов с невалидными данными")
public class CreateOrderWithInvalidDataTest {
    private OrderRequests orderRequests;

    @Before
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
    }

    @Test
    @DisplayName("Попытка создания заказа с невалидным ингредиентом")
    @Description("Тест проверяет обработку API запроса с несуществующим хэшем ингредиента\n" +
            "Ожидаемый результат:\n" +
            "- Статус код 500 (Internal Server Error)")
    public void testCreateOrderWithInvalidIngredient() {
        // Создаем заказ с невалидным хэшем ингредиента
        Order invalidOrder = new Order(List.of("invalid_ingredient_hash_123"));

        // Отправляем запрос
        Response response = orderRequests.createOrderWithoutAuth(invalidOrder);

        // Проверяем ответ
        response.then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

}