# Matale SafeRide – School Transport Tracking Application

An Android Studio application prototype developed to address school transportation tracking and communication challenges in the Matale area.

## Project Overview

Matale SafeRide is a mobile school-transport tracking prototype designed to improve communication between school-transport drivers and parents.

The prototype focuses on problems such as:

- Lack of real-time vehicle visibility
- Repeated driver phone calls
- Uncertainty about child pickup and drop-off
- Delayed communication during traffic delays
- Communication during emergencies

The application provides two user roles: **Driver** and **Parent**.

## Key Features

### Driver Features

- Driver login
- Driver registration
- Start and end school trips
- View simulated live route
- Child pickup and drop-off status
- Send delay notifications
- Send emergency alerts
- View transport status

### Parent Features

- Parent login
- Parent registration
- View school vehicle route
- View child transport status
- View estimated arrival
- View driver and vehicle information
- Receive delay notifications
- Receive emergency alerts

## Live Route Simulation

The prototype includes an API-key-free simulated Matale route with an animated school-van marker.

The simulated map is designed for reliable demonstration during the project viva.

For a production deployment, the simulated map can be replaced with real GPS tracking and mapping services.

## Safety Features

The application includes:

- Child pickup confirmation
- Child drop-off confirmation
- Delay notifications
- Emergency incident alerts
- Breakdown alerts
- Accident alerts
- Medical emergency alerts
- Unsafe road condition alerts

## Application Flow

### Driver Flow

```text
Login
  ↓
Start Trip
  ↓
Live Location
  ↓
Child Pickup
  ↓
Child Drop-off
  ↓
Delay / Emergency Alert
  ↓
End Trip
