# Backend

## Booking confirmation messages

After a booking is saved, TravelGo attempts an email and an SMS to the customer's registered email address and phone number. A delivery failure does not cancel the booking. The booking response includes `notificationEmailStatus` and `notificationSmsStatus` (`sent`, `failed`, `not_configured`, or `no_recipient`).

Configure email by setting these environment variables before starting Spring Boot:

```text
TRAVELGO_SMTP_HOST=smtp.gmail.com
TRAVELGO_SMTP_PORT=587
TRAVELGO_SMTP_USERNAME=your-sender@gmail.com
TRAVELGO_SMTP_PASSWORD=your-mail-provider-app-password
TRAVELGO_SMTP_FROM=your-sender@gmail.com
TRAVELGO_SMTP_STARTTLS=true
```

For Gmail, use an app password rather than your account password. Store provider credentials in the process environment; do not commit them to this repository.

Configure SMS with a Twilio account and an E.164 sender number:

```text
TRAVELGO_TWILIO_ACCOUNT_SID=your-account-sid
TRAVELGO_TWILIO_AUTH_TOKEN=your-auth-token
TRAVELGO_TWILIO_FROM_NUMBER=+15551234567
```

Indian 10-digit mobile numbers beginning with 6-9 are normalized to `+91`. Other recipients must already use E.164 format. Leave a channel unconfigured to disable it; booking creation remains available and the response reports its status.

## Staff and package changes

Staff and package deletion is a soft delete. Deleted staff can no longer sign in or be assigned to new trips; deleted packages are omitted from browsing and new bookings. Existing bookings retain the related staff/package record and display it as deleted. Admin package edits and staff profile edits update the shared records used by booking and trip views.
