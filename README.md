# Team 3 - College-Management-System (Java Swing Authentication & Registration System)

A desktop application built with Java Swing and MySQL providing role-aware authentication and registration for Students and Faculty.

---

## 1. Prerequisites
- **Java**: OpenJDK / Oracle JDK 21 (or Java 17+ compatible)
- **MySQL**: MySQL 8.0+ or MariaDB 10.5+
- **JDBC Driver**: MySQL Connector/J 8.3.0 (included under `lib/mysql-connector-j-8.3.0.jar`)

---

## 2. Database Setup & Schema Application

1. Ensure your MySQL server is running (e.g., via MySQL Community Server, XAMPP, or Docker):
   ```bash
   # Example: Verify service status on Windows PowerShell
   Get-Service MySQL*
   ```

2. Log in to your MySQL server using the MySQL command-line client or GUI (MySQL Workbench, phpMyAdmin, DBeaver):
   ```bash
   mysql -u root -p
   ```

3. Execute the provided migration script `schema.sql`:
   ```sql
   SOURCE schema.sql;
   ```
   *Alternatively, apply from the terminal directly:*
   ```bash
   mysql -u root -p < schema.sql
   ```

This creates:
- Database: `team3_auth_db` (using `utf8mb4` charset and `InnoDB` engine)
- **15 tables** for complete college management system:

**Authentication & Profiles (3 tables):**
- `accounts` - User credentials and roles
- `student_profiles` - Student-specific information  
- `faculty_profiles` - Faculty-specific information

**Extended Management (9 tables):**
- `users`, `departments`, `students`, `faculty` - User and department management
- `semesters`, `subjects`, `faculty_subjects` - Academic data
- `academic_records`, `results` - Student performance tracking

**Library & Grievances (3 tables):**
- `books`, `library_transactions` - Library management
- `complaints` - Student complaints system

---

## 3. Database Connection Configuration

Configuration is loaded dynamically with the following priority order:
1. **Environment Variables**: `DB_URL`, `DB_USER`, `DB_PASSWORD`
2. **Local Configuration File**: `db.properties` in the project root
3. **Safe Local Defaults**: `jdbc:mysql://localhost:3306/team3_auth_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`, user: `root`, blank password.

### Setting Up `db.properties`
Copy the example file from config folder:
```powershell
Copy-Item config/db.properties.example db.properties
```
Edit `db.properties` with your database credentials:
```properties
db.url=jdbc:mysql://localhost:3306/team3_auth_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_secret_password
```
> **Security Note:** `db.properties` is listed in `.gitignore` to prevent committing secrets to source control. Never commit database passwords.

---

## 4. How to Build and Run the Application

### Using Build Scripts (Recommended)

```powershell
# Compile all Java files
.\compile.ps1

# Run the application
.\run.ps1

# Clean build files
.\clean.ps1
```

### Manual Commands

```powershell
# Compile manually
javac -cp ".;lib/*" -d build src/backend/**/*.java src/frontend/**/*.java

# Run manually  
java -cp "build;lib/mysql-connector-j-8.3.0.jar" LoginFrame
```

**Note:** The `compile.ps1` script automatically finds and compiles ALL `.java` files in the project, including new files you add.

---

## 5. User Flows

### Registering as a Student
1. Launch `LoginFrame`.
2. Click **"Register here"** and select **"Student Registration"**.
3. Fill in all fields:
   - **Full Name**: e.g., `Jane Doe`
   - **Register Number**: e.g., `STU2026001` (unique)
   - **Email Address**: e.g., `jane.doe@university.edu` (valid email format, unique)
   - **Phone Number**: e.g., `+1-555-0145` (minimum 7 digits)
   - **Date of Birth**: `YYYY-MM-DD` (e.g., `2004-03-22`)
   - **Department / Semester / Course**: Select from dropdowns.
   - **Username**: e.g., `janedoe` (minimum 3 characters, unique)
   - **Password**: At least 8 characters with at least one letter and one number or special character.
   - **Terms and Conditions**: Check the box.
4. Click **"Register"**. A confirmation dialog confirms success after the transaction commits atomically, then returns you to the Login screen.

### Registering as Faculty
1. Launch `LoginFrame`.
2. Click **"Register here"** and select **"Faculty Registration"**.
3. Fill in all fields:
   - **Full Name**: e.g., `Dr. Alan Turing`
   - **Faculty ID**: e.g., `FAC1042` (unique)
   - **Official Email Address**: e.g., `a.turing@university.edu` (unique)
   - **Office Extension**: Optional (e.g., `402A`)
   - **Department / Designation / Course Load**: Select from dropdowns.
   - **Specialization**: e.g., `Theoretical Computer Science`
   - **Username**: e.g., `aturing` (unique)
   - **Password**: At least 8 characters with at least one letter and one number or special character.
   - **Terms of Service**: Check the box.
4. Click **"Register"**. On database commit, you are returned to Login.

### Logging In
1. Select the appropriate role radio button (**Student** or **Faculty**). Role selection is strictly required.
2. Enter your identifier:
   - **For Student**: Enter either your **Username** or your **Register Number**.
   - **For Faculty**: Enter either your **Username** or your **Faculty ID**.
3. Enter your password.
4. (Optional) Check **Remember Me** to safely retain your username and role preference across app restarts.
5. Click **"Login"** (or press Enter in the password field).

---

## 6. Security & Architecture Decisions

1. **Password Hashing**: Uses `PBKDF2WithHmacSHA256` with 65,536 iterations and a cryptographically secure 16-byte random salt per password (`SecureRandom`). Stored format: `PBKDF2$65536$<salt>$<hash>`. Verification uses `MessageDigest.isEqual` to resist timing attacks. Passwords are handled in `char[]` and zeroed out in memory immediately after derivation.
2. **Atomic Registration**: Both the `accounts` entry and the role profile (`student_profiles` or `faculty_profiles`) are inserted in a single database transaction (`setAutoCommit(false)` ... `commit()`). If any step or constraint fails, the entire transaction is rolled back, preventing orphaned rows.
3. **Role Segregation**: Authentication queries enforce `WHERE a.role = ?`. A student cannot log in using the Faculty role, and a faculty member cannot log in using the Student role.
4. **Email Uniqueness Decision**: In this institution system, email addresses are scoped as unique per role (`student_profiles(email)` and `faculty_profiles(email)`), allowing clear separation while preventing impersonation within each group.
5. **Remember Me**: Persists only the non-sensitive `username` and `role` preference via the standard Java user preference backing store (`java.util.prefs.Preferences` under user space). Passwords and password hashes are **NEVER** saved.
6. **Responsive UI**: All database operations execute on background threads via `SwingWorker`, ensuring the UI never freezes during slow network or database calls. Buttons are disabled while requests are active.

---

## 7. Known Limitations & Password Recovery

- **Password Recovery**: Self-service recovery is intentionally not configured without a verified SMTP/SMS service. Clicking **"Forgot Password?"** displays an informative dialog directing users to the institution's administrator at `admin@institution.edu`. No information about username or email existence is revealed.

---

## 8. Troubleshooting

| Issue | Likely Cause | Solution |
| :--- | :--- | :--- |
| **"Cannot reach MySQL server"** | MySQL service is not running or port is incorrect | Check if MySQL is running (`Get-Service MySQL80` or `Start-Service MySQL80`). Verify host and port in `db.properties`. |
| **"Database 'team3_auth_db' does not exist"** | `schema.sql` was not run | Run `Get-Content database/schema.sql \| mysql -u root -p` to initialize the database and all 15 tables. |
| **"Database access denied for user..."** | Wrong MySQL username or password in `db.properties` | Open `db.properties` and verify `db.user` and `db.password`. |
| **"The username '...' is already taken"** | Account with this username already exists | Choose a different unique username. |
| **"The register number '...' is already registered"** | Duplicate student register number | Enter the correct assigned student register number. |
| **"Invalid Date of Birth format"** | Date format is not `YYYY-MM-DD` | Enter date in ISO format, e.g., `2003-05-15`. |
| **SSL/TLS handshake errors** | MySQL server SSL requirements mismatch | For local dev, ensure `useSSL=false&allowPublicKeyRetrieval=true` is set. In production, configure valid CA certificates and set `useSSL=true&requireSSL=true`. |
| **"MySQL Connector/J driver class not found"** | Missing JAR in classpath | Ensure `lib/mysql-connector-j-8.3.0.jar` exists. |
| **Compilation errors** | Java version incompatible | Ensure Java 17+ is installed: `java -version` |

---

## 9. Project Structure

```
team3/
├── src/
│   ├── backend/              # Backend/Business Logic Layer
│   │   ├── config/           # DatabaseConfig.java
│   │   ├── services/         # AuthService.java
│   │   └── utils/            # PasswordUtil, ValidationUtil, PreferencesUtil, AppTest
│   │
│   └── frontend/             # Frontend/Presentation Layer
│       ├── ui/               # LoginFrame.java
│       └── forms/            # StudentRegistration, FacultyRegistration
│
├── database/
│   └── schema.sql           # Complete database schema (15 tables)
│
├── config/
│   ├── db.properties        # Database configuration (source)
│   └── db.properties.example # Configuration template
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar  # MySQL JDBC driver
│
├── build/                   # Compiled .class files (auto-generated)
│
├── compile.ps1              # Automated compilation script
├── run.ps1                  # Application launcher script
├── clean.ps1                # Build cleanup script
├── db.properties            # Active database config (root copy)
│
├── README.md                # This file
├── QUICK_START.md           # Quick reference guide
└── PROJECT_STRUCTURE.md     # Detailed architecture documentation
```

**Key Points:**
- **Backend** (`src/backend/`) - Business logic, database operations, no UI
- **Frontend** (`src/frontend/`) - User interface with Java Swing components  
- **Scripts** - `compile.ps1` auto-finds ALL `.java` files (new or old)
- **Database** - Complete schema with 15 tables in `database/schema.sql`

For more details, see `PROJECT_STRUCTURE.md` and `QUICK_START.md`.
