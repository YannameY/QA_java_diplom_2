import io.qameta.allure.Step;
import jdk.jfr.Description;
import methods.RequestSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Order;
import methods.OrderRequests;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
@DisplayName("Тесты создания заказов без авторизации")
@Description("Параметризованные тесты проверки создания заказов без авторизации с различными наборами ингредиентов")
public class CreateOrderWithoutAuthorizationTest {
    private final int fromIndex;
    private final int toIndex;
    private final int expectedStatusCode;
    private OrderRequests orderRequests;

    public CreateOrderWithoutAuthorizationTest(int fromIndex, int toIndex, int expectedStatusCode) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameterized.Parameters(name = "Ингредиенты {0}-{1}, ожидаемый статус {2}")
    public static Object[][] getTestData() {
        return new Object[][]{
                {0, 1, SC_OK},  // С ингредиентами
                {0, 2, SC_OK},  // С несколькими ингредиентами
                {0, 0, SC_BAD_REQUEST}    // Без ингредиентов
        };
    }

    @Before
    @Step("Подготовка тестового окружения")
    public void setUp() {
        RestAssured.requestSpecification = RequestSpec.requestSpecification();
        orderRequests = new OrderRequests();
    }

    @Test
    @DisplayName("Попытка создания заказа без авторизации")
    @Description("Проверка создания заказа без авторизации с различными комбинациями ингредиентов")
    public void testCreateOrderWithoutAuthorization() {
        Response ingredientsResponse = orderRequests.getIngredient();
        List<String> ingredients = ingredientsResponse.then()
                .statusCode(SC_OK)
                .extract().path("data._id");
        // Проверяем достаточно ли ингредиентов для теста
        if (toIndex > ingredients.size()) {
            throw new AssertionError("Недостаточно ингредиентов для теста. Доступно: " + ingredients.size());
        }
        // Формируем заказ
        Order testOrder = new Order(ingredients.subList(fromIndex, toIndex));
        // Отправляем запрос без авторизации
        Response response = orderRequests.createOrderWithoutAuth(testOrder);
        // Проверяем статус код
        response.then()
                .statusCode(expectedStatusCode);
        // Дополнительные проверки для разных случаев
        if (expectedStatusCode == SC_UNAUTHORIZED) {
            response.then()
                    .body("success", equalTo(false))
                    .body("message", equalTo("You should be authorised"));
        } else if (expectedStatusCode == SC_BAD_REQUEST) {
            response.then()
                    .body("success", equalTo(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        }
    }
}