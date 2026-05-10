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

- [x] **Entity changes** — `FaultReport`: added `trackingCode` (unique index, `FLT-YYYYMMDD-XXXXX` format),
  `reportedAt`, `sourceType` (`FaultSourceType` enum: CUSTOMER_PORTAL / OPERATOR_CALL_CENTER / IMPORTED); `@PrePersist`
  sets defaults (protects `ImportServiceImpl` from breaking). `FaultStatusHistory`: added `note`, `customerVisible`,
  `changedBy` (User), `changedByCustomer`. `FaultSourceType` enum created.
- [x] **Repository custom methods** — `FaultReportRepository` extended with `JpaSpecificationExecutor<FaultReport>`,
  `findByTrackingCode(String)`, `existsByTrackingCode(String)`;
  `FaultStatusHistoryRepository`: `findByFaultReportIdOrderByChangedAtDesc(Long)`;
  `AttachmentRepository`: `existsByIdAndFaultReportId(Long, Long)` (used by Module 6)
- [x] **Request DTOs** — `CreateFaultReportRequest` (CUSTOMER: title, description, locationId, faultType),
  `OperatorCreateFaultRequest` (+ optional customerId), `UpdateFaultReportRequest`, `FaultStatusUpdateRequest`
  (status, note, customerVisible) — all records
- [x] **Response DTOs** — `FaultReportResponse` (full, 15 fields), `FaultReportSummaryResponse` (paginated list),
  `FaultStatusHistoryResponse` (id, faultStatus, changedAt, note, customerVisible, changedByName),
  `TrackFaultResponse` (tracking code lookup: filters to customerVisible=true history) — all records
- [x] **Utility** — `util/TrackingCodeGenerator` — generates `FLT-YYYYMMDD-XXXXX`, 5-retry uniqueness check via
  `existsByTrackingCode`; `@PrePersist` on `FaultReport` provides UUID-based fallback for direct-entity paths
  (ImportServiceImpl)
- [x] **Specification** — `specification/FaultSpecification` — 8 static factory methods: hasFaultType,
  hasFaultPriority, hasFaultClassification, hasLocationId, hasRegionId, hasCustomerId, reportedAfter, reportedBefore,
  hasCurrentStatus (correlated subquery on FaultStatusHistory to find latest status per fault)
- [x] **Service interface** — `FaultReportService`: old 4 methods kept but `@Deprecated`; added `createByCustomer`,
  `createByOperator`, `getFiltered(... Pageable) → Page<FaultReportSummaryResponse>`, `getMyFaults`,
  `getFaultById`, `getByTrackingCode`, `updateFault`, `updateStatus`, `getStatusHistory`, `delete`
- [x] **Service implementation** — `FaultReportServiceImpl` rewritten: all 4 `RuntimeException("Not found")` replaced
  with `ResourceNotFoundException`; `@Transactional` on class; new constructor injects UserRepository,
  FaultStatusHistoryRepository, FaultReportMapper, FaultStatusHistoryMapper, TrackingCodeGenerator;
  `buildFault()` private helper shared by all creation paths; AI lifecycle (`runAiLifecycle`) unchanged
- [x] **Workflow service** — `FaultWorkflowService/Impl`: added 6-arg `changeStatus(fault, status, changedBy,
  changedByCustomer, note, customerVisible)`; old 2-arg delegates to it (existing callers unaffected)
- [x] **Controller + endpoints** — `FaultController` at `/api/v1/faults` with 10 endpoints:
  `POST /` (CUSTOMER), `POST /operator` (OPERATOR/ADMIN/MANAGER),
  `GET /` (paginated + filtered, ADMIN/OPERATOR/MANAGER/DISPATCHER),
  `GET /my` (CUSTOMER), `GET /track/{code}` (authenticated), `GET /{id}` (authenticated),
  `PUT /{id}` (ADMIN/OPERATOR), `PATCH /{id}/status` (authenticated), `GET /{id}/history` (authenticated),
  `DELETE /{id}` (ADMIN)
- [x] **Mapper(s)** — `FaultReportMapper` (toResponse, toSummaryResponse), `FaultStatusHistoryMapper` (toResponse)
- [x] **Migration** — auto-DDL (`ddl-auto=create-drop`) + Flyway disabled; no migration scripts needed for dev/H2.
  `ImportServiceImpl` patched with `fault.setSourceType(FaultSourceType.IMPORTED)` (minimal fix, no structural change).
  Old DTOs (`CreateFaultReportDto`, `ChangeStatusDto`, `FaultReportResponseDto`, `StatusHistoryDto`) marked
  `@Deprecated`.

---

## Module 6 — Attachments

> Covers: upload file(s) to a fault report, download attachment, list attachments per fault.

- [x] **Entity changes** — `Attachment`: added `fileType` (MIME, not null), `fileSize` (bytes, not null), `uploadedAt`
  (not null), `uploadedByUser` (ManyToOne User, nullable lazy), `uploadedByCustomer` (ManyToOne Customer, nullable
  lazy);
  enforced in service (exactly one non-null per upload)
- [x] **Repository custom methods** — `AttachmentRepository`: `findByFaultReportId` (existed),
  `existsByIdAndFaultReportId`
  (new — used for ownership check in download/delete)
- [x] **Request DTOs** — none (uses `MultipartFile` + `@AuthenticationPrincipal`)
- [x] **Response DTOs** — `AttachmentResponse` (id, fileName, fileType, fileSize, uploadedAt) (record)
- [x] **Service interface** — `AttachmentService`: old 3 methods kept but `@Deprecated`; added
  `upload(Long, MultipartFile, String callerEmail) → AttachmentResponse`,
  `listAttachments(Long faultId) → List<AttachmentResponse>`,
  `download(Long faultId, Long attachmentId, String callerEmail) → Resource`,
  `delete(Long faultId, Long attachmentId)`
- [x] **Service implementation** — `AttachmentServiceImpl` rewritten with `@RequiredArgsConstructor`:
  validates MIME type against `app.attachments.allowed-types`, validates size against `app.attachments.max-size-bytes`;
  writes to `app.attachments.storage-path` with UUID-based stored filename; sets all entity fields;
  download returns `FileSystemResource`; delete removes physical file via `Files.deleteIfExists` then DB row;
  old legacy `upload()` sets `filePath="legacy-upload"` to satisfy NOT NULL constraint without real disk write
- [x] **Controller + endpoints** — `AttachmentController` at `/api/v1/faults/{faultId}/attachments`:
  `POST /` (upload, authenticated), `GET /` (list, authenticated),
  `GET /{attachmentId}/download` (authenticated), `DELETE /{attachmentId}` (authenticated)
- [x] **Mapper(s)** — `helpers/AttachmentMapper` (`@Component`)
- [x] **Config** — `application.properties`: `app.attachments.storage-path=./uploads/attachments`,
  `app.attachments.max-size-bytes=10485760`,
  `app.attachments.allowed-types=image/jpeg,image/png,image/jpg,application/pdf`
- [x] **gitignore** — `/uploads/` added
- [x] **Custom exceptions** — `BadRequestException` for invalid file type/size; `ResourceNotFoundException` for missing
  fault/attachment; `AttachmentDto` marked `@Deprecated`

---

## Module 7 — AI Classification

> Covers: POST fault description to Python AI service, store result, display to operators, allow manual override.

- [x] **Entity** — `FaultClassificationResult` (separate table; `FaultClassification` enum kept untouched as category
  type). Keywords stored via `@ElementCollection` in `fault_classification_result_keywords` join table
  (`keyword VARCHAR(100)`). `isFallback` / `nlpProcessed` flags persisted so fallback rows can be re-queued.
- [x] **Repository** — `FaultClassificationResultRepository`: `findByFaultReportId`, `findByPredictedFaultCategory`,
  `findByNlpProcessed`
- [x] **Request DTOs** — `ManualClassificationOverrideRequest` (predictedFaultCategory, predictedSeverity, safetyRisk,
  extractedKeywords) (record)
- [x] **Response DTOs** — `ClassificationResponse` (id, faultReportId, predictedFaultCategory, predictedSeverity,
  classificationConfidence, nlpProcessed, extractedKeywords, classifiedAt, isFallback) (record)
- [x] **Mapper** — `helpers/FaultClassificationResultMapper` (`@Component`)
- [x] **Service interface** — `FaultClassificationService`: `classifyFault`, `reclassify`, `getByFault`,
  `manualOverride`
- [x] **Service implementation** — `FaultClassificationServiceImpl`: `classifyFault` skips re-call if
  `nlpProcessed=true`; `reclassify` always re-runs; AI category validated against `FaultClassification` enum (warns,
  stores raw string); `manualOverride` sets `nlpProcessed=true`, `isFallback=false`; async TODO noted
- [x] **Controller** — `FaultClassificationController` at `/api/v1/faults/{faultId}/classification`:
  `POST /classify` (DISPATCHER/OPERATOR/ADMIN), `POST /reclassify` (same roles), `GET /` (authenticated),
  `PUT /override` (DISPATCHER/ADMIN)
- [x] **Lifecycle wiring** — `FaultReportServiceImpl.createFault()` calls `classifyFault()` after `faultRepo.save()`;
  wrapped in try/catch so classification failure never blocks fault creation; logs warning on failure
- [x] **Shared AI infrastructure** — `client/ai/AiClient` + 6 DTOs in `client/ai/dto/` + `AiHealthCheckService`
  (built as Module 7 prerequisite); uses `RestClient` + `SimpleClientHttpRequestFactory` for timeout;
  graceful fallbacks on all three methods

> **Note:** `predictedFaultCategory` stored as raw `String` (AI value); validated against `FaultClassification` enum
> but never rejected — unrecognized categories are logged as warnings. `nlpProcessed=false` marks fallback rows for
> future re-run via `reclassify` endpoint.

---

## Module 8 — Risk Prediction

> Covers: trigger risk prediction per location/equipment/region, store result, show contributing factors.

- [x] **Entity restructure** — `RiskPrediction` replaced: removed legacy `faultReport` OneToOne + `probability` +
  `recommendation`; added `location` (ManyToOne nullable), `equipment` (ManyToOne nullable), `riskScore`, `riskLevel`,
  `contributingFactors` (TEXT, pipe-separated), `predictionDate`, `isFallback`. `FaultReport.riskPrediction`
  back-reference removed (was unused in all service code).
- [x] **Repository** — `RiskPredictionRepository` rewritten: `findByLocationId`, `findByEquipmentId`,
  `findByRiskLevel`, `findTop10ByOrderByRiskScoreDesc`, `findByPredictionDateAfter`,
  `findTopByLocationIdOrderByPredictionDateDesc`, `findTopByEquipmentIdOrderByPredictionDateDesc`;
  `findTopRiskZonesWithLocation()` updated to join directly on `location` (removed faultReport join).
  `FaultReportRepository` extended with `countByLocationId` for fault frequency input.
- [x] **Request DTOs** — `TriggerRiskPredictionRequest` (locationId, equipmentId, regionId — all optional) (record)
- [x] **Response DTOs** — `RiskPredictionResponse` (id, locationId, equipmentId, riskScore, riskLevel,
  contributingFactors, predictionDate, isFallback) (record)
- [x] **Mapper** — `helpers/RiskPredictionMapper` (`@Component`): `toResponse`, `serializeFactors`,
  `deserializeFactors`
- [x] **Service interface** — `RiskPredictionService`: `predictForLocation`, `predictForEquipment`,
  `predictForRegion`, `getLatestForLocation`, `getLatestForEquipment`, `getHighRiskZones(int limit)`,
  `getHistoryForLocation`
- [x] **Service implementation** — `RiskPredictionServiceImpl`: wires weather directly via
  `weatherService.getLatestForRiskInput(locationId)` (Optional, never throws); `criticalityLevel` defaults to 1
  (no field on Location), `equipmentAgeYears` defaults to 0.0 (no install date on Equipment) — noted inline;
  `predictForRegion` iterates per-location, wraps each call in try/catch, adds async TODO; all fallback
  results are persisted
- [x] **Controller** — `RiskPredictionController` at `/api/v1/risk-predictions`:
  `POST /location/{id}`, `POST /equipment/{id}` (DISPATCHER/MANAGER/ADMIN),
  `POST /region/{id}` (MANAGER/ADMIN),
  `GET /location/{id}/latest`, `GET /equipment/{id}/latest`, `GET /location/{id}/history` (authenticated),
  `GET /high-risk?limit=10` (authenticated)
- [x] **Dashboard updated** — `DashboardServiceImpl.toMapRiskZoneResponse()` updated to use `rp.getLocation()`
  and `rp.getRiskScore()` / `rp.getRiskLevel()` directly

> **Note on defaults:** `criticalityLevel=1` and `equipmentAgeYears=0.0` are passed when no source data exists.
> These can be improved once Module 5 adds fault timestamps (for recency weighting) and equipment gets an
> `installedAt` field.

---

## Module 9 — Priority Scoring

> Covers: auto-calculate priority score for active faults, assign category (CRITICAL/HIGH/MEDIUM/LOW), show explanation,
> allow manual override.

- [x] **Entity** — `FaultPriorityRecord` (separate table; `FaultPriority` enum kept as categorical type).
  Fields: `id`, `faultReport` (OneToOne unique), `priorityLevel` (FaultPriority enum), `priorityScore`, `explanation`
  (TEXT), `calculatedAt`, `calculationSource` (`"AI"`, `"FALLBACK"`, `"MANUAL"`), `isFallback`.
- [x] **Repository** — `FaultPriorityRepository`: `findByFaultReportId`, `findByPriorityLevel`,
  `findTop10ByOrderByPriorityScoreDesc`, `findByCalculatedAtAfter`;
  `findTopActiveFaultPriorities(limit)` — native SQL JOIN on FaultStatusHistory to exclude RESOLVED/CLOSED faults.
  `FaultReportRepository` extended with `countByLocationIdReportedAfter` (native SQL via FaultStatusHistory join)
  for recurrence check.
- [x] **Request DTOs** — `ManualPriorityOverrideRequest` (priorityLevel, explanation) (record)
- [x] **Response DTOs** — `FaultPriorityResponse` (id, faultReportId, priorityLevel, priorityScore, explanation,
  calculatedAt, calculationSource, isFallback) (record)
- [x] **Mapper** — `helpers/FaultPriorityMapper` (`@Component`)
- [x] **Service interface** — `FaultPriorityService`: `calculatePriority`, `recalculate`, `getByFault`,
  `manualOverride`, `getTopPriorityFaults`
- [x] **Service implementation** — `FaultPriorityServiceImpl`: `calculatePriority` skips re-call if non-fallback record
  exists; `recalculate` always re-runs; reads FaultClassificationResult if available (falls back to FaultReport fields);
  `weatherService.getLatestForRiskInput` used (never throws); recurrence check via `countByLocationIdReportedAfter`
  (>1 fault = recurring); `manualOverride` sets `calculationSource="MANUAL"`, `isFallback=false`, also updates
  `FaultReport.faultPriority`; `getTopPriorityFaults` uses native-SQL active-fault filter.
- [x] **Controller** — `FaultPriorityController` at `/api/v1/faults/{faultId}/priority`:
  `POST /calculate` (DISPATCHER/OPERATOR/ADMIN), `POST /recalculate` (DISPATCHER/MANAGER/ADMIN),
  `GET /` (authenticated), `PUT /override` (DISPATCHER/MANAGER/ADMIN).
  `PriorityQueryController` at `/api/v1/priorities`: `GET /top?limit=10` (DISPATCHER/MANAGER/ADMIN).
- [x] **Lifecycle wiring** — `FaultReportServiceImpl.createFault()` calls `calculatePriority()` after
  `classifyFault()`; both wrapped in independent try/catch; order: save fault → classify → calculate priority →
  set REPORTED status.

> **Note:** `affectedUsersEstimate` defaults to `0` (no field on `FaultReport`); `locationCriticality` uses the
> region name as a proxy label (no criticality field on `Location`). Both can be improved once those fields exist.

---

## Module 10 — Crews & Crew Members

> Covers: admin/dispatcher view and CRUD of crews and their members; availability tracking.
> Partially done: `CrewService/Impl`, `CrewController` exist (only list + getById + assignToFault).

- [x] **Repository custom methods** — `CrewRepository`: `findAvailableCrews` (no active assignment), `findByName`;
  `CrewMemberRepository`: `findByCrewId`, `findByUserId`
- [x] **Request DTOs** — `CreateCrewRequest`, `UpdateCrewRequest`, `AddCrewMemberRequest` (userId, position) (as
  `record`s)
- [x] **Response DTOs** — `CrewResponse` (full, including members list), `CrewSummaryResponse`, `CrewMemberResponse` (as
  `record`s)
- [x] **Service interface** — extend `CrewService`: `create`, `update`, `delete`, `addMember`, `removeMember`,
  `getAvailable`; new `CrewMemberService`
- [x] **Service implementation** — rewrite `CrewServiceImpl` to fix exceptions, add missing CRUD; create
  `CrewMemberServiceImpl`
- [x] **Controller + endpoints** — fix `CrewController` to `/api/v1/crews`; add `POST`, `PUT /{id}`, `DELETE /{id}`,
  `GET /available`; `POST /{id}/members`, `DELETE /{id}/members/{memberId}`; `@PreAuthorize(DISPATCHER, ADMIN)`
- [x] **Mapper(s)** — `CrewMapper`, `CrewMemberMapper`
- [x] **Custom exceptions** — reuse `ResourceNotFoundException`, `ConflictException`

---

## Module 11 — Fault Assignment

> Covers: assign crew to fault, AI recommendation, accept/reject assignment, reassign.
> `FaultAssignment` entity and `FaultAssigmentRepository` (typo in name) exist. Assignment logic currently embedded in
`CrewServiceImpl`.

- [x] **Repository custom methods** — `FaultAssigmentRepository`: `findByFaultReportId`, `findByCrewId`,
  `findActiveByFaultReportId`, `findPendingByCrewId`
- [x] **Request DTOs** — `AssignCrewRequest` (crewId, assignmentNote), `ReassignRequest` (newCrewId, reason),
  `AcceptAssignmentRequest` (accepted: boolean) (as `record`s)
- [x] **Response DTOs** — `AssignmentResponse` (id, faultId, crewId, crewName, status, assignedAt, acceptedAt, note),
  `CrewRecommendationResponse` (recommended crew list with score/reason) (as `record`s)
- [x] **Service interface** — `FaultAssignmentService`: `assignCrew(Long faultId, AssignCrewRequest)`,
  `reassign(Long assignmentId, ReassignRequest)`, `acceptAssignment(Long assignmentId)`, `getByFault(Long faultId)`,
  `recommendCrew(Long faultId)`
- [x] **Service implementation** — `FaultAssignmentServiceImpl`: extract assignment logic from CrewServiceImpl;
  implement recommendation (call AI client or simple heuristic by availability + location proximity)
- [x] **Controller + endpoints** — `FaultAssignmentController`: `POST /api/v1/faults/{id}/assignments` (DISPATCHER),
  `PUT /api/v1/assignments/{id}/accept` (FIELD_CREW), `PUT /api/v1/assignments/{id}/reassign` (DISPATCHER),
  `GET /api/v1/faults/{id}/assignments`, `GET /api/v1/faults/{id}/crew-recommendations`
- [x] **Mapper(s)** — `FaultAssignmentMapper`
- [x] **Custom exceptions** — `ConflictException` (crew already assigned); reuse `ResourceNotFoundException`

---

## Module 12 — Interventions

> Covers: start intervention, update notes/status, close case (resolved/closed).
> Partially done: `InterventionService/Impl`, `InterventionController` exist (create + list by fault only).

- [x] **Repository custom methods** — `InterventionRepository`: `findByFaultReportId` (exists in impl), `findByCrewId`,
  `findByFaultReportIdAndResolutionStatus`
- [x] **Request DTOs** — `CreateInterventionRequest`, `UpdateInterventionRequest` (resolutionNotes, rootCause,
  resolutionStatus, durationMinutes), `CloseInterventionRequest` (as `record`s)
- [x] **Response DTOs** — `InterventionResponse` (full: id, faultId, crewId, crewName, startedAt, endedAt,
  durationMinutes, resolutionStatus, notes, rootCause) (as `record`)
- [x] **Service interface** — extend `InterventionService`: `update(Long id, UpdateInterventionRequest)`,
  `close(Long id, CloseInterventionRequest)`, `getById(Long id)`, `getByCrewId(Long crewId)`
- [x] **Service implementation** — extend `InterventionServiceImpl`: fix exceptions; add update, close (triggers status
  change to RESOLVED/CLOSED); set `startedAt` on create, `endedAt` on close
- [x] **Controller + endpoints** — fix `InterventionController` to `/api/v1/interventions`; keep
  `POST /api/v1/faults/{faultId}/interventions`; add `GET /{id}`, `PUT /{id}`, `PUT /{id}/close`;
  `@PreAuthorize(FIELD_CREW, DISPATCHER)`
- [x] **Mapper(s)** — `InterventionMapper`
- [x] **Custom exceptions** — `BadRequestException` (closing already-closed intervention); reuse others

---

## Module 13 — Weather Integration

> Covers: fetch weather data from external API per location, store in `WeatherData`, use in risk prediction.
> **Built FIRST (before Modules 7/8/9)** so that Module 8 (Risk Prediction) can wire WeatherService directly
> during its own implementation — no back-fill step needed.

- [x] **Entity** — `WeatherData` (id, location FK, recordedAt, temperature, windSpeed, humidity, precipitation,
  weatherCondition enum, sourceApi)
- [x] **Repository** — `WeatherDataRepository`: `findByLocationId`, `findTopByLocationIdOrderByRecordedAtDesc`,
  `findByLocationIdAndRecordedAtBetween`, `deleteByRecordedAtBefore`
- [x] **Response DTO** — `WeatherDataResponse` (record in `dto/response/`)
- [x] **Provider abstraction** — `client/weather/` package: `WeatherProvider` interface, `WeatherFetchResult` record,
  `WmoCodeMapper` (WMO codes → SUNNY/CLOUDY/RAINY/SNOW/STORM), `OpenMeteoWeatherProvider`
  (`@ConditionalOnProperty(weather.provider=open-meteo)`, uses `RestClient`, no API key required)
- [x] **Service interface** — `WeatherService`: `fetchAndStore`, `fetchAndStoreForAll`, `getLatest`, `getHistory`,
  `getLatestForRiskInput` (never throws — for Module 8)
- [x] **Service implementation** — `WeatherServiceImpl`: inline mapping; `fetchAndStoreForAll` iterates all locations
  (Location has no criticalityLevel field — `only-critical-locations` flag reserved for future use); 100 ms delay
  between calls when >10 locations
- [x] **Scheduler** — `WeatherRefreshScheduler` (`@ConditionalOnProperty(weather.refresh.enabled=true)`,
  `@Scheduled(cron)`) in `scheduling/` package; `@EnableScheduling` already on main class
- [x] **Controller + endpoints** — `WeatherController` at `/api/v1/weather`:
  `POST /fetch/{locationId}` (DISPATCHER/MANAGER/ADMIN), `POST /fetch/all` (ADMIN),
  `GET /location/{id}/latest` (authenticated), `GET /location/{id}/history?from&to` (DISPATCHER/MANAGER/ADMIN,
  max 90 days)
- [x] **ExternalApiException handler** — added 503 handler to `GlobalExceptionHandler`
- [x] **Unit test** — `WmoCodeMapperTest` covers all 5 WeatherCondition values + unknown-code fallback
- [x] **Config** — `application.properties`: `weather.provider`, `weather.api.base-url`, `weather.api.timeout-seconds`,
  `weather.refresh.enabled`, `weather.refresh.cron`, `weather.refresh.only-critical-locations`
- [x] **DB** — auto-created by `ddl-auto=create-drop` (dev/H2); index on `(location_id, recorded_at)`

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
| 5  | Fault Reports (core)  | Done ✓                |
| 6  | Attachments           | Done ✓                |
| 7  | AI Classification     | Done ✓                |
| 8  | Risk Prediction       | Done ✓                |
| 9  | Priority Scoring      | Done ✓                |
| 10 | Crews & Crew Members  | Done ✓                |
| 11 | Fault Assignment      | Done ✓                |
| 12 | Interventions         | Done ✓                |
| 13 | Weather Integration   | Done ✓                |
| 14 | Notifications         | In progress (partial) |
| 15 | Dashboard & Analytics | Done ✓                |
| 16 | Import                | Done ✓                |
| 17 | Export                | Done ✓                |
| 18 | Audit Log             | In progress (partial) |
