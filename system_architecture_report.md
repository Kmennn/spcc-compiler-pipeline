# System Architecture Report: SPCC Compiler Pipeline

## 1. Problem Statement
The complexity of modern compilers often hides the fundamental transformations that code undergoes. In an academic context (SPCC), there is a need for a tool that not only performs these transformations but also **explains** them visually. This project aims to bridge the gap between abstract theory (Macros, IR, Assembler) and practical implementation by providing a transparent, step-by-step pipeline.

---

## 2. Directory & Component Breakdown

### 📂 `com.example.pipeline.processor` (The Engine)
- **`MacroProcessor.java`**: Implements the first pass. It uses a **Two-Pass Macro Processor** logic (simplified into a single robust engine) that maintains Macro Name Tables (MNT) and Macro Definition Tables (MDT). It handles parameter substitution using positional and keyword arguments.
- **`MiniCompiler.java`**: The core "translator". It scans the expanded source code, validates syntax (Variable declarations, Assignment, Output), and performs **Constant Folding** optimizations. It produces **3-Address Code (3AC)** which is easy to map to hardware registers.
- **`MiniAssembler.java`**: The final stage. It maps the 3AC instructions to a custom **Virtual Machine ISA**. It handles register allocation (R1, R2) and generates a machine-readable representation using LDR, STR, and ADD instructions.

### 📂 `com.example.pipeline.service` (The Orchestrator)
- **`PipelineService.java`**: This is the "brain" of the application. Errors are detected immediately with line-level highlighting, ensuring the pipeline stops if a stage fails. It also constructs the **Execution Timeline**, capturing the state of variables at every step.

### 📂 `com.example.pipeline.controller` (The Gateway)
- **`PipelineController.java`**: Exposes a clean REST API (`/api/v1/pipeline/process`). It receives raw source code and returns a JSON payload containing the results of all three phases, including the full execution trace.

---

## 3. Data Flow & Logic
1.  **Input**: User enters code in the CodeMirror editor.
2.  **Phase 1 (Macro)**: `#MACRO` blocks are parsed into memory. `@CALL` statements are replaced with the expanded bodies from MDT, with parameters substituted via the Argument List Array (ALA).
3.  **Phase 2 (Compilation)**: The expanded code is tokenized. `VAR` creates entries in the Symbol Table. `SET` operations are transformed into 3-Address format (e.g., `t1 = x + y`).
4.  **Phase 3 (Assembly)**: 3-Address codes are translated into assembly instructions (e.g., `LDR R1, X`, `ADD R1, R2`, `STR Z, R1`).
5.  **Persistence**: The entire run (Input -> Result) is saved in `compiler.db` using JPA, allowing for historical analysis.

---

## 4. UI/UX Philosophy
The frontend uses an **Interactive UI** to visualize each stage of the compiler.
- **Explainable Steps**: Every line of the generated 3AC and Assembly code is interactive. Hovering over a line triggers a popup that explains what the instruction does in plain English.
- **Timeline View**: Converts the technical logs into a direct narrative of variable evolution (e.g., "X updated → 30"), making it easy to trace logic.

---

## 5. Deployment & Maintenance
- **Scalability**: The backend is built on Spring Boot, making it ready for high-concurrency environments.
- **Portability**: A `Dockerfile` is provided for containerized deployment, ensuring the pipeline runs identically on local machines, VPS, or cloud platforms like Render.
