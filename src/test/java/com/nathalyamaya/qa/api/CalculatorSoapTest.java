package com.nathalyamaya.qa.api;

import com.nathalyamaya.qa.common.Config;
import io.restassured.response.Response;
import io.restassured.path.xml.XmlPath;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * SOAP web-service tests against a public ASMX calculator. The same requests are in
 * soapui/Calculator-soapui-project.md for running them in SoapUI.
 */
public class CalculatorSoapTest {

    private static final String ENDPOINT = "/calculator.asmx";
    private static final String NAMESPACE = "http://tempuri.org/";

    private Response call(String operation, int a, int b) {
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <%1$s xmlns="%2$s">
                      <intA>%3$d</intA>
                      <intB>%4$d</intB>
                    </%1$s>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(operation, NAMESPACE, a, b);

        return given()
                .baseUri(Config.soapBaseUrl())
                .contentType("text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + NAMESPACE + operation + "\"")
                .body(envelope)
                .when().post(ENDPOINT);
    }

    /** Finds an element anywhere in the SOAP body by local name, ignoring namespaces. */
    private static String element(Response response, String name) {
        return new XmlPath(response.asString()).getString("**.find { it.name() == '" + name + "' }");
    }

    @DataProvider(name = "operations")
    public Object[][] operations() {
        return new Object[][]{
                {"Add", 10, 3, 13},
                {"Add", -5, 5, 0},
                {"Subtract", 10, 3, 7},
                {"Multiply", 6, 7, 42},
                {"Divide", 10, 2, 5},
        };
    }

    @Test(groups = {"api", "soap", "smoke"}, dataProvider = "operations",
            description = "TC-SOAP-01..05: arithmetic operations return the correct result")
    public void operationReturnsCorrectResult(String operation, int a, int b, int expected) {
        Response response = call(operation, a, b);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(element(response, operation + "Result")).isEqualTo(String.valueOf(expected));
    }

    @Test(groups = {"api", "soap", "regression"},
            description = "TC-SOAP-06: divide by zero returns a SOAP fault, not a result")
    public void divideByZeroReturnsSoapFault() {
        Response response = call("Divide", 10, 0);

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(element(response, "faultcode")).isEqualTo("soap:Server");
        assertThat(element(response, "DivideResult")).isNullOrEmpty();
    }
}
