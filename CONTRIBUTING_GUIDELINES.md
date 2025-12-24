# Contributing to SleepyShop

Thank you for your interest in contributing to SleepyShop! We appreciate your time and effort in helping improve this plugin.

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle (included via wrapper)
- A Paper server (1.21+) for testing
- Git for version control

### Setting Up Your Development Environment

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/sleepyshop.git
   cd sleepyshop
   ```
3. Build the project:
   ```bash
   ./gradlew build
   ```
4. The compiled plugin will be in `build/libs/`

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue on GitHub with:

- A clear, descriptive title
- Steps to reproduce the issue
- Expected vs. actual behavior
- Server version and plugin version
- Any relevant error messages or logs

### Suggesting Features

Feature suggestions are welcome! Please open an issue with:

- A clear description of the feature
- Why this feature would be useful
- Any implementation ideas you might have

### Pull Requests

1. Create a new branch for your feature or fix:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following our coding standards

3. Test your changes thoroughly on a Paper server

4. Commit your changes with clear, descriptive commit messages:
   ```bash
   git commit -m "Add: description of your changes"
   ```

5. Push to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

6. Open a Pull Request on GitHub with:
   - A clear description of what your PR does
   - Any related issue numbers (e.g., "Fixes #123")
   - Screenshots or examples if relevant

## Code Standards

### Java Code Style

- Use 4 spaces for indentation (no tabs)
- Follow standard Java naming conventions
- Write clear, self-documenting code with comments where necessary
- Keep methods focused and concise
- Use meaningful variable and method names

### Database Operations

- All database operations should be asynchronous
- Use proper error handling and logging
- Test database changes with SQLite

### Plugin Compatibility

- Ensure compatibility with Paper 1.21+
- Test with different server configurations when possible
- Avoid breaking changes to existing functionality

## Testing

Before submitting your PR:

- Test on a Paper server (1.21+)
- Verify that existing features still work
- Test edge cases and error conditions
- Check that the plugin loads and unloads cleanly

## Communication

- Be respectful and constructive in all interactions
- Ask questions if something is unclear
- Be patient while waiting for reviews

## License

By contributing to SleepyShop, you agree that your contributions will be licensed under the BSD-2-Clause License.

---

Thank you for contributing to SleepyShop! Your help makes this project better for everyone. 💤✨
