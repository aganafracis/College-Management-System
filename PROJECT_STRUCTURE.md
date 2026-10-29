# Project Structure

```
team3/
│
├── src/                          # Source code directory
│   ├── backend/                  # Backend/Business Logic Layer
│   │   ├── services/            # Business services
│   │   │   └── AuthService.java         # Authentication & Registration logic
│   │   ├── utils/               # Utility classes
│   │   │   ├── PasswordUtil.java        # Password hashing & security
│   │   │   ├── PreferencesUtil.java     # User preferences management
│   │   │   ├── ValidationUtil.java      # Input validation utilities
│   │   │   └── AppTest.java             # Test cases
│   │   └── config/              # Configuration classes
│   │       └── DatabaseConfig.java      # Database connection config
│   │
│   └── frontend/                 # Frontend/Presentation Layer
│       ├── ui/                   # User interface screens
│       │   └── LoginFrame.java          # Main login screen
│       └── forms/                # Registration forms
│           ├── StudentRegistration.java  # Student registration form
│           └── FacultyRegistration.java  # Faculty registration form
│
├── database/                     # Database scripts
│   └── schema.sql               # Database schema & table creation
│
├── config/                       # Configuration files
│   ├── db.properties            # Database connection settings (not in git)
│   └── db.properties.example    # Example database config
│
├── lib/                          # External libraries
│   └── mysql-connector-j-8.3.0.jar  # MySQL JDBC driver
│
├── build/                        # Compiled .class files
│
├── docs/                         # Documentation
│   └── README.md                # Project documentation
│
├── db.properties                 # Active database config (root copy)
├── compile.ps1                   # Build script
├── run.ps1                       # Run script
├── clean.ps1                     # Clean script
├── PROJECT_STRUCTURE.md          # This file
├── QUICK_START.md                # Quick reference guide
└── .gitignore                    # Git ignore rules

```

## Component Classification

### Backend Components (Business Logic)
These handle data processing, business rules, and database operations:

1. **AuthService.java** - Core authentication and registration service
   - Handles login verification
   - Manages user registration (Student & Faculty)
   - Database transactions
   - Role-based authentication

2. **DatabaseConfig.java** - Database configuration manager
   - Loads database credentials
   - Manages connection pooling
   - Handles connection lifecycle

3. **PasswordUtil.java** - Security utility
   - Password hashing (PBKDF2)
   - Password verification
   - Secure salt generation

4. **ValidationUtil.java** - Input validation
   - Email format validation
   - Phone number validation
   - Username validation
   - Data sanitization

5. **PreferencesUtil.java** - User preferences
   - Remember me functionality
   - Preference storage/retrieval

### Frontend Components (User Interface)
These handle user interaction and display:

1. **LoginFrame.java** - Main login screen (Java Swing)
   - Login form UI
   - Role selection (Student/Faculty)
   - Remember me checkbox
   - Navigation to registration

2. **StudentRegistration.java** - Student registration form
   - Student information input fields
   - Form validation
   - UI components (text fields, dropdowns)
   - Registration submission

3. **FacultyRegistration.java** - Faculty registration form
   - Faculty information input fields
   - Form validation
   - UI components
   - Registration submission

## Architecture Pattern

This project follows a **2-Tier Architecture**:

```
┌─────────────────────────────────────┐
│         FRONTEND LAYER              │
│  (Java Swing UI - Presentation)     │
│  - LoginFrame                       │
│  - StudentRegistration              │
│  - FacultyRegistration              │
└──────────────┬──────────────────────┘
               │
               │ calls
               ↓
┌─────────────────────────────────────┐
│         BACKEND LAYER               │
│  (Business Logic + Data Access)     │
│  - AuthService                      │
│  - DatabaseConfig                   │
│  - Utilities (Password, Validation) │
└──────────────┬──────────────────────┘
               │
               │ SQL queries
               ↓
┌─────────────────────────────────────┐
│         DATABASE LAYER              │
│         (MySQL)                     │
│  - team3_auth_db                    │
└─────────────────────────────────────┘
```

## Data Flow Example (Login)

1. **Frontend**: User enters credentials in `LoginFrame.java`
2. **Frontend**: Validates input format using `ValidationUtil.java`
3. **Backend**: Calls `AuthService.login(username, password, role)`
4. **Backend**: `DatabaseConfig` provides database connection
5. **Backend**: `PasswordUtil` verifies password hash
6. **Database**: Query executed to fetch user data
7. **Backend**: Returns authentication result
8. **Frontend**: Displays success/error message

## Build & Run Commands

### Compile (using script - recommended)
```powershell
.\compile.ps1
```
This automatically finds and compiles ALL .java files in the project.

### Run (using script - recommended)
```powershell
.\run.ps1
```

### Clean (remove compiled files)
```powershell
.\clean.ps1
```

### Manual Commands (if needed)
```powershell
# Compile manually
javac -cp ".;lib/*" -d build src/backend/services/*.java src/backend/config/*.java src/backend/utils/*.java src/frontend/ui/*.java src/frontend/forms/*.java

# Run manually
java -cp "build;lib/mysql-connector-j-8.3.0.jar" LoginFrame
```

## Database Setup

1. Ensure MySQL is running:
   ```powershell
   Get-Service MySQL80
   ```

2. Run database schema:
   ```powershell
   Get-Content database/schema.sql | mysql -u root -pnewpassword123
   ```

3. Configure `config/db.properties` with your database credentials
4. Copy to root for application to find it:
   ```powershell
   Copy-Item config/db.properties db.properties
   ```

**Note:** The application looks for `db.properties` in the root directory.

## Notes

- **Backend** = Server-side logic, no UI, handles data
- **Frontend** = Client-side, UI components, user interaction
- All `.class` files (compiled bytecode) go to `build/`
- Configuration files are in `config/`
- Database scripts are in `database/`
