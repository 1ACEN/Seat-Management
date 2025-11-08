# Seat-Management
this is a intro project based on the concepts of OOPs

## MySQL connection

This project can be configured to use a MySQL database for user authentication.

Environment variables used (optional; defaults listed):

- DB_URL (default: jdbc:mysql://localhost:3306/seatbooking?useSSL=false&serverTimezone=UTC)
- DB_USER (default: root)
- DB_PASSWORD (default: empty)

On first run the app will create a `users` table if it doesn't exist and also insert a default admin user (`admin` / `admin123`) if needed.

Build and run (from project root):

```bash
mvn -f "seatbooking/pom.xml" -DskipTests package
java -cp "seatbooking/target/classes;seatbooking/target/dependency/*" com.booking.Main
```

Note: Set the environment variables appropriately before running, for example in Windows CMD:

```bat
set DB_URL=jdbc:mysql://localhost:3306/seatbooking
set DB_USER=root
set DB_PASSWORD=yourpassword
```

You must create the actual MySQL database/schema `seatbooking` or change the DB_URL to point to an existing schema.
