# Main-Project
PG Room Booking System
Overview

The PG Room Booking System is a web application designed to facilitate the booking of PG (Paying Guest) rooms. Users can browse available rooms, book rooms, make payments, and manage their bookings. The system also includes features like grievance handling, email notifications, and secure password encryption.

This project is built using Java with the Spring Boot framework, PostgreSQL for the database, and integrates Razorpay for payment processing. It also uses Java Mail Sender for email notifications and AES256 for secure password encryption.

Features

Room Management:
View available rooms.
Book a room with specific preferences (e.g., AC, food included).
Check room availability based on criteria.

User Management:
User registration and login.
Update user details (email, password, etc.).
Delete user account.

Payment Integration:
Secure payment processing via Razorpay.

Grievance Handling:
Raise grievances.
View and resolve pending grievances.

Email Notifications:
Send email notifications for booking confirmation, payment success, and grievance updates.

Security:
Password encryption using AES256.
Secure API endpoints.

Lombok:
Reduces boilerplate code with Lombok annotations.

Technologies Used:
Backend: Java, Spring Boot
Database: PostgreSQL
Payment Gateway: Razorpay
Email Service: Java Mail Sender
Password Encryption: AES256
API Testing: Postman
Code Simplification: Lombok

Prerequisites:
Before running the project, ensure you have the following installed:
Java JDK 17 or higher
Maven (for dependency management)
PostgreSQL (for database)
Postman (for API testing)
