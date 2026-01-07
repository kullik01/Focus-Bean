---
trigger: always_on
---

---
trigger: always_on
---

# 🧭 General Coding Rules (Enterprise-Grade Java/Kotlin)

## 🎯 Philosophy

All code must be **professional, production-ready, and enterprise-grade**.
Code quality must reflect the standards of a **senior software engineer** — robust, maintainable, and clear.

> **Core principle:**
> Always choose the **most professional** solution, but prefer the **simplest and most minimal** approach that fully solves the problem.

Professionalism means delivering **finished, complete solutions**, not scaffolding or drafts.

---

## 🧩 Design & Architecture Principles

* Favor **clarity, maintainability, and simplicity** over clever or overly abstract solutions.
* Avoid “smart” shortcuts — write code that future maintainers will **immediately understand**.
* **Encapsulation, immutability, and modular design** should be prioritized.
* All public APIs must be **well-documented**, **type-safe**, and **self-explanatory**.
* Respect **SOLID**, **DRY**, and **KISS** principles — apply them with engineering judgment, not dogma.
* All business logic must be separated from UI, framework, and persistence layers.
* Avoid premature optimization. Optimize **only after** profiling and data-backed validation.

---

## ⚙️ Dependency Management

* **New dependencies must not be added casually.**
  * Before introducing a new library, consider:
    * Is it actively maintained?
    * Does it add long-term value or just convenience?
    * Can the same result be achieved using the **standard library or existing codebase**?
* Prefer **first-party or Kotlin/Java standard solutions**.
* Avoid dependency overlap.
* All dependencies must have **enterprise-approved licenses**.

---

## 🧠 Code Quality & Readability

* Code must be **clean, predictable, and consistent**.
* Prefer **readable** over “clever.”
* Each function or method must do **exactly one thing**.
* Use **meaningful names** that express intent.
* Never leave commented-out code.
* Avoid magic numbers, hard-coded strings, and hidden side effects.
* Always prefer **explicitness over implicitness**.

---

## 🔍 Testing & Reliability

* Every piece of code must be **testable**.
* Unit tests are **mandatory** for business logic and critical paths.
* Use integration tests when components interact.
* Prefer **JUnit 5** (Java) or **Kotest** (Kotlin).
* Follow the **AAA pattern (Arrange – Act – Assert)**.
* Tests must be:
  * Fast
  * Deterministic
  * Self-contained
* Coverage must be **meaningful** — verify behavior, not just execution.

---

## 🧾 Documentation & Comments

* Write **KDoc** or **Javadoc** for all classes, interfaces, and functions (public and private).
* Comments must explain **why**, not **what**.
* For complex logic, include **rationale comments** explaining design decisions.
* TODOs are **not allowed** in final output.

  If future work is genuinely unavoidable, it must:
  * Be explicitly requested by the user, **and**
  * Be fully documented with context, owner, and priority

  Otherwise, the work must be completed now.

---

## 🔐 Error Handling & Resilience

* Handle errors **gracefully** — never expose raw exceptions to end users.
* Use custom exception types **only when semantically meaningful**.
* Document which exceptions can occur and under which conditions.
* Logging must be **informative, not noisy**:
  * INFO for normal operations
  * WARN for recoverable issues
  * ERROR for unexpected failures
* Never swallow exceptions. If caught, they must be logged or rethrown appropriately.

---

## 🧰 Tooling & Build Quality

* Code must compile **without warnings**.
* Prefer **Gradle Kotlin DSL**.
* Follow static analysis rules (Detekt, SpotBugs, SonarQube).
* Ensure builds are **reproducible** and dependency-locked.
* Formatting must follow `ktlint` or `spotless`.

---

## 🔒 Security & Compliance

* Never hardcode credentials, secrets, or API keys.
* Use environment variables or configuration management systems.
* Validate **all external input**.
* Ensure all network communication is **TLS-secured**.
* Follow **OWASP** secure coding practices.

---

## ✅ Task Completion & Definition of Done (CRITICAL)

All work must be **fully implemented, feature complete, and production-ready**.

### 🚫 Strictly Prohibited
The following are **not allowed** in final output:
* `TODO`, `FIXME`, `XXX`, or placeholder comments
* Stub methods or unimplemented branches
* Pseudocode where real code is possible
* “Assume this exists” or deferred logic
* Missing validation, error handling, or tests
* Partial implementations of required features

### ✅ Definition of Done
A task is complete **only if**:
* All requirements are fully implemented
* All logic paths are handled
* All public APIs are documented
* All private members and methods are documented
* All edge cases are addressed
* All business logic has unit tests
* Code compiles cleanly
* No placeholders, stubs, or TODOs remain

### 🛑 Incomplete Requirements
If any requirement **cannot be completed** due to missing information:
1. Clearly state what is missing
2. Propose concrete options or assumptions
3. Ask for clarification **before producing incomplete code**

---

## 🔍 Mandatory Self-Verification

Before finalizing any response, verify:
* There are **no TODOs, stubs, or placeholders**
* The implementation is **feature complete**
* The result would be acceptable as a **final production pull request**

If any of these checks fail, the solution must be revised.

---

## 🧠 Professional Expectations

* Think and act like a senior engineer submitting a **final PR**, not a draft.
* Do not defer work to future refactors or follow-ups.
* Deliver **maintainable, long-lived systems**, not quick fixes.

---

### ✅ Summary

> **Professionalism means balance.**
> Deliver **complete, enterprise-quality solutions** — robust, secure, tested, and documented —
> while keeping them as **simple as possible**, but **never simpler than correctness allows**.
