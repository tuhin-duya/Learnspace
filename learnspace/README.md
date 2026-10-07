# LearnSpace

A Spring Boot online learning platform modeled on the supplied marketplace project's architecture. It includes session-based accounts, course catalogues, purchasable batches, enrollment tracking, and recorded lesson playback.

## Run

Install Java 17+, Maven, and MySQL 8+. Ensure MySQL is running, then from this folder run:

```powershell
mvn spring-boot:run
```

Open http://localhost:8080.

The application connects to a MySQL database named `learnspace` on `localhost:3306` and creates it automatically when the configured MySQL user has permission. Configure a different connection without editing source code:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/learnspace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='your-password'
mvn spring-boot:run
```

It seeds two courses and batches on first start. Demo administrator: `admin@learnspace.local` / `ChangeMe123!`.

## Razorpay payments

Batch enrollment is created only after Razorpay Checkout returns a payment and the server verifies its signature. Set Razorpay test keys before starting the app:

```powershell
$env:RAZORPAY_KEY_ID='rzp_test_...'
$env:RAZORPAY_KEY_SECRET='your-test-key-secret'
```

Use test keys while developing and replace them with live keys only when ready to accept real payments. Keep the key secret private; it is never exposed to the browser.
