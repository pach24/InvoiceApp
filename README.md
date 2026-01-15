# NexoSolar



A native Android application focused on displaying and filtering invoice data, created to showcase clean architecture, MVVM-based presentation logic, and good development practices rather than complex business functionality.

> **Last Updated:** January 2026 

---

## Table of Contents

- 📄 [Overview](#overview)
- 🖼️ [Visual Showcase](#visual-showcase)
- 💡 [Features](#features)
- 🏛️ [Architecture](#architecture)
- 🧩 [Modularization Strategy](#modularization-strategy)
- 💾 [Installation](#installation)
- 🚀 [Usage](#usage)
- 🛠️ [Tech Stack](#tech-stack)
- 📚 [Lessons Learned](#lessons-learned)
- ⚖️ [License](#license)

---

## 📄 Overview

Developed during a **Professional Traineeship at Viewnext**, this project demonstrates **enterprise-grade Android development** practices, bridging the gap between academic theory and **industry standards**.

The application is engineered with a focus on **Scalability** and **Maintainability**, strictly adhering to **SOLID principles** and **Clean Architecture (MVVM)**. It moves beyond simple functionality to showcase a decoupled, modular codebase ready for complex business requirements.

Key technical highlights include:
- A robust **Offline-First** strategy using **Room** as the **Single Source of Truth (SSOT)**.
- **Reactive UI** updates utilizing **LiveData** and separation of concerns.
- A flexible networking layer supporting both **Real API (Retrofit)** and **Mocking strategies** for isolated development.
- Commitment to **Code Quality** with comprehensive **Unit Testing** (JUnit/Mockito).

​

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

- **Offline-First Architecture:** Uses **Room Database** as the single source of truth.
- **Smart Loading:** Skeleton shimmer animation during data fetching.
- **Invoice Management:**
  - Robust Filtering: Status, date range, and amount.
  - Visual status indicators.
- **Installation Management:** 
  -  **Installation Details:** View technical specs of your solar setup.
  -  **Energy Monitoring:** Track self-consumption and energy generation.
- **Dual Data Source:** Toggle between **Real API (Retrofit)** and **Mock Data (Retromock)** instantly via a UI switch.
- **Quality Assurance:** Unit Tests for Domain and ViewModels.


---

## 🏛️ Architecture

The project follows a strict **Clean Architecture** approach combined with **MVVM (Model-View-ViewModel)**. This ensures a unidirectional data flow, adherence to **SOLID principles**, and high testability by decoupling the business logic from the Android framework.

### 📱 Presentation Layer (UI + ViewModel)
- **Pattern:** MVVM. The View (Activities/Fragments) observes the ViewModel and reacts to state changes.
- **State Management:** Uses `LiveData` to propagate data reactively to the UI.
- **Dependency Injection:** ViewModels (`InvoiceViewModel`, `InstallationViewModel`) receive dependencies via a Factory, preventing tight coupling with repositories.

### 🧠 Domain Layer (Business Logic)
- **Pure Java Module:** Completely isolated from the Android SDK.
- **Use Cases:** Encapsulate specific business rules (e.g., `GetInvoicesUseCase`, `FilterInvoicesUseCase`). They orchestrate the flow between the Repository and the Presentation layer.
- **Testability:** Being pure Java, this layer is tested continuously with fast-running JUnit tests.

### 💾 Data Layer (Repository + Sources)
- **Repository Pattern:** Acts as a mediator that abstracts the origin of the data.
- **Single Source of Truth (SSOT):** The app prioritizes **Room Database** for data retrieval, ensuring total offline capabilities.
- **Dual Network Strategy:** 
  - **Retrofit:** For production-grade HTTP requests.
  - **Retromock:** For simulating backend responses during development or demos.

---

## 🧩 Modularization Strategy

The application is strictly modularized to enforce separation of concerns, scalability, and faster build times:

- **`app`**: The **Presentation Layer**. Contains Activities, Fragments, ViewModels, and Dependency Injection setup. It depends on all other modules.
- **`domain`**: The **Business Logic Layer**. A pure Java module containing Use Cases, Domain Models, and Repository Interfaces. It has **zero Android dependencies**.
- **`data`**: The **Data Layer**. Responsible for the Repository implementation and local persistence (Room Database). It orchestrates data fetching strategies.
- **`data-retrofit`**: The **Network Layer**. Dedicated module for API communication, containing Retrofit service definitions, DTOs, and Retromock client implementation.
- **`core`**: **Shared Utilities**. Contains common extensions, helper classes, and constants used across the entire application.


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

You can download the latest APK of **NexoSolar** from the releases section:

- **Version:** v1.0.0 – Initial Demo Release  
- **Download:** [app-release.apk](https://github.com/pach24/InvoiceApp/releases/download/v1.0.0/app-release.apk)

> ⚠️ Make sure to allow installation from **unknown sources** on your Android device before installing.

---

## 🚀 Usage

### Basic Flow

1. **Launch the app** to open `MainActivity`.
2. **Select Data Source**:
   - Use the toggle switch to choose between **Retrofit (Real API)** and **Retromock (Mock Data)**.
   - This preference controls the data source for the entire session.
3. **Enter Smart Solar Dashboard**:
   - Tap the enter button to navigate to `SmartSolarActivity`.
   - Here you can access the new monitoring features:
     - **🏠 Installation:** View technical details and status of your solar setup.
     - **⚡ Energy:** Monitor real-time self-consumption and energy generation.
4. **View Invoices**:
   - Navigate to the **Invoices** section (launches `InvoiceListActivity`).
   - The app loads bills through `InvoiceViewModel` → `GetInvoicesUseCase` → `InvoiceRepository` (using Room for offline cache).

### Filtering Invoices

1. In the invoice list (`InvoiceListActivity`), open the filter panel from the toolbar/menu.
2. In `FilterFragment`, configure your criteria:
   - **Status:** Select checkboxes (Paid, Pending, Cancelled, Fixed Fee, Payment Plan...).
   - **Date Range:** Use the "From" and "Until" date pickers to set a period.
   - **Amount Range:** Adjust the `RangeSlider` to set minimum and maximum amounts.
3. Tap **Apply**:
   - The fragment sends the filter parameters back to the activity via a `Bundle`.
   - The list updates instantly to show only matching invoices.
4. **Reset:** Tap the "Reset" button in the filter panel to clear all filters and restore the full list.


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

- **Single Source of Truth:** Implementing **Room** as the cache layer taught me that the UI should never observe the network directly. By observing the database, the app remains responsive and fully functional offline.
- **Modularization Discipline:** keeping the `domain` module as a pure Java library (no Android dependencies) forced me to write cleaner, decoupled code that is strictly focused on business rules.
- **Abstraction Power:** Designing the toggle between **Retrofit and Retromock** demonstrated the value of coding against interfaces. It allows the app to be testable and demos to run without relying on a stable internet connection or backend.
- **Unit Testing:** Writing tests for the `InvoiceViewModel` helped verify that the complex filtering logic (dates, amounts, status) works correctly without needing to run the app on a device constantly.

## ⚖️ License

This project is currently for educational and portfolio purposes.  











