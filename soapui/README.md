# Running the SOAP tests in SoapUI

The same calculator service tested in `CalculatorSoapTest.java` can be tested in
[SoapUI Open Source](https://www.soapui.org/downloads/soapui/) (free).

## Steps

1. **File → New SOAP Project**
   - Project name: `Calculator`
   - Initial WSDL: `http://www.dneonline.com/calculator.asmx?WSDL`
   - Tick **Create TestSuite**, then OK. SoapUI generates a request for each operation (Add, Subtract, Multiply, Divide).
2. Open the generated **Add** request and set:
   ```xml
   <tem:intA>10</tem:intA>
   <tem:intB>3</tem:intB>
   ```
3. Add assertions to the test step (**Assertions → +**):
   - **SOAP Response** (response is valid SOAP)
   - **Not SOAP Fault**
   - **XPath Match**: `declare namespace t='http://tempuri.org/'; //t:AddResult` expected `13`
4. Add a **Divide** step with `intB = 0` and assert **SOAP Fault** (negative test, matches TC-SOAP-06).
5. Optional data-driven version: add a **DataSource** step (Pro) or a Groovy script that loops over
   the same rows as the Java `operations` data provider.
6. **File → Save Project** and save it into this folder as `Calculator-soapui-project.xml`, then commit it.

## Run from the command line

SoapUI ships a runner, so a saved project can run in CI too:

```bash
<SoapUI install dir>/bin/testrunner.sh -r -j -f target/soapui soapui/Calculator-soapui-project.xml
```

`-j` writes JUnit-style XML reports, the same format the Java suites produce.

> Observation logged as BUG-003 in TEST_PLAN.md: the divide-by-zero fault returns the full .NET
> stack trace and the server's file path (`G:\PleskVhosts\...`) in `faultstring`, which is an
> information-disclosure issue.
