---
name: android-xml-ui-refactor
description: Refactor Android screens to a consistent XML-first architecture. Use this whenever the user wants to unify Android UI structure, convert Fragment or Activity screens away from hand-built Java view trees, standardize layout naming, move stable page structure into XML, extract reusable view or item blocks, or enforce the pattern "page-level XML + local reusable layout pieces + Java only for binding, state, and interactions" in an Android Studio project.
---

# Android XML-First UI Refactor

Use this skill when an Android screen has drifted into mixed responsibilities and the user wants consistency, better maintainability, or easier UI polishing.

The target architecture is:
- page-level XML owns stable screen structure
- reusable `view_*.xml` and `item_*.xml` files own repeated local blocks
- Java owns data binding, screen state, event wiring, permission flow, adapters, and navigation
- verification includes Android resource processing and Java compilation

## What good looks like

A refactored screen should have these traits:
- The main page shell is inflated from `res/layout/fragment_*.xml` or `activity_*.xml`.
- Stable sections are not created with large `new LinearLayout()` or `new TextView()` trees in Java.
- Shared or repeated substructures are extracted into clearly named layout files such as `view_page_header.xml`, `view_settings_row.xml`, or `item_community_poi.xml`.
- Java code reads like a binder: `bindHeader`, `bindControls`, `bindList`, `refresh`, `showDialog`, adapter `bind(...)`.
- Naming is boring in a good way: predictable, local, and easy to grep.
- After edits, `:app:processDebugResources` and `:app:compileDebugJavaWithJavac` both pass.

## Default workflow

Follow this sequence unless the user asks for a narrower slice.

1. Inspect the current screen entry points.
2. Decide whether the page already has XML, has partial XML, or is mostly built in Java.
3. Identify stable page regions, repeated rows/cards/items, and dynamic stateful regions.
4. Create or clean up the page-level XML first.
5. Extract only genuinely repeated or semantically meaningful local blocks.
6. Rewrite Java so it binds, updates, and reacts instead of composing the full page tree.
7. Run resource and Java compilation checks.
8. Summarize what moved into XML, what stayed in Java, and what the next migration target should be.

## How to inspect a screen

Start with the screen entry points:
- `Fragment` or `Activity` class
- matching `fragment_*.xml` or `activity_*.xml`
- nearby `item_*.xml`, `view_*.xml`, adapters, and mock data providers

Look for these migration signals:
- long `onCreateView()` methods that instantiate many views directly
- repeated paddings, margins, chip styles, button styles, or cards created in Java
- Java methods that both build UI structure and bind data
- screens that have partial XML but still create major subtrees in code
- list rows that should be RecyclerView items instead of ad hoc child insertion

## Refactor rules

### 1. Move structure, not every last detail

Move stable layout structure into XML first:
- page header
- hero or summary area
- filter bars
- cards, sections, and empty states
- list containers
- primary actions

Keep in Java only what truly depends on runtime behavior:
- data text or image binding
- adapter population
- conditional visibility
- listeners and callbacks
- permission requests
- dialogs, toasts, navigation, broadcasts

### 2. Extract local reusable blocks selectively

Create a separate layout file when one of these is true:
- the same structure appears multiple times on the same screen
- the block has a clear semantic role like header, metric row, action row, record item, or POI card
- extracting it makes the page XML dramatically easier to scan

Do not extract tiny one-off wrappers just to chase abstraction.

### 3. Prefer predictable naming

Use these naming conventions by default:
- page shell: `fragment_home.xml`, `activity_main.xml`
- reusable local block: `view_home_metric_item.xml`, `view_settings_row.xml`
- list item: `item_medicine.xml`, `item_community_poi.xml`
- ids should be page-local and readable: `community_status`, `medicine_filter`, `header_title`
- Java bind methods should describe responsibility: `bindHeader`, `bindControls`, `bindList`, `refreshFilter`

Avoid broad renames unless they clearly pay for themselves.

### 4. Keep Java in binder mode

Java should mostly do this kind of work:
- inflate the page XML
- find views once
- bind labels, adapters, listeners, and callbacks
- compute visible data subsets
- update small bits of state
- open dialogs or route navigation

Java should not read like a layout engine unless there is a very good reason.

### 5. When dealing with lists

If the page renders repeated cards or rows, prefer:
- `RecyclerView`
- dedicated `item_*.xml`
- a focused adapter and holder

Adapter bind methods should map model data to view state and nothing more.

### 6. Reuse existing tokens before inventing new ones

Before adding new styles or drawables, inspect existing:
- `dimens.xml`
- `themes.xml`
- shared drawables such as cards, chips, or buttons
- existing `view_*.xml` blocks already used by sibling screens

Push the codebase toward consistency before adding new variants.

## Decision guide: what goes where

Put it in XML when it is:
- structural
- repeated
- visually stable
- easier to review as markup

Put it in Java when it is:
- computed at runtime
- stateful
- callback driven
- permission or navigation related
- adapter or dialog behavior

If unsure, ask: would a designer or reviewer want to inspect this as layout? If yes, it probably belongs in XML.

## Output expectations

When you apply this skill to a task, aim to leave behind:
- a clearer page shell XML
- zero or more extracted `view_*.xml` or `item_*.xml` files
- a slimmer Fragment or Activity class
- preserved user-visible behavior unless the user asked for redesign
- a short note on what is now standardized and what still remains to migrate

## Verification

After structural edits, run the smallest useful checks.

Default Android verification:
- `./gradlew :app:processDebugResources`
- `./gradlew :app:compileDebugJavaWithJavac`

If the environment depends on a custom `JAVA_HOME`, set it before running Gradle.

Treat these failures as common and fixable:
- BOM or encoding issues in Java files
- malformed XML
- broken ids after extraction
- missing imports after moving adapter logic
- string or drawable references that were not added

## Communication style

When reporting progress:
- say what moved into XML
- say what stayed in Java and why
- mention any new reusable block files
- mention verification results
- keep the next recommended migration target concrete

## Example trigger cases

Use this skill for prompts like:
- "Continue cleaning Android fragments into XML-first structure."
- "This screen still builds its layout in Java. Can you unify it with the rest?"
- "Move this Activity to page-level XML plus reusable include blocks."
- "Standardize naming and structure for Android UI files without over-renaming."
- "Refactor this Fragment so Java only binds data and handles state."

## Reference

For a migration checklist and review rubric, read `references/checklist.md`.