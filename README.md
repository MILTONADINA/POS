# Java Point-of-Sale System (POS)

A fully functional, modular Point-of-Sale (POS) application developed in Java, showcasing practical implementation of object-oriented principles, GUI development, and data-driven transaction processing. Designed to simulate real-world retail operations including item management, payment processing, and session handling.

---

## 🧠 Key Skills Demonstrated

- **Object-Oriented Design (OOP):** Inheritance, abstraction, composition, aggregation, interfaces
- **Java GUI (Swing):** Form-based input, panels, event handling with `ActionListener`s
- **File I/O and Data Parsing:** CSV ingestion with error-handling and structured memory loading
- **Design Patterns:** Modular MVC-style separation across `PD`, `DM`, and `UI` packages
- **Unit Testing:** Functional and data-layer testing with robust test classes
- **Business Logic Modeling:** Cash, check, and credit payment types; tax calculation; price promotions

---

## 🧱 Tech Stack

- **Language:** Java SE
- **GUI:** Java Swing
- **Testing:** JUnit-style unit tests
- **Build Tools:** Manual via IDE or terminal
- **Date Utility:** LGoodDatePicker JAR (for GUI date input)
- **Data Format:** CSV

---

## 🗂️ Project Structure Overview

src/
├── POSPD/ → Domain models (Item, Sale, Cashier, Payment, Tax)
├── POSDM/ → Data management (CSV parser, in-memory store)
├── POSUI/ → Full-featured GUI with real-time interaction
├── POSTests/ → Functional and integration test suites




---

## 🚀 How to Run

### Option 1: Using Eclipse or IntelliJ

1. Open the `POS` folder as a Java project
2. Add `LGoodDatePicker-10.4.1.jar` to the classpath
3. Run `Start.java` inside `POSUI`

### Option 2: Terminal

```bash
cd POS/src
javac -cp .;../LGoodDatePicker-10.4.1.jar POSUI/Start.java
java -cp .;../LGoodDatePicker-10.4.1.jar POSUI.Start
