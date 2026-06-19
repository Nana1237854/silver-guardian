# UI Unification Phase 1

This note tracks the first implementation batch of the XML-first UI unification work.

## Scope completed

- Shared sizing and component tokens extended in:
  - `app/src/main/res/values/dimens.xml`
  - `app/src/main/res/values/themes.xml`
- Login-page sizing aligned more closely with shared tokens in:
  - `app/src/main/res/layout/activity_login.xml`
  - `app/src/main/res/layout/item_user_avatar.xml`
- Album page migrated to a page-level XML root:
  - `app/src/main/res/layout/fragment_album.xml`
- Album page now reuses shared XML building blocks:
  - `app/src/main/res/layout/view_page_header.xml`
  - `app/src/main/res/layout/view_profile_header.xml`
  - `app/src/main/res/layout/view_hero_banner.xml`
  - `app/src/main/res/layout/view_section_card.xml`
  - `app/src/main/res/layout/view_action_row.xml`
- `AlbumFragment` now treats Java as a binding/state layer for the main header, hero, section card, and action row:
  - `app/src/main/java/com/silverguardian/prototype/fragments/AlbumFragment.java`
- Settings page now uses a page-level XML shell plus reusable row/card blocks:
  - `app/src/main/res/layout/fragment_settings.xml`
  - `app/src/main/res/layout/view_settings_profile_card.xml`
  - `app/src/main/res/layout/view_settings_row.xml`
- `SettingsFragment` now binds data and interactions instead of constructing the whole page tree in Java:
  - `app/src/main/java/com/silverguardian/prototype/fragments/SettingsFragment.java`
- Main shell now separates content container and reusable bottom-nav shell:
  - `app/src/main/res/layout/activity_main.xml`
  - `app/src/main/res/layout/view_bottom_navigation_shell.xml`
- `MainActivity` now separates primary tab destinations from contextual screens such as fraud and memory:
  - `app/src/main/java/com/silverguardian/prototype/MainActivity.java`
- Home page now uses a cleaner XML-first structure with reusable welcome, metric, and shortcut blocks:
  - `app/src/main/res/layout/fragment_home.xml`
  - `app/src/main/res/layout/view_home_welcome_hero.xml`
  - `app/src/main/res/layout/view_home_metric_item.xml`
  - `app/src/main/res/layout/view_home_shortcut_row.xml`
- `HomeFragment` now binds summary values, shortcut rows, and AI entry behavior instead of relying on a large inline layout block:
  - `app/src/main/java/com/silverguardian/prototype/fragments/HomeFragment.java`
- Health page now uses a page-level XML shell with reusable metric rows and analysis card blocks:
  - `app/src/main/res/layout/fragment_health.xml`
  - `app/src/main/res/layout/view_health_metric_row.xml`
  - `app/src/main/res/layout/view_health_analysis_card.xml`
- `HealthFragment` now binds header, summary, metrics, AI analysis, and recording actions instead of building the page tree in Java:
  - `app/src/main/java/com/silverguardian/prototype/fragments/HealthFragment.java`
- Memory page now uses a page-level XML shell and a reusable record card for list rendering:
  - `app/src/main/res/layout/fragment_memory.xml`
  - `app/src/main/res/layout/view_memory_record_item.xml`
- `MemoryFragment` now uses RecyclerView + adapter binding for filtering, add-memory, empty state, and delete interactions:
  - `app/src/main/java/com/silverguardian/prototype/fragments/MemoryFragment.java`
- Medicine page now uses a page-level XML shell with reusable header structure and Java-side binding for filtering, list state, and dialogs:
  - `app/src/main/res/layout/fragment_medicine.xml`
  - `app/src/main/res/layout/item_medicine.xml`
- `MedicineFragment` now binds reminder summary, filter state, RecyclerView rows, library selection, and add/delete/detail actions instead of mixing page composition logic into Java:
  - `app/src/main/java/com/silverguardian/prototype/fragments/MedicineFragment.java`
- Community page now uses a page-level XML shell plus a reusable POI card item for nearby-service rendering:
  - `app/src/main/res/layout/fragment_community.xml`
  - `app/src/main/res/layout/item_community_poi.xml`
- `CommunityFragment` now binds chip filters, status summary, RecyclerView results, permission handling, and map-navigation actions instead of building the page structure in Java:
  - `app/src/main/java/com/silverguardian/prototype/fragments/CommunityFragment.java`
- Bluetooth page now uses a page-level XML shell plus a reusable device row item for BLE and demo-device rendering:
  - `app/src/main/res/layout/activity_bluetooth.xml`
  - `app/src/main/res/layout/item_bluetooth_device.xml`
- `BluetoothActivity` now binds page header, status strip, primary actions, RecyclerView results, scan state, permission flow, and connect behavior instead of composing the page tree in Java:
  - `app/src/main/java/com/silverguardian/prototype/BluetoothActivity.java`
- Child-mode page now uses a page-level XML shell plus reusable detail and alert rows for care-summary rendering:
  - `app/src/main/res/layout/activity_child_mode.xml`
  - `app/src/main/res/layout/item_child_mode_detail_row.xml`
  - `app/src/main/res/layout/item_child_mode_alert_row.xml`
- `ChildModeActivity` now binds section containers, photo-upload actions, health and medicine summaries, alert history, and family contacts instead of building the page tree in Java:
  - `app/src/main/java/com/silverguardian/prototype/ChildModeActivity.java`

## New migration rule

For future page cleanup, prefer this order:

1. Add or reuse a page-level XML layout.
2. Reuse an existing `view_*.xml` shared block where possible.
3. Keep Java focused on data binding, click listeners, and screen-state switching.
4. Avoid introducing new hand-built `new LinearLayout()` trees for stable page sections.

## Suggested next targets

Phase 1 core targets are now completed.

Recommended next pass:
1. Review older secondary screens for token alignment and naming consistency.
2. Merge any obviously duplicated local row patterns only where the value is real.
3. Decide whether Phase 2 should focus on visual polish, shared component deepening, or adapter/list standardization.

These pages now have a clearer path to the same XML-first structure used in the updated album, settings, main-shell, home, health, memory, medicine, community, bluetooth, and child-mode flows.
