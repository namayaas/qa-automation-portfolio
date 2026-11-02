# Test Plan: QA Automation Portfolio

| | |
|---|---|
| **Author** | Nathaly Amaya-Flores |
| **Version** | 1.0 |
| **Status** | Active. Automated suites run in CI on every push and nightly. |

## 1. Purpose and scope

This plan defines how three public practice systems are tested at the UI, API (REST and SOAP) and
mobile levels, using a mix of manual and automated testing. It covers what is tested, how test
conditions and test data were chosen, the entry and exit criteria, risks, and the defects found.

**In scope**

| System under test | Interface | Tooling |
|---|---|---|
| [Sauce Demo](https://www.saucedemo.com) web store | Web UI | Selenium WebDriver 4, Java, TestNG, Page Object Model |
| [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html) booking API | REST / JSON | REST Assured, JSON Schema validation |
| [DNE Online Calculator](http://www.dneonline.com/calculator.asmx) | SOAP / XML | REST Assured (XML), SoapUI |
| Android Settings app | Mobile UI | Appium 2, UiAutomator2 *(in progress: written, not yet run)* |

**Out of scope:** load and performance testing, security penetration testing, and browsers other than
Chrome (see section 11 for planned extensions).

> These are practice systems with no published requirements. The requirements in section 6 were
> derived from each system's documentation and observed behavior, the same way a tester works from
> acceptance criteria on a real story.

## 2. Test objectives

1. Verify the critical user journeys work end to end: log in, browse, add to cart, check out.
2. Verify the API's contract (status codes, response schema) and its authorization rules.
3. Verify the SOAP service returns correct results and a proper fault on invalid input.
4. Give fast, repeatable feedback: smoke tests in about 1 minute, full regression in under 5.
5. Find and document defects with clear reproduction steps.

## 3. Test approach

| Level / type | What it covers | Manual or automated |
|---|---|---|
| **Smoke** | One happy path per feature (login, cart, checkout, API health, CRUD create/read) | Automated (`smoke` group) |
| **Functional / regression** | Positive, negative and boundary cases for every requirement | Automated (`regression` group) |
| **API contract** | JSON Schema check on responses; status codes; auth rules | Automated |
| **Data-driven** | Same test logic run over external data sets (section 5) | Automated |
| **Exploratory** | Time-boxed charters for behavior that's hard to script (section 7) | Manual |
| **Usability / accessibility** | Keyboard navigation, focus order, error-message clarity | Manual |
| **Mobile** | App launch and navigation on an Android emulator/device | Automated tests written; not yet executed |

**Design techniques used:** equivalence partitioning, boundary value analysis, error guessing,
state-transition testing (booking lifecycle: created → updated → patched → deleted) and
end-to-end scenario testing.

**Automation design**

- **Page Object Model**: locators and page actions live in `ui/pages/`, so a UI change is fixed in one place.
- **Stable locators**: the site's `data-test` attributes, never brittle XPath or styling classes.
- **Explicit waits only**: no `Thread.sleep`. Waits re-poll on stale elements caused by re-renders.
- **Isolated tests**: a fresh browser per UI test; every API test cleans up the bookings it creates.
- **Ordered lifecycle**: the CRUD flow uses `dependsOnMethods`, so an early failure *skips* the later
  steps instead of reporting misleading failures.
- **Evidence on failure**: screenshots (UI) and the full request/response log (API) are saved automatically.

## 4. Test environment

| Item | Value |
|---|---|
| Language / runtime | Java 17+ (verified on 17 in CI and 21 locally) |
| Build | Maven via wrapper (`./mvnw`), no local Maven install needed |
| Browser | Chrome, headless by default (`-Dheadless=false` to watch) |
| Driver management | Selenium Manager, which downloads the matching chromedriver automatically |
| CI | GitHub Actions, `ubuntu-latest`, API and UI in parallel jobs, nightly schedule |
| Mobile | Android emulator or USB device plus a local Appium 2 server |

All endpoints and timeouts can be overridden with `-D` properties or environment variables (`Config.java`).

## 5. Test data design

Test data is kept **outside the code** in `src/test/resources/testdata/`, so new conditions can be
added without changing the tests.

### 5.1 Login: `login-negative.psv` (equivalence partitions)

| Data ID | Partition | Username | Password | Expected |
|---|---|---|---|---|
| TC-UI-02 | Valid user, account locked | `locked_out_user` | valid | "Sorry, this user has been locked out." |
| TC-UI-03 | Valid user, wrong password | `standard_user` | invalid | "Username and password do not match…" |
| TC-UI-04 | Unknown user | `unknown_user` | valid | "Username and password do not match…" |
| TC-UI-05 | Blank username | *(empty)* | valid | "Username is required" |
| TC-UI-06 | Blank password | `standard_user` | *(empty)* | "Password is required" |

### 5.2 Bookings: `bookings-valid.json` (boundary values and special characters)

| Data ID | Condition | Why it matters |
|---|---|---|
| TD-01 | `totalprice` = 0 | Lower boundary of a non-negative amount |
| TD-02 | `totalprice` = 999999, 9-month stay | Large values and long ranges |
| TD-03 | Accented characters (`José Peña-Núñez`) | UTF-8 handling for international names |
| TD-04 | Single-character names; check-in = check-out | Minimum length; same-day boundary |
| TD-05 | Space and apostrophe (`Mary Ann O'Brien`) | Characters that often break escaping |

### 5.3 SOAP operations (in-code data provider)

| Operation | Inputs | Expected | Technique |
|---|---|---|---|
| Add | 10, 3 | 13 | Typical value |
| Add | -5, 5 | 0 | Negative input, zero result |
| Subtract | 10, 3 | 7 | Typical value |
| Multiply | 6, 7 | 42 | Typical value |
| Divide | 10, 2 | 5 | Typical value |
| Divide | 10, 0 | SOAP fault | Error guessing / invalid input |

## 6. Requirements traceability matrix

| Req ID | Requirement | Test cases | Type | Automated in |
|---|---|---|---|---|
| R-UI-1 | A valid user can log in and see the product list | TC-UI-01 | Positive | `LoginTest` |
| R-UI-2 | Invalid, blank or locked-out logins are refused with a clear message | TC-UI-02 to 06 | Negative, data-driven | `LoginTest` |
| R-UI-3 | Products can be sorted by price and name | TC-UI-07, 08, 09 | Functional | `InventoryTest` |
| R-UI-4 | The cart badge reflects items added and removed | TC-UI-10 | State | `InventoryTest` |
| R-UI-5 | A user can buy an item end to end | TC-UI-11 | End to end | `CheckoutTest` |
| R-UI-6 | Checkout requires customer details | TC-UI-12 | Negative | `CheckoutTest` |
| R-API-1 | The service exposes a health check | TC-API-01 | Smoke | `AuthApiTest` |
| R-API-2 | Valid credentials return a token; invalid ones don't | TC-API-02, 03, 04 | Positive / negative | `AuthApiTest` |
| R-API-3 | Bookings can be created, read, searched, updated, patched and deleted | TC-API-05 to 10 | State transition | `BookingCrudApiTest` |
| R-API-4 | Responses follow the booking contract | TC-API-06 | Contract (JSON Schema) | `BookingCrudApiTest` |
| R-API-5 | Changes require a valid token | TC-API-12, 13 | Security / negative | `BookingNegativeApiTest` |
| R-API-6 | Unknown resources return 404 | TC-API-11 | Negative | `BookingNegativeApiTest` |
| R-API-7 | Boundary and international data is stored exactly | TC-API-14 (TD-01 to 05) | Boundary, data-driven | `BookingNegativeApiTest` |
| R-API-8 | Invalid payloads return a client error (4xx) | TC-API-15 | Negative | `BookingNegativeApiTest` |
| R-SOAP-1 | Arithmetic operations return correct results | TC-SOAP-01 to 05 | Functional, data-driven | `CalculatorSoapTest` |
| R-SOAP-2 | Invalid input returns a SOAP fault | TC-SOAP-06 | Negative | `CalculatorSoapTest` |
| R-MOB-1 | The Settings app launches and can be navigated | TC-MOB-01, 02 | Mobile UI | `AndroidSettingsTest` (written, **not yet executed**) |
| R-UX-1 | The store is usable by keyboard and with special user profiles | MT-01 to 04 | Manual, exploratory | Section 7 |

**Coverage:** all 18 requirements have at least one test case. 16 are covered by automated tests that run
in CI; R-UX-1 is covered by manual testing; R-MOB-1 has automated tests written but **not yet executed**
on an emulator or device, so it is not counted as verified.

## 7. Manual test cases and exploratory charters

Some behavior is cheaper and more reliable to check by hand than to automate.

### MT-01: Keyboard-only login and checkout (accessibility)

| Step | Action | Expected result |
|---|---|---|
| 1 | Open the site; use only **Tab**, **Shift+Tab**, **Enter** and **Space** | Focus starts on the username field |
| 2 | Tab through username, password, Login | Focus order matches visual order; focus is always visible |
| 3 | Log in, add an item and check out using only the keyboard | Every control is reachable and operable |
| 4 | Trigger a validation error | The error is announced or reachable and clearly worded |

### MT-02 to MT-04: exploratory charters (30 minutes each)

| ID | Charter | Look for |
|---|---|---|
| MT-02 | Explore the store as `problem_user` | Wrong images, broken sorting, fields that don't accept input |
| MT-03 | Explore the store as `performance_glitch_user` | Slow pages; whether the automated waits are long enough |
| MT-04 | Explore browser back/forward and refresh during checkout | Lost cart state, duplicate orders, unexpected logouts |

Findings from these sessions are logged in section 9. Any repeatable finding becomes a candidate for automation.

## 8. Entry and exit criteria

**Entry:** the system under test is reachable (`/ping` returns 201; the login page loads) and the build compiles.

**Exit (a release is "green"):**

- 100% of smoke tests pass.
- At least 95% of regression tests pass, and every failure has a logged defect or a known cause.
- No open Critical or High defects without an agreed workaround.
- The traceability matrix shows every requirement covered.

## 9. Defect log

Defects found while building these suites. `knownDefect` tests assert the *correct* behavior, so
they are **expected to fail** until the defect is fixed. They're excluded from CI and can be run with
`./mvnw test -Dsuite=known-defects`.

| ID | Severity | Summary | Steps to reproduce | Expected | Actual | Test |
|---|---|---|---|---|---|---|
| BUG-001 | Medium | Failed login returns **200 OK** | `POST /auth` with `{"username":"admin","password":"wrong"}` | 401 or 403 | 200 with `{"reason":"Bad credentials"}`; clients that only check the status code treat it as success | TC-API-04 |
| BUG-002 | Medium | Missing required fields cause a **500 server error** | `POST /booking` with `{"firstname":"OnlyAFirstName"}` | 400 with a validation message | 500 Internal Server Error | TC-API-15 |
| BUG-003 | Medium (security) | SOAP fault **leaks stack trace and server path** | Call `Divide` with `intB = 0` | Generic fault message | `faultstring` contains the .NET stack trace and `G:\PleskVhosts\...` path | TC-SOAP-06 (observation) |
| BUG-004 | Low | Standard `Accept` header list is rejected with **418** | `POST /booking` with `Accept: application/json, text/json` | 200 (JSON is acceptable) | 418 I'm a teapot; only an exact `application/json` works | Worked around in `BaseApiTest` |

## 10. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Public practice sites are shared and can be slow or down | False failures | One automatic retry, logged as `RETRY` so flakiness stays visible; `-Dretries=0` to check real stability; nightly runs separate site outages from code changes |
| Restful-Booker data is reset periodically and shared with other users | Tests collide with others' data | Each test creates its own data and deletes it; no reliance on pre-existing records |
| UI re-renders cause stale elements | Intermittent UI failures | Explicit waits that re-poll; the suite was verified at 3/3 clean runs with retries off |
| Locators change | Many tests break at once | Page Object Model keeps locators in one place; `data-test` attributes are used |
| Mobile tests need a device | Can't run in CI; not yet executed at all | Tests skip (not fail) without an Appium server; first run on an emulator is the next planned step |

## 11. Reporting and future work

**Reporting:** TestNG and Surefire reports in `target/surefire-reports/` (HTML plus JUnit XML), failure
screenshots in `target/screenshots/`. CI uploads both as build artifacts on every run.

**Planned extensions:**

- **Next:** run the Appium tests (TC-MOB-01, 02) on an Android emulator and record the results.
- Cross-browser runs (Firefox, Edge) through a CI matrix.
- Allure reporting with step-level screenshots.
- Saved SoapUI project plus `testrunner` in CI (`soapui/README.md`).
- Run mobile tests in CI on an Android emulator (`reactivecircus/android-emulator-runner`).
- Basic load test of the booking API (k6 or JMeter).
