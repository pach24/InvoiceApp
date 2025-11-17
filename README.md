# InvoiceApp

Android application that displays a list of invoices and allows the user to filter them by status, date range, and amount.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Main Components](#main-components)
  - [MainActivity](#mainactivity)
  - [InvoiceListActivity](#invoicelistactivity)
  - [FilterFragment](#filterfragment)
  - [InvoiceViewModel](#invoiceviewmodel)
  - [GetInvoicesUseCase](#getinvoicesusecase)
- [Networking](#networking)
- [Filtering Logic](#filtering-logic)
- [How to Run](#how-to-run)
- [Possible Improvements](#possible-improvements)

---

## Features

- Display a list of invoices using a `RecyclerView` and a custom `InvoiceAdapter`.
- Filter invoices by:
  - Status (paid, pending payment, cancelled, fixed fee, payment plan).
  - Date range (from oldest date to a selected end date).
  - Amount range using a `RangeSlider`.
- Toggle between real API data (Retrofit) and mocked data (Retromock) from the main screen.
- Use of `ViewModel`, `LiveData`, and a simple clean architecture approach with Use Case and Repository.
- Full-screen UI using Edge-to-Edge with proper window insets handling.

---

## Architecture

The app follows a simple layered structure:

- **UI Layer**
  - `MainActivity`: Entry point, navigation to the invoice list and toggle between Retrofit and Retromock.
  - `InvoiceListActivity`: Displays the invoice list and opens the filter fragment.
  - `FilterFragment`: UI for selecting filter criteria (status, dates, amount).

- **Presentation Layer**
  - `InvoiceViewModel`: Holds and manages UI-related invoice data using `LiveData`.
  - `InvoiceViewModelFactory`: Creates `InvoiceViewModel` with custom parameters (`useMock`, `Context`).

- **Domain Layer**
  - `GetInvoicesUseCase`: Encapsulates the logic for fetching invoices from the repository.

- **Data Layer**
  - `InvoiceRepository`: Provides invoice data, either from Retrofit or Retromock.
  - `ApiService`: Retrofit interface for API endpoints.
  - `RetroFitClient` / `RetromockClient`: Clients that create Retrofit/Retromock instances.

- **Model**
  - `Invoice`: Data model representing an invoice (date, amount for sorting, status, etc.).

---

## Technologies Used

> You can replace the text labels below with icons (Shields badges or SVG logos), for example:
> `![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)`

- ![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
- ![Java](https://img.shields.io/badge/Java-007396?logo=java&logoColor=white)
- ![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?logo=android-studio&logoColor=white)
- ![Retrofit](https://img.shields.io/badge/Retrofit-2E7D32?logo=square&logoColor=white)
- ![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue)
- ![Material Design](https://img.shields.io/badge/Material_Design-757575?logo=material-design&logoColor=white)

(Adjust, add or remove badges depending on what you actually use: LiveData, ViewModel, Retromock, etc.)

---

## Main Components

### MainActivity

- Manages the `useMock` flag to switch between real API calls and mocked responses.
- Navigates to `InvoiceListActivity`, passing `USERETROMOCK` in the `Intent`.
- Uses Edge-to-Edge mode and adjusts paddings with `WindowInsetsCompat`.

### InvoiceListActivity

- Uses `ActivityInvoiceListBinding` for layout binding.
- Sets up a `RecyclerView` with `InvoiceAdapter` and `LinearLayoutManager`.
- Receives the `useMock` flag from `MainActivity`.
- Creates an `InvoiceViewModel` using `InvoiceViewModelFactory` and calls `cargarFacturas()` to load invoices.
- Observes `LiveData<List<Invoice>>` to update the list and enable/disable the filter menu item.
- Opens `FilterFragment` passing:
  - Maximum invoice amount.
  - Oldest invoice date.
- Applies filters using:
  - Selected statuses.
  - Date range (start and end).
  - Minimum and maximum amount.
- Provides `aplicarFiltros(Bundle)` to filter data and `restoreMainView()` to restore the main list view after closing the fragment.

### FilterFragment

- Uses `FragmentFilterBinding` for the filter UI.
- Shares `InvoiceViewModel` with the activity via `ViewModelProvider(requireActivity())`.
- Initializes:
  - Default checkboxes for frequently used statuses.
  - Default dates (oldest invoice date as start, current date as end).
  - `RangeSlider` configured with the maximum invoice amount.
- Opens a `DatePickerDialog` to pick start and end dates in `dd/MM/yyyy` format.
- Validates that the start date is not after the end date.
- Builds a `Bundle` with:
  - Selected statuses.
  - Start and end dates.
  - Min and max amounts from the slider.
- Calls `InvoiceListActivity.aplicarFiltros(bundle)` and only closes the fragment if results are found.
- Offers a reset button to clear filters and restore default values.

### InvoiceViewModel

- Holds `MutableLiveData<List<Invoice>> facturas`.
- Uses `GetInvoicesUseCase` to load invoices from the repository depending on `useMock`.
- Exposes `LiveData<List<Invoice>> getFacturas()` for the UI.
- Helper methods:
  - `getMaxImporte()`: Returns the maximum invoice amount.
  - `getOldestDate()`: Returns the earliest invoice date as a string, comparing `dd/MM/yyyy` values.

### GetInvoicesUseCase

- Depends on `InvoiceRepository`.
- Provides `execute(boolean useMock, Callback<List<Invoice>> callback)` to asynchronously fetch invoices.
- Handles success and failure and forwards the result to the caller.

---

## Networking

- Uses **Retrofit** as the HTTP client for real API calls.
- Uses **Retromock** as a mocking layer to simulate responses without a real backend.
- `ApiService` defines endpoints to retrieve invoices.
- `RetroFitClient` and `RetromockClient` configure and expose Retrofit/Retromock instances.

---

## Filtering Logic

Filtering is implemented in `InvoiceListActivity`:

- Receives selected filter values from `FilterFragment` via `Bundle`.
- Retrieves the current invoice list from `InvoiceViewModel`.
- Converts date strings to `Date` objects once to optimize performance.
- Applies filters:
  - Status: invoice status must be contained in the selected list.
  - Date range: invoice date must be between start and end dates (inclusive).
  - Amount: invoice amount must be between `importeMin` and `importeMax`.
- Updates `InvoiceAdapter` with the filtered list if there are results.

---

## How to Run

1. Clone this repository into Android Studio.
2. Sync Gradle to download all required dependencies.
3. Run the app on an emulator or a physical device with a recent Android version.
4. In the main screen:
   - Use the toggle button to switch between Retrofit and Retromock.
   - Open the invoice list.
   - Use the filter icon in the toolbar to open the filter fragment and apply filters.

---

## Possible Improvements

- Replace string-based date comparison with `LocalDate` or `java.time` API.
- Add pagination or lazy loading for large invoice lists.
- Improve error handling and user feedback for network failures.
- Extract filtering logic into a separate use case or helper class to improve testability.
- Add unit tests for `InvoiceViewModel`, `GetInvoicesUseCase`, and filtering logic.
