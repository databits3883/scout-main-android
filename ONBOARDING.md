# Android Scouting App - Quick Start Guide

Welcome! This guide will get you up to speed on how this app works.

## What Does This App Do?

This is a **field scouting app** for FRC robotics competitions. Scouts use it to:
1. **Collect data** about robot performance during matches
2. **Scan QR codes** from a master device to receive schedules/config
3. **Generate QR codes** containing collected data
4. **Sync to Google Sheets** for team analysis

## The Big Picture

```
┌─────────────────────────────────────────────────────────────┐
│                         USER                                │
│   Scout collects data → Generates QR → Master scans it      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      ANDROID APP                            │
│                                                             │
│   Fragments (screens) → ViewModels → Stores → Database     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Key Concepts

### 1. Scouting Modes

The app has three scouting modes, each as a separate screen:

| Mode | What It Does | Fragment |
|------|--------------|----------|
| **Crowd** | Scout multiple teams in queue | `Crowd.java` |
| **Pit** | Scout one team in detail | `Pit.java` |
| **Special** | Collect extra metrics | `Special.java` |

### 2. The Master/Scout Relationship

```
┌──────────────┐                    ┌──────────────┐
│ MASTER DEVICE│                    │ SCOUT DEVICE │
│  (Tablet)    │                    │  (Phone)     │
│              │  ───QR Code────▶   │              │
│  Has schedule│                    │  Receives    │
│  Sends config│  ◀───QR Code────   │  Sends data  │
└──────────────┘                    └──────────────┘
```

The master device sends:
- Role provisioning (who you are, what position)
- Match schedules (which teams when)
- Google Sheets config

Scout devices send back:
- Collected match data

### 3. QR Code Format

All QR codes use comma-delimited format with a prefix:

```
role,Crowd,position,2,name,Alex,locked,true,match,5,delete,false,special,true
ScoutData,Alex,Jordan,Sam
GoogleConfig,1Bx7...,Crowd!A1:D,Pit!A1:D,Special!A1:D
MatchData,1,[team,match,data][team,match,data]
1234,5,0,1,2,3,4,5,Alex  ← Team data (no prefix)
```

## Where Things Live

```
app/src/main/java/com/databits/androidscouting/
│
├── fragment/              ← SCREENS (start here)
│   ├── Scanner.java       ← QR scanner (main hub)
│   ├── QR.java            ← QR code display
│   ├── Crowd.java         ← Crowd scouting
│   ├── Pit.java           ← Pit scouting
│   ├── Provision.java     ← Device setup
│   └── settings/          ← Settings screens
│
├── viewmodel/             ← STATE MANAGEMENT
│   ├── AppRepositoriesViewModel.java  ← App-wide data access
│   ├── ProvisionViewModel.java        ← Provisioning state
│   └── CameraSettingsViewModel.java   ← Camera preferences
│
├── data/
│   ├── repository/        ← DATA INTERFACES
│   │   ├── ProvisionSettingsStore.java
│   │   ├── ScheduleStore.java
│   │   └── impl/          ← Implementations
│   │
│   ├── database/          ← ROOM DATABASE
│   ├── dao/               ← Data access objects
│   └── entity/            ← Database tables
│
├── util/                  ← HELPERS
│   ├── ScoutUtils.java    ← Export cell data
│   ├── TeamInfo.java      ← Team lookups
│   └── MatchInfo.java     ← Match state
│
└── adapter/               ← RECYCLERVIEW BINDINGS
    └── MultiviewTypeAdapter.java

core-model/     ← Data classes (ScanPayload, DeviceRole)
core-domain/    ← Business logic (use cases, composers)
core-data/      ← Parsers and decoders
feature-scanner/← Scanner feature module
```

## How Data Flows

### When User Enters Data

```
User taps buttons/enters text
         │
         ▼
RecyclerView with MultiviewTypeAdapter
         │
         ▼
ScoutUtils.exportCell() → CSV string
         │
         ▼
Build QR payload with team/match/scouter
         │
         ▼
Navigate to QR fragment
         │
         ▼
Display QR code + save to ScheduleStore
```

### When User Scans a QR Code

```
Camera sees QR code
         │
         ▼
ProcessScanPayloadUseCase.decode()
         │
         ├── "role,..." → ApplyRoleProvisionUseCase
         ├── "MatchData,..." → ImportMatchDataChunkUseCase
         ├── "ScoutData,..." → Save scouter list
         └── team data → QueueScanDataUseCase
         │
         ▼
UI updates via Stores
```

## The Store Pattern

Instead of one big repository, we use **narrow interfaces**:

```java
// Don't do this:
BigRepository repo;  // Has 100 methods

// Do this:
ProvisionSettingsStore provision;  // 30 methods about provisioning
ScheduleStore schedule;            // 30 methods about schedules
CameraSettingsStore camera;        // 15 methods about camera
SyncStore sync;                    // 10 methods about sync
```

**Why?** Easier to test, easier to understand, clearer dependencies.

### Getting a Store

```java
// In a Fragment:
AppRepositoriesViewModel appViewModel = new ViewModelProvider(
    requireActivity(),
    new AppRepositoriesViewModel.Factory(requireContext())
).get(AppRepositoriesViewModel.class);

ProvisionSettingsStore provision = appViewModel.getRepositories().provisionSettingsStore;
ScheduleStore schedule = appViewModel.getRepositories().scheduleStore;

// Now use it:
String role = provision.getDeviceRole();
int team = schedule.getTeamNumber(match, position);
```

## Common Tasks

### Add a New Setting

1. **Add to Store interface** (`ProvisionSettingsStore.java`):
   ```java
   boolean isMyNewFeatureEnabled();
   void setMyNewFeature(boolean enabled);
   ```

2. **Implement in repository** (`DefaultPreferenceRepository.java`):
   ```java
   @Override
   public boolean isMyNewFeatureEnabled() {
       return configPreference.getBoolean("my_new_feature", false);
   }
   ```

3. **Use in Fragment/ViewModel**:
   ```java
   if (provisionStore.isMyNewFeatureEnabled()) { ... }
   ```

### Add a New Cell Type in Forms

1. **Add enum** in `model/CellType.java`

2. **Add binder** in `adapter/`:
   ```java
   case MY_NEW_TYPE:
       // Bind your view
       break;
   ```

3. **Add export logic** in `ScoutUtils.exportCell()`:
   ```java
   case MY_NEW_TYPE:
       finalString.append(myValue);
       break;
   ```

4. **Create JSON layout** with your new cell type

### Add a New QR Payload Type

1. **Add to `ScanPayload.kt`** (core-model):
   ```kotlin
   data class MyNewPayload(val data: String) : ScanPayload
   ```

2. **Add decoder case** in `DelimitedScanPayloadDecoder.kt` (core-data)

3. **Create use case** in `core-domain/scanner/`

4. **Handle in `Scanner.java`** payload handler

## Key Patterns to Follow

### 1. Fragments Are Dumb

Fragments should only:
- Display UI
- Forward user actions to ViewModels
- Observe LiveData

```java
// Good: Fragment just observes
viewModel.getMatchData().observe(getViewLifecycleOwner(), data -> {
    updateUI(data);
});

// Bad: Fragment does business logic
String result = data.split(",")[0] + data.split(",")[1];
```

### 2. Use Stores, Not Preferences Directly

```java
// Good
provisionStore.getDeviceRole();

// Bad
PowerPreference.getFileByName("Config").getString("device_role");
```

### 3. Background Work Goes to Executor

```java
// In BaseScoutFragment subclasses:
runInBackground(() -> {
    // Database operations here
    List<Team> teams = scheduleStore.getTeams();
    
    runOnUiIfActive(() -> {
        // Update UI with results
        adapter.setData(teams);
    });
});
```

### 4. Build Payloads with Composers

```java
// Good: Use composer
String payload = new RoleProvisionPayloadComposer().compose(
    DeviceRole.CROWD, 2, "Alex", true, 5, false, true
);

// Bad: String concatenation
String payload = "role," + role + ",position," + pos + ...
```

## Running the App

```bash
# Build
./gradlew assembleDebug

# Run tests
./gradlew test

# Run on device
./gradlew installDebug
```

## Architecture Tests

Before submitting code, tests check:

- ViewModels don't access DAOs directly
- Fragments don't access repository implementations
- No one bypasses the Store interfaces

Run them: `./gradlew test`

## Need More Detail?

See `ARCHITECTURE.md` for:
- Full module dependency graph
- Complete data flow diagrams
- All use cases and gateways
- Database schema
- ArchUnit rules
