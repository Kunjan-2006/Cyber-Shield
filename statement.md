# CyberShield - Problem Statement

## Problem Statement

A cybersecurity team can receive a large number of alerts and incidents within a short period of time. When several incidents arrive together, the available analysts may not be enough to handle everything at once. This creates the need to decide which incident should be handled first and which analyst is suitable for the job.

If these decisions are handled manually, an important incident could be left waiting while another incident is being worked on. It can also be difficult to make sure that an analyst has the skills needed for a particular incident.

CyberShield is a college project created to simulate this situation. The application gives incidents a severity level, puts pending incidents into a priority queue, and checks the skills and availability of analysts before an incident is assigned.

## Scope

The project covers the basic incident-management flow, starting from reporting an incident and continuing through prioritization, analyst assignment, investigation, evidence tracking, and resolution.

The application is designed as a desktop simulation rather than a complete real-world security platform. Its purpose is to demonstrate Java programming, data structures, database handling, and basic incident-response concepts in one project.

## Target Users

- **Security Analysts**: Can receive incident assignments and update the progress or resolution status of their work.
- **SOC Managers**: Can use the system to view incidents, check evidence and affected systems, and get an overall view through the dashboard.

## High-Level Features

1. **Incident Reporting and Queuing** – New incidents can be entered and placed in the queue for processing.
2. **Severity-Based Prioritization** – Incidents are ordered according to their severity so that more serious incidents are considered first.
3. **Analyst Skill Matching** – The system checks the required skill of an incident and looks for an analyst who has that skill and is available.
4. **Evidence and Affected-System Tracking** – Information related to evidence and systems involved in an incident can be stored and updated.
5. **Interactive Dashboard** – The dashboard provides a quick view of the current incidents and analyst status.

## Expected Outcome

The expected result is a working desktop application built with Java and JavaFX. SQLite is used to store the application's data, while Java concepts such as `PriorityQueue` are used to implement the incident-prioritization logic.

The project should demonstrate how these concepts can be combined to create a small incident-management application and should also provide a base that can be expanded with more features later.

**Important:** CyberShield is an academic simulation. It does not connect to real networks or cybersecurity infrastructure and should not be used as an actual incident-response system.
