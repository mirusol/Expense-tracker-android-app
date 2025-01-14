# Expense Tracker App

## Project Done By:
- Bilciurescu Elena - Alina
- Solomon Miruna - Maria
- Toma Daria - Maria  
**Group: 1231EA**

---

## Overview
The **Expense Tracker App** is a personal finance management application designed to help users effectively track their expenses, set and manage budgets, monitor savings goals, and access real-time stock market updates.

---

## Key Features

- **Transaction Management**: Add, view, edit, delete, and filter transactions.
- **Budget Tracking**: Set budgets, track usage, and receive notifications for thresholds.
- **Savings Goals**: Monitor progress with persistent notifications.
- **Stock Market Data**: Fetch real-time updates using the Alpha Vantage API.
- **Dark/Light Theme Toggle**: Personalize the app appearance dynamically.
- **Backup Transactions**: Automatically save transactions in the background.
- **Data Sharing**: Share the most recent transactions via external apps.
- **Notifications**: Alerts for budget thresholds, savings progress, Airplane Mode status, and stock updates.
- **Visual Insights**: View budget status and progress through dynamic bar charts.

---

## Components in the App

### **Activities**
We have used multiple activities to create distinct screens for user interaction:

- **MainActivity**: The home screen for navigation between app features.
- **AddTransactionActivity**: Allows users to add new transactions with details.
- **SetBudgetActivity**: Enables users to set and modify their budget.
- **ViewTransactionsActivity**: Displays a list of all transactions with edit/delete options.
- **ViewBudgetStatusActivity**: Provides visual budget status updates through a dynamic bar chart showing the budget and remaining balance.

### **Broadcast Receivers**
We have used a broadcast receiver to intercept `AIRPLANE_MODE_CHANGED` events and display notifications about the Airplane Mode status (ON/OFF).

### **Content Provider**
We have used a content provider to expose the most recent transactions, enabling external apps to access and share transaction summaries.

### **Foreground Services**
We have used a foreground service to monitor savings goal progress, displaying a persistent notification with the current progress.

### **Background Services**
We have used a background service (using `WorkManager`) to back up transaction data periodically without user intervention.

### **Bound Services**
We have used a bound service to handle the app's light/dark theme toggling, allowing users to switch themes dynamically.

### **Shared Preferences**
We have used shared preferences to store and retrieve user preferences, such as budget settings and the current theme.

### **SQLite Database**
We have used an SQLite database to store transaction data, including descriptions, amounts, categories, and dates, enabling efficient data querying and management.

### **External API**
We have used the Alpha Vantage API to fetch real-time stock market data for selected companies and display it within the app.

### **Notifications**
We have used notifications to alert the user about:
- Budget usage thresholds.
- Savings goal progress.
- Airplane Mode toggling.
- Stock updates through a foreground service.

---

## GitHub Link
[Expense Tracker Android App](https://github.com/mirusol/Expense-tracker-android-app)
