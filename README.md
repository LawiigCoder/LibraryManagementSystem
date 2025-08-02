# LibraryManagementSystem
This is my LibraryManagementSystem using JAVA Code 

# 📚 Library Management System
A **Java Swing desktop application** with **JDBC and MySQL** designed for educational institutions such as the **Royal University of Phnom Penh (RUPP)**. This system helps libraries manage book stock, members, categories, and transactions efficiently through a user-friendly graphical interface.
---
## 🌐 Overview
The Library Management System provides full CRUD management for books, members, transactions, and categories. It features a modern Swing interface, integrates with a **MySQL** database via **JDBC**, and supports real-time updates for inventory and lending status.
---
## 🚀 Features
- ✅ Add, edit, and delete books (title, author, publisher, year, ISBN, category, status)
- 👥 Manage members (student card, name, email, phone, membership date, status)
- 🔁 Record, update, and delete transactions (borrow, return, due dates)
- 🏷️ Add, edit, and delete categories
- 📊 Dashboard with quick navigation to all major modules
- 🔍 Search and browse books by title, author, or category
- 📅 View transaction history and current status
- 🗃️ Real-time inventory and member status tracking
### ⚙️ General Features
- 📦 Real-time inventory and lending updates
- 🗃️ MySQL database via JDBC
- 🖼️ Modern, clean Java Swing GUI
- 🏛️ Easy setup with Docker for database
---
## 🖥️ Technologies Used
- Java (Swing)
- JDBC (Java Database Connectivity)
- MySQL (or any supported SQL database)
---
# 🗄️ Library Database Setup with MySQL & Docker
This repository helps you quickly set up a MySQL database for the library management system using Docker, and provides SQL scripts and Java usage instructions.
## Prerequisites
- [Docker](https://www.docker.com/products/docker-desktop/)
- [Java JDK](https://adoptopenjdk.net/) (version 8 or above)
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (e.g., `mysql-connector-j-9.3.0.jar`)
- Basic knowledge of MySQL and SQL commands
## Getting Started
### 1. Run MySQL with Docker
```sh
docker run --name test-db -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123 -d mysql:latest
```
- `--name test-db`: Names the container `test-db`
- `-p 3306:3306`: Maps MySQL port 3306 to your host
- `-e MYSQL_ROOT_PASSWORD=123`: Sets the root password to `123`
- `-d mysql:latest`: Runs the latest official MySQL image in detached mode
### 2. Connect to MySQL
```sh
docker exec -it test-db mysql -u root -p
```
Enter the password: `123`
### 3. Create the Database and Tables
At the MySQL prompt, paste the following SQL to set up your schema:
```sql
CREATE DATABASE IF NOT EXISTS Library_DB;
USE Library_DB;
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    year_published INT,
    isbn VARCHAR(20) UNIQUE,
    category_id INT,
    status VARCHAR(50) DEFAULT 'Available',
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);
CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    student_card_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    membership_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'Active'
);
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);
```
Alternatively, save the SQL commands above in a file (e.g., `init.sql`) and run:
```sh
docker cp init.sql test-db:/init.sql
docker exec -it test-db mysql -u root -p Library_DB < /init.sql
```
---
## 🏃 Running the Java Library Management System
1. **Compile your Java files** (if not already compiled):
   ```sh
   javac -cp ".;mysql-connector-j-9.3.0.jar" LibraryManagementSystem.java
   ```
2. **Run the main class**:
   ```sh
   java -cp ".;mysql-connector-j-9.3.0.jar" LibraryManagementSystem
   ```
   - On **Linux/macOS**, use `:` instead of `;` in the classpath:
     ```sh
     java -cp ".:mysql-connector-j-9.3.0.jar" LibraryManagementSystem
     ```
   - Ensure `mysql-connector-j-9.3.0.jar` is in the same directory as your `.class` files or provide the correct path.
---
## 🗂️ Database Schema
- **categories**: Stores book categories.
- **books**: Stores book details and links to categories.
- **members**: Library member data.
- **transactions**: Tracks book lending/borrowing activity.
---
## 📝 License
This project is provided for educational purposes.
---

Feel free to contribute or open issues!
