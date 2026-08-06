# Strict C11 Machine-Legibility Standard Plugin

A lightweight, zero-dependency POSIX shell linter and testing framework designed to enforce strict C11 compliance and machine-legibility standards across C codebases.

## Features
- **POSIX Compliant:** Written in pure shell script (`sh`), running smoothly inside minimal Linux roots and mobile environments (like iSH on iOS).
- **Compliance Rules:** Detects deprecated or prohibited patterns (such as `gets()`, the deprecated `register` storage class, and unsafe string copies).
- **Automated CI/CD:** Integrated with GitHub Actions for automated code quality checks on every push and pull request.

## Usage
Run the test suite and compliance linter locally using the helper script:
```bash
./check

License
Distributed under the MIT License.
