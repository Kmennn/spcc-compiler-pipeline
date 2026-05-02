# Viva Voce & Demonstration Guide

## 1. The 2-Minute Pitch (Perfect for Examiners)
> "Good morning/afternoon. My project is a **Compiler Execution Pipeline** designed to visualize the internal mechanics of a language processor. 
> 
> Unlike standard compilers, this system exposes the 'unseen' phases. It starts with a **Two-Pass Macroprocessor** using MNT and MDT tables. Then, it moves to the **Compilation phase** where it performs **Constant Folding optimization** before generating **Three-Address Code (IR)**. 
> 
> Finally, it translates that IR into **Assembly instructions** using a Register-based ISA and provides a **Value Evolution Timeline** to narrate the execution. The entire system is built with a Spring Boot backend and an interactive, responsive frontend for real-time visualization."

---

## 2. Demonstration Workflow
1.  **Input**: Enter a simple program with a macro (e.g., the default `ADD` macro).
2.  **Execute**: Click "Execute Pipeline".
3.  **Step 1**: Show the **Macro Expansion**. Point out how `@CALL` was replaced.
4.  **Step 2**: Show the **IR View**. Highlight a temporary variable (e.g., `t1`).
5.  **Step 3**: Open the **Explanation View** in the Assembly stage to show the plain-English translation of `LDR/STR` instructions.
6.  **Step 4**: Show the **MNT/MDT Tables** to prove the two-pass logic.
7.  **Step 5**: Show the **Timeline** to narrate how the final result was reached.

---

## 3. Top 5 Expected Questions & Answers

### Q1: What is the difference between MNT and MDT?
**A**: MNT (Macro Name Table) is a directory that stores the macro's name and where its definition starts. MDT (Macro Definition Table) contains the actual expanded code template with positional parameters.

### Q2: How does your compiler handle Optimization?
**A**: It performs **Constant Folding**. During the IR generation phase, the compiler checks if an expression consists only of constants. If so, it evaluates it at compile-time rather than generating code to calculate it at runtime.

### Q3: Why did you use Three-Address Code (3AC) for IR?
**A**: 3AC is a standard intermediate form that breaks complex expressions into simple instructions with at most three operands. This makes it much easier to perform optimizations and generate target assembly code.

### Q4: Explain your Assembly ISA (Instruction Set Architecture).
**A**: It is a **Load-Store Architecture**. We use `LDR` to bring values from memory to registers, perform arithmetic like `ADD` on registers, and use `STR` to save the result back to memory. This mimics how real CPUs like ARM function.

### Q5: What happens if there is a syntax error?
**A**: The **Mini Compiler** performs syntax validation. If an error is found, it populates a **Diagnostics** stage with the line number and error type, halting the pipeline to prevent invalid code execution.
