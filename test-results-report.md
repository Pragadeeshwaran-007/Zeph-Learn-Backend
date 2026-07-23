# ZephLearn Backend — Automated Test Results Report

## Executive Summary

| Metric | Value |
|---|---|
| **Total Tests Run** | 58 |
| **Total Passed** | 58 |
| **Total Failed** | 0 |
| **Overall Pass Percentage** | 100.00% |

---

## Detailed Test Results by Test Class

### 1. AuthServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_ReturnAuthResponse_When_SignupWithNewEmail` | User signup with a new email address | Creates user, hashes password with BCrypt, generates JWT token, returns AuthResponse | User saved with BCrypt hash, JWT generated, AuthResponse returned | ✅ PASS |
| `should_ThrowException_When_SignupWithDuplicateEmail` | User signup with an already registered email | Rejects registration, throws `RuntimeException("Email already exists")` | Threw `RuntimeException("Email already exists")`, no user saved | ✅ PASS |
| `should_HashPassword_When_SigningUp` | Password encryption during user signup | Calls `PasswordEncoder.encode()` before persisting user entity | `passwordEncoder.encode()` called; plain text password never saved | ✅ PASS |
| `should_ReturnJwtToken_When_LoginCredentialsAreValid` | Local user authentication with valid credentials | Validates password match, updates user streak, returns valid JWT token | Password matched, streak updated, valid JWT returned | ✅ PASS |
| `should_ThrowException_When_LoginEmailDoesNotExist` | Login attempt with unregistered email | Throws `RuntimeException("Invalid email or password")` | Threw `RuntimeException("Invalid email or password")` | ✅ PASS |
| `should_ThrowException_When_LoginPasswordIsWrong` | Login attempt with invalid password | Throws `RuntimeException("Invalid email or password")` | Threw `RuntimeException("Invalid email or password")` | ✅ PASS |
| `should_ThrowException_When_LoginAttemptedForGoogleAccount` | Password login attempt on Google-authenticated user | Blocks login, throws `RuntimeException("This account uses Google Sign-In...")` | Threw `RuntimeException("This account uses Google Sign-In...")` | ✅ PASS |
| `should_UpdateStreakOnLogin_When_CredentialsAreValid` | Streak increment upon successful login | Invokes `StreakService.updateStreak()` and includes updated streak in response | `StreakService.updateStreak()` called, updated streak returned in response | ✅ PASS |
| `should_CreateNewUserAndReturnToken_When_GoogleLoginWithNewAccount` | Google OAuth login for first-time user | Verifies ID token, creates Google user entity (no password), returns JWT | ID token verified, new user persisted with provider GOOGLE, JWT returned | ✅ PASS |
| `should_ThrowException_When_GoogleTokenIsInvalid` | Google OAuth login with invalid/expired token | Rejects authentication, throws `RuntimeException("Invalid Google ID token")` | Threw `RuntimeException("Invalid Google ID token")` | ✅ PASS |

---

### 2. GoogleTokenServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_ReturnGoogleUserInfo_When_TokenIsValid` | Google ID token verification logic | Extracts googleId, email, and display name into `GoogleUserInfo` record | `GoogleUserInfo` record returned with googleId, email, and name | ✅ PASS |
| `should_ThrowException_When_TokenIsInvalid` | Handling of invalid or expired Google ID tokens | Throws `RuntimeException("Invalid Google ID token")` | Threw `RuntimeException("Invalid Google ID token")` | ✅ PASS |
| `should_ThrowException_When_TokenEmailIsBlank` | Google token payload missing email | Throws `RuntimeException("Google account email is not available")` | Threw `RuntimeException("Google account email is not available")` | ✅ PASS |
| `should_FallbackToEmailPrefix_When_NameIsNull` | Google token payload missing display name | Falls back to email prefix before `@` as user display name | Name set to email prefix (`charlie`) when name claim is null | ✅ PASS |
| `should_WrapCheckedExceptions_When_VerifierThrowsIOException` | Handling network/verifier IO exceptions | Catches checked `IOException` and wraps into `RuntimeException` | Wrapped into `RuntimeException("Failed to verify Google ID token")` | ✅ PASS |

---

### 3. SubmissionServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_ReturnAccepted_When_RunCodeMatchesExpectedOutput` | Single-run execution matching expected output | Sets verdict to `"Accepted"`, returns stdout | Verdict set to `"Accepted"`, stdout matched expected output | ✅ PASS |
| `should_ReturnWrongAnswer_When_RunOutputDiffersFromExpected` | Single-run execution mismatching expected output | Overrides Judge0 verdict to `"Wrong Answer"` | Verdict changed to `"Wrong Answer"` when output differed | ✅ PASS |
| `should_SubmitSuccessfully_When_AllTestCasesPass` | Problem submission with passing test cases | Executes all test cases, updates problem total/accepted count, saves submission | All test cases passed, verdict `"Accepted"`, problem stats updated and submission saved | ✅ PASS |
| `should_ThrowException_When_ProblemNotFoundOnSubmit` | Submission attempt for non-existent problem ID | Throws `RuntimeException("Problem not found")` | Threw `RuntimeException("Problem not found")` | ✅ PASS |
| `should_HandleEmptyTestCasesGracefully_When_Submitting` | Submitting solution to a problem with 0 test cases | Returns 0 passed, 0 total, verdict `"Accepted"` without breaking | Passed count 0, total count 0, verdict `"Accepted"` returned | ✅ PASS |

---

### 4. ProblemServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_ReturnAllProblemsWithoutHiddenTestCases_When_GetAll` | Public problem listing | Returns list of problem DTOs with hidden test cases stripped out | Problem DTOs returned with only visible test cases | ✅ PASS |
| `should_ReturnProblemDTO_When_GetByIdExists` | Fetching problem by ID | Returns problem DTO with hidden test cases stripped out | Problem DTO returned matching ID without hidden test cases | ✅ PASS |
| `should_ThrowException_When_ProblemNotFoundById` | Fetching problem by non-existent ID | Throws `RuntimeException("Problem not found")` | Threw `RuntimeException("Problem not found")` | ✅ PASS |
| `should_CreateAndSaveProblem_When_ValidDTOProvided` | Creating a new problem with test cases | Maps DTO to entity, saves problem & test cases, returns DTO with generated ID | Problem and test cases saved, returned DTO contains generated ID | ✅ PASS |
| `should_DeleteProblem_When_IdProvided` | Deleting a problem by ID | Calls `ProblemRepository.deleteById()` | `problemRepository.deleteById()` invoked successfully | ✅ PASS |

---

### 5. NotificationServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_CreateNotification_When_ValidRequestGiven` | Notification creation by admin | Trims message, saves notification, returns NotificationDTO | Notification saved with admin ID, DTO returned | ✅ PASS |
| `should_GetNotificationsForUser_WithCorrectReadStatus` | Fetching notifications for user | Maps each broadcast notification with user's read/unread status | Notifications mapped with `read = true/false` and timestamp | ✅ PASS |
| `should_MarkAsRead_When_ValidUserAndNotificationIdGiven` | Marking notification as read | Creates/updates `UserNotificationStatus` with `isRead = true` and timestamp | Status updated to read, `readAt` timestamp set | ✅ PASS |
| `should_ThrowException_When_MarkingNonExistentNotificationAsRead` | Marking non-existent notification as read | Throws `RuntimeException("Notification not found")` | Threw `RuntimeException("Notification not found")` | ✅ PASS |

---

### 6. UserServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_ReturnUserProfileWithRankAndSolvedCount_When_UserExists` | User profile retrieval | Calculates unique accepted problems, updates streak, computes leaderboard rank | Profile returned with streak, solvedCount, solvedProblemIds, and rank | ✅ PASS |
| `should_ThrowException_When_UserNotFoundOnGetProfile` | Fetching profile for non-existent user | Throws `RuntimeException("User not found")` | Threw `RuntimeException("User not found")` | ✅ PASS |
| `should_DeleteUser_When_IdGiven` | User account deletion | Calls `UserRepository.deleteById()` | `userRepository.deleteById()` invoked successfully | ✅ PASS |

---

### 7. StreakServiceTest (Service Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_SetStreakToOne_When_UserHasNoLastActiveDate` | Streak calculation for brand new user | Sets streak to 1 and lastActiveDate to today | Streak set to 1, lastActiveDate updated to today | ✅ PASS |
| `should_KeepStreakUnchanged_When_UserAlreadyActiveToday` | Multiple logins on the same day | Keeps streak unchanged, lastActiveDate remains today | Streak count unchanged | ✅ PASS |
| `should_IncrementStreak_When_UserWasActiveYesterday` | Consecutive day login | Increments streak by 1, updates lastActiveDate to today | Streak incremented from 5 to 6 | ✅ PASS |
| `should_ResetStreakToOne_When_UserMissedDays` | Missed day login | Resets streak to 1, updates lastActiveDate to today | Streak reset to 1 after missing active days | ✅ PASS |

---

### 8. AuthControllerTest (Controller Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_Return200AndAuthResponse_When_SignupWithValidPayload` | `POST /api/auth/signup` with valid payload | Returns HTTP 200 OK with AuthResponse JSON | HTTP 200 OK, JSON containing token and user details | ✅ PASS |
| `should_Return400BadRequest_When_SignupPayloadIsInvalid` | `POST /api/auth/signup` with invalid input | Returns HTTP 400 Bad Request (Bean Validation error) | HTTP 400 Bad Request | ✅ PASS |
| `should_Return200AndAuthResponse_When_LoginWithValidCredentials` | `POST /api/auth/login` with valid payload | Returns HTTP 200 OK with AuthResponse JSON | HTTP 200 OK, JSON containing JWT token | ✅ PASS |
| `should_Return200AndAuthResponse_When_GoogleLoginWithValidIdToken` | `POST /api/auth/google` with valid token | Returns HTTP 200 OK with AuthResponse JSON | HTTP 200 OK, JSON containing token for Google user | ✅ PASS |

---

### 9. SubmissionControllerTest (Controller Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_RejectUnauthenticatedRequest_When_SubmittingWithoutAuth` | `POST /api/submissions/submit` without Bearer token | Rejects request, returns HTTP 403 Forbidden | HTTP 403 Forbidden returned | ✅ PASS |
| `should_AllowAuthenticatedSubmit_When_UserIsLoggedIn` | `POST /api/submissions/submit` with valid authentication | Processes submission, returns HTTP 200 OK with SubmitResponse | HTTP 200 OK, SubmitResponse JSON returned | ✅ PASS |

---

### 10. NotificationControllerTest (Controller Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_RejectUnauthenticatedRequest_When_FetchingNotificationsWithoutAuth` | `GET /api/notifications` without Bearer token | Rejects request, returns HTTP 403 Forbidden | HTTP 403 Forbidden returned | ✅ PASS |
| `should_ReturnUserNotifications_When_AuthenticatedUserRequests` | `GET /api/notifications` for authenticated user | Returns HTTP 200 OK with list of notification DTOs | HTTP 200 OK, notification list returned | ✅ PASS |
| `should_MarkNotificationAsRead_When_AuthenticatedUserCallsMarkAsRead` | `PATCH /api/notifications/{id}/read` for user | Returns HTTP 200 OK with updated notification DTO (`read = true`) | HTTP 200 OK, `read = true` returned in JSON | ✅ PASS |

---

### 11. AdminNotificationControllerTest (Controller Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_Return403Forbidden_When_NonAdminAttemptsToBroadcastNotification` | `POST /api/admin/notifications` by non-admin (`ROLE_USER`) | Rejects unauthorized broadcast, returns HTTP 403 Forbidden | HTTP 403 Forbidden returned | ✅ PASS |
| `should_AllowBroadcastNotification_When_UserIsAdmin` | `POST /api/admin/notifications` by admin (`ROLE_ADMIN`) | Allows broadcast, returns HTTP 200 OK with created NotificationDTO | HTTP 200 OK, created notification returned | ✅ PASS |

---

### 12. JwtUtilTest (Security Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_GenerateValidJwtToken_When_EmailAndRoleProvided` | JWT token generation and parsing | Generates token, parses email and role correctly, returns `validateToken() = true` | Claims extracted successfully, token validated | ✅ PASS |
| `should_RejectExpiredToken_When_TokenIsExpired` | Expired JWT token validation | Returns `validateToken() = false` for expired token | Returned `false` for expired token | ✅ PASS |
| `should_RejectTamperedToken_When_SignatureIsModified` | Modified JWT token signature | Returns `validateToken() = false` for tampered token | Returned `false` for tampered token | ✅ PASS |
| `should_RejectTokenSignedWithDifferentSecret` | JWT token signed with untrusted key | Returns `validateToken() = false` for token signed with wrong secret | Returned `false` for untrusted signature | ✅ PASS |

---

### 13. JwtFilterTest (Security Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_PassThroughWithoutAuth_When_NoAuthorizationHeaderPresent` | Filter behavior without Authorization header | Passes request down filter chain without modifying SecurityContext | SecurityContext left null, filterChain.doFilter() called | ✅ PASS |
| `should_SetAuthenticationInSecurityContext_When_ValidBearerTokenProvided` | Filter behavior with valid Bearer token | Validates token, loads user details, sets UsernamePasswordAuthenticationToken in SecurityContext | SecurityContext populated with user authentication | ✅ PASS |
| `should_NotSetAuthentication_When_TokenIsInvalid` | Filter behavior with invalid Bearer token | Catches error, leaves SecurityContext unauthenticated, continues filter chain | SecurityContext left null, filterChain.doFilter() called | ✅ PASS |

---

### 14. UserRepositoryTest (Repository Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_FindUserByEmail_When_UserExistsInDatabase` | JPA query method `findByEmail()` in H2 in-memory DB | Returns Optional containing saved User entity | User entity found matching email | ✅ PASS |
| `should_ThrowDataIntegrityViolationException_When_EmailIsNotUnique` | Unique constraint on email column | Prevents saving second user with duplicate email, throws `DataIntegrityViolationException` | Threw `DataIntegrityViolationException` on flush | ✅ PASS |

---

### 15. SubmissionRepositoryTest (Repository Layer)

| Test Method Name | What Was Tested | Expected Output / Behavior | Actual Output / Behavior | Status |
|---|---|---|---|---|
| `should_FindSubmissionsByUserId_OrderedBySubmittedAtDesc` | JPA query `findByUserIdOrderBySubmittedAtDesc()` in H2 | Returns user's submissions sorted by submission time descending | Submissions filtered by userId and ordered correctly | ✅ PASS |
| `should_FindSubmissionsByUserIdAndProblemId` | JPA query `findByUserIdAndProblemIdOrderBySubmittedAtDesc()` in H2 | Returns submissions filtered by both userId and problemId | Only matching submissions returned | ✅ PASS |
