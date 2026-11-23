Personal Expense Tracker

A desktop-based Personal Expense Tracker application developed using Java Swing, JDBC, and MySQL. It helps users manage their income, expenses, monthly budgets, and financial reports.

Features

- User Registration: Create an account with secure password hashing.
- User Login: Authenticate registered users.
- Dashboard: View total income, expenses, and current balance.
- Add Transactions:Record income and expenses.
- Manage Transactions: Search, filter, edit, and delete transactions.
- Monthly Reports: View monthly income, expenses, savings, and category-wise spending.
- Monthly Budget: Set a monthly spending limit and monitor budget usage.
- Budget Alerts: Receive warnings when spending reaches or exceeds the budget.
- Logout: Securely return to the login screen.

 Technologies Used

| Technology                     | Purpose 

| Java                           | Application development 
| Java Swing                     | Graphical user interface 
| JDBC                           | Database connectivity 
| MySQL                          | Data storage 
| MySQL Workbench                | Database management 
| Visual Studio Code             | Code editor 

 Project Structure

PersonalExpenseTracker/
├── images/
├── lib/
├── src/
│   ├── Main.java
│   ├── DBConnection.java
│   ├── PasswordUtil.java
│   ├── UserDAO.java
│   ├── LoginGUI.java
│   ├── RegisterGUI.java
│   ├── DashboardGUI.java
│   ├── AddTransactionGUI.java
│   ├── EditTransactionGUI.java
│   ├── ViewTransactionsGUI.java
│   ├── TransactionDAO.java
│   ├── MonthlyReportGUI.java
│   ├── BudgetGUI.java
│   └── BudgetDAO.java
├── bin/
├── database_setup.sql
└── README.md


Database

The application uses a MySQL database named `personal_expense_tracker`.

It contains three main tables:

1. users – Stores registered user information and hashed passwords.
2. transactions – Stores income and expense records.
3. budgets – Stores monthly spending limits for each user.

How to Run the Project

 Requirements

- Java Development Kit (JDK)
- MySQL Server
- MySQL Connector/J
- Visual Studio Code or another Java-compatible IDE

 Setup

1. Open the project folder in Visual Studio Code.
2. Start MySQL Server.
3. Execute `database_setup.sql` in MySQL Workbench to create the database and tables.
4. Configure your MySQL connection settings in `DBConnection.java`.
5. Ensure the MySQL Connector/J JAR file is present in the `lib` folder.

 Compile

Open PowerShell in the project folder and run:


javac -encoding UTF-8 -cp "lib/*" -d bin src/*.java


 Run

java -cp "bin;lib/*" Main


 Application Workflow

1. Register a new account.
2. Log in using your email and password.
3. Add income and expense transactions.
4. View your financial summary on the dashboard.
5. Search, edit, or delete transactions.
6. Generate monthly financial reports.
7. Set a monthly budget and monitor spending.
8. Log out when finished.

 Security

- Passwords are stored using PBKDF2-based password hashing.
- SQL queries use prepared statements.
- Transaction and budget records are associated with individual user accounts.

 Future Enhancements

- Export financial reports to PDF or Excel.
- Add graphical charts for income and expenses.
- Add recurring transactions.
- Provide custom expense categories.
- Support backup and restore.

 Conclusion

The Personal Expense Tracker provides a simple way to organize personal finances. It combines Java Swing for the user interface and MySQL for persistent data storage, allowing users to track expenses, monitor savings, and manage monthly budgets.