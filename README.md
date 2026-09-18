# CyberShield – Cyber Incident Management & Response System

> **Detect. Prioritize. Assign. Resolve.**

## Overview

CyberShield is a Java application that we developed as a college project to understand how a cybersecurity team could keep track of several incidents at the same time. The main focus of the project is the situation where many incidents are reported but the number of analysts and available resources is limited.

The application lets us create and manage incidents, assign their severity, find suitable analysts, keep information about affected systems and evidence, and follow the progress of a response operation.

This project is only a simulation for academic use. It is not connected to a real SOC, network, or cybersecurity monitoring system.

## Problem Statement

A security team can receive several alerts or incidents at once. Since every incident cannot be investigated immediately, the team needs a way to decide what should be handled first. The decision can depend on the severity of the incident as well as whether an analyst with the required skill is available.

CyberShield was made to simulate this process. Incidents are placed in a `PriorityQueue`, with more serious incidents being considered first. When an incident is ready to be assigned, the system checks for an available analyst who has the required skill.

## Objectives

- Allow users to report and validate cybersecurity incidents.
- Give each incident a severity level: Critical, High, Medium, or Low.
- Process pending incidents according to their priority.
- Find an available analyst with the skill required for an incident.
- Keep track of investigation and response operations.
- Store evidence connected to an incident.
- Provide a dashboard so that the current state of the system can be viewed in one place.

## Features

- **Dashboard**: Shows information about incidents and analysts so the current state of the application can be checked quickly.
- **Incident Management**: Allows incidents to be created, viewed, and processed using the priority queue.
- **Analyst Management**: Allows analysts to be added and their availability to be tracked, such as Available or On Mission.
- **Affected Systems**: Stores information about systems involved in an incident and their recovery status.
- **Evidence Management**: Allows logs, screenshots, and other incident-related artifacts to be recorded.
- **Response Operations**: Keeps track of an incident from assignment through investigation and resolution.

## Technologies Used

- Java 17
- JavaFX
- FXML
- Maven
- SQLite
- JUnit 5

## Architecture

We organized the application using an MVC/layered approach. The idea was to avoid putting everything in the JavaFX controllers and instead keep the different responsibilities separate.

- **JavaFX/FXML – Views**: Contains the screens and layouts shown to the user.
- **Controllers**: Receive user actions and handle communication between the interface and the application logic.
- **Services**: Contains the main rules of the application, including incident prioritization and analyst assignment.
- **Repository**: Handles communication with the SQLite database and database queries.

## PriorityQueue Implementation

The `PriorityQueue` is one of the main Java concepts used in CyberShield. It keeps the pending incidents in priority order.

The severity order used by the application is:

`CRITICAL > HIGH > MEDIUM > LOW`

If two incidents have the same severity, their reported time is used to decide which one should be considered first.

The `processNextIncident()` method first looks at the incident at the top of the queue. It then checks whether an available analyst with the required skill can be found. This allows the incident to be assigned without immediately removing it from the queue when a suitable analyst is not available.

## How to Run

### Requirements

Before running the project, make sure Java 17 and Maven are installed.

### Steps

1. Clone or extract the project.
2. Open a terminal inside the project directory.
3. Run the tests first:

```bash
mvn clean test
```

4. If the tests pass, start the application:

```bash
mvn javafx:run
```

## Demo Data

The application can be populated with demo data for testing. For example:

- **INC-001** – Ransomware, with **CRITICAL** severity and **Malware Analysis** as the required skill.
- **A-001** – An analyst who has the **Malware Analysis** skill.

The data used for demonstration is fictional.

## Future Enhancements

Some features that could be added in a future version are:

- Integration with a real SIEM.
- Automatic collection of security alerts.
- Role-based access control.
- Threat-intelligence integration.
- More detailed incident reports and notifications.

## Disclaimer

CyberShield is an academic simulation developed for learning and demonstration purposes. It does not connect to real cybersecurity infrastructure, monitor live networks, or perform actual network scanning.
