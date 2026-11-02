package hms.web;

import com.sun.net.httpserver.*;
import hms.*;
import hms.dao.UserDAO;
import hms.db.DBConnection;
import hms.security.UserSession;
import hms.ui.demo.DemoDataStore;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP REST & Static Web Server for HospitalOOP70.
 * Serves the modern HTML5/CSS3/JavaScript frontend and REST API endpoints on port 8080.
 * Reuses existing Java domain logic, algorithms (Dijkstra, PriorityQueue), and MySQL DAOs.
 */
public class HospitalWebServer {

    public static final int DEFAULT_PORT = 8080;
    private final int port;
    private HttpServer server;
    private final DemoDataStore store;
    private final File webRoot;

    // Simple in-memory session token store: token -> SessionData
    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();

    public static class SessionData {
        public final String username;
        public final String role;
        public final String fullName;
        public final long loginTime;

        public SessionData(String username, String role, String fullName) {
            this.username = username;
            this.role = role;
            this.fullName = fullName;
            this.loginTime = System.currentTimeMillis();
        }
    }

    public HospitalWebServer() {
        this(DEFAULT_PORT);
    }

    public HospitalWebServer(int port) {
        this.port = port;
        this.store = DemoDataStore.getInstance();
        this.webRoot = findWebRoot();
    }

    private File findWebRoot() {
        File[] candidates = new File[] {
            new File("frontend"),
            new File("HospitalOOP70/frontend"),
            new File("../frontend"),
            new File(System.getProperty("user.dir"), "frontend"),
            new File(System.getProperty("user.dir"), "HospitalOOP70/frontend")
        };
        for (File f : candidates) {
            if (f.exists() && f.isDirectory()) {
                return f;
            }
        }
        return new File("frontend");
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(16));

        // API Handler
        server.createContext("/api", this::handleApiRequest);

        // Static File Handler
        server.createContext("/", this::handleStaticRequest);

        server.start();
        System.out.println("==================================================================");
        System.out.println("   HOSPITAL OOP70 - WEB SERVER STARTED ON HTTP://LOCALHOST:" + port);
        System.out.println("   Web Root: " + webRoot.getAbsolutePath());
        System.out.println("   Database Status: " + (DBConnection.isConnected() ? "ONLINE (MySQL)" : "OFFLINE (Demo Mode)"));
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("[HospitalWebServer] Server stopped.");
        }
    }

    // ==================== STATIC FILE HANDLER ====================
    private void handleStaticRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendCorsResponse(exchange, 204, "");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        File targetFile = new File(webRoot, path.substring(1));
        if (!targetFile.exists() || targetFile.isDirectory()) {
            // Fallback for SPA routing: serve index.html
            targetFile = new File(webRoot, "index.html");
        }

        if (!targetFile.exists()) {
            String notFound = "<html><body style='font-family:sans-serif;text-align:center;padding:50px;'>"
                + "<h2>404 - File Not Found</h2>"
                + "<p>Target: " + path + "</p>"
                + "<p><a href='/'>Return to Hospital Portal</a></p></body></html>";
            sendResponse(exchange, 404, "text/html; charset=UTF-8", notFound.getBytes(StandardCharsets.UTF_8));
            return;
        }

        String mime = getMimeType(targetFile.getName());
        byte[] bytes = Files.readAllBytes(targetFile.toPath());
        sendResponse(exchange, 200, mime, bytes);
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    // ==================== REST API ROUTER ====================
    private void handleApiRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            sendCorsResponse(exchange, 204, "");
            return;
        }

        String uri = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        String body = readRequestBody(exchange);
        Map<String, Object> jsonBody = SimpleJson.parseObject(body);

        try {
            // AUTH
            if (uri.equals("/api/auth/login") && "POST".equals(method)) {
                handleLogin(exchange, jsonBody);
            } else if (uri.equals("/api/auth/logout") && "POST".equals(method)) {
                handleLogout(exchange);
            } else if (uri.equals("/api/auth/me") && "GET".equals(method)) {
                handleAuthMe(exchange);
            }
            // DASHBOARD
            else if (uri.equals("/api/dashboard/stats") && "GET".equals(method)) {
                handleDashboardStats(exchange);
            }
            // PATIENTS
            else if (uri.equals("/api/patients") && "GET".equals(method)) {
                handleGetPatients(exchange, queryParams);
            } else if (uri.equals("/api/patients") && "POST".equals(method)) {
                handleRegisterPatient(exchange, jsonBody);
            } else if (uri.startsWith("/api/patients/") && "PUT".equals(method)) {
                String patientId = uri.substring("/api/patients/".length());
                handleUpdatePatient(exchange, patientId, jsonBody);
            }
            // EMERGENCY
            else if (uri.equals("/api/emergency") && "POST".equals(method)) {
                handleEmergencyIntake(exchange, jsonBody);
            }
            // PRIORITY QUEUE
            else if (uri.equals("/api/priority-queue") && "GET".equals(method)) {
                handleGetPriorityQueue(exchange);
            } else if (uri.equals("/api/priority-queue/call-next") && "POST".equals(method)) {
                handleCallNextPatient(exchange);
            }
            // OPD CONSULTATIONS
            else if (uri.equals("/api/opd/consultations") && "GET".equals(method)) {
                handleGetConsultations(exchange);
            } else if (uri.equals("/api/opd/consultations") && "POST".equals(method)) {
                handleSaveConsultation(exchange, jsonBody);
            } else if (uri.equals("/api/opd/recommend-ipd") && "POST".equals(method)) {
                handleRecommendIpd(exchange, jsonBody);
            }
            // ADMISSION REQUESTS & ALLOCATIONS
            else if (uri.equals("/api/admissions/requests") && "GET".equals(method)) {
                handleGetAdmissionRequests(exchange);
            } else if (uri.equals("/api/admissions/requests") && "POST".equals(method)) {
                handleCreateAdmissionRequest(exchange, jsonBody);
            } else if (uri.equals("/api/admissions/allocate") && "POST".equals(method)) {
                handleAllocateBed(exchange, jsonBody);
            } else if (uri.equals("/api/admissions/cancel") && "POST".equals(method)) {
                handleCancelAdmission(exchange, jsonBody);
            }
            // BEDS
            else if (uri.equals("/api/beds") && "GET".equals(method)) {
                handleGetBeds(exchange, queryParams);
            } else if (uri.equals("/api/beds/clean") && "POST".equals(method)) {
                handleFinishCleaning(exchange, jsonBody);
            }
            // DOCTORS
            else if (uri.equals("/api/doctors") && "GET".equals(method)) {
                handleGetDoctors(exchange);
            } else if (uri.equals("/api/doctors") && "POST".equals(method)) {
                handleAddDoctor(exchange, jsonBody);
            } else if (uri.equals("/api/doctors/toggle-duty") && "POST".equals(method)) {
                handleToggleDuty(exchange, jsonBody);
            }
            // HOSPITALS
            else if (uri.equals("/api/hospitals") && "GET".equals(method)) {
                handleGetHospitals(exchange);
            }
            // REFERRALS & DIJKSTRA
            else if (uri.equals("/api/referrals") && "GET".equals(method)) {
                handleGetReferrals(exchange);
            } else if (uri.equals("/api/referrals/calculate") && "POST".equals(method)) {
                handleCalculateDijkstraRoute(exchange, jsonBody);
            } else if (uri.equals("/api/referrals") && "POST".equals(method)) {
                handleCreateReferral(exchange, jsonBody);
            }
            // DISCHARGE
            else if (uri.equals("/api/discharge/admitted") && "GET".equals(method)) {
                handleGetAdmittedPatients(exchange);
            } else if (uri.equals("/api/discharge") && "POST".equals(method)) {
                handleProcessDischarge(exchange, jsonBody);
            } else if (uri.equals("/api/discharge/note") && "POST".equals(method)) {
                handleAddTreatmentNote(exchange, jsonBody);
            }
            // CLINICAL HISTORY AUDIT
            else if (uri.equals("/api/history") && "GET".equals(method)) {
                handleGetHistory(exchange, queryParams);
            }
            // REPORTS
            else if (uri.equals("/api/reports") && "GET".equals(method)) {
                handleGetReports(exchange);
            }
            // SETTINGS & DATABASE
            else if (uri.equals("/api/settings/db") && "GET".equals(method)) {
                handleGetDbConfig(exchange);
            } else if (uri.equals("/api/settings/db/test") && "POST".equals(method)) {
                handleTestDbConnection(exchange);
            } else if (uri.equals("/api/settings/users") && "GET".equals(method)) {
                handleGetUsers(exchange);
            } else {
                sendJsonResponse(exchange, 404, Map.of("success", false, "error", "Endpoint not found: " + uri));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of(
                "success", false,
                "error", "Internal server error: " + ex.getMessage()
            ));
        }
    }

    // ==================== ENDPOINT IMPLEMENTATIONS ====================

    private void handleLogin(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String username = getString(body, "username", "");
        String password = getString(body, "password", "");
        String role = getString(body, "role", "Administrator");

        if (username.isEmpty() || password.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Username and password required."));
            return;
        }

        boolean authenticated = false;
        String fullName = username;

        if (DBConnection.isConnected()) {
            UserDAO userDAO = new UserDAO();
            authenticated = userDAO.authenticate(username, password, role);
            UserDAO.UserRecord rec = userDAO.findByUsername(username);
            if (rec != null) fullName = rec.getFullName();
        } else {
            // Resilient demo credentials
            boolean validDemo = ("admin".equalsIgnoreCase(username) || "staff".equalsIgnoreCase(username) ||
                                 "doctor".equalsIgnoreCase(username) || "receptionist".equalsIgnoreCase(username))
                                 && "hospital123".equals(password);
            authenticated = validDemo;
            if ("admin".equalsIgnoreCase(username)) fullName = "Dr. Administrator";
            else if ("doctor".equalsIgnoreCase(username)) fullName = "Dr. John Clinical";
            else if ("staff".equalsIgnoreCase(username)) fullName = "Nurse Officer";
            else if ("receptionist".equalsIgnoreCase(username)) fullName = "Intake Clerk";
        }

        if (!authenticated) {
            sendJsonResponse(exchange, 401, Map.of("success", false, "error", "Invalid username, password, or role mismatch."));
            return;
        }

        // Generate session token
        String token = "HMS-" + UUID.randomUUID().toString();
        SessionData session = new SessionData(username, role, fullName);
        activeSessions.put(token, session);

        // Update backend UserSession
        UserSession.getInstance().login(username, role, fullName);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("token", token);
        res.put("user", Map.of(
            "username", username,
            "role", role,
            "fullName", fullName,
            "isAdmin", "Administrator".equalsIgnoreCase(role),
            "isDoctor", "Doctor".equalsIgnoreCase(role) || "Administrator".equalsIgnoreCase(role),
            "isReceptionist", "Receptionist".equalsIgnoreCase(role) || "Administrator".equalsIgnoreCase(role),
            "isStaff", "Hospital Staff".equalsIgnoreCase(role) || "Administrator".equalsIgnoreCase(role)
        ));
        sendJsonResponse(exchange, 200, res);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = getBearerToken(exchange);
        if (token != null) {
            activeSessions.remove(token);
        }
        UserSession.getInstance().logout();
        sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Logged out successfully."));
    }

    private void handleAuthMe(HttpExchange exchange) throws IOException {
        String token = getBearerToken(exchange);
        SessionData s = (token != null) ? activeSessions.get(token) : null;
        if (s == null) {
            // Fallback to singleton session if available
            UserSession us = UserSession.getInstance();
            s = new SessionData(us.getUsername(), us.getRole(), us.getFullName());
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("authenticated", true);
        res.put("username", s.username);
        res.put("role", s.role);
        res.put("fullName", s.fullName);
        sendJsonResponse(exchange, 200, res);
    }

    private void handleDashboardStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = new LinkedHashMap<>();

        int totalPatients = store.getTotalPatientsCount();
        int waiting = store.getWaitingPatientsCount();
        int admitted = store.getAdmittedPatientsCount();
        int discharges = store.getTotalDischargesCount();

        int opdCount = 0;
        int emergencyCount = 0;
        for (Patient p : store.getPatients()) {
            if ("EMERGENCY".equalsIgnoreCase(p.getVisitType())) emergencyCount++;
            else opdCount++;
        }

        int availGen = store.getAvailableGeneralBedsCount();
        int availIcu = store.getAvailableIcuBedsCount();
        int totalBeds = store.getBeds().size();
        int occupiedBeds = 0;
        int cleaningBeds = 0;
        for (Bed b : store.getBeds()) {
            if (b.isOccupied()) occupiedBeds++;
            else if ("CLEANING".equalsIgnoreCase(b.getStatus())) cleaningBeds++;
        }

        stats.put("totalPatients", totalPatients);
        stats.put("opdPatients", opdCount);
        stats.put("ipdPatients", admitted);
        stats.put("emergencyPatients", emergencyCount);
        stats.put("availableBeds", availGen + availIcu);
        stats.put("occupiedBeds", occupiedBeds);
        stats.put("cleaningBeds", cleaningBeds);
        stats.put("totalBeds", totalBeds);
        stats.put("doctorsCount", store.getDoctors().size());
        stats.put("pendingAdmissions", (int) store.getAdmissionRequests().stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count());
        stats.put("referralsCount", store.getReferrals().size());
        stats.put("totalDischarges", discharges);
        stats.put("waitingQueueCount", waiting);

        // Bed occupancy percent
        int occPercent = totalBeds > 0 ? (occupiedBeds * 100) / totalBeds : 0;
        stats.put("occupancyRatePercent", occPercent);

        // Department census breakdown
        Map<String, Integer> deptCensus = new LinkedHashMap<>();
        for (Patient p : store.getPatients()) {
            String d = p.getDepartment() != null ? p.getDepartment() : "General Medicine";
            deptCensus.put(d, deptCensus.getOrDefault(d, 0) + 1);
        }
        stats.put("departmentCensus", deptCensus);

        // Recent patients
        List<Map<String, Object>> recentList = new ArrayList<>();
        List<Patient> allP = store.getPatients();
        int start = Math.max(0, allP.size() - 5);
        for (int i = allP.size() - 1; i >= start; i--) {
            Patient p = allP.get(i);
            recentList.add(patientToMap(p));
        }
        stats.put("recentPatients", recentList);

        // Activity log
        List<Map<String, Object>> recentLogs = new ArrayList<>();
        List<DemoDataStore.HistoryRecord> logs = store.getActivityHistory();
        int logStart = Math.max(0, logs.size() - 8);
        for (int i = logs.size() - 1; i >= logStart; i--) {
            DemoDataStore.HistoryRecord r = logs.get(i);
            recentLogs.add(Map.of(
                "historyId", r.getHistoryId(),
                "patientId", r.getPatientId(),
                "activityType", r.getActivityType(),
                "description", r.getDescription(),
                "timestamp", r.getTimestamp(),
                "staffUser", r.getStaffUser()
            ));
        }
        stats.put("recentActivity", recentLogs);

        sendJsonResponse(exchange, 200, Map.of("success", true, "data", stats));
    }

    private void handleGetPatients(HttpExchange exchange, Map<String, String> params) throws IOException {
        String search = params.getOrDefault("search", "").toLowerCase();
        String visitType = params.getOrDefault("visitType", "");

        List<Map<String, Object>> list = new ArrayList<>();
        for (Patient p : store.getPatients()) {
            boolean matchSearch = search.isEmpty() 
                || p.getPatientId().toLowerCase().contains(search)
                || p.getName().toLowerCase().contains(search)
                || (p.getContactNumber() != null && p.getContactNumber().contains(search));
            boolean matchType = visitType.isEmpty() || "ALL".equalsIgnoreCase(visitType)
                || visitType.equalsIgnoreCase(p.getVisitType());

            if (matchSearch && matchType) {
                list.add(patientToMap(p));
            }
        }

        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleRegisterPatient(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String name = getString(body, "name", "").trim();
        int age = getInt(body, "age", 0);
        String gender = getString(body, "gender", "Other");
        String contact = getString(body, "contact", "").trim();
        String address = getString(body, "address", "");
        String visitType = getString(body, "visitType", "OPD").trim().toUpperCase();
        int severity = getInt(body, "severity", 3);
        String department = getString(body, "department", "General Medicine");

        // VALIDATION RULES
        if (name.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient full name is required."));
            return;
        }
        if (age <= 0 || age > 125) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Age must be a valid integer between 1 and 125."));
            return;
        }
        if (contact.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Contact phone number is required."));
            return;
        }
        // STRICT RULE: Initial registration visit type MUST be OPD or EMERGENCY only! Direct IPD registration is forbidden.
        if (!"OPD".equalsIgnoreCase(visitType) && !"EMERGENCY".equalsIgnoreCase(visitType)) {
            sendJsonResponse(exchange, 400, Map.of(
                "success", false,
                "error", "Invalid Visit Type. Direct IPD registration is strictly forbidden. Patients must register as 'OPD' or 'EMERGENCY' and be admitted via doctor consultation."
            ));
            return;
        }

        Patient p = store.registerPatient(name, age, gender, contact, address, visitType, severity, department);
        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "Patient registered successfully.",
            "data", patientToMap(p)
        ));
    }

    private void handleUpdatePatient(HttpExchange exchange, String patientId, Map<String, Object> body) throws IOException {
        Patient p = store.getPatientById(patientId);
        if (p == null) {
            sendJsonResponse(exchange, 404, Map.of("success", false, "error", "Patient not found: " + patientId));
            return;
        }

        String name = getString(body, "name", p.getName()).trim();
        int age = getInt(body, "age", p.getAge());
        String gender = getString(body, "gender", p.getGender());
        String contact = getString(body, "contact", p.getContactNumber());
        String address = getString(body, "address", p.getAddress());

        boolean ok = store.updatePatientDemographics(patientId, name, age, gender, contact, address);
        sendJsonResponse(exchange, 200, Map.of(
            "success", ok,
            "message", ok ? "Demographics updated successfully." : "Failed to update demographics.",
            "data", patientToMap(store.getPatientById(patientId))
        ));
    }

    private void handleEmergencyIntake(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String name = getString(body, "name", "").trim();
        int age = getInt(body, "age", 0);
        String gender = getString(body, "gender", "Unknown");
        String contact = getString(body, "contact", "");
        int severity = getInt(body, "severity", 1);
        String department = getString(body, "department", "Emergency / Trauma");
        String doctorName = getString(body, "doctorName", "Attending Emergency Officer");

        if (name.isEmpty() || age <= 0) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Valid patient name and age required."));
            return;
        }

        Patient p = store.registerEmergencyPatient(name, age, gender, contact, severity, department, doctorName);
        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "Emergency patient admitted to priority queue.",
            "data", patientToMap(p)
        ));
    }

    private void handleGetPriorityQueue(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Patient> sorted = store.getSortedQueueList();
        int pos = 1;
        for (Patient p : sorted) {
            Map<String, Object> m = patientToMap(p);
            m.put("queuePosition", pos++);
            list.add(m);
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleCallNextPatient(HttpExchange exchange) throws IOException {
        Patient called = store.callNextPatient();
        if (called == null) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Priority queue is currently empty."));
            return;
        }
        sendJsonResponse(exchange, 200, Map.of(
            "success", true,
            "message", "Patient called from priority queue.",
            "data", patientToMap(called)
        ));
    }

    private void handleGetConsultations(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Encounter e : store.getEncounters()) {
            list.add(encounterToMap(e));
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleSaveConsultation(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String doctorId = getString(body, "doctorId", "DOC-001");
        String doctorName = getString(body, "doctorName", "Attending Medical Officer");
        String hospitalId = getString(body, "hospitalId", "H001");
        String department = getString(body, "department", "General Medicine");
        String symptoms = getString(body, "symptoms", "");
        String diagnosis = getString(body, "diagnosis", "");
        String prescription = getString(body, "prescription", "");
        String notes = getString(body, "notes", "");

        if (patientId.isEmpty() || symptoms.isEmpty() || diagnosis.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID, presenting symptoms, and clinical diagnosis are required."));
            return;
        }

        Encounter enc = store.saveConsultation(patientId, doctorId, doctorName, hospitalId, department, symptoms, diagnosis, prescription, notes);
        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "Consultation encounter recorded successfully.",
            "data", encounterToMap(enc)
        ));
    }

    private void handleRecommendIpd(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String doctorId = getString(body, "doctorId", "DOC-001");
        String doctorName = getString(body, "doctorName", "Attending Physician");
        String bedRequirement = getString(body, "bedRequirement", "General Bed");
        String hospitalId = getString(body, "hospitalId", "H001");
        String reason = getString(body, "reason", "Inpatient clinical monitoring indicated");
        String dept = getString(body, "department", "General Medicine");
        String symptoms = getString(body, "symptoms", "Acute inpatient symptoms");
        String diagnosis = getString(body, "diagnosis", "Clinical assessment diagnosis");
        String treatmentNotes = getString(body, "treatmentNotes", "Inpatient bed admission required");

        if (patientId.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID is required."));
            return;
        }

        AdmissionRequest req = store.recommendAdmission(patientId, doctorId, doctorName, dept, hospitalId, symptoms, diagnosis, treatmentNotes, bedRequirement, reason);
        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "IPD admission recommendation logged successfully.",
            "data", admissionRequestToMap(req)
        ));
    }

    private void handleGetAdmissionRequests(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AdmissionRequest req : store.getAdmissionRequests()) {
            list.add(admissionRequestToMap(req));
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleCreateAdmissionRequest(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String bedRequirement = getString(body, "bedRequirement", "General Bed");
        String hospitalId = getString(body, "hospitalId", "H001");

        if (patientId.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID required."));
            return;
        }

        AdmissionRequest req = store.createAdmissionRequest(patientId, bedRequirement, hospitalId);
        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "Admission request created.",
            "data", admissionRequestToMap(req)
        ));
    }

    private void handleAllocateBed(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String bedId = getString(body, "bedId", "");

        if (patientId.isEmpty() || bedId.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID and Bed ID required."));
            return;
        }

        boolean ok = store.allocateBed(patientId, bedId);
        if (!ok) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Bed allocation failed. Bed may be occupied, cleaning, or incompatible."));
            return;
        }

        sendJsonResponse(exchange, 200, Map.of(
            "success", true,
            "message", "Bed " + bedId + " successfully allocated to Patient " + patientId
        ));
    }

    private void handleCancelAdmission(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String reqId = getString(body, "requestId", "");
        boolean ok = store.cancelAdmissionRequest(reqId);
        sendJsonResponse(exchange, 200, Map.of(
            "success", ok,
            "message", ok ? "Admission request cancelled." : "Could not cancel admission request."
        ));
    }

    private void handleGetBeds(HttpExchange exchange, Map<String, String> params) throws IOException {
        String hospFilter = params.getOrDefault("hospital", "ALL");
        String statusFilter = params.getOrDefault("status", "ALL");

        List<Map<String, Object>> list = new ArrayList<>();
        for (Bed b : store.getBeds()) {
            boolean matchH = "ALL".equalsIgnoreCase(hospFilter) || b.getHospitalId().equalsIgnoreCase(hospFilter);
            boolean matchS = "ALL".equalsIgnoreCase(statusFilter) || b.getStatus().equalsIgnoreCase(statusFilter);

            if (matchH && matchS) {
                list.add(bedToMap(b));
            }
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleFinishCleaning(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String bedId = getString(body, "bedId", "");
        if (bedId.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Bed ID required."));
            return;
        }

        boolean ok = store.finishBedCleaning(bedId);
        sendJsonResponse(exchange, 200, Map.of(
            "success", ok,
            "message", ok ? "Bed " + bedId + " sanitized and restored to AVAILABLE." : "Bed was not in CLEANING state."
        ));
    }

    private void handleGetDoctors(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Doctor d : store.getDoctors()) {
            list.add(Map.of(
                "doctorId", d.getDoctorId(),
                "name", d.getName(),
                "department", d.getDepartment(),
                "hospitalId", d.getHospitalId(),
                "onDuty", d.isOnDuty(),
                "dutyStatus", d.isOnDuty() ? "ON DUTY" : "OFF DUTY"
            ));
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleAddDoctor(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String name = getString(body, "name", "").trim();
        String dept = getString(body, "department", "General Medicine");
        String hospId = getString(body, "hospitalId", "H001");
        boolean onDuty = getBoolean(body, "onDuty", true);

        if (name.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Doctor name required."));
            return;
        }

        String docId = "DOC-" + (store.getDoctors().size() + 101);
        Hospital h = store.getHospitalById(hospId);
        String hospName = h != null ? h.getHospitalName() : "District General Hospital";
        Doctor d = new Doctor(docId, name, dept, hospId, hospName, onDuty);
        store.addDemoDoctor(d);

        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "Doctor added successfully.",
            "data", Map.of("doctorId", docId, "name", name, "department", dept, "hospitalId", hospId, "onDuty", onDuty)
        ));
    }

    private void handleToggleDuty(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String doctorId = getString(body, "doctorId", "");
        Doctor d = store.getDoctorById(doctorId);
        if (d == null) {
            sendJsonResponse(exchange, 404, Map.of("success", false, "error", "Doctor not found: " + doctorId));
            return;
        }
        boolean newDuty = !d.isOnDuty();
        store.updateDoctorDuty(doctorId, newDuty);
        sendJsonResponse(exchange, 200, Map.of(
            "success", true,
            "doctorId", doctorId,
            "onDuty", newDuty,
            "message", "Duty status updated to: " + (newDuty ? "ON DUTY" : "OFF DUTY")
        ));
    }

    private void handleGetHospitals(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Hospital h : store.getHospitals()) {
            list.add(Map.of(
                "hospitalId", h.getHospitalId(),
                "hospitalName", h.getHospitalName(),
                "location", h.getLocation(),
                "generalBedCapacity", h.getGeneralBedCapacity(),
                "icuBedCapacity", h.getIcuBedCapacity(),
                "availableGeneralBeds", h.getAvailableGeneralBedsCount(),
                "availableIcuBeds", h.getAvailableICUBedsCount(),
                "status", "ACTIVE NODE"
            ));
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleGetReferrals(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DemoDataStore.ReferralRecord r : store.getReferrals()) {
            list.add(Map.of(
                "referralId", r.getReferralId(),
                "patientId", r.getPatientId(),
                "patientName", r.getPatientName(),
                "sourceHospital", r.getSourceHospital(),
                "destHospital", r.getDestHospital(),
                "bedType", r.getBedType(),
                "travelMinutes", r.getTravelMinutes(),
                "timestamp", r.getTimestamp()
            ));
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleCalculateDijkstraRoute(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String src = getString(body, "sourceHospitalId", "").trim();
        String dest = getString(body, "destHospitalId", "").trim();

        if (src.isEmpty() || dest.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Source and destination hospital IDs required."));
            return;
        }

        RoutePlanner planner = store.getRoutePlanner();
        RoutePlanner.RouteResult route = planner.findShortestPath(src, dest);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("source", src);
        res.put("destination", dest);
        res.put("path", route.getPath());
        res.put("pathString", route.getPathString());
        res.put("travelMinutes", route.getTotalMinutes());
        res.put("isDirect", route.getPath().size() == 2);

        sendJsonResponse(exchange, 200, res);
    }

    private void handleCreateReferral(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String src = getString(body, "sourceHospitalId", "");
        String dest = getString(body, "destHospitalId", "");
        String bedType = getString(body, "bedType", "General Bed");

        if (patientId.isEmpty() || src.isEmpty() || dest.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID, source, and destination hospitals required."));
            return;
        }

        RoutePlanner.RouteResult route = store.getRoutePlanner().findShortestPath(src, dest);
        int travelMinutes = route.getTotalMinutes();
        DemoDataStore.ReferralRecord r = store.createReferral(patientId, src, dest, bedType, travelMinutes);
        sendJsonResponse(exchange, 201, Map.of(
            "success", true,
            "message", "Patient referral confirmed.",
            "data", Map.of(
                "referralId", r.getReferralId(),
                "patientId", r.getPatientId(),
                "travelMinutes", r.getTravelMinutes(),
                "timestamp", r.getTimestamp()
            )
        ));
    }

    private void handleGetAdmittedPatients(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Patient p : store.getPatients()) {
            String st = p.getStatus() != null ? p.getStatus().toUpperCase() : "";
            if (st.contains("ADMITTED") || st.contains("IPD")) {
                Bed assignedBed = null;
                for (Bed b : store.getBeds()) {
                    if (p.getPatientId().equalsIgnoreCase(b.getPatientId())) {
                        assignedBed = b;
                        break;
                    }
                }
                Map<String, Object> m = patientToMap(p);
                m.put("bedId", assignedBed != null ? assignedBed.getBedId() : "UNASSIGNED");
                m.put("hospitalId", assignedBed != null ? assignedBed.getHospitalId() : "H001");
                list.add(m);
            }
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleProcessDischarge(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String notes = getString(body, "notes", "Patient discharged in stable condition");

        if (patientId.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID required."));
            return;
        }

        boolean ok = store.dischargePatient(patientId, notes);
        if (!ok) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient is not admitted or cannot be discharged."));
            return;
        }

        sendJsonResponse(exchange, 200, Map.of(
            "success", true,
            "message", "Patient " + patientId + " discharged successfully. Bed entered CLEANING state."
        ));
    }

    private void handleAddTreatmentNote(HttpExchange exchange, Map<String, Object> body) throws IOException {
        String patientId = getString(body, "patientId", "");
        String note = getString(body, "note", "");
        String staff = getString(body, "staff", "Clinical Officer");

        if (patientId.isEmpty() || note.isEmpty()) {
            sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Patient ID and treatment note text required."));
            return;
        }

        store.recordInpatientTreatmentNote(patientId, note, staff);
        sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Treatment progress note appended."));
    }

    private void handleGetHistory(HttpExchange exchange, Map<String, String> params) throws IOException {
        String pId = params.getOrDefault("patientId", "").toLowerCase();
        String type = params.getOrDefault("type", "ALL");

        List<Map<String, Object>> list = new ArrayList<>();
        for (DemoDataStore.HistoryRecord r : store.getActivityHistory()) {
            boolean matchP = pId.isEmpty() || r.getPatientId().toLowerCase().contains(pId);
            boolean matchT = "ALL".equalsIgnoreCase(type) || r.getActivityType().equalsIgnoreCase(type);
            if (matchP && matchT) {
                list.add(Map.of(
                    "historyId", r.getHistoryId(),
                    "patientId", r.getPatientId(),
                    "activityType", r.getActivityType(),
                    "description", r.getDescription(),
                    "timestamp", r.getTimestamp(),
                    "staffUser", r.getStaffUser()
                ));
            }
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    private void handleGetReports(HttpExchange exchange) throws IOException {
        Map<String, Object> reports = new LinkedHashMap<>();

        reports.put("totalAdmitted", store.getAdmittedPatientsCount());
        reports.put("totalDischarges", store.getTotalDischargesCount());
        int cleaningCount = 0;
        int occupiedCount = 0;
        int genTotal = 0, genAvail = 0;
        int icuTotal = 0, icuAvail = 0;
        for (Bed b : store.getBeds()) {
            if (b instanceof ICUBed) {
                icuTotal++;
                if (b.isAvailable()) icuAvail++;
            } else {
                genTotal++;
                if (b.isAvailable()) genAvail++;
            }
            if ("CLEANING".equalsIgnoreCase(b.getStatus())) cleaningCount++;
            if (b.isOccupied()) occupiedCount++;
        }
        reports.put("cleaningTurnaround", cleaningCount);
        reports.put("generalBedTotal", genTotal);
        reports.put("generalBedAvailable", genAvail);
        reports.put("icuBedTotal", icuTotal);
        reports.put("icuBedAvailable", icuAvail);

        // Visit Category Volume
        int opd = 0, emg = 0, ipd = 0;
        Map<Integer, Integer> severityMap = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) severityMap.put(i, 0);

        for (Patient p : store.getPatients()) {
            if ("EMERGENCY".equalsIgnoreCase(p.getVisitType())) emg++;
            else if ("IPD".equalsIgnoreCase(p.getVisitType()) || p.getStatus().contains("ADMITTED")) ipd++;
            else opd++;

            int sev = p.getPriority();
            severityMap.put(sev, severityMap.getOrDefault(sev, 0) + 1);
        }

        reports.put("visitBreakdown", Map.of("OPD", opd, "Emergency", emg, "IPD", ipd));
        reports.put("severityBreakdown", severityMap);

        sendJsonResponse(exchange, 200, Map.of("success", true, "data", reports));
    }

    private void handleGetDbConfig(HttpExchange exchange) throws IOException {
        boolean connected = DBConnection.isConnected();
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("connected", connected);
        cfg.put("host", DBConnection.getHost());
        cfg.put("port", DBConnection.getPort());
        cfg.put("database", DBConnection.getDatabaseName());
        cfg.put("user", DBConnection.getUsername());
        cfg.put("envConfigured", System.getenv("DB_PASSWORD") != null);
        cfg.put("status", connected ? "ONLINE (MySQL Connection Active)" : "OFFLINE (Resilient In-Memory Demo Mode)");
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", cfg));
    }

    private void handleTestDbConnection(HttpExchange exchange) throws IOException {
        boolean ok = DBConnection.isConnected();
        sendJsonResponse(exchange, 200, Map.of(
            "success", true,
            "connected", ok,
            "message", ok ? "MySQL Database is connected and responding." : "Database connection failed (Awaiting DB_PASSWORD environment variable)."
        ));
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        if (DBConnection.isConnected()) {
            UserDAO dao = new UserDAO();
            for (UserDAO.UserRecord u : dao.findAll()) {
                list.add(Map.of(
                    "userId", u.getUserId(),
                    "username", u.getUsername(),
                    "role", u.getRole(),
                    "fullName", u.getFullName()
                ));
            }
        } else {
            list.add(Map.of("userId", 1, "username", "admin", "role", "Administrator", "fullName", "System Administrator"));
            list.add(Map.of("userId", 2, "username", "doctor", "role", "Doctor", "fullName", "Dr. John Clinical"));
            list.add(Map.of("userId", 3, "username", "staff", "role", "Hospital Staff", "fullName", "Ward Operations Staff"));
            list.add(Map.of("userId", 4, "username", "receptionist", "role", "Receptionist", "fullName", "Patient Intake Officer"));
        }
        sendJsonResponse(exchange, 200, Map.of("success", true, "data", list));
    }

    // ==================== HELPER CONVERTERS ====================
    private Map<String, Object> patientToMap(Patient p) {
        if (p == null) return Collections.emptyMap();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("patientId", p.getPatientId());
        m.put("name", p.getName());
        m.put("age", p.getAge());
        m.put("gender", p.getGender());
        m.put("contact", p.getContactNumber() != null ? p.getContactNumber() : "");
        m.put("address", p.getAddress() != null ? p.getAddress() : "");
        m.put("visitType", p.getVisitType() != null ? p.getVisitType() : "OPD");
        m.put("priority", p.getPriority());
        m.put("severityText", "Level " + p.getPriority() + " (" + p.getEmergencyLevel() + ")");
        m.put("emergencyLevel", p.getEmergencyLevel());
        m.put("status", p.getStatus() != null ? p.getStatus() : "WAITING");
        m.put("department", p.getDepartment() != null ? p.getDepartment() : "General Medicine");
        m.put("registrationDateTime", p.getRegistrationDateTime());
        return m;
    }

    private Map<String, Object> encounterToMap(Encounter e) {
        if (e == null) return Collections.emptyMap();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("encounterId", e.getEncounterId());
        m.put("patientId", e.getPatientId());
        m.put("doctorId", e.getDoctorId());
        m.put("doctorName", e.getDoctorName());
        m.put("hospitalId", e.getHospitalId());
        m.put("department", e.getDepartment());
        m.put("symptoms", e.getSymptoms());
        m.put("diagnosis", e.getDiagnosis());
        m.put("prescription", e.getTreatmentNotes() != null ? e.getTreatmentNotes() : "");
        m.put("notes", e.getNotes() != null ? e.getNotes() : "");
        m.put("timestamp", e.getEncounterDate() != null ? e.getEncounterDate() : "");
        m.put("status", "COMPLETED");
        return m;
    }

    private Map<String, Object> admissionRequestToMap(AdmissionRequest r) {
        if (r == null) return Collections.emptyMap();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("requestId", r.getRequestId());
        String pId = r.getPatient() != null ? r.getPatient().getPatientId() : "";
        m.put("patientId", pId);
        m.put("bedRequirement", r.getBedRequirement());
        m.put("hospitalId", r.getHospitalId());
        m.put("status", r.getStatus());
        m.put("patientName", r.getPatient() != null ? r.getPatient().getName() : "Unknown");
        return m;
    }

    private Map<String, Object> bedToMap(Bed b) {
        if (b == null) return Collections.emptyMap();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bedId", b.getBedId());
        m.put("hospitalId", b.getHospitalId());
        Hospital h = store.getHospitalById(b.getHospitalId());
        m.put("hospitalName", h != null ? h.getHospitalName() : b.getHospitalId());
        m.put("bedType", b.getBedType());
        m.put("department", b.getDepartment());
        m.put("status", b.getStatus());
        m.put("isOccupied", b.isOccupied());
        m.put("isAvailable", b.isAvailable());
        m.put("isIcu", b instanceof ICUBed);
        m.put("assignedPatientId", b.getPatientId() != null ? b.getPatientId() : "—");
        return m;
    }

    // ==================== HTTP UTILITIES ====================
    private void sendJsonResponse(HttpExchange exchange, int status, Object data) throws IOException {
        String json = SimpleJson.stringify(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, status, "application/json; charset=UTF-8", bytes);
    }

    private void sendCorsResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, status, "text/plain", bytes);
    }

    private void sendResponse(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        Headers h = exchange.getResponseHeaders();
        h.set("Content-Type", contentType);
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    private String getBearerToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private String getString(Map<String, Object> map, String key, String def) {
        Object o = map.get(key);
        return o != null ? o.toString() : def;
    }

    private int getInt(Map<String, Object> map, String key, int def) {
        Object o = map.get(key);
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof String) {
            try { return Integer.parseInt(((String) o).trim()); } catch (Exception ignored) {}
        }
        return def;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        Object o = map.get(key);
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof String) return Boolean.parseBoolean((String) o);
        return def;
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }
        try {
            HospitalWebServer ws = new HospitalWebServer(port);
            ws.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
