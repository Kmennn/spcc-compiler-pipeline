# 🚀 SPCC Compiler Pipeline

A transparent, visual, and explainable compiler pipeline designed for academic excellence. This project demonstrates the journey of source code from high-level macros down to virtual machine assembly, featuring a modern glassmorphic UI with real-time explainability.

---

## 🎯 Project Overview

This system implements a classic 3-stage compiler architecture tailored for the **System Programming & Compiler Construction (SPCC)** curriculum. It provides students and developers with a "X-Ray" view of how code is transformed, optimized, and executed.

### Key Phases:
1.  **Macro Processor**: Handles `#MACRO` definitions, nested expansion, and Argument List Array (ALA) substitution using MNT/MDT tables.
2.  **Mini Compiler**: Performs syntax validation, semantic analysis, and generates **3-Address Code (3AC)** Intermediate Representation (IR).
3.  **Mini Assembler**: Translates IR into a custom instruction set (ISA) targeting a virtual register machine.

---

## ✨ Features

- 🎨 **Marimba Design System**: A premium, glassmorphic UI using high-end typography (*Instrument Serif* & *Jost*).
- 🧠 **Explainable AI (XAI)**: Real-time tooltips and a "Story View" that narrate the logic behind every transformation.
- ⚡ **IntelliSense**: Custom syntax highlighting and auto-completion powered by CodeMirror.
- 📊 **Execution Timeline**: Step-by-step breakdown of state changes and macro expansions.
- 📱 **Fully Responsive**: Optimized for desktop, tablet, and mobile browsers.

---

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3.2.5, Spring Data JPA.
- **Database**: SQLite (Zero-config local persistence).
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (ES6+).
- **Libraries**: CodeMirror (Editor), Lucide (Icons), Google Fonts.
- **DevOps**: Docker, Render (Cloud Deployment).

---

## 🚀 Quick Start

### Prerequisites
- JDK 21 or higher.
- Maven 3.9+.

### Local Execution
1. Clone the repository.
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Open `http://localhost:8080` in your browser.

### Docker
```bash
docker build -t spcc-pipeline .
docker run -p 8080:8080 spcc-pipeline
```

---

## 📝 Language Specification

The pipeline supports a custom high-level assembly-like language:

```text
#MACRO INCREMENT &VAL
  SET &VAL = &VAL + 1
#MEND

VAR X = 5
@CALL INCREMENT(X)
OUT X
```

---

## 📂 Project Structure

- `src/main/java/com/example/pipeline/processor`: Core logic for Macro, Compiler, and Assembler.
- `src/main/resources/static`: Frontend assets and UI logic.
- `PipelineService.java`: Orchestration layer.
- `PipelineController.java`: REST API endpoints.

---

## 📜 License
Academic use only. Designed for SPCC curriculum demonstration.
