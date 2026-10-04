# Contributing to StrataSearch

## Development Setup

1. **Prerequisites:** Java 21, Maven 3.8+, Node 20+
2. Clone the repo and run `./mvnw install -DskipTests`
3. `cd stratasearch-frontend && npm install && npm run dev`
4. Or use `./start.ps1` to launch both backend and frontend

## Commit Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix | When to use |
|--------|-------------|
| `feat` | New feature |
| `fix`  | Bug fix |
| `refactor` | No-behavior change |
| `test` | Tests |
| `docs` | Documentation |
| `chore` | Build / CI / tooling |
| `ui`   | Frontend visual change |
| `a11y` | Accessibility |
| `ci`   | CI/CD pipeline |

## Pull Request Guidelines

- Keep PRs focused on a single concern
- Run `./mvnw test` and `npm run build` before submitting
