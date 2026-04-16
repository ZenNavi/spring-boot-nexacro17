# AGENTS.md

## Project Scope
- This file applies to the entire repository.
- Runtime baseline: Spring Boot `2.7.x`, Java `8`, Maven, MyBatis, H2, Apache POI, and Nexacro client assets.
- Changes should preserve Java 8 compatibility unless the user explicitly changes the runtime target.

## Repository Layout
- `src/main/java/com/example/nexacro`: application code by layer (`config`, `controller`, `service`, `mapper`, `dto`, `excel`, `util`).
- `src/main/java/com/nexacro/xapi`: local Nexacro platform compatibility classes; treat as framework-adapter code.
- `src/main/resources/mapper`: MyBatis XML mappers.
- `src/main/resources/sql`: SQL bootstrap scripts.
- `src/test/java/com/example/nexacro`: unit and service tests.
- `nexacro`: Nexacro form and shared script assets.
- `target`: build output; do not edit generated files here.

## Working Rules
- Keep changes minimal and scoped to the user request.
- Preserve the current package and layer structure; do not move files unless required.
- Prefer fixing the root cause over adding workaround logic.
- Do not edit `target` outputs.
- When changing mapper interfaces, keep the related XML mapper in sync.
- When changing Excel export behavior, check both Java builder/service code and the Nexacro script/form usage.
- Use `javax.servlet` APIs, not `jakarta.servlet`, unless the runtime target changes.

## Code Style
- Match existing Java style and naming.
- Keep source compatible with Java 8: no `record`, switch expressions, pattern matching `instanceof`, or other post-Java-8 language features.
- Prefer constructor injection or existing Spring wiring patterns already used nearby.
- Keep controller logic thin; place business logic in `service` or `excel` helpers.
- Put reusable parsing or formatting logic in `util` only when it is genuinely cross-cutting.
- Avoid adding new dependencies unless clearly necessary.

## Validation
- Prefer targeted validation first, then broader checks if needed.
- Primary test command: `mvn test`.
- For focused checks, run `mvn -Dtest=ClassName test`.
- If behavior touches MyBatis mappings or Spring wiring, favor at least the relevant unit/service tests before finishing.

## Notes For Agents
- Search with `rg`/`rg --files` before using slower tools.
- Read large files in chunks.
- If adding repo-specific conventions later, keep this file practical and short.
