# Android Scouting App Architecture

## Overview

A field scouting application for FRC (First Robotics Competition) events. The app supports multiple scouting modes (Crowd, Pit, Special), QR code scanning/data generation, and Google Sheets synchronization.

## Module Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                              app                                     │
│   Fragments, ViewModels, UI Controllers, Adapters, Layout System    │
│   Navigation, MainActivity, Feature Composition Roots               │
└─────────────────────────────────────────────────────────────────────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐  ┌─────────────────┐  ┌─────────────────┐
│feature-scanner│  │   core-domain   │  │    core-data    │
│ Scanner       │  │ Use Cases       │  │ Parsers,        │
│ Bootstrap     │  │ Gateways        │  │ Decoders        │
└───────────────┘  │ Composers       │  └─────────────────┘
        │          └─────────────────┘           │
        │                    │                    │
        └────────────────────┼────────────────────┘
                             ▼
                    ┌─────────────────┐
                    │   core-model    │
                    │ Sealed classes, │
                    │ Enums, Models   │
                    └─────────────────┘
```

### Module Responsibilities

| Module | Purpose | Dependencies |
|--------|---------|--------------|
| `core-model` | Pure Kotlin/Java domain models, sealed interfaces, enums | None |
| `core-domain` | Business logic: use cases, gateway interfaces, composers | core-model |
| `core-data` | Data parsing/encoding implementations, decoders | core-domain, core-model |
| `feature-scanner` | Scanner feature bootstrap, feature-scoped wiring | core-domain, core-data, core-model |
| `app` | Android UI, ViewModels, fragments, navigation, Room/PowerPreference | All modules |

## Dependency Flow

```
app → feature-scanner → core-domain → core-model
                      ↘ core-data   ↗
```

**Rules:**
- `core-model` has no dependencies
- `core-domain` depends only on `core-model`
- `core-data` depends on `core-domain` (implements its gateway interfaces)
- `feature-scanner` depends on `core-domain`, `core-data`, `core-model`
- `app` depends on everything but encapsulates all Android-specifics

## Layer Architecture

### Presentation Layer (`app/src/main/java/.../fragment/`)

Fragments are thin views that delegate to ViewModels and controllers.

```
┌──────────────────┐    ┌────────────────────┐    ┌─────────────────┐
│    Fragment      │───▶│     ViewModel      │───▶│   Store (iface) │
│  (UI lifecycle)  │    │ (state management) │    │                 │
└──────────────────┘    └────────────────────┘    └─────────────────┘
         │
         ▼
┌──────────────────┐
│    Controller    │    Controllers encapsulate complex UI behavior
│ (camera, upload) │    (ScannerCameraController, ScannerUploadCoordinator)
└──────────────────┘
```

**Key Fragments:**
- `Scanner` - QR code scanning orchestrator
- `QR` - QR code generation/display
- `Crowd`, `Pit`, `Special` - Scouting data collection
- `Provision` - Device role provisioning
- `Main` - Entry point navigation

**Base Class:** `BaseScoutFragment` provides:
- Background thread execution (`runInBackground`)
- Lifecycle-safe UI callbacks (`runOnUiIfActive`)
- Common scouting utilities access

### ViewModel Layer (`app/src/main/java/.../viewmodel/`)

ViewModels are scoped to Activity or Fragment and hold references to Store interfaces.

| ViewModel | Scope | Purpose |
|-----------|-------|---------|
| `AppRepositoriesViewModel` | Activity | Composition root - provides `AppRepositories` |
| `CameraSettingsViewModel` | Dialog | Camera preferences (torch, zoom, exposure) |
| `ProvisionViewModel` | Fragment | Device role and provisioning state |
| `SyncStatusViewModel` | Activity | Upload queue status, scouter list |

### Domain Layer (`core-domain`)

Pure business logic with no Android dependencies.

#### Use Cases
| Use Case | Purpose |
|----------|---------|
| `ProcessScanPayloadUseCase` | Decode raw QR strings into `ScanPayload` |
| `ApplyRoleProvisionUseCase` | Apply role provisioning from QR scan |
| `ImportMatchDataChunkUseCase` | Import schedule chunks from master device |
| `QueueScanDataUseCase` | Queue team data for upload |
| `FindMatchedTeamSlotUseCase` | Match scanned team to schedule slot |

#### Gateway Interfaces (in core-domain, implemented in app)
```kotlin
interface RoleProvisionGateway {
    fun clearAllData()
    fun updateDeviceRole(role: String)
    fun updateCrowdPosition(position: Int)
    // ...
}

interface MatchDataImportGateway { ... }
interface UploadQueueGateway { ... }
```

#### Payload Composers
Build structured payloads for QR generation:
- `RoleProvisionPayloadComposer` - "role,..." format
- `ScouterListPayloadComposer` - "ScoutData,..." format
- `MatchDataChunkPayloadComposer` - "MatchData,..." format
- `GoogleConfigPayloadCodec` - "GoogleConfig,..." format

### Data Layer (`core-data`, `app/.../data/`)

#### Parsers/Decoders (`core-data`)
- `DelimitedScanPayloadDecoder` - Parses QR payload strings into `ScanPayload` sealed class
- `FlexibleCsvParser` - CSV parsing utility

#### Store Interfaces (`app/.../data/repository/`)
Narrow interfaces following Interface Segregation Principle:

```
┌────────────────────┐
│ ProvisionSettingsStore │  Device role, scouter, match state
├────────────────────┤
│ CameraSettingsStore │    Torch, zoom, haptic feedback
├────────────────────┤
│ ScheduleStore      │      Team schedules, match data, chunk tracking
├────────────────────┤
│ SyncStore          │       Scouter list, upload queue status
└────────────────────┘
```

#### Implementations (`app/.../data/repository/impl/`)
- `DefaultPreferenceRepository` - Implements all 4 store interfaces; delegates to PowerPreference + Room
- `PowerPreferenceSettingsRepository` - Settings via PowerPreference library
- `RoomScheduleRepository` - Schedule data via Room DAOs
- `RoomUploadRepository` - Upload queue via Room

#### Gateways (`app/.../data/repository/adapter/`)
Adapter pattern connecting core-domain gateways to app store interfaces:
- `StoreRoleProvisionGateway`
- `StoreMatchDataImportGateway`
- `StoreUploadQueueGateway`

### Database Layer (`app/.../data/`)

Room database with 7 entities:

| Entity | Purpose |
|--------|---------|
| `TeamMatchSchedule` | Cached schedule from master |
| `MatchData` | Collected scouting data |
| `UploadQueueItem` | Pending uploads |
| `Scouter` | Scouter name list |
| `PitTeamRemaining` | Teams remaining in pit queue |
| `ProcessedChunk` | Deduplication for schedule chunks |
| `SeenLine` | Deduplication for data lines |

## Dependency Injection Pattern

This app uses **manual dependency injection** (no Hilt/Dagger). Composition roots are explicit.

### App Graph Construction

```
MainActivity
    │
    ├── PreferenceRepositoryProvider.init(this)
    │       │
    │       └── Creates DefaultPreferenceRepository
    │           └── Wraps in AppRepositories
    │
    └── ViewModelProvider.get(AppRepositoriesViewModel::class.java)
            │
            └── AppRepositoriesViewModel provides AppRepositories
                    │
                    └── Fragments access stores via:
                        appRepositoriesViewModel.getRepositories().provisionSettingsStore
```

### Scanner Feature Wiring

`ScannerDependencies` is the composition root for the Scanner feature:

```java
static ScannerDependencies create(Context context) {
    AppRepositories repositories = PreferenceRepositoryProvider.graph(context);
    return new ScannerDependencies(
        repositories.provisionSettingsStore,
        repositories.scheduleStore,
        new ScannerCameraController(),
        // ... controllers and use cases
        ScannerFeatureBootstrap.provideProcessScanPayloadUseCase(),
        new ApplyRoleProvisionUseCase(new StoreRoleProvisionGateway(repositories.provisionSettingsStore)),
        // ... more use cases with gateway adapters
    );
}
```

## Data Flow

### QR Scan Flow
```
Camera Frame
    │
    ▼
ScannerCameraController (MLKit barcode detection)
    │
    ▼
ProcessScanPayloadUseCase
    │
    ▼
DelimitedScanPayloadDecoder → ScanPayload sealed class
    │
    ├── ScanPayload.RoleProvision → ApplyRoleProvisionUseCase → StoreRoleProvisionGateway
    ├── ScanPayload.MatchDataChunk → ImportMatchDataChunkUseCase → StoreMatchDataImportGateway
    ├── ScanPayload.TeamData → QueueScanDataUseCase → StoreUploadQueueGateway
    └── ScanPayload.GoogleConfig → (applies configuration)
```

### QR Generation Flow
```
User clicks "Generate QR"
    │
    ▼
ScoutUtils.exportCell(recyclerView) → CSV cell data
    │
    ▼
[Pit/Crowd/Special].java builds payload with team, match, scouter
    │
    ▼
Navigate to QR fragment with payload bundle
    │
    ▼
QrCodeGenerator.generateQRCode(data)
    │
    ▼
Display bitmap + save to ScheduleStore
```

### Upload Flow
```
Scanner scans team data
    │
    ▼
QueueScanDataUseCase → UploadQueueGateway → RoomUploadRepository
    │
    ▼
UploadQueueItem inserted
    │
    ▼
SheetsUpdateTask pulls pending items
    │
    ▼
Google Sheets API upload
    │
    ├── Success → markUploadSuccess()
    └── Failure → markUploadFailed() (retry logic)
```

## Architecture Enforcement

ArchUnit tests in `ArchitectureTest.java` enforce:

1. **ViewModels** cannot access DAOs directly
2. **ViewModels** cannot access `PreferenceRepositoryProvider` or `AppRepositories` (except `AppRepositoriesViewModel`)
3. **Fragments** cannot access repository implementations (`impl` package)
4. **Fragments and Utils** cannot call `PreferenceRepositoryProvider.graph()` directly
5. **DAOs** only accessed by repositories, database, or test code
6. **Gateways** must follow `Store*Gateway` naming convention
7. **No cycles** between top-level packages

## Key Components

### Scanner Decomposition

The `Scanner` fragment delegates to focused controllers:

| Controller | Responsibility |
|------------|----------------|
| `ScannerCameraController` | Camera lifecycle, torch, preview |
| `ScannerCameraUiController` | Camera settings UI (zoom, exposure) |
| `ScannerUiFeedbackController` | Haptic/audio feedback, reticle |
| `ScannerTeamScheduleController` | Team schedule loading/caching |
| `ScannerUploadCoordinator` | Upload timing, batch coordination |
| `ScannerPayloadCoordinator` | Payload routing to handlers |
| `ScannerTeamUiHelper` | Team slot UI updates |
| `UploadAuditLogger` | CSV audit logging |

### Layout System

Dynamic form layouts loaded from JSON:
- `LayoutParser` - Parses JSON layout definitions
- `LayoutPresenter` - Applies layout to RecyclerView
- `LayoutManager` - High-level layout loading
- `MoshiProvider` - Moshi JSON adapter configuration

### Adapter Pattern

`MultiviewTypeAdapter` with cell binders:
- `StandardCellBinder` - Basic cell types
- `PickerCellBinder` - Number pickers
- `TeamSelectCellBinder` - Team selection spinners
- `SpecialCellBinder` - Special scouting cells

## Threading Model

- **Main Thread:** UI updates, ViewModel observation
- **Background Executor:** Room operations, file I/O, network
- `BaseScoutFragment.runInBackground()` - ExecutorService for background work
- `BaseScoutFragment.runOnUiIfActive()` - Posts to main thread if fragment is active
- `SheetsUpdateTask` - Dedicated threading with deterministic callbacks

## Navigation

Jetpack Navigation Component with safe argument passing:
- NavHostFragment in MainActivity
- Bundle-based argument passing for QR data
- Shared ViewModel for cross-fragment state

## Testing Strategy

| Test Type | Location | Purpose |
|-----------|----------|---------|
| Architecture | `app/src/test/` | ArchUnit boundary enforcement |
| Unit | `app/src/test/`, `core-*/src/test/` | Use cases, composers, parsers |
| Integration | `app/src/test/` | Repository behavior with Room |

Key test files:
- `ArchitectureTest.java` - Boundary enforcement
- `ScannerUploadCoordinatorTest.java` - Upload lifecycle
- `DelimitedScanPayloadDecoderTest.kt` - QR parsing
- `RepositoryIntegrationTest.java` - Store operations

## Build Configuration

- Gradle with Kotlin DSL
- KSP for annotation processing
- Configuration cache enabled
- Modules: `app`, `core-model`, `core-domain`, `core-data`, `feature-scanner`
