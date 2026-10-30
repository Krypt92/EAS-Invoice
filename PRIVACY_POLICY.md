# Privacy Policy for EAS-Invoice & Quote Generator

**Application Name:** EAS-Invoice & Quote Generator (Remix EAS Invoice)  
**Package Identifier:** `com.aistudio.easinvoice`  
**Developer:** Kryptic DSD  
**Developer Contact:** `krypticdsd@gmail.com`
**Effective Date:** October 30, 2026  

---

### Core Commitment: 100% On-Device & Zero Server Collection

> **Summary:** EAS-Invoice & Quote Generator operates completely offline on your device. We do NOT collect, transmit, inspect, store, or sell any personal or business data to external servers. All clients, invoices, quotes, products, company settings, and banking details are stored exclusively in your device's local database.

---

## 1. Introduction

This Privacy Policy governs your use of the mobile software application **EAS-Invoice & Quote Generator** ("Application"), created and maintained by **Kryptic DSD**. 

This policy is designed to comply with:
- **Google Play Developer Program Policies** (User Data Policy and Data Safety requirements)
- **General Data Protection Regulation (GDPR)** (Regulation (EU) 2016/679)
- **Protection of Personal Information Act (POPIA)** (Act No. 4 of 2013, South Africa)
- **California Consumer Privacy Act (CCPA / CPRA)**

---

## 2. Information Handled by the Application

### A. Data Stored Strictly On-Device:
When you use EAS-Invoice & Quote Generator, all data entered remains within the application's local SQLite database:
1. **Client Records:** Contact names, business names, addresses, phone numbers, email addresses, and VAT/registration numbers.
2. **Catalog & Line Items:** Item descriptions, standard unit prices, discounts, and tax rates.
3. **Invoices & Quotes:** Generated documents, document numbers, issued dates, due dates, itemized amounts, totals, and notes.
4. **Company & Banking Details:** Your company name, address, tax numbers, logo, bank name, account holder, account number, branch code, and payment reference notes.
5. **Snapshots & Local Backups:** Encrypted or standard JSON database snapshots saved locally on your device storage.

### B. Data NOT Collected by Developer:
- **No Remote Transmission:** None of the above data is ever uploaded or transmitted to Kryptic DSD or any remote servers.
- **No Personal Identifiers:** We do not collect names, email addresses, IP addresses, Android Advertising IDs, or hardware identifiers.
- **No Third-Party Analytics:** The application does not contain analytics or telemetry SDKs (such as Firebase Analytics, Facebook SDK, Mixpanel, or AppsFlyer).
- **No Advertising SDKs:** The application is ad-free and contains no ad networks.

---

## 3. Permissions and Device Hardware Access

The Application adheres strictly to Android's principle of least privilege:

| Permission / System Feature | Usage Description |
| :--- | :--- |
| **No Dangerous Permissions** | The App does NOT request `READ_CONTACTS`, `CAMERA`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION`, or broad storage permissions. |
| **Android Photo Picker (`PickVisualMedia`)** | When selecting your company logo, the app uses Android's zero-permission photo picker. The app only accesses the specific image file selected by you. |
| **File Storage Access Framework (SAF)** | When importing or exporting backup JSON files, the app uses Android's system file picker without requiring broad filesystem permissions. |
| **Android FileProvider** | When sharing generated PDF invoices or CSV spreadsheets (e.g. via WhatsApp, Gmail, or Google Drive), temporary read permission is granted only to the specific recipient app chosen by you. |

---

## 4. Google Play Data Safety Declarations

For compliance with Google Play's **Data safety section** in Google Play Console:

- **Data collection:** **No** (The app does not collect user data).
- **Data sharing:** **No** (The app does not share user data with third parties).
- **Data encrypted in transit:** **Not Applicable / Yes** (No data is transmitted over the internet; local file transfers use secure OS providers).
- **Data deletion requests:** **Yes** (Users can directly edit or delete all data inside the app, or wipe data entirely via Android Application Settings).

---

## 5. Security & Encryption

- **Operating System Sandboxing:** Your data is isolated in the Android application sandbox and is not accessible to other third-party apps on the device.
- **Optional AES Backup Encryption:** When exporting full database backups, you have the option to encrypt the file using military-grade AES (Advanced Encryption Standard) with a password of your choice.
- **User Device Security:** Because all data resides locally on your device, we strongly encourage users to protect their devices with strong screen locks (PIN, password, fingerprint, or biometric authentication).

---

## 6. User Rights, Data Retention & Right to Erasure

- **Retention:** Data remains stored locally on your device for as long as you keep the app installed or until you explicitly delete it.
- **Right to Erasure (Deletion):** You can delete any individual client, product, document, or snapshot from within the app. You can permanently erase all records by clearing application data in Android:  
  `Settings > Apps > EAS-Invoice & Quote Generator > Storage > Clear Data`, or by uninstalling the application.
- **Data Portability:** You can export your data at any time via the "Export Backup JSON" and "Export CSV Spreadsheet" features in Settings.

---

## 7. Legal Frameworks

- **South Africa (POPIA):** You are the **Responsible Party** under the Protection of Personal Information Act (Act 4 of 2013) for the information of your clients entered into the app. Kryptic DSD does not process or hold this information.
- **European Union (GDPR):** All data processing is strictly local on the user's hardware. Kryptic DSD does not act as a Data Controller or Data Processor.
- **United States (COPPA):** The application is a business tool not directed at children under the age of 13. We do not knowingly collect personal information from children.

---

## 8. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. Any revisions will be reflected in an updated "Effective Date" at the top of this document, and the updated policy will always be available both inside the app and online.

---

## 9. Contact Information

If you have questions about this Privacy Policy or require support, please contact:

**Kryptic DSD**  
Email: [krypticdsd@gmail.com](mailto:krypticdsd@gmail.com)  

Package: `com.aistudio.easinvoice`
