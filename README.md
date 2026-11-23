# SmartStay Hotel Booking System

A command-line hotel booking system written in Java. It was built for **CI6115 Programming III – Patterns and Algorithms** (Coursework 1) at Kingston University, delivered through ESoft Metro Campus.

SmartStay lets an administrator manage rooms and lets guests register, search for free rooms, make bookings and receive a detailed invoice. All business rules from the assessment brief are enforced.

> Student: **K.H. Nandun Lakshan De Silva** · Kingston ID **K2703399**
> Every class name is prefixed with the student ID, as the brief requires.

---

## Features

**Administrator**
- Add, update and remove rooms (rooms with active bookings are protected)
- Change room availability (Available / Reserved / Under Maintenance)
- View all rooms with their number of active bookings
- View all reservations sorted by check-in date
- View all registered guests

**Guest**
- Register with NIC/passport, full name, contact number and email
- Search available rooms by room type and date range
- Make a reservation (room type → dates → pick a free room → guests)
- Search reservations by guest name or booking ID
- View reservations by check-in date
- Modify or cancel an eligible reservation
- Generate a detailed invoice

## Business rules

| Room type | Rate / night | Breakfast | Extra guest | Tax | Base / max guests |
|---|---|---|---|---|---|
| Standard | LKR 6,000 | No | LKR 1,000 | 10% | 2 / 4 |
| Deluxe | LKR 9,000 | Yes | LKR 1,500 | 12% | 2 / 4 |
| Suite | LKR 14,000 | Yes | LKR 2,000 | 15% | 3 / 5 |
| Family Suite | LKR 18,000 | Yes | LKR 2,500 | 15% | 4 / 6 |

- Refundable booking advance of LKR 10,000
- Only rooms with status *Available* can be booked
- Bookings must be made at least 2 days before check-in
- No changes or cancellations within 24 hours of check-in
- 12% discount on the base charge for stays of 5 nights or more

**Bill order:** base (rate × nights) → + extra guest charges → − discount → + tax → − advance

## How to run

Requires **Java 11 or later** (developed and tested with Eclipse Temurin 21).

```bash
cd src
javac *.java
java K2703399_SmartStayApp
```

Eight sample rooms (R001–R008, two of each type) are loaded at start-up.

## Project structure

```
SmartStay/
├── src/
│   ├── K2703399_SmartStayApp.java      # Console UI (menus and input)
│   ├── K2703399_HotelSystem.java       # Data and all business rules
│   ├── K2703399_Reservation.java       # Booking, eligibility, invoice creation
│   ├── K2703399_Invoice.java           # Bill calculation and printing
│   ├── K2703399_Room.java              # Abstract base class for rooms
│   ├── K2703399_StandardRoom.java      # Standard package (extends Room)
│   ├── K2703399_DeluxeRoom.java        # Deluxe package (extends Room)
│   ├── K2703399_SuiteRoom.java         # Suite package (extends Room)
│   ├── K2703399_FamilySuiteRoom.java   # Family Suite package (extends Room)
│   ├── K2703399_Person.java            # Abstract base class
│   ├── K2703399_Guest.java             # Guest (extends Person)
│   ├── K2703399_Admin.java             # Administrator (extends Person)
│   ├── K2703399_RoomType.java          # Enum: room package names
│   ├── K2703399_BedType.java           # Enum
│   └── K2703399_AvailabilityStatus.java# Enum
└── docs/
    ├── SmartStay_ClassDiagram.png      # UML class diagram (export)
    ├── SmartStay_ClassDiagram.drawio   # UML class diagram (source, draw.io)
    └── SmartStay_ClassDiagram.drawio.svg # UML class diagram (SVG export)
```

## Design overview

- **Layers:** UI (`SmartStayApp`) → logic and data (`HotelSystem`) → domain classes and enums
- **OOP:** abstraction (`Person` and `Room` are abstract), inheritance (`Guest` and `Admin` extend `Person`; `StandardRoom`, `DeluxeRoom`, `SuiteRoom` and `FamilySuiteRoom` extend `Room`), encapsulation (private and protected fields, controlled updates), polymorphism (overridden `getDetails()` and room package methods, overloaded `searchAvailableRooms()` and constructors)
- **Data structures:** `ArrayList` for rooms, guests and reservations; `HashMap` for fast guest lookup by NIC/passport
- **Algorithms:** linear and hash-based search, date-range overlap check, rule validation, invoice calculation, sorting by check-in date

The class diagram is in `docs/SmartStay_ClassDiagram.drawio` and can be opened at [app.diagrams.net](https://app.diagrams.net).

## Known limitations

- Data is kept in memory only and is lost when the program closes
- Money is stored as `double`
- Date rules use the computer's system date
- One administrator and no login
- Command-line interface only

## Academic note

This repository is coursework submitted for assessment. Please do not copy it for your own coursework.
Use of generative AI tools is declared in the accompanying report.
