# Implementation Plan — EVN Grid Fault Management System

> Source of truth: `docs/PROJECT_CONTEXT.md` + `docs/Project Specification - Team 3.pdf`
>
> Mark items `[x]` as each one is finished. Finish and verify one module before moving to the next.

---

## Legend

- `[x]` — done and correct
- `[ ]` — not yet done **or** exists but needs significant rework
- Items marked **(exists, needs rework)** have partial code already but do not meet conventions
  (wrong base path, `RuntimeException` instead of custom exceptions, plain classes instead of records,
  missing `@PreAuthorize`, missing pagination/filtering, etc.)

---

## Existing code snapshot (do not regenerate)

| Layer        | What exists                                                                                                                                                                                                                       |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Entities     | All 18 entities are in `model/domain/`                                                                                                                                                                                            |
| Enums        | All 9 enums are in `model/enums/`                                                                                                                                                                                                 |
| Repositories | All repos extend `JpaRepository` — no custom methods yet                                                                                                                                                                          |
| Security     | `JwtSecurityWebConfig`, `WebSecurityConfig`, `JwtFilter`, `CustomUsernamePasswordAuthenticationProvider`, `JwtHelper`, `JwtConstants` — **DO NOT TOUCH**                                                                          |
| DTOs         | 15 flat DTOs in `dto/` — plain classes, not records, not in `request/`/`response/` subpackages                                                                                                                                    |
| Services     | `UserService/Impl`, `FaultReportService/Impl`, `FaultWorkflowService/Impl`, `CrewService/Impl`, `InterventionService/Impl`, `AttachmentService/Impl`, `NotificationService/Impl`, `AuditLogService/Impl`, `TokenBlacklistService` |
| Controllers  | `AuthController`, `UserController`, `FaultController`, `CrewController`, `InterventionController`, `AttachmentController`, `NotificationController`                                                                               |

**Cross-cutting issues to fix in every module touched:**

- Base paths must be `/api/v1/...` (current code uses `/api/...`)
- All DTOs must be `record` types in `dto/request/` or `dto/response/`
- Replace all `throw new RuntimeException(...)` with proper custom exceptions
- Add `@PreAuthorize` on every protected endpoint
- `GlobalExceptionHandler` with `@RestControllerAdvice` must be created in `exception/`

---

## Module 1 — Auth & Users

> Covers: customer registration/login, internal user login, profile view/update, logout.
> Partially done: `AuthController`, `UserController`, `UserService/Impl` exist.

- [x] **Repository custom methods** — `UserRepository`: `findByEmail`, `existsByEmail`; `CustomerRepository`:
  `findByUserId`, `findByUserEmail`, `existsByUserEmail`
- [x] **Request DTOs** — `RegisterCustomerRequest`, `LoginRequest`, `UpdateProfileRequest`, `RegisterUserRequest`,
  `UpdateRoleRequest` (as `record`s in `dto/request/`)
- [x] **Response DTOs** — `LoginResponse` (token + userId + role), `UserProfileResponse`, `UserSummaryResponse` (as
  `record`s in `dto/response/`)
- [x] **Service interface** — `UserService` updated with admin methods + new return types
- [x] **Service implementation** — `UserServiceImpl` rewritten: uses `ResourceNotFoundException`/`ConflictException`/
  `BadRequestException`; logout fixed (handles JWT exceptions); admin user-creation added
- [x] **Controller + endpoints** — `AuthController` (`POST /api/auth/register`, `/login`, `/logout` — kept at
  `/api/auth` to respect security whitelist) and `UserController` (`GET/PUT /api/v1/users/profile`; admin:
  `GET /api/v1/users`, `POST /api/v1/users`, `GET /api/v1/users/{id}`, `PUT /api/v1/users/{id}/role`); `@PreAuthorize`
  added
- [x] **Mapper(s)** — inline `toProfileResponse()` in `UserServiceImpl`
- [x] **Custom exceptions** — all exist; `GlobalExceptionHandler` covers all custom exceptions + generic handler

---

## Module 2 — Customers

> Covers: admin/operator CRUD of customer profiles; customer views own profile.

- [x] **Repository custom methods** — `CustomerRepository`: `findByUserId`, `findByUserEmail`, `existsByUserEmail`
- [x] **Request DTOs** — `UpdateCustomerRequest` (record)
- [x] **Response DTOs** — `CustomerResponse` (id, firstName, lastName, email, contact) (record, no validation
  annotations)
- [x] **Service interface** — `CustomerService` (added `delete`)
- [x] **Service implementation** — `CustomerServiceImpl` fixed: `ResourceNotFoundException`, `delete()` added, user
  fields saved
- [x] **Controller + endpoints** — `CustomerController`: `/api/v1/customers` with `@PreAuthorize`; ADMIN/OPERATOR for
  list/detail/update; CUSTOMER for `/me`; ADMIN for DELETE
- [x] **Mapper(s)** — inline `mapToResponse()` in `CustomerServiceImpl`
- [x] **Custom exceptions** — reuses `ResourceNotFoundException`

---

## Module 3 — Regions & Locations

> Covers: admin CRUD for regions; admin CRUD for locations (linked to region).

- [x] **Repository custom methods** — `RegionRepository`: `findByName`, `existsByName`; `LocationRepository`:
  `findAllByRegionId`, `findByAddress`, `findByLongitude`, `findByLatitude`, `findByLongitudeAndLatitude`
- [x] **Request DTOs** — `CreateRegionRequest`, `UpdateRegionRequest`, `CreateLocationRequest`,
  `UpdateLocationRequest` (records)
- [x] **Response DTOs** — `RegionResponse`, `LocationResponse` (records)
- [x] **Service interface** — `RegionService`, `LocationService`
- [x] **Service implementation** — `RegionServiceImpl` (ResourceNotFoundException, ConflictException for duplicate
  names), `LocationServiceImpl` (ResourceNotFoundException)
- [x] **Controller + endpoints** — `RegionController` (`/api/v1/regions`, ADMIN write / authenticated read,
  `GET /{regionId}/locations`); `LocationController` (`/api/v1/locations`, ADMIN write / authenticated read)
- [x] **Mapper(s)** — inline in service implementations
- [x] **Custom exceptions** — `ConflictException` for duplicate region name; reuses `ResourceNotFoundException`

---

## Module 4 — Equipment

> Covers: admin CRUD for equipment items; equipment tied to a location and an EquipmentType enum.

- [x] **Repository custom methods** — `EquipmentRepository`: `findByLocationId`, `findByEquipmentType`,
  `findByLocationIdAndEquipmentType`
- [x] **Request DTOs** — `EquipmentRequest` (name, equipmentType, locationId) (record)
- [x] **Response DTOs** — `EquipmentResponse` (full, with region info), `EquipmentSummaryResponse` (record)
- [x] **Service interface** — `EquipmentService`
- [x] **Service implementation** — `EquipmentServiceImpl`
- [x] **Controller + endpoints** — `EquipmentController`: full CRUD at `/api/v1/equipment` (ADMIN write / authenticated
  read); `GET /api/v1/locations/{locationId}/equipment`; `GET /api/v1/equipment/by-type`
- [x] **Mapper(s)** — inline in service (`mapToResponse`, `mapToSummary`)
- [x] **Custom exceptions** — reuses `ResourceNotFoundException`
- [x] **EquipmentType enum** — added values: TRANSFORMER, CABLE, OVERHEAD_LINE, SWITCHGEAR, METER, SUBSTATION,
  CIRCUIT_BREAKER, FUSE, CAPACITOR_BANK, DISTRIBUTION_BOX

---

## Module 5 — Fault Reports (core)

> Covers: submit fault (customer + operator), list/filter/search, detail view, status update, status history.
> Partially done: `FaultReportService/Impl`, `FaultWorkflowService/Impl`, `FaultController` exist but are incomplete.

- [ ] **Repository custom methods** — `FaultReportRepository` must extend `JpaSpecificationExecutor<FaultReport>`; add
  `findByTrackingCode`; `FaultStatusHistoryRepository`: `findByFaultReportIdOrderByChangedAtDesc`
- [ ] **Request DTOs** — `CreateFaultRequest` (trackingCode auto-generated), `UpdateFaultRequest`,
  `ChangeStatusRequest`, `FaultFilterRequest` (status, priority, regionId, locationId, faultType, dateFrom, dateTo,
  source) (as `record`s)
- [ ] **Response DTOs** — `FaultReportResponse` (full), `FaultReportSummaryResponse` (for paginated list),
  `StatusHistoryResponse` (as `record`s)
- [ ] **Service interface** — `FaultReportService`: extend with `getFiltered(FaultFilterRequest, Pageable)`,
  `getStatusHistory(Long)`, `getByTrackingCode(String)`, `createByOperator(...)`
- [ ] **Service implementation** — fix `FaultReportServiceImpl`: custom exceptions, tracking code via `UUID`, proper
  mapping; complete `FaultWorkflowServiceImpl`
- [ ] **Controller + endpoints** — fix `FaultController` to `/api/v1/faults`; add `GET /api/v1/faults` (paginated +
  filtered), `GET /api/v1/faults/{id}/history`, `GET /api/v1/faults/track/{code}`; add `@PreAuthorize`; add
  `GET /api/v1/faults/my` for CUSTOMER
- [ ] **Mapper(s)** — `FaultReportMapper`, `FaultStatusHistoryMapper`
- [ ] **Custom exceptions** — reuse; no new ones needed

---

## Module 6 — Attachments

> Covers: upload file(s) to a fault report, download attachment, list attachments per fault.
> Partially done: `AttachmentService/Impl`, `AttachmentController` exist.

- [ ] **Repository custom methods** — `AttachmentRepository`: `findByFaultReportId`, `existsByIdAndFaultReportId`
- [ ] **Request DTOs** — none (uses `MultipartFile`); `AttachmentMetadataRequest` if needed
- [ ] **Response DTOs** — `AttachmentResponse` (id, fileName, fileType, fileSize, uploadedAt, uploadedBy) (as `record`)
- [ ] **Service interface** — `AttachmentService`: `upload(Long faultId, MultipartFile file, Long userId)`,
  `download(Long attachmentId)`, `listByFault(Long faultId)`, `delete(Long id)`
- [ ] **Service implementation** — complete `AttachmentServiceImpl`: store files on disk / local path, set entity
  fields, link to fault and user
- [ ] **Controller + endpoints** — fix `AttachmentController` to `/api/v1/faults/{faultId}/attachments`; `POST` (
  upload), `GET` (list), `GET /{id}/download`, `DELETE /{id}`; `@PreAuthorize`
- [ ] **Mapper(s)** — `AttachmentMapper`
- [ ] **Custom exceptions** — `BadRequestException` for invalid file type/size; reuse `ResourceNotFoundException`

---

## Module 7 — AI Classification

> Covers: POST fault description to Python AI service, store result, display to operators, allow manual override.
> Nothing exists beyond `FaultClassification` enum; no `client/` package exists yet.

- [ ] **Repository custom methods** — No dedicated repo needed (classification is stored on `FaultReport`); query via
  `FaultReportRepository`
- [ ] **Request DTOs** — `ClassificationRequest` (faultId, description sent to AI), `ManualClassificationRequest` (
  override by operator) (as `record`s)
- [ ] **Response DTOs** — `ClassificationResponse` (category, severity, confidence, keywords, safetyRisk) (as `record`)
- [ ] **Service interface** — `ClassificationService`: `classifyFault(Long faultId)`,
  `overrideClassification(Long faultId, ManualClassificationRequest)`
- [ ] **Service implementation** — `ClassificationServiceImpl`: calls `AiClient.classify()`, stores result on
  `FaultReport`, falls back gracefully if AI unreachable
- [ ] **Controller + endpoints** — `ClassificationController`: `POST /api/v1/faults/{id}/classify` (OPERATOR,
  DISPATCHER), `PUT /api/v1/faults/{id}/classification` (manual override)
- [ ] **Mapper(s)** — `ClassificationMapper` (AI response → entity fields / response DTO)
- [ ] **Custom exceptions** — `AiServiceException` (wraps unreachable AI, logged + default applied)

> **Also create in this module:** `client/AiClient` (using `WebClient`, configured via `ai.service.base-url` in
`application.properties`), covering all three AI endpoints (`/classify`, `/predict-risk`, `/calculate-priority`).

---

## Module 8 — Risk Prediction

> Covers: trigger risk prediction per fault (location + equipment + weather + history), store result, show factors.
> `RiskPrediction` entity + `RiskPredictionRepository` (empty) exist.

- [ ] **Repository custom methods** — `RiskPredictionRepository`: `findByFaultReportId`, `findByLocationId` (for
  dashboard risk zones)
- [ ] **Request DTOs** — `RiskPredictionRequest` (locationId, equipmentId, weatherData, historyWindow) (as `record`)
- [ ] **Response DTOs** — `RiskPredictionResponse` (score, level LOW/MEDIUM/HIGH, contributingFactors, predictedAt) (as
  `record`)
- [ ] **Service interface** — `RiskPredictionService`: `predictRisk(Long faultId)`,
  `predictRiskForLocation(Long locationId)`, `getByFault(Long faultId)`
- [ ] **Service implementation** — `RiskPredictionServiceImpl`: calls `AiClient.predictRisk()`, persists
  `RiskPrediction`, falls back gracefully
- [ ] **Controller + endpoints** — `RiskPredictionController`: `POST /api/v1/faults/{id}/predict-risk` (OPERATOR,
  DISPATCHER), `GET /api/v1/faults/{id}/risk`, `GET /api/v1/locations/{id}/risk`
- [ ] **Mapper(s)** — `RiskPredictionMapper`
- [ ] **Custom exceptions** — reuse `AiServiceException` from Module 7

---

## Module 9 — Priority Scoring

> Covers: auto-calculate priority score for active faults, assign category (CRITICAL/HIGH/MEDIUM/LOW), show explanation,
> allow manual override.
> Nothing exists beyond `FaultPriority` enum.

- [ ] **Repository custom methods** — `FaultReportRepository`: `findByFaultPriorityOrderByCreatedAtDesc`,
  `findActiveFaults` (status not CLOSED/RESOLVED)
- [ ] **Request DTOs** — `PriorityOverrideRequest` (manualPriority, reason) (as `record`)
- [ ] **Response DTOs** — `PriorityResponse` (faultId, score, level, explanation, calculationSource AUTO/MANUAL,
  calculatedAt) (as `record`)
- [ ] **Service interface** — `PriorityService`: `calculatePriority(Long faultId)`,
  `overridePriority(Long faultId, PriorityOverrideRequest)`, `recalculateAllActive()`
- [ ] **Service implementation** — `PriorityServiceImpl`: calls `AiClient.calculatePriority()`, updates
  `FaultReport.faultPriority`, persists explanation; falls back gracefully
- [ ] **Controller + endpoints** — `PriorityController`: `POST /api/v1/faults/{id}/calculate-priority` (DISPATCHER),
  `PUT /api/v1/faults/{id}/priority` (manual override, DISPATCHER/MANAGER), `GET /api/v1/faults/{id}/priority`
- [ ] **Mapper(s)** — inline in service (no dedicated mapper needed for this module)
- [ ] **Custom exceptions** — reuse `AiServiceException`; `BadRequestException` for invalid manual priority value

---

## Module 10 — Crews & Crew Members

> Covers: admin/dispatcher view and CRUD of crews and their members; availability tracking.
> Partially done: `CrewService/Impl`, `CrewController` exist (only list + getById + assignToFault).

- [ ] **Repository custom methods** — `CrewRepository`: `findAvailableCrews` (no active assignment), `findByName`;
  `CrewMemberRepository`: `findByCrewId`, `findByUserId`
- [ ] **Request DTOs** — `CreateCrewRequest`, `UpdateCrewRequest`, `AddCrewMemberRequest` (userId, position) (as
  `record`s)
- [ ] **Response DTOs** — `CrewResponse` (full, including members list), `CrewSummaryResponse`, `CrewMemberResponse` (as
  `record`s)
- [ ] **Service interface** — extend `CrewService`: `create`, `update`, `delete`, `addMember`, `removeMember`,
  `getAvailable`; new `CrewMemberService`
- [ ] **Service implementation** — rewrite `CrewServiceImpl` to fix exceptions, add missing CRUD; create
  `CrewMemberServiceImpl`
- [ ] **Controller + endpoints** — fix `CrewController` to `/api/v1/crews`; add `POST`, `PUT /{id}`, `DELETE /{id}`,
  `GET /available`; `POST /{id}/members`, `DELETE /{id}/members/{memberId}`; `@PreAuthorize(DISPATCHER, ADMIN)`
- [ ] **Mapper(s)** — `CrewMapper`, `CrewMemberMapper`
- [ ] **Custom exceptions** — reuse `ResourceNotFoundException`, `ConflictException`

---

## Module 11 — Fault Assignment

> Covers: assign crew to fault, AI recommendation, accept/reject assignment, reassign.
> `FaultAssignment` entity and `FaultAssigmentRepository` (typo in name) exist. Assignment logic currently embedded in
`CrewServiceImpl`.

- [ ] **Repository custom methods** — `FaultAssigmentRepository`: `findByFaultReportId`, `findByCrewId`,
  `findActiveByFaultReportId`, `findPendingByCrewId`
- [ ] **Request DTOs** — `AssignCrewRequest` (crewId, assignmentNote), `ReassignRequest` (newCrewId, reason),
  `AcceptAssignmentRequest` (accepted: boolean) (as `record`s)
- [ ] **Response DTOs** — `AssignmentResponse` (id, faultId, crewId, crewName, status, assignedAt, acceptedAt, note),
  `CrewRecommendationResponse` (recommended crew list with score/reason) (as `record`s)
- [ ] **Service interface** — `FaultAssignmentService`: `assignCrew(Long faultId, AssignCrewRequest)`,
  `reassign(Long assignmentId, ReassignRequest)`, `acceptAssignment(Long assignmentId)`, `getByFault(Long faultId)`,
  `recommendCrew(Long faultId)`
- [ ] **Service implementation** — `FaultAssignmentServiceImpl`: extract assignment logic from CrewServiceImpl;
  implement recommendation (call AI client or simple heuristic by availability + location proximity)
- [ ] **Controller + endpoints** — `FaultAssignmentController`: `POST /api/v1/faults/{id}/assignments` (DISPATCHER),
  `PUT /api/v1/assignments/{id}/accept` (FIELD_CREW), `PUT /api/v1/assignments/{id}/reassign` (DISPATCHER),
  `GET /api/v1/faults/{id}/assignments`, `GET /api/v1/faults/{id}/crew-recommendations`
- [ ] **Mapper(s)** — `FaultAssignmentMapper`
- [ ] **Custom exceptions** — `ConflictException` (crew already assigned); reuse `ResourceNotFoundException`

---

## Module 12 — Interventions

> Covers: start intervention, update notes/status, close case (resolved/closed).
> Partially done: `InterventionService/Impl`, `InterventionController` exist (create + list by fault only).

- [ ] **Repository custom methods** — `InterventionRepository`: `findByFaultReportId` (exists in impl), `findByCrewId`,
  `findByFaultReportIdAndResolutionStatus`
- [ ] **Request DTOs** — `CreateInterventionRequest`, `UpdateInterventionRequest` (resolutionNotes, rootCause,
  resolutionStatus, durationMinutes), `CloseInterventionRequest` (as `record`s)
- [ ] **Response DTOs** — `InterventionResponse` (full: id, faultId, crewId, crewName, startedAt, endedAt,
  durationMinutes, resolutionStatus, notes, rootCause) (as `record`)
- [ ] **Service interface** — extend `InterventionService`: `update(Long id, UpdateInterventionRequest)`,
  `close(Long id, CloseInterventionRequest)`, `getById(Long id)`, `getByCrewId(Long crewId)`
- [ ] **Service implementation** — extend `InterventionServiceImpl`: fix exceptions; add update, close (triggers status
  change to RESOLVED/CLOSED); set `startedAt` on create, `endedAt` on close
- [ ] **Controller + endpoints** — fix `InterventionController` to `/api/v1/interventions`; keep
  `POST /api/v1/faults/{faultId}/interventions`; add `GET /{id}`, `PUT /{id}`, `PUT /{id}/close`;
  `@PreAuthorize(FIELD_CREW, DISPATCHER)`
- [ ] **Mapper(s)** — `InterventionMapper`
- [ ] **Custom exceptions** — `BadRequestException` (closing already-closed intervention); reuse others

---

## Module 13 — Weather Integration

> Covers: fetch weather data from external API per location, store in `WeatherData`, use in risk prediction.
> `WeatherData` entity + `WeatherDataRepository` (empty) exist. No service or client.

- [x] **Repository custom methods** — `WeatherDataRepository`: `findLatestByLocationId`,
  `findByLocationIdAndRecordedAtBetween`
- [x] **Request DTOs** — `WeatherFetchRequest` (locationId) (as `record`)
- [x] **Response DTOs** — `WeatherDataResponse` (temperature, windSpeed, humidity, precipitation, condition,
  recordedAt) (as `record`)
- [x] **Service interface** — `WeatherService`: `fetchAndStore(Long locationId)`, `getLatest(Long locationId)`,
  `getHistory(Long locationId, LocalDateTime from, LocalDateTime to)`
- [x] **Service implementation** — `WeatherServiceImpl`: calls `WeatherClient` (WebClient, URL from
  `weather.api.base-url` + `weather.api.key`), maps response to `WeatherData`, saves
- [x] **Controller + endpoints** — `WeatherController`: `POST /api/v1/weather/fetch` (ADMIN/OPERATOR),
  `GET /api/v1/locations/{id}/weather`, `GET /api/v1/locations/{id}/weather/history`
- [x] **Mapper(s)** — `WeatherDataMapper`
- [x] **Custom exceptions** — `ExternalApiException` (weather API unreachable); reuse `ResourceNotFoundException`

> **Also create:** `client/WeatherClient` using `WebClient`, configured via `application.properties`.

---

## Module 14 — Notifications

> Covers: send notifications to customer on fault status change; notify crew on assignment; list/mark-read.
> Partially done: `NotificationService/Impl`, `NotificationController` exist.

- [ ] **Repository custom methods** — `SystemNotificationRepository`: `findByUserIdOrderByCreatedAtDesc`,
  `findByUserIdAndNotificationStatus`, `countByUserIdAndNotificationStatus(UNREAD)`
- [ ] **Request DTOs** — none (notifications are created internally by service events)
- [ ] **Response DTOs** — `NotificationResponse` (id, title, message, type, isRead, createdAt) (as `record`)
- [ ] **Service interface** — extend `NotificationService`:
  `notifyCustomerStatusChange(Long faultId, FaultStatus newStatus)`, `notifyCrewAssignment(Long assignmentId)`,
  `markAsRead(Long notificationId, Long userId)`, `markAllAsRead(Long userId)`, `getForUser(Long userId, Pageable)`
- [ ] **Service implementation** — extend `NotificationServiceImpl`: fix exceptions, implement markAsRead, integrate
  with fault status change flow (call from `FaultWorkflowService`) and assignment flow
- [ ] **Controller + endpoints** — fix `NotificationController` to `/api/v1/notifications`; `GET` (paginated,
  authenticated user), `PUT /{id}/read`, `PUT /read-all`, `GET /unread-count`
- [ ] **Mapper(s)** — `NotificationMapper`
- [ ] **Custom exceptions** — `UnauthorizedException` (marking another user's notification); reuse others

---

## Module 15 — Dashboard & Analytics

> Covers: KPI summary (active faults, avg response time, avg resolution time, critical count), region analytics, fault
> type analytics, crew performance, map data endpoints.

- [x] **Repository custom methods** — `FaultReportRepository`: countActiveFaults, countCriticalActiveFaults,
  countFaultsToday, avgResponseTimeMinutes, avgResolutionTimeMinutes, countByRegion, countByFaultType,
  findActiveFaultsForMap, countByPeriodDay/Week/Month (all native SQL via FaultStatusHistory joins).
  `InterventionRepository`: countByCrew. `RiskPredictionRepository`: findTopRiskZonesWithLocation (JOIN FETCH).
  `FaultAssigmentRepository`: countDistinctActiveCrews, findActiveCrewIds.
- [x] **Response DTOs** — `DashboardKpiResponse`, `FaultsByRegionResponse`, `FaultsByTypeResponse`,
  `FaultsByPeriodResponse`, `CrewPerformanceResponse`, `MapFaultResponse`, `MapRiskZoneResponse`,
  `MapCrewLocationResponse` (all records in `dto/response/dashboard/`)
- [x] **Service interface** — `DashboardService`: getKpis, getFaultsByRegion, getFaultsByType, getFaultsByPeriod(
  groupBy, from, to), getCrewPerformance, getActiveFaultsForMap, getRiskZonesForMap, getCrewsForMap
- [x] **Service implementation** — `DashboardServiceImpl`: @Transactional(readOnly=true), all queries delegated to
  repositories; groupBy dispatch in getFaultsByPeriod; BadRequestException for invalid groupBy or date range
- [x] **Controller + endpoints** — `DashboardController` at `/api/v1/dashboard`: GET /kpis, /faults-by-region,
  /faults-by-type, /faults-by-period?groupBy&from&to, /crew-performance, /map/faults, /map/risk-zones, /map/crews;
  @PreAuthorize per role
- [x] **Limitations noted** — `Intervention` has no timestamp fields → avgDurationMin/efficiencyPercent return 0.0.
  `Crew` has no coordinates → MapCrewLocationResponse lat/lon are null. Active status derived from FaultStatusHistory (
  latest entry). RiskPrediction.probability used as riskScore; riskLevel derived from thresholds (>0.7=HIGH, >
  0.4=MEDIUM, else LOW).

---

## Module 16 — Import

> Covers: upload CSV/Excel file of faults, locations, or equipment; validate; persist; track in `ImportBatch`.
> `ImportBatch` entity + `ImportBatchRepository` (empty) exist. No service or controller.

- [x] **Repository custom methods** — `ImportBatchRepository`: `findByCreatedByUserId`, `findByImportStatus`
- [x] **Request DTOs** — none (uses `MultipartFile` + `@RequestParam type`)
- [x] **Response DTOs** — `ImportBatchResponse` (id, fileName, fileType, totalRecords, successfulRecords, failedRecords,
  status, createdAt, errors list) (as `record`)
- [x] **Service interface** — `ImportService`: `importFaults(MultipartFile, Long userId)`,
  `importLocations(MultipartFile, Long userId)`, `importEquipment(MultipartFile, Long userId)`,
  `getBatchStatus(Long batchId)`
- [x] **Service implementation** — `ImportServiceImpl`: detect CSV vs XLSX, use Apache POI for Excel, validate each row,
  persist valid rows, accumulate errors, save `ImportBatch` with counts
- [x] **Controller + endpoints** — `ImportController`: `POST /api/v1/import/faults`, `POST /api/v1/import/locations`,
  `POST /api/v1/import/equipment`, `GET /api/v1/import/{batchId}`; `@PreAuthorize(ADMIN, OPERATOR)`
- [x] **Mapper(s)** — row-to-entity mapping logic inside service (no separate mapper class)
- [x] **Custom exceptions** — `BadRequestException` (unsupported file format, empty file); row-level errors collected
  into list (not thrown)

---

## Module 17 — Export

> Covers: export fault list, interventions, analytics to CSV or Excel file download.
> `ExportBatch` entity + `ExportBatchRepository` (empty) exist. No service or controller.

- [x] **Repository custom methods** — `ExportBatchRepository`: `findByCreatedByUserId`, `findByExportStatus`
- [x] **Request DTOs** — `ExportRequest` (type: FAULTS/INTERVENTIONS/ANALYTICS, format: CSV/XLSX, filters) (as `record`)
- [x] **Response DTOs** — `ExportBatchResponse` (id, type, format, status, downloadUrl, createdAt) (as `record`); actual
  file returned as `ResponseEntity<Resource>`
- [x] **Service interface** — `ExportService`: `exportFaults(ExportRequest, Long userId)`,
  `exportInterventions(ExportRequest, Long userId)`, `exportAnalytics(ExportRequest, Long userId)`,
  `getFile(Long batchId, User requestingUser)`
- [x] **Service implementation** — `ExportServiceImpl`: query data, use Apache POI for XLSX / Apache Commons CSV for
  CSV, write to configured storage path, record in `ExportBatch`
- [x] **Controller + endpoints** — `ExportController`: `POST /api/v1/export/faults`,
  `POST /api/v1/export/interventions`, `POST /api/v1/export/analytics`, `GET /api/v1/export/{batchId}/download`;
  `@PreAuthorize(MANAGER, ADMIN, DISPATCHER)`
- [x] **Mapper(s)** — none; data fetched and written inline in service
- [x] **Custom exceptions** — `ResourceNotFoundException` (batch not found or file missing); `BadRequestException` (
  invalid export type)

---

## Module 18 — Audit Log

> Covers: AOP-based automatic logging of critical actions (create fault, status change, assign crew, priority change,
> close case); admin view of log.
> `AuditLog` entity, `AuditLogRepository` (empty), `AuditLogService/Impl` exist.

- [ ] **Repository custom methods** — `AuditLogRepository`: `findByUserIdOrderByTimestampDesc`,
  `findByEntityNameAndEntityId`, `findAllByOrderByTimestampDesc(Pageable)`
- [ ] **Request DTOs** — none (logs created automatically)
- [ ] **Response DTOs** — `AuditLogResponse` (id, actorEmail, actorRole, entityName, entityId, actionType, oldValue,
  newValue, timestamp) (as `record`)
- [ ] **Service interface** — extend `AuditLogService`:
  `log(String entityName, Long entityId, String actionType, String oldValue, String newValue, User actor)`,
  `getAll(Pageable)`, `getByEntity(String entityName, Long entityId)`
- [ ] **Service implementation** — extend `AuditLogServiceImpl`: implement persistence; fix to use proper entity fields
  from `AuditLog`
- [ ] **Controller + endpoints** — `AuditLogController`: `GET /api/v1/audit-logs` (paginated, ADMIN/MANAGER),
  `GET /api/v1/audit-logs/{entityName}/{entityId}` (per-entity history)
- [ ] **Mapper(s)** — `AuditLogMapper`
- [ ] **Custom exceptions** — none needed

> **Also create in this module:** `aspect/AuditAspect` — `@Around` on annotated service methods; extract actor from
`SecurityContextHolder`; call `AuditLogService.log(...)`. Create `@AuditAction` annotation.

---

## Cross-cutting tasks (do before or alongside Module 1)

- [x] Create `exception/ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`,
  `ConflictException` — done
- [ ] Create `exception/AiServiceException`, `ExternalApiException`, `ImportValidationException` — deferred to Modules
  7/13/16
- [x] Create `exception/GlobalExceptionHandler` (`@RestControllerAdvice`) — done
- [x] DTOs as `record` types in `dto/request/` and `dto/response/` — done for Modules 1-4
- [x] Fix controller base paths to `/api/v1/...` — done (AuthController kept at `/api/auth/` because security config
  whitelists `/api/auth/**` and cannot be changed)
- [ ] Add `application.properties` entries: `ai.service.base-url`, `weather.api.base-url`, `weather.api.key`
- [ ] Create `client/` package with `AiClient` (Module 7) and `WeatherClient` (Module 13)

---

## Progress tracker

| #  | Module                | Status                |
|----|-----------------------|-----------------------|
| 1  | Auth & Users          | Done ✓                |
| 2  | Customers             | Done ✓                |
| 3  | Regions & Locations   | Done ✓                |
| 4  | Equipment             | Done ✓                |
| 5  | Fault Reports (core)  | In progress (partial) |
| 6  | Attachments           | In progress (partial) |
| 7  | AI Classification     | Not started           |
| 8  | Risk Prediction       | Not started           |
| 9  | Priority Scoring      | Not started           |
| 10 | Crews & Crew Members  | In progress (partial) |
| 11 | Fault Assignment      | In progress (partial) |
| 12 | Interventions         | In progress (partial) |
| 13 | Weather Integration   | Done ✓                |
| 14 | Notifications         | In progress (partial) |
| 15 | Dashboard & Analytics | Done ✓                |
| 16 | Import                | Done ✓                |
| 17 | Export                | Done ✓                |
| 18 | Audit Log             | In progress (partial) |
