# Contributing to hLobby

Thank you for your interest in contributing. This document explains how to get
started and what we expect from pull requests.

## Development setup

1. Clone the repository.
2. Ensure you have Java 21 installed.
3. Run `./gradlew build` to verify your environment.

## Project structure

- `src/main/java/dev/hauch/hlobby` — plugin source code.
- `src/main/resources/config.yml` — default configuration.
- `src/main/resources/lang/` — bundled translations.
- `src/test/java` — unit tests.

## Coding style

- The project follows a four-space indentation style.
- All public classes and methods must have Javadoc comments.
- Prefer immutability and small, focused classes.
- Keep the plugin in English: code, comments, Javadoc and default messages.
- Do not break existing configuration files unless absolutely necessary; bump
  `ConfigManager.CONFIG_VERSION` when you do.

## Tests

Please add unit tests for new logic, especially selector strategies and
utilities. Run the full test suite before submitting:

```bash
./gradlew test
```

## Commit messages

Use clear, descriptive commit messages in English. Prefix the summary with a
conventional type such as `feat:`, `fix:`, `docs:`, `build:`, `refactor:` or
`test:`.

## Pull request process

1. Open an issue first for large changes.
2. Create a feature branch.
3. Make your changes and add tests.
4. Ensure `./gradlew build` passes.
5. Submit the pull request with a clear description.

## Code of conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).

## License

By contributing, you agree that your contributions will be licensed under the
[MIT License](LICENSE).
