package methods;

import constants.ApiConstants;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

// Класс RequestSpec используется для предоставления базовой спецификации запроса (RequestSpecification),
// которая будет использоваться при выполнении запросов
public class RequestSpec {

    public static RequestSpecification requestSpecification() {
        return given().log().all()
                .contentType(ContentType.JSON)
                .baseUri(ApiConstants.BURGERS_URL);
    }

}