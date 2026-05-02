# SPCC Compiler Pipeline — System Report

## 1. Project Overview
This project is an educational visualization of a **multi-stage compiler pipeline**. It demonstrates how high-level source code is transformed through various phases into machine-executable instructions, providing full transparency into internal tables and intermediate representations.

### Problem Statement
Modern compilers are often "black boxes" that take source code and produce binaries without explaining the intermediate steps. This project solves that by visualizing:
- **Macro Processing** (Two-pass expansion)
- **Intermediate Representation** (Three-Address Code)
- **Code Optimization** (Constant Folding)
- **Assembly Translation** (Targeting a Virtual ISA)

---

## 2. System Directory Structure
```text
spcc-compiler-pipeline/
├── src/main/java/com/example/pipeline/
│   ├── controller/      # REST Endpoints (PipelineController)
│   ├── model/           # Data structures (StageResponse, ErrorInfo)
│   ├── processor/       # CORE LOGIC
│   │   ├── MacroProcessor.java    # MNT/MDT Logic
│   │   ├── MiniCompiler.java      # IR & Optimization
│   │   └── MiniAssembler.java     # Assembly Generation
│   └── service/         # Orchestration (PipelineService)
├── src/main/resources/
│   └── static/          # FRONTEND (HTML/CSS/JS)
│       └── index.html   # Main Dashboard
├── system_architecture_report.md  # Detailed Technical Doc
├── README.md            # Setup & Usage
└── pom.xml              # Project Dependencies (Spring Boot)
```

---

## 3. How It Works (Stage by Stage)

### Stage 01: Macroprocessor
- **Logic**: Performs a two-pass expansion.
- **Tables**:
    - **MNT (Macro Name Table)**: Stores macro names, number of arguments, and pointer to MDT.
    - **MDT (Macro Definition Table)**: Stores the actual body of the macro with positional parameters (e.g., `&A`, `&B`).
- **Output**: Expanded source code where all `@CALL` statements are replaced by macro bodies.

### Stage 02: Mini Compiler (IR & Optimization)
- **Syntax Analysis**: Validates keywords like `VAR`, `SET`, `OUT`.
- **Optimization**: Performs **Constant Folding**. If a variable is assigned a static calculation (e.g., `X = 10 + 20`), it is simplified to `X = 30` during IR generation.
- **IR Generation**: Produces **Three-Address Code (3AC)** using temporaries (e.g., `t1 = X + Y`).

### Stage 03: Mini Assembler
- **Translation**: Maps IR lines to a **Virtual ISA**.
- **Instructions**:
    - `LDI`: Load Immediate (Constant to Register)
    - `LDR`: Load Register (Memory to Register)
    - `STR`: Store Register (Register to Memory)
    - `ADD/SUB/MUL/DIV`: Arithmetic operations
- **Registers**: Uses a pool of registers (`R1`, `R2`, etc.) to minimize memory access.

### Stage 04: Execution Timeline
- **Narrative**: Provides a step-by-step evolution of variable values.
- **Visibility**: Shows exactly when `X` was updated or when a temporary computation occurred.

---

## 4. Technical Specifications
- **Backend**: Java 17, Spring Boot 3.x
- **Frontend**: Vanilla JS, CSS (Glassmorphic inspiration), CodeMirror (IDE integration)
- **Database**: SQLite (Run History persistence)
- **Deployment**: Dockerized for cross-platform execution
