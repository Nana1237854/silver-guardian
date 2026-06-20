# Migration Checklist

Use this checklist when applying the Android XML-first refactor pattern.

## Screen audit

- Confirm the entry Java file and matching layout file.
- Search for hand-built view trees in `onCreateView()` or `onCreate()`.
- Mark stable sections, repeated rows, and dynamic regions.
- Inspect nearby shared blocks and design tokens before inventing new ones.

## Layout split

- Create or clean up the page-level `fragment_*.xml` or `activity_*.xml` first.
- Keep top-level structure readable in one screenful when possible.
- Extract reusable local blocks only when they have real semantic value.
- Use `view_*.xml` for local building blocks and `item_*.xml` for list rows.

## Java cleanup

- Replace layout composition code with XML inflation.
- Add focused bind methods such as `bindHeader`, `bindControls`, `bindList`, `refresh`.
- Keep Java responsible for listeners, adapters, state changes, dialogs, permissions, and navigation.
- Do not move runtime logic into XML just to be pure.

## Naming review

- File names should be page-local and grep-friendly.
- View ids should clearly describe the page role.
- Avoid mass renames unless they unlock real clarity.

## Verification

- Run `:app:processDebugResources`.
- Run `:app:compileDebugJavaWithJavac`.
- Fix encoding or BOM issues first if `javac` reports illegal characters at file start.
- Check for missing ids, broken imports, or missing resources after extraction.

## Final report

- List the new or changed XML files.
- State what major sections moved out of Java.
- Note any behavior intentionally preserved.
- Recommend the next highest-value page to migrate.