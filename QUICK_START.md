# 🚀 Quick Start Guide

## Project Organization Complete! ✅

Your project has been reorganized into a clean, professional structure that separates **Backend** and **Frontend** code.

---

## 📁 New Folder Structure

```
team3/
│
├── 🔧 src/
│   ├── 💾 backend/           ← BACKEND CODE (Business Logic)
│   │   ├── services/         → AuthService.java
│   │   ├── config/           → DatabaseConfig.java
│   │   └── utils/            → PasswordUtil, ValidationUtil, PreferencesUtil
│   │
│   └── 🎨 frontend/          ← FRONTEND CODE (User Interface)
│       ├── ui/               → LoginFrame.java
│       └── forms/            → StudentRegistration, FacultyRegistration
│
├── 🗄️  database/             ← Database schemas
├── ⚙️  config/               ← Configuration files (db.properties)
├── 📚 lib/                  ← External libraries (MySQL connector)
├── 🏗️  build/               ← Compiled .class files
└── 📖 docs/                 ← Documentation
```

---

## 🎯 Understanding Backend vs Frontend

### 💾 **BACKEND** (src/backend/)
**What it does:** Handles business logic, data processing, and database operations

**Files:**
- `AuthService.java` - Login & Registration logic
- `DatabaseConfig.java` - Database connection management
- `PasswordUtil.java` - Password hashing & security
- `ValidationUtil.java` - Input validation
- `PreferencesUtil.java` - User preferences storage

**Characteristics:**
- ❌ No UI components (no JFrame, JPanel, JButton)
- ✅ Database queries
- ✅ Business rules
- ✅ Data validation
- ✅ Security operations

### 🎨 **FRONTEND** (src/frontend/)
**What it does:** Displays UI and handles user interactions

**Files:**
- `LoginFrame.java` - Login screen
- `StudentRegistration.java` - Student registration form
- `FacultyRegistration.java` - Faculty registration form

**Characteristics:**
- ✅ JFrame, JPanel, JButton, JTextField (Swing components)
- ✅ Event listeners (button clicks, form submission)
- ✅ User input collection
- ✅ Display messages to users
- ❌ Minimal business logic (calls backend services)

---

## ⚡ Quick Commands

### 1️⃣ Compile the Project
```powershell
.\compile.ps1
```

### 2️⃣ Run the Application
```powershell
.\run.ps1
```

### 3️⃣ Clean Build Files
```powershell
.\clean.ps1
```

---

## 🔄 How They Work Together

```
┌─────────────────────┐
│   USER INTERFACE    │  ← Frontend (LoginFrame.java)
│  (Frontend Layer)   │    User enters username/password
└──────────┬──────────┘
           │
           │ User clicks "Login" button
           ↓
┌─────────────────────┐
│   BUSINESS LOGIC    │  ← Backend (AuthService.java)
│   (Backend Layer)   │    Validates credentials
└──────────┬──────────┘    Checks password hash
           │
           │ SQL Query
           ↓
┌─────────────────────┐
│      DATABASE       │  ← MySQL Database
│  (Data Layer)       │    Fetches user data
└─────────────────────┘
```

---

## 📝 Example: Login Process

### Frontend Code (LoginFrame.java)
```java
// User clicks login button
loginButton.addActionListener(e -> {
    String username = usernameField.getText();
    String password = passwordField.getText();
    
    // ⬇️ CALLS BACKEND ⬇️
    AuthResult result = AuthService.login(username, password, role);
    
    if (result.isSuccess()) {
        showSuccessMessage();
    }
});
```

### Backend Code (AuthService.java)
```java
public static AuthResult login(String username, String password, String role) {
    // Business logic
    // Database queries
    // Password verification
    return new AuthResult(success, message);
}
```

---

## 🎓 Key Concepts

| Layer | Responsibility | Example |
|-------|---------------|---------|
| **Frontend** | What the user sees and clicks | Buttons, forms, text fields |
| **Backend** | What happens behind the scenes | Login logic, password checking |
| **Database** | Where data is stored | User accounts, student info |

---

## 📚 More Information

- See `PROJECT_STRUCTURE.md` for detailed architecture
- See `docs/README.md` for full project documentation
- Configuration files are in `config/`
- Database scripts are in `database/`

---

## 🔑 Database Credentials

- **Username:** root
- **Password:** newpassword123
- **Database:** team3_auth_db

---

**Happy Coding!** 🎉
