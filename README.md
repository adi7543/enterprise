# Enterprise-1.0

**Enterprise-1.0** is a lightweight, desktop-based business management software built using **Java Swing** and **SQLite**. It supports offline invoicing, sales and payment tracking, and includes a simple license activation system tied to a specific machine.

---

Key Features

Invoicing
- Generate invoices from pending transactions.
- Each invoice includes:
  - Customer info
  - Date
  - List of items
  - Total amount
- Auto-incremented `InvoiceID` starting from `1001`.
- PDF generation using **iText 7.5** (includes both **Original** and **Duplicate** copies).
- Auto-preview of invoice after creation.

Payments
- Record payments received from customers.
- Maintain and view outstanding balances.

Licensing System
- Requires **User ID** and **License Key** on **first run**.
- License key is **bound to the PC** (uses machine identifiers).
- Works **offline**—no need for an internet connection to activate.

SQLite Local Database
- Self-contained `.db` file located in the application directory.
- No server or installation required.
- Fast, reliable, and portable.

 Simple Swing Interface
- User-friendly GUI built with Java Swing.
- Easy navigation and quick access to features.

Installer
- Built using **Inno Setup**.
- Bundles:
  - `.exe` file
  - Embedded JRE
  - SQLite database file
  - Application icon

