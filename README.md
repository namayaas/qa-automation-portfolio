# QA Automation Portfolio

[![CI](https://github.com/namayaas/qa-automation-portfolio/actions/workflows/ci.yml/badge.svg)](https://github.com/namayaas/qa-automation-portfolio/actions/workflows/ci.yml)

UI and API (REST and SOAP) test automation in **Java**, built around a written
[**test plan**](TEST_PLAN.md) with test data sets, a requirements traceability matrix and a defect log.

| Layer | System under test | Tools | Tests |
|---|---|---|---|
| Web UI | [Sauce Demo](https://www.saucedemo.com) store | Selenium WebDriver 4, Page Object Model | 12 |
| REST API | [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html) | REST Assured, JSON Schema | 17 |
| SOAP API | [DNE Calculator](http://www.dneonline.com/calculator.asmx) | REST Assured (XML), [SoapUI](soapui/README.md) | 6 |
| Mobile *(in progress)* | Android Settings app | Appium 2, UiAutomator2 | 2 written, not yet run on a device |
| Known defects | Restful-Booker | REST Assured | 2 (expected to fail) |

TestNG runs everything; Maven builds it; GitHub Actions runs API and UI suites on every push and nightly.

## Highlights

- **Test design first.** [TEST_PLAN.md](TEST_PLAN.md) covers scope, approach, equivalence partitions,
  boundary values, entry/exit criteria, risks and an 18-requirement traceability matrix.
- **Data-driven.** Test conditions live in [`testdata/`](src/test/resources/testdata/) files, not in code.
- **Contract testing.** API responses are validated against a [JSON Schema](src/test/resources/schemas/booking-schema.json).
- **Real defects found.** Four defects were found and documented with reproduction steps (for example,
  failed login returns `200 OK`). Each has a test that asserts the *correct* behavior.
- **Built for stability.** Explicit waits only, isolated test data with cleanup, and one *logged* retry for
  public-site network blips. The UI suite passed 3/3 consecutive runs with retries disabled.
- **Debuggable failures.** Screenshots on UI failure; full request/response logs on API failure.

## Run it

Needs **Java 17+** and **Chrome**. The Maven wrapper downloads Maven, and Selenium Manager downloads chromedriver.

```bash
./mvnw test                      # API + UI (default)
./mvnw test -Dsuite=api          # REST + SOAP only, about 15 seconds
./mvnw test -Dsuite=ui           # Selenium only, headless
./mvnw test -Dsuite=ui -Dheadless=false   # watch the browser
./mvnw test -Dsuite=known-defects         # the documented failing tests
./mvnw test -Dretries=0          # disable retries to check for flakiness
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

Reports: `target/surefire-reports/index.html`. Failure screenshots: `target/screenshots/`.

### Mobile tests (in progress)

> **Status:** the two Appium tests are written and compile, but have not yet been run against an
> emulator or device. They are not part of the CI suites or the 35-test count above.

```bash
npm install -g appium
appium driver install uiautomator2
appium                           # leave running
# Start an Android emulator (Android Studio → Device Manager) or plug in a device; check `adb devices`
./mvnw test -Dsuite=mobile       # add -Dandroid.udid=<serial> to target one device
```

Without a running Appium server, the mobile tests **skip** instead of failing.

## Project layout

```
src/test/java/com/nathalyamaya/qa/
├── common/     Config (env overrides), test-data loaders, logged retry
├── ui/         BaseUiTest (browser per test, screenshot on failure) and tests
│   └── pages/  Page objects: Login, Inventory, Cart, Checkout
├── api/        REST tests (auth, CRUD lifecycle, negative/data-driven) and the SOAP test
└── mobile/     Appium tests (in progress, not yet run on a device)
src/test/resources/
├── suites/     TestNG suites: all, api, ui, mobile, known-defects
├── testdata/   Login partitions (.psv), booking boundary values (.json)
└── schemas/    JSON Schema for the booking contract
soapui/         How to run the SOAP tests in SoapUI
TEST_PLAN.md    Test plan, traceability matrix, defect log
```

## Configuration

Every setting can be overridden with `-Dkey=value` or an environment variable (`KEY_WITH_UNDERSCORES`):
`ui.baseUrl`, `api.baseUrl`, `soap.baseUrl`, `appium.url`, `headless`, `wait.seconds`, `retries`.

The credentials in this repo are the public demo logins published by each practice site, not secrets.
