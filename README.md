# Course Management Mobile Application

A full-stack mobile course management system developed using Spring Boot and Android, supporting JWT authentication, role-based access control, Redis-powered caching, and asynchronous media upload workflows integrated with RabbitMQ and Firebase Storage.
## Tech Stack

### Backend
- Spring Boot
- Spring Security
- JWT Authentication
- RESTful API Development

### Database
- MySQL
- Oracle Database

### Infrastructure
- Redis Caching
- RabbitMQ Message Broker
- Firebase Storage
- Docker

### Mobile
- Android Java

### Version Control
- Git & GitHub

## Demo Mobile Screen 
<table align="center">
  <tr>
    <td align="center">
      <img src="docs/images/login.png" width="220"/><br/>
      <b>Login</b>
    </td>
    <td align="center">
      <img src="docs/images/user_profile.png" width="220"/><br/>
      <b>User Profile</b>
    </td>
    <td align="center">
      <img src="docs/images/dashboard.png" width="219"/><br/>
      <b>Dashboard</b>
    </td>
     <td align="center">
      <img src="docs/images/course_detai.png" width="220"/><br/>
      <b>Course Details</b>
    </td>
  </tr>
</table>

## System Flow Architecture

![Architecture](docs/images/system_flow.png)

## ERD Diagram

![ERD](docs/images/erd.png)

## Use Case Diagram

![Use Case](docs/images/usecases.png)

## Key Features

- JWT-based authentication & role-based authorization
- Redis caching for API optimization
- Asynchronous media upload using RabbitMQ
- Firebase Storage integration
- RESTful API design
- Global exception handling
- DTO mapping with MapStruct
- Dockerized backend services
