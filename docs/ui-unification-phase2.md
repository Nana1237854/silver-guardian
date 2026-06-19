# UI Unification Phase 2

This note tracks the second implementation batch of the XML-first UI unification work.

## Scope completed in this batch

- Community detail/map page migrated from hand-built Java view trees to a page-level XML shell:
  - `app/src/main/res/layout/activity_community.xml`
  - `app/src/main/java/com/silverguardian/prototype/CommunityActivity.java`
- Fraud detail page migrated from a hand-built ScrollView tree to a page-level XML shell:
  - `app/src/main/res/layout/activity_fraud_detail.xml`
  - `app/src/main/java/com/silverguardian/prototype/FraudDetailActivity.java`
- Photo detail page migrated from a hand-built ScrollView tree to a page-level XML shell plus reusable info rows:
  - `app/src/main/res/layout/activity_photo_detail.xml`
  - `app/src/main/res/layout/view_detail_info_row.xml`
  - `app/src/main/java/com/silverguardian/prototype/PhotoDetailActivity.java`
- Mainline shared blocks and spacing were tightened in place instead of introducing new ad hoc layout code:
  - `app/src/main/res/layout/view_community_search_section.xml`
  - `app/src/main/res/layout/view_settings_row.xml`
  - `app/src/main/res/layout/item_community_poi.xml`
- Mainline fragments moved further toward resource-driven copy:
  - `app/src/main/java/com/silverguardian/prototype/fragments/SettingsFragment.java`
  - `app/src/main/java/com/silverguardian/prototype/fragments/CommunityFragment.java`
- Album page received another high-value local cleanup pass for the all-photos entry and content descriptions:
  - `app/src/main/java/com/silverguardian/prototype/fragments/AlbumFragment.java`
- UI-facing copy used by the touched pages was consolidated into `strings.xml` for this batch.

## Verification completed

- `:app:processDebugResources`
- `:app:compileDebugJavaWithJavac`

Both checks passed after the batch.

## Remaining candidates

Highest-value next targets:
1. Continue cleaning `AlbumFragment` remaining `TEXT_*` copy and dialog labels into `strings.xml`.
2. Remove residual hardcoded copy and spacing drift in `SettingsFragment` and `CommunityFragment`.
3. Decide whether the next batch should target older utility/detail pages or focus on deeper token standardization.

## Batch rule reinforced

For follow-up UI cleanup, keep this order:
1. Move stable page structure into `activity_*.xml` or `fragment_*.xml`.
2. Reuse existing `view_*.xml` or `item_*.xml` blocks before creating new local structures.
3. Keep Java in binder mode: data, visibility, adapters, listeners, permission flow, and navigation only.
4. Move UI-facing copy into `strings.xml` when the text is stable and screen-facing.