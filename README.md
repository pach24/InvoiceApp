# NexoSolar



A native Android application focused on displaying and filtering invoice data, created to showcase clean architecture, MVVM-based presentation logic, and good development practices rather than complex business functionality.

> **Last Updated:** January 2026 

---

## Table of Contents

- 📄 [Overview](#overview)
- 🖼️ [Visual Showcase](#visual-showcase)
- 💡 [Features](#features)
- 🏛️ [Architecture](#architecture)
- 💾 [Installation](#installation)
- 🚀 [Usage](#usage)
- 📊 [Use Case Diagram](#use-case-diagram)
- 🛠️ [Tech Stack](#tech-stack)
- 📚 [Lessons Learned](#lessons-learned)
- ⚖️ [License](#license)

---

## 📄 Overview

This project is a small Android app that loads a list of invoices and allows users to filter them by status, date range, and amount.  
The main goal of the project is to demonstrate modern Android development practices (MVVM, separation of concerns, repository pattern) and a production-like structure suitable for a professional environment or portfolio.  

---

## 🖼️ Visual Showcase

<img width="1084" height="884" alt="Image" src="https://github.com/user-attachments/assets/46060c84-848a-4473-ac0d-4dd869fbb5da" />

### App Demo

<div align="center">
<img src="https://github.com/user-attachments/assets/34b71390-8bb1-4beb-b220-61250f1019e7" width="300">

  <p><em>Quick overview of the invoice navigation and filtering flow.</em></p>
</div>

### Gallery

| Login | Main Activity | Invoice List | Skeleton Shimmer |
|:---:|:---:|:---:|:---:|
| <img width="200" alt="Image" src="https://github.com/user-attachments/assets/c07cf24f-d442-4def-a367-cc242f6905df" /> | <img width="200"  alt="Image" src="https://github.com/user-attachments/assets/ac727795-bdaf-48c0-9b48-725e1ef6c3ce" /> | <img width= "200" alt="Image" src="https://github.com/user-attachments/assets/1372326a-36bd-446f-8744-bb3725714f0e" />      | <img width="200" alt="Image" src="https://github.com/user-attachments/assets/74af082a-6f32-40ab-bf64-35b166329ccb" />
 | <img width="200" alt="Image" src="https://github.com/user-attachments/assets/c27f9aea-66c5-480a-b93f-c33c014658ea" /> | <img width="200" alt="Image" src="https://github.com/user-attachments/assets/b34e1597-b4f9-481c-8212-d9e45991cc5f" /> | <img width="200" alt="Image" src="https://github.com/user-attachments/assets/63776004-d4b9-4e49-8f32-51077922fb2e" /> | <img width="200" alt="Image" src="https://github.com/user-attachments/assets/977b6e80-6056-4f90-9246-3b6f55848dc4" />
| Filters | Date Picker Calendar | Empty State | Invoice Details View |


---

## 💡 Features

- **Offline-First Architecture:** Uses **Room Database** as the single source of truth, ensuring the app works seamlessly without internet.
- **Smart Loading:** Skeleton shimmer animation provides immediate visual feedback during data fetching.
- **Robust Filtering:** Filter by status, date range (using modern `MaterialDatePicker`), and amount.
- **State Management:**
  -  **Loading:** Shimmer effect.
  -  **Success:** RecyclerView list.
  -  **Empty:** Custom illustration and helpful text when no results are found.
  -  **Error:** Retry mechanism for network failures.
- **Dual Data Source:** Toggle between **Real API (Retrofit)** and **Mock Data (Retromock)** instantly.
- **Quality Assurance:** Comprehensive **Unit Tests** for Domain (Use Cases) and UI Logic (ViewModels).
- **Modern UI:** Material Design 3 components, rounded corners, and edge-to-edge support.



---

## 🏛️ Architecture

The project follows a **Clean Architecture** approach with **MVVM**, ensuring separation of concerns and high testability.

### Presentation Layer (UI + ViewModel)
- Pattern: **MVVM (Model–View–ViewModel)**.
- **Dependency Injection:** ViewModels receive dependencies (UseCases) via a Factory, keeping them decoupled from data sources.
- **Unit Testing:** `InvoiceViewModel` is tested using JUnit 4 and Mockito to verify filtering logic and state updates.

### Domain Layer (Use Case)
- `GetInvoicesUseCase`:
  - Pure Java logic.
  - Orchestrates data flow between the Repository and ViewModel.
  - Tested in isolation to ensure correct business rules.

### Data Layer (Repository + Room + API)
- `InvoiceRepository`:
  - Implements the **Single Source of Truth** pattern.
  - Fetches data from API (Retrofit/Retromock) and saves it to **Room Database**.
  - The UI always observes the local database, ensuring offline capability.
- `AppDatabase` (Room):
  - Local persistence for invoices.
  - Handles complex types like `LocalDate` using TypeConverters.

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

## 💾 Installation

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

## 🧪 Testing Strategy

The project includes a robust suite of Unit Tests ensuring the reliability of business logic and view states:

- **Domain Layer Tests:** Verify that `GetInvoicesUseCase` correctly interacts with the Repository.
- **ViewModel Tests:** Ensure `InvoiceViewModel` correctly filters data based on user input (Dates, Amounts, Status) and handles the initial data load.
- **Tools Used:** 
  - **JUnit 4:** Test framework.
  - **Mockito:** For mocking dependencies and verifying interactions.
  - **InstantTaskExecutorRule:** For testing LiveData synchronously.

---

## 💾 Download / Release

You can download the latest APK of **InvoiceApp** from the releases section:

- **Version:** v1.0.0 – Initial Demo Release  
- **Download:** [app-release.apk](https://github.com/pach24/InvoiceApp/releases/download/v1.0.0/app-release.apk)

> ⚠️ Make sure to allow installation from **unknown sources** on your Android device before installing.

---

## 🚀 Usage

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

## 📊 Use Case Diagram

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


---

## 🛠️ Tech Stack

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Room](https://img.shields.io/badge/Room_Database-42A5F5?logo=sqlite&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-2962FF)
![JUnit](https://img.shields.io/badge/Testing-JUnit_4-25A162?logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Testing-Mockito-yellow?logo=mockito&logoColor=black)
![Retrofit](https://img.shields.io/badge/Retrofit-2E7D32)


---

## 📚 Lessons Learned

- The benefits of **decoupled architecture**: separating UI, domain, and data layers simplifies reasoning and refactoring.  
- Robust **UI state management** using `ViewModel` and `LiveData` improves resilience to configuration changes.  
- Designing for **multiple data sources** (Retrofit vs Retromock) encourages clean abstractions and easier testing.  
- Structuring code with future testing in mind makes it easier to gradually introduce unit tests for ViewModels and use cases.  

## ⚖️ License

This project is currently for educational and portfolio purposes.  











