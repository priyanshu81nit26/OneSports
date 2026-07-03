# District

Neighbourhood hub for **organizing events**, **joining communities**, and **participating** in local activities. Built with the same space-themed Compose UI as StayFocus, but with its own Firebase backend.

## App sections

1. **Pulse** — Personal activity feed + orbit stats (hosting, going, communities, connections).
2. **Organise** — Create events linked to your communities; members get notified in their feed.
3. **Communities** — Join gyms, societies, clubs; post updates; connect with members.
4. **Participate** — Register, save events for later, browse open listings.
5. **Connections** — Accept/decline requests; view your local circle.

### Cross-feature integration

- **Activity feed** (`activity_feed`) — fan-out on events, community updates, joins, saves, and connections.
- **Saved events** (`event_saves`) — bookmark without registering; appears in Participate tab.
- **Community-linked events** — optional community picker when creating events.
- **Connection requests** — real accept/decline flow (no auto-accept); badge on home header.
- **Mission timelines** — multi-day orbit schedule for marathons and festivals; gamified circular timeline on event detail.

## Project layout

```
district/
├── android/          # Jetpack Compose app (applicationId: app.district)
└── firebase/         # Cloud Functions + Firestore rules/indexes
```

**Do not edit** `StayFocus/` — District is a separate app in this folder.

## Firebase setup

1. Register Android app `app.district` in Firebase project `raksham-4b713` (or your own project).
2. Download `google-services.json` into `android/app/`.
3. Deploy backend from repo root:

```bash
cd firebase/functions
npm install
npm run build
cd ../..
firebase deploy --only functions,firestore:rules,firestore:indexes
```

### Collections

- `events`, `event_registrations`, `event_saves`
- `communities`, `community_members`, `community_updates`
- `connection_requests`, `connections`
- `activity_feed` (per-user fan-out timeline)
- `users`, `profiles`, `usernames` (auth/profile)

All writes go through Cloud Functions; clients read Firestore directly.

## Android build

Open `district/android` in Android Studio (JDK 17+). Sync Gradle and run on device/emulator.

Package: `app.district`  
Theme: space / neon glass (`SpaceBackground`, `NeonGlassPanel`, `Arcade` tokens)

## Auth flow

Welcome → Sign up / Sign in → Profile setup (username, photo, PIN) → Home (3 tabs)

Settings: PIN-protected sign out and account deletion.
