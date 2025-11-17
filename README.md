# InvoiceApp

A native Android application focused on displaying and filtering invoice data, created to showcase clean architecture, MVVM-based presentation logic, and good development practices rather than complex business functionality.

> **Last Updated:** November 2025  

---

## Table of Contents

- [Overview](#overview)
- [Visual Showcase](#visual-showcase)
- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [Use Case Diagram](#use-case-diagram)
- [Tech Stack](#tech-stack)
- [Lessons Learned](#lessons-learned)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

This project is a small Android app that loads a list of invoices and allows users to filter them by status, date range, and amount.  
The main goal of the project is to demonstrate modern Android development practices (MVVM, separation of concerns, repository pattern) and a production-like structure suitable for a professional environment or portfolio.  

---

## Visual Showcase

<img width="1402" height="852" alt="Image" src="https://github.com/user-attachments/assets/5f4f54a9-08bb-4572-b915-69a97416bda1" />

The showcase highlights the core flow:

1. Main screen with data source selection (Retrofit vs Retromock).  
2. Invoice list screen with loaded data.  
3. Filter panel with statuses, date range, and amount range.  

---

## Features

- Display a list of invoices using `RecyclerView` and a custom `InvoiceAdapter`.  
- Filter invoices by:
  - Status (paid, pending payment, cancelled, fixed fee, payment plan, etc.).  
  - Date range using date pickers in `dd/MM/yyyy` format.  
  - Amount range using a `RangeSlider` with a dynamic maximum value based on the data.  
- Toggle between **real API data (Retrofit)** and **mocked data (Retromock)** from the main screen.  
- Edge-to-edge layout with proper handling of system bars.  

---

## Architecture

The project follows a layered design with a focus on clarity, testability, and maintainability.

### Presentation Layer (UI + ViewModel)

- Pattern: **MVVM (Model–View–ViewModel)**.  
- Responsibilities:
  - UI logic and state management.  
  - Observing `LiveData` and updating views.  
- Main components:
  - `MainActivity`: entry point, navigation to the invoice list, and toggle between Retrofit and Retromock.  
  - `InvoiceListActivity`: displays the invoice list, observes `LiveData<List<Invoice>>`, and integrates filtering.  
  - `FilterFragment`: panel to select filter criteria (status, dates, amount).  

### Domain Layer (Use Case)

- `GetInvoicesUseCase`:
  - Encapsulates the logic to request invoices from the repository.  
  - Hides the details of whether data comes from a real API or mocked responses.  

### Data Layer (Repository + API Clients)

- `InvoiceRepository`:
  - Implements the Repository pattern to abstract data sources.  
  - Decides whether to use Retrofit or Retromock based on a flag (`useMock`).  
- `ApiService` + `RetroFitClient`:
  - Handle real HTTP requests to the remote API.  
- `RetromockClient`:
  - Provides mocked responses for local development, demos, or offline use.  

This structure helps keep the code decoupled, easier to evolve, and prepared for unit testing.

---

## Installation

### Prerequisites

- Android Studio (Giraffe or newer recommended).  
- JDK 8 or higher (managed by Android Studio).  
- Android device or emulator with a recent Android version.  

### Steps

1. **Clone the repository:**

```
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

2. **Open the project in Android Studio:**

- Choose “Open an Existing Project”.  
- Select the project folder.  

3. **Sync Gradle and build:**

- Android Studio will automatically download dependencies.  
- Wait until the sync and initial build finishes.  

4. **Run the app:**

- Select a device or emulator.  
- Click **Run** ▶️ in Android Studio.  

---

## Usage

### Basic Flow

1. **Launch the app** to open `MainActivity`.  
2. **Toggle data source**:
- Use the toggle button to switch between **Retrofit (real API)** and **Retromock (mock data)**.  
- The current mode is passed to the next screen via an intent extra (`USERETROMOCK`).  
3. **Open the invoice list**:
- Tap the button to navigate to `InvoiceListActivity`.  
- The app loads invoices through `InvoiceViewModel` → `GetInvoicesUseCase` → `InvoiceRepository`.  

### Filtering Invoices

1. In the invoice list, open the filter panel from the toolbar/menu.  
2. In `FilterFragment`, configure:
- **Status** checkboxes (paid, pending payment, cancelled, fixed fee, payment plan…).  
- **Date range** using the “from” and “until” buttons (date pickers).  
- **Amount range** using the `RangeSlider` (min and max).  
3. Tap **Apply**:
- The fragment sends the filter data back to `InvoiceListActivity` via a `Bundle`.  
- The activity applies the filters and updates the list if there are results.  
4. Optionally, tap **Reset** in the filter panel to restore default values (status, dates, and slider).  

---

## Use Case Diagram

### Conceptual Use Cases

- Open app.  
- Select data source (Retrofit / Retromock).  
- View invoice list.  
- Refresh invoices.  
- Open filter panel.  
- Filter invoices by status.  
- Filter invoices by date range.  
- Filter invoices by amount range.  
- Apply filters.  
- Reset filters.  
- View filtered results.  

### Use Case Summary

| Use Case                     | Description                                                                 |
|-----------------------------|-----------------------------------------------------------------------------|
| Open app                    | The user launches the app and reaches the main screen.                     |
| Select data source          | The user chooses between real API (Retrofit) or mock data (Retromock).     |
| View invoice list           | The user sees the loaded invoices in a `RecyclerView`.                     |
| Refresh invoices            | The user triggers a reload of invoices from the repository.                |
| Open filter panel           | The user opens the `FilterFragment` from the toolbar/menu.                 |
| Filter by status            | The user selects one or more invoice statuses.                             |
| Filter by date range        | The user sets start and end dates via date pickers.                        |
| Filter by amount range      | The user adjusts the amount range using a `RangeSlider`.                   |
| Apply filters               | The user applies all selected filter criteria.                             |
| Reset filters               | The user clears filters and restores default values.                       |
| View filtered results       | The user sees only invoices matching the chosen criteria.                  |

If you want a UML version, you can generate a diagram from a textual spec (for example, with PlantUML) based on this table.

---

## Tech Stack

![Java](https://img.shields.io/badge/Java-007396?logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Android%20Studio](https://img.shields.io/badge/Android_Studio-3DDC84?logo=android-studio&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-2962FF)
![Retrofit](https://img.shields.io/badge/Retrofit-2E7D32)
![Retromock](https://img.shields.io/badge/Retromock-8E24AA)
![Material%20Design](https://img.shields.io/badge/Material_Design_Components-757575?logo=material-design&logoColor=white)
![ViewModel](https://img.shields.io/badge/Android%20ViewModel-4285F4)
![LiveData](https://img.shields.io/badge/LiveData-FF6F00)

---

## Lessons Learned

- The benefits of **decoupled architecture**: separating UI, domain, and data layers simplifies reasoning and refactoring.  
- Robust **UI state management** using `ViewModel` and `LiveData` improves resilience to configuration changes.  
- Designing for **multiple data sources** (Retrofit vs Retromock) encourages clean abstractions and easier testing.  
- Structuring code with future testing in mind makes it easier to gradually introduce unit tests for ViewModels and use cases.  

## License

This project is currently for educational and portfolio purposes.  











