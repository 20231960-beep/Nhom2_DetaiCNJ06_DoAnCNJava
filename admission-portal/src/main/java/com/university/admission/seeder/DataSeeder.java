package com.university.admission.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * DataSeeder
 * ===========================================================================
 * Sinh du lieu gia lap cho TOAN BO 10 bang trong CSDL, mo phong he thong da
 * VAN HANH LIEN TUC TRONG 5 NAM (2021 - 2026), phuc vu yeu cau nop bai:
 *   - Cac bang nghiep vu phat sinh (users, candidates, applications, documents,
 *     application_status_history, notifications) dat toi thieu ~1000 dong.
 *   - Cac bang danh muc (admission_sessions, majors, session_major,
 *     major_combination) giu so luong hop ly nhu ngoai doi thuc, KHONG ep 1000.
 *
 * CACH CHAY:
 *   Class nay chi chay khi Spring Boot duoc khoi dong voi profile "seed", de
 *   khong bao gio vo tinh chay lai trong luc dung app binh thuong. Vi du:
 *     mvn spring-boot:run -Dspring-boot.run.profiles=seed
 *   hoac khi chay file jar:
 *     java -jar admission-portal.jar --spring.profiles.active=seed
 *
 *   Sau khi chay xong (xem log "SEED HOAN TAT"), dung app, roi dung mysqldump
 *   de xuat lai file .sql nop cho thay. Class se TU DONG BO QUA neu bang users
 *   da co san > 500 dong (tranh chay 2 lan bi nhan doi du lieu).
 *
 * KY THUAT:
 *   Dung JdbcTemplate (INSERT tho, KHONG qua Hibernate) vi mot so cot dung
 *   @CreationTimestamp (vd users.created_at, notifications.created_at...) se
 *   bi Hibernate tu dong ghi de bang thoi gian HIEN TAI moi khi save() - dieu
 *   nay khien khong the "lui" ngay thang de mo phong 5 nam qua. Insert thang
 *   qua JDBC cho phep dat created_at/changed_at theo dung moc thoi gian mong
 *   muon, dong thoi insert theo lo (batch) nen rat nhanh voi hang nghin dong.
 *
 * Tai khoan seed dung chung 1 mat khau: "Pass@123" (da ma hoa BCrypt).
 * ===========================================================================
 */
@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final Random rnd = new Random(20260925L); // seed co dinh -> ket qua tai lap duoc

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 25, 0, 0);
    private static final int BATCH_SIZE = 500;

    public DataSeeder(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // DU LIEU THAM CHIEU (Vietnamese name pools, provinces, majors...)
    // =========================================================================

    private static final String[] HO = {
            "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ",
            "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"
    };
    private static final String[] DEM_NAM = {"Văn", "Hữu", "Đức", "Minh", "Quốc", "Thành", "Công", "Trọng", "Xuân", "Anh"};
    private static final String[] DEM_NU = {"Thị", "Ngọc", "Thu", "Kim", "Hồng", "Diệu", "Thanh", "Mỹ", "Lan", "Bích"};
    private static final String[] TEN_NAM = {
            "An", "Bình", "Cường", "Dũng", "Đạt", "Đức", "Hải", "Hùng", "Huy", "Khánh",
            "Khoa", "Long", "Minh", "Nam", "Phong", "Phúc", "Quân", "Sơn", "Thắng", "Tuấn", "Việt", "Vinh"
    };
    private static final String[] TEN_NU = {
            "Anh", "Chi", "Duyên", "Giang", "Hà", "Hạnh", "Hoa", "Huyền", "Lan", "Linh",
            "Mai", "My", "Ngọc", "Nhi", "Phương", "Quỳnh", "Thảo", "Thu", "Trang", "Trâm", "Vy", "Yến"
    };
    private static final String[] PROVINCES = {
            "Hà Nội", "Hải Phòng", "Quảng Ninh", "Bắc Ninh", "Nam Định", "Thanh Hóa",
            "Nghệ An", "Hà Tĩnh", "Đà Nẵng", "Thừa Thiên Huế", "Khánh Hòa", "Bình Định",
            "Đắk Lắk", "TP. Hồ Chí Minh", "Bình Dương", "Đồng Nai", "Cần Thơ", "An Giang",
            "Kiên Giang", "Long An"
    };

    // {code, name, faculty}
    private static final String[][] NEW_MAJORS = {
            {"7480103", "Kỹ thuật phần mềm", "Công nghệ thông tin"},
            {"7480101", "Khoa học máy tính", "Công nghệ thông tin"},
            {"7480104", "Hệ thống thông tin", "Công nghệ thông tin"},
            {"7480202", "An toàn thông tin", "Công nghệ thông tin"},
            {"7520201", "Kỹ thuật điện", "Kỹ thuật"},
            {"7520207", "Kỹ thuật điện tử viễn thông", "Kỹ thuật"},
            {"7520103", "Kỹ thuật cơ khí", "Kỹ thuật"},
            {"7520130", "Kỹ thuật ô tô", "Kỹ thuật"},
            {"7340301", "Kế toán", "Kinh tế"},
            {"7340201", "Tài chính - Ngân hàng", "Kinh tế"},
            {"7340115", "Marketing", "Kinh tế"},
            {"7380101", "Luật", "Luật"},
            {"7380107", "Luật kinh tế", "Luật"},
            {"7220209", "Ngôn ngữ Nhật", "Ngoại ngữ"},
            {"7220204", "Ngôn ngữ Trung Quốc", "Ngoại ngữ"},
            {"7220210", "Ngôn ngữ Hàn Quốc", "Ngoại ngữ"},
            {"7810101", "Du lịch", "Du lịch - Khách sạn"},
            {"7810201", "Quản trị khách sạn", "Du lịch - Khách sạn"},
            {"7580101", "Kiến trúc", "Kiến trúc - Xây dựng"},
            {"7580201", "Kỹ thuật xây dựng", "Kiến trúc - Xây dựng"},
            {"7320101", "Báo chí", "Báo chí - Truyền thông"}
    };

    // Cac to hop xet tuyen goc dua theo dac thu nganh (moi nganh se duoc them D01 mac dinh)
    private static List<String> combosForFaculty(String faculty) {
        switch (faculty) {
            case "Công nghệ thông tin":
            case "Kỹ thuật":
                return Arrays.asList("A00", "A01");
            case "Kinh tế":
                return Arrays.asList("A00", "A01");
            case "Luật":
                return Arrays.asList("A00", "C00");
            case "Ngoại ngữ":
                return Arrays.asList("D14");
            case "Du lịch - Khách sạn":
                return Arrays.asList("A01", "C00");
            case "Kiến trúc - Xây dựng":
                return Arrays.asList("A00", "A01");
            case "Báo chí - Truyền thông":
                return Arrays.asList("C00", "C19");
            default:
                return Arrays.asList("A00");
        }
    }

    // 3 mon tinh diem xet tuyen tuong ung moi to hop (khop voi cac cot diem trong Application)
    private static final Map<String, String[]> COMBO_SUBJECTS = new java.util.HashMap<>();
    static {
        COMBO_SUBJECTS.put("A00", new String[]{"math", "physics", "chemistry"});
        COMBO_SUBJECTS.put("A01", new String[]{"math", "physics", "english"});
        COMBO_SUBJECTS.put("B00", new String[]{"math", "chemistry", "biology"});
        COMBO_SUBJECTS.put("C00", new String[]{"literature", "history", "geography"});
        COMBO_SUBJECTS.put("C19", new String[]{"literature", "history", "civic"});
        COMBO_SUBJECTS.put("C20", new String[]{"literature", "geography", "civic"});
        COMBO_SUBJECTS.put("D01", new String[]{"literature", "math", "english"});
        COMBO_SUBJECTS.put("D14", new String[]{"literature", "history", "english"});
    }
    // To hop nao doi hoi khoi Tu nhien (NATURAL) hay Xa hoi (SOCIAL)
    private static final Set<String> NATURAL_COMBOS = new LinkedHashSet<>(Arrays.asList("A00", "A01", "B00"));
    private static final Set<String> SOCIAL_COMBOS = new LinkedHashSet<>(Arrays.asList("C00", "C19", "C20", "D14"));

    // =========================================================================
    // MO HINH DU LIEU TAM TRONG BO NHO
    // =========================================================================

    private static class MajorRec {
        long id;
        List<String> combos;
        MajorRec(long id, List<String> combos) { this.id = id; this.combos = combos; }
    }

    private static class SessionRec {
        long id;
        LocalDateTime start;
        LocalDateTime end;
        String status; // OPEN / CLOSED
        SessionRec(long id, LocalDateTime start, LocalDateTime end, String status) {
            this.id = id; this.start = start; this.end = end; this.status = status;
        }
    }

    // =========================================================================
    // RUN
    // =========================================================================

    @Override
    public void run(String... args) {
        Integer existingUsers = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (existingUsers != null && existingUsers > 500) {
            log.warn("DataSeeder: bang users da co {} dong (>500), BO QUA de tranh nhan doi du lieu.", existingUsers);
            return;
        }

        long t0 = System.currentTimeMillis();
        log.info("=== DataSeeder: bat dau sinh du lieu mo phong 5 nam van hanh (2021-2026) ===");

        List<MajorRec> majors = seedMajors();
        List<SessionRec> sessions = seedSessions();
        seedSessionMajors(sessions, majors);

        String defaultPasswordHash = passwordEncoder.encode("Pass@123");
        List<Long> staffIds = seedStaffAndAdmin(defaultPasswordHash);
        List<long[]> candidateUserPairs = seedCandidates(sessions, defaultPasswordHash); // [userId, candidateId, sessionIdx]

        Counters c = seedApplicationsAndChildren(candidateUserPairs, sessions, majors, staffIds);

        long ms = System.currentTimeMillis() - t0;
        log.info("=== SEED HOAN TAT trong {} ms ===", ms);
        log.info("majors moi: {} | sessions moi: {} | session_major: {}", majors.size(), sessions.size(), c.sessionMajorCount);
        log.info("users moi: {} | candidates moi: {}", candidateUserPairs.size() + staffIds.size(), candidateUserPairs.size());
        log.info("applications: {} | documents: {} | status_history: {} | notifications: {}",
                c.applications, c.documents, c.history, c.notifications);
    }

    private static class Counters {
        int sessionMajorCount, applications, documents, history, notifications;
    }

    // =========================================================================
    // 1) MAJORS + MAJOR_COMBINATION (danh muc - khong ep 1000)
    // =========================================================================
    private List<MajorRec> seedMajors() {
        long nextMajorId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM majors", Long.class);

        List<Object[]> majorRows = new ArrayList<>();
        List<Object[]> comboRows = new ArrayList<>();
        List<MajorRec> result = new ArrayList<>();

        for (String[] m : NEW_MAJORS) {
            long id = nextMajorId++;
            String code = m[0], name = m[1], faculty = m[2];
            majorRows.add(new Object[]{id, code, name, faculty, "Chương trình đào tạo ngành " + name});

            Set<String> combos = new LinkedHashSet<>(combosForFaculty(faculty));
            combos.add("D01"); // moi nganh deu chap nhan D01 de dam bao luon co to hop kha thi
            for (String cb : combos) {
                comboRows.add(new Object[]{id, cb});
            }
            result.add(new MajorRec(id, new ArrayList<>(combos)));
        }

        batchInsert("INSERT INTO majors (id, code, name, faculty, description) VALUES (?,?,?,?,?)", majorRows);
        batchInsert("INSERT INTO major_combination (major_id, combination_code) VALUES (?,?)", comboRows);

        // Doc lai TOAN BO majors + combo (gom ca nganh cu id 1,2,3,6... da co san) tu DB
        // de dung chung cho cac buoc sau (session_major, applications).
        return loadAllMajors();
    }

    private List<MajorRec> loadAllMajors() {
        Map<Long, List<String>> map = new java.util.LinkedHashMap<>();
        jdbc.query("SELECT id FROM majors", rs -> {
            map.put(rs.getLong("id"), new ArrayList<>());
        });
        jdbc.query("SELECT major_id, combination_code FROM major_combination WHERE combination_code IS NOT NULL", rs -> {
            long mid = rs.getLong("major_id");
            String cb = rs.getString("combination_code");
            map.computeIfAbsent(mid, k -> new ArrayList<>()).add(cb);
        });
        List<MajorRec> out = new ArrayList<>();
        for (Map.Entry<Long, List<String>> e : map.entrySet()) {
            List<String> combos = e.getValue().isEmpty() ? Arrays.asList("D01") : e.getValue();
            out.add(new MajorRec(e.getKey(), combos));
        }
        return out;
    }

    // =========================================================================
    // 2) ADMISSION_SESSIONS: 10 dot moi (2021-2025, 2 dot/nam) - da CLOSED
    //    (2 dot nam 2026 da co san trong CSDL, giu nguyen, status OPEN)
    // =========================================================================
    private List<SessionRec> seedSessions() {
        long nextId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM admission_sessions", Long.class);
        List<Object[]> rows = new ArrayList<>();
        List<SessionRec> newSessions = new ArrayList<>();

        for (int year = 2021; year <= 2025; year++) {
            // Dot 1: thang 3 - thang 6
            LocalDateTime s1 = LocalDateTime.of(year, 3, 1, 0, 0);
            LocalDateTime e1 = LocalDateTime.of(year, 6, 15, 23, 59);
            long id1 = nextId++;
            rows.add(new Object[]{id1, "Tuyển sinh Đại học " + year + " - Đợt 1", year + "-" + (year + 4),
                    Timestamp.valueOf(s1), Timestamp.valueOf(e1), "CLOSED",
                    "Đợt xét tuyển đầu tiên năm " + year, Timestamp.valueOf(s1.minusDays(10))});
            newSessions.add(new SessionRec(id1, s1, e1, "CLOSED"));

            // Dot 2: thang 8 - thang 10
            LocalDateTime s2 = LocalDateTime.of(year, 8, 1, 0, 0);
            LocalDateTime e2 = LocalDateTime.of(year, 10, 15, 23, 59);
            long id2 = nextId++;
            rows.add(new Object[]{id2, "Tuyển sinh Đại học " + year + " - Đợt 2", year + "-" + (year + 4),
                    Timestamp.valueOf(s2), Timestamp.valueOf(e2), "CLOSED",
                    "Đợt xét tuyển bổ sung năm " + year, Timestamp.valueOf(s2.minusDays(10))});
            newSessions.add(new SessionRec(id2, s2, e2, "CLOSED"));
        }

        batchInsert("INSERT INTO admission_sessions (id, name, academic_year, start_date, end_date, status, description, created_at) " +
                "VALUES (?,?,?,?,?,?,?,?)", rows);

        // Ghep voi 2 dot 2026 da co san (OPEN) de dung chung cho seed candidate/application
        List<SessionRec> all = new ArrayList<>(newSessions);
        long belowId = newSessions.isEmpty() ? Long.MAX_VALUE : newSessions.get(0).id;
        jdbc.query("SELECT id, start_date, end_date, status FROM admission_sessions WHERE id < ?",
                rs -> {
                    all.add(new SessionRec(rs.getLong("id"), rs.getTimestamp("start_date").toLocalDateTime(),
                            rs.getTimestamp("end_date").toLocalDateTime(), rs.getString("status")));
                },
                belowId);
        all.sort((a, b) -> a.start.compareTo(b.start));
        return all;
    }

    // =========================================================================
    // 3) SESSION_MAJOR: moi dot (10 dot moi) gan voi ~15-20 nganh ngau nhien
    // =========================================================================
    private void seedSessionMajors(List<SessionRec> sessions, List<MajorRec> majors) {
        long nextId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM session_major", Long.class);
        List<Object[]> rows = new ArrayList<>();

        for (SessionRec s : sessions) {
            if (!"CLOSED".equals(s.status)) continue; // 2 dot 2026 da co san session_major roi, bo qua
            List<MajorRec> shuffled = new ArrayList<>(majors);
            java.util.Collections.shuffle(shuffled, rnd);
            int howMany = 15 + rnd.nextInt(6); // 15-20 nganh moi dot
            for (int i = 0; i < howMany && i < shuffled.size(); i++) {
                MajorRec m = shuffled.get(i);
                int quota = 50 + rnd.nextInt(151); // 50-200 chi tieu
                BigDecimal benchmark = randScore(15, 27);
                rows.add(new Object[]{nextId++, s.id, m.id, quota, benchmark});
            }
        }
        batchInsert("INSERT INTO session_major (id, session_id, major_id, quota, benchmark_score) VALUES (?,?,?,?,?)", rows);
        // Nap lai anh xa session -> danh sach major duoc mo (dung cho buoc sinh applications)
        this.sessionMajorMap = new java.util.HashMap<>();
        jdbc.query("SELECT session_id, major_id FROM session_major", rs -> {
            sessionMajorMap.computeIfAbsent(rs.getLong("session_id"), k -> new ArrayList<>()).add(rs.getLong("major_id"));
        });
    }

    private Map<Long, List<Long>> sessionMajorMap;

    // =========================================================================
    // 4) STAFF + ADMIN moi (so luong nho, khong can 1000)
    // =========================================================================
    private List<Long> seedStaffAndAdmin(String pwHash) {
        long nextId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM users", Long.class);
        List<Object[]> rows = new ArrayList<>();
        List<Long> staffIds = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            long id = nextId++;
            boolean male = rnd.nextBoolean();
            String fullName = randomFullName(male);
            LocalDateTime createdAt = randomDateBetween(LocalDateTime.of(2021, 1, 1, 0, 0), NOW.minusDays(30));
            rows.add(new Object[]{id, "staff" + String.format("%03d", i + 10), pwHash,
                    "staff" + String.format("%03d", i + 10) + "@admission.edu.vn", fullName, "STAFF", true,
                    Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt)});
            staffIds.add(id);
        }
        for (int i = 1; i <= 2; i++) {
            long id = nextId++;
            boolean male = rnd.nextBoolean();
            String fullName = randomFullName(male);
            LocalDateTime createdAt = randomDateBetween(LocalDateTime.of(2021, 1, 1, 0, 0), NOW.minusDays(30));
            rows.add(new Object[]{id, "admin" + String.format("%02d", i + 1), pwHash,
                    "admin" + String.format("%02d", i + 1) + "@admission.edu.vn", fullName, "ADMIN", true,
                    Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt)});
        }

        batchInsert("INSERT INTO users (id, username, password, email, full_name, role, enabled, created_at, updated_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?)", rows);
        return staffIds;
    }

    // =========================================================================
    // 5) CANDIDATES (+ tai khoan USER role=CANDIDATE) - ~1000 dong
    // =========================================================================
    private List<long[]> seedCandidates(List<SessionRec> sessions, String pwHash) {
        long nextUserId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM users", Long.class);
        long nextCandidateId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM candidates", Long.class);

        int total = 1000;
        List<Object[]> userRows = new ArrayList<>();
        List<Object[]> candidateRows = new ArrayList<>();
        List<long[]> pairs = new ArrayList<>(); // {userId, candidateId, sessionIndex}

        // Trong so theo nam: cang gan hien tai cang nhieu thi sinh (he thong "phat trien dan")
        int[] weightByYearIndex = {6, 8, 10, 12, 14, 20}; // 2021,2022,2023,2024,2025,2026
        int totalWeight = Arrays.stream(weightByYearIndex).sum();

        for (int i = 1; i <= total; i++) {
            boolean male = rnd.nextBoolean();
            String fullName = randomFullName(male);

            // Chon nam theo trong so, roi chon 1 trong cac dot cua nam do
            int pick = rnd.nextInt(totalWeight);
            int yearIdx = 0, acc = 0;
            for (int w = 0; w < weightByYearIndex.length; w++) {
                acc += weightByYearIndex[w];
                if (pick < acc) { yearIdx = w; break; }
            }
            int year = 2021 + yearIdx;
            List<SessionRec> ofYear = new ArrayList<>();
            for (SessionRec s : sessions) if (s.start.getYear() == year) ofYear.add(s);
            if (ofYear.isEmpty()) ofYear = sessions; // an toan
            SessionRec cohort = ofYear.get(rnd.nextInt(ofYear.size()));
            int sessionIdx = sessions.indexOf(cohort);

            long userId = nextUserId++;
            long candidateId = nextCandidateId++;

            LocalDateTime registeredAt = randomDateBetween(cohort.start.minusDays(45), cohort.start.plusDays(10));
            if (registeredAt.isAfter(NOW)) registeredAt = NOW.minusDays(1);

            String username = "ts" + String.format("%05d", i);
            String email = username + "@gmail.com";

            userRows.add(new Object[]{userId, username, pwHash, email, fullName, "CANDIDATE", true,
                    Timestamp.valueOf(registeredAt), Timestamp.valueOf(registeredAt)});

            int graduationYear = year; // tot nghiep THPT dung nam dang ky xet tuyen (da so)
            candidateRows.add(new Object[]{
                    candidateId, userId,
                    java.sql.Date.valueOf(java.time.LocalDate.of(graduationYear - 18, 1 + rnd.nextInt(12), 1 + rnd.nextInt(28))),
                    male ? "MALE" : "FEMALE",
                    String.format("%012d", 300000000000L + i),
                    "0" + (3 + rnd.nextInt(7)) + String.format("%08d", rnd.nextInt(100000000)),
                    randomAddress(),
                    PROVINCES[rnd.nextInt(PROVINCES.length)],
                    "THPT " + PROVINCES[rnd.nextInt(PROVINCES.length)] + " " + (1 + rnd.nextInt(5)),
                    graduationYear
            });

            pairs.add(new long[]{userId, candidateId, sessionIdx});

            if (userRows.size() >= BATCH_SIZE) {
                batchInsert("INSERT INTO users (id, username, password, email, full_name, role, enabled, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)", userRows);
                batchInsert("INSERT INTO candidates (id, user_id, dob, gender, id_card_number, phone, address, province, high_school, graduation_year) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)", candidateRows);
                userRows.clear();
                candidateRows.clear();
            }
        }
        if (!userRows.isEmpty()) {
            batchInsert("INSERT INTO users (id, username, password, email, full_name, role, enabled, created_at, updated_at) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)", userRows);
            batchInsert("INSERT INTO candidates (id, user_id, dob, gender, id_card_number, phone, address, province, high_school, graduation_year) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)", candidateRows);
        }
        return pairs;
    }

    // =========================================================================
    // 6) APPLICATIONS + DOCUMENTS + APPLICATION_STATUS_HISTORY + NOTIFICATIONS
    // =========================================================================
    private Counters seedApplicationsAndChildren(List<long[]> candidateUserPairs, List<SessionRec> sessions,
                                                  List<MajorRec> majors, List<Long> staffIds) {
        Counters c = new Counters();

        long nextAppId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM applications", Long.class);
        long nextDocId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM documents", Long.class);
        long nextHistId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM application_status_history", Long.class);
        long nextNotiId = jdbc.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM notifications", Long.class);

        List<Object[]> appRows = new ArrayList<>();
        List<Object[]> docRows = new ArrayList<>();
        List<Object[]> histRows = new ArrayList<>();
        List<Object[]> notiRows = new ArrayList<>();

        for (long[] pair : candidateUserPairs) {
            long userId = pair[0], candidateId = pair[1];
            int sessionIdx = (int) pair[2];
            SessionRec session = sessions.get(sessionIdx);
            List<Long> availableMajors = sessionMajorMap.getOrDefault(session.id, java.util.Collections.emptyList());
            if (availableMajors.isEmpty()) continue;

            // Moi thi sinh nop it nhat 1 nguyen vong; ~8% nop them nguyen vong 2
            int numApps = (rnd.nextInt(100) < 8) ? 2 : 1;
            for (int p = 1; p <= numApps; p++) {
                long majorId = availableMajors.get(rnd.nextInt(availableMajors.size()));
                MajorRec majorRec = findMajor(majors, majorId);

                long appId = nextAppId++;
                boolean natural = rnd.nextBoolean();

                BigDecimal math = randScore(3, 10), lit = randScore(3, 10), eng = randScore(2, 10);
                BigDecimal phy = null, che = null, bio = null, his = null, geo = null, civ = null;
                if (natural) { phy = randScore(3, 10); che = randScore(3, 10); bio = randScore(3, 10); }
                else { his = randScore(3, 10); geo = randScore(3, 10); civ = randScore(3, 10); }

                String status = pickStatus(session.status);
                LocalDateTime submittedAt = null;
                LocalDateTime updatedAt;
                String combo = null;
                BigDecimal score = null;

                if (!"DRAFT".equals(status)) {
                    submittedAt = randomDateBetween(session.start, session.end);
                    if (submittedAt.isAfter(NOW)) submittedAt = NOW.minusDays(1);
                }

                boolean scored = Arrays.asList("APPROVED", "ADMITTED", "NOT_ADMITTED", "ENROLLED").contains(status);
                if (scored) {
                    List<String> feasible = new ArrayList<>();
                    for (String cb : majorRec.combos) {
                        boolean needsNatural = NATURAL_COMBOS.contains(cb);
                        boolean needsSocial = SOCIAL_COMBOS.contains(cb);
                        if (needsNatural && !natural) continue;
                        if (needsSocial && natural) continue;
                        feasible.add(cb);
                    }
                    if (feasible.isEmpty()) feasible.add("D01");
                    combo = feasible.get(rnd.nextInt(feasible.size()));
                    String[] subj = COMBO_SUBJECTS.get(combo);
                    score = sumSubjects(subj, math, lit, eng, phy, che, bio, his, geo, civ);
                }

                updatedAt = submittedAt != null ? submittedAt.plusDays(1 + rnd.nextInt(20)) : session.start;
                if (updatedAt.isAfter(NOW)) updatedAt = NOW;

                appRows.add(new Object[]{
                        appId, candidateId, session.id, majorId, p, score, status,
                        submittedAt == null ? null : Timestamp.valueOf(submittedAt),
                        Timestamp.valueOf(updatedAt), null,
                        bio, che, civ, eng, geo, his, lit, math, phy,
                        natural ? "NATURAL" : "SOCIAL", combo
                });

                // ---- application_status_history: chuoi trang thai hop le ----
                List<String> chain = statusChain(status);
                LocalDateTime cursor = submittedAt != null ? submittedAt : session.start;
                String prev = null;
                Long changer = staffIds.isEmpty() ? null : staffIds.get(rnd.nextInt(staffIds.size()));
                for (String st : chain) {
                    Long changedBy = "SUBMITTED".equals(st) ? Long.valueOf(userId) : changer;
                    histRows.add(new Object[]{nextHistId++, appId, prev, st,
                            changedBy,
                            Timestamp.valueOf(cursor), historyNote(st)});
                    // thong bao cho thi sinh khi trang thai thay doi (bo qua buoc SUBMITTED dau tien do chinh thi sinh tu thao tac)
                    if (!"SUBMITTED".equals(st) || chain.size() == 1) {
                        notiRows.add(new Object[]{nextNotiId++, userId, notiTitle(st), notiContent(st, majorId),
                                rnd.nextBoolean(), Timestamp.valueOf(cursor.plusHours(1))});
                    }
                    prev = st;
                    cursor = cursor.plusDays(1 + rnd.nextInt(15));
                    if (cursor.isAfter(NOW)) cursor = NOW;
                }

                // ---- documents: ho so khong con DRAFT thi phai co it nhat 2 file dinh kem ----
                if (!"DRAFT".equals(status)) {
                    String[] mandatoryTypes = {"TRANSCRIPT", "ID_CARD"};
                    for (String dt : mandatoryTypes) {
                        docRows.add(buildDocRow(nextDocId++, appId, dt, status, submittedAt));
                    }
                    if (rnd.nextInt(100) < 25) { // 25% co them giay to phu
                        docRows.add(buildDocRow(nextDocId++, appId, "PRIORITY_CERT", status, submittedAt));
                    }
                }

                c.applications++;
            }

            if (appRows.size() >= BATCH_SIZE) {
                flushAppBatches(appRows, docRows, histRows, notiRows, c);
            }
        }
        flushAppBatches(appRows, docRows, histRows, notiRows, c);

        c.sessionMajorCount = jdbc.queryForObject("SELECT COUNT(*) FROM session_major", Integer.class);
        return c;
    }

    private void flushAppBatches(List<Object[]> appRows, List<Object[]> docRows, List<Object[]> histRows,
                                  List<Object[]> notiRows, Counters c) {
        if (!appRows.isEmpty()) {
            batchInsert("INSERT INTO applications (id, candidate_id, session_id, major_id, priority_order, score, status, " +
                    "submitted_at, updated_at, note, biology_score, chemistry_score, civic_score, english_score, " +
                    "geography_score, history_score, literature_score, math_score, physics_score, subject_group, " +
                    "admission_combination) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", appRows);
            appRows.clear();
        }
        if (!docRows.isEmpty()) {
            batchInsert("INSERT INTO documents (id, application_id, doc_type, file_name, file_path, status, uploaded_at, reviewer_note) " +
                    "VALUES (?,?,?,?,?,?,?,?)", docRows);
            c.documents += docRows.size();
            docRows.clear();
        }
        if (!histRows.isEmpty()) {
            batchInsert("INSERT INTO application_status_history (id, application_id, old_status, new_status, changed_by, changed_at, note) " +
                    "VALUES (?,?,?,?,?,?,?)", histRows);
            c.history += histRows.size();
            histRows.clear();
        }
        if (!notiRows.isEmpty()) {
            batchInsert("INSERT INTO notifications (id, user_id, title, content, is_read, created_at) VALUES (?,?,?,?,?,?)", notiRows);
            c.notifications += notiRows.size();
            notiRows.clear();
        }
    }

    private Object[] buildDocRow(long id, long appId, String docType, String appStatus, LocalDateTime submittedAt) {
        String status;
        if ("REJECTED".equals(appStatus) && rnd.nextInt(100) < 30) status = "INVALID";
        else if ("SUBMITTED".equals(appStatus) || "NEED_SUPPLEMENT".equals(appStatus)) status = "PENDING";
        else status = "VALID";
        LocalDateTime uploadedAt = (submittedAt != null ? submittedAt : NOW.minusDays(30)).plusHours(rnd.nextInt(48));
        if (uploadedAt.isAfter(NOW)) uploadedAt = NOW;
        String fileName = docType.toLowerCase() + "_" + appId + ".pdf";
        String filePath = "uploads/documents/" + fileName;
        return new Object[]{id, appId, docType, fileName, filePath, status, Timestamp.valueOf(uploadedAt), null};
    }

    // Chuoi trang thai hop le dan den trang thai cuoi cung
    private List<String> statusChain(String finalStatus) {
        switch (finalStatus) {
            case "DRAFT": return Arrays.asList();
            case "SUBMITTED": return Arrays.asList("SUBMITTED");
            case "NEED_SUPPLEMENT": return Arrays.asList("SUBMITTED", "NEED_SUPPLEMENT");
            case "APPROVED": return Arrays.asList("SUBMITTED", "APPROVED");
            case "REJECTED": return Arrays.asList("SUBMITTED", "REJECTED");
            case "ADMITTED": return Arrays.asList("SUBMITTED", "APPROVED", "ADMITTED");
            case "NOT_ADMITTED": return Arrays.asList("SUBMITTED", "APPROVED", "NOT_ADMITTED");
            case "ENROLLED": return Arrays.asList("SUBMITTED", "APPROVED", "ADMITTED", "ENROLLED");
            default: return Arrays.asList("SUBMITTED");
        }
    }

    private String pickStatus(String sessionStatus) {
        int r = rnd.nextInt(100);
        if ("CLOSED".equals(sessionStatus)) {
            // dot da ket thuc lau -> phan lon ho so da co ket qua cuoi cung
            if (r < 35) return "ENROLLED";
            if (r < 60) return "NOT_ADMITTED";
            if (r < 75) return "REJECTED";
            if (r < 85) return "ADMITTED";
            if (r < 92) return "APPROVED";
            if (r < 97) return "SUBMITTED";
            return "NEED_SUPPLEMENT";
        } else {
            // dot dang mo (2026) -> phan lon con dang xu ly
            if (r < 10) return "DRAFT";
            if (r < 50) return "SUBMITTED";
            if (r < 60) return "NEED_SUPPLEMENT";
            if (r < 85) return "APPROVED";
            if (r < 95) return "ADMITTED";
            return "REJECTED";
        }
    }

    private String historyNote(String status) {
        switch (status) {
            case "SUBMITTED": return "Thí sinh nộp hồ sơ";
            case "NEED_SUPPLEMENT": return "Yêu cầu bổ sung minh chứng";
            case "APPROVED": return "Hồ sơ hợp lệ";
            case "REJECTED": return "Hồ sơ không hợp lệ";
            case "ADMITTED": return "Trúng tuyển theo điểm chuẩn";
            case "NOT_ADMITTED": return "Không đạt điểm chuẩn";
            case "ENROLLED": return "Thí sinh đã xác nhận nhập học";
            default: return null;
        }
    }

    private String notiTitle(String status) {
        switch (status) {
            case "SUBMITTED": return "Nộp hồ sơ thành công";
            case "NEED_SUPPLEMENT": return "Cần bổ sung hồ sơ";
            case "APPROVED": return "Hồ sơ đã được duyệt";
            case "REJECTED": return "Hồ sơ bị từ chối";
            case "ADMITTED": return "Chúc mừng bạn đã trúng tuyển!";
            case "NOT_ADMITTED": return "Kết quả xét tuyển";
            case "ENROLLED": return "Xác nhận nhập học thành công";
            default: return "Cập nhật hồ sơ";
        }
    }

    private String notiContent(String status, long majorId) {
        return historyNote(status) + " (ngành mã #" + majorId + ").";
    }

    // =========================================================================
    // HAM TIEN ICH
    // =========================================================================

    private MajorRec findMajor(List<MajorRec> majors, long id) {
        for (MajorRec m : majors) if (m.id == id) return m;
        return majors.get(0);
    }

    private BigDecimal sumSubjects(String[] subj, BigDecimal math, BigDecimal lit, BigDecimal eng, BigDecimal phy,
                                    BigDecimal che, BigDecimal bio, BigDecimal his, BigDecimal geo, BigDecimal civ) {
        Map<String, BigDecimal> map = new java.util.HashMap<>();
        map.put("math", math); map.put("literature", lit); map.put("english", eng);
        map.put("physics", phy); map.put("chemistry", che); map.put("biology", bio);
        map.put("history", his); map.put("geography", geo); map.put("civic", civ);
        BigDecimal total = BigDecimal.ZERO;
        for (String s : subj) {
            BigDecimal v = map.get(s);
            if (v == null) v = randScore(3, 10); // du phong neu thieu (khong nen xay ra)
            total = total.add(v);
        }
        return total.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal randScore(double min, double max) {
        double v = min + rnd.nextDouble() * (max - min);
        return BigDecimal.valueOf(Math.round(v * 4) / 4.0).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String randomFullName(boolean male) {
        String ho = HO[rnd.nextInt(HO.length)];
        String dem = male ? DEM_NAM[rnd.nextInt(DEM_NAM.length)] : DEM_NU[rnd.nextInt(DEM_NU.length)];
        String ten = male ? TEN_NAM[rnd.nextInt(TEN_NAM.length)] : TEN_NU[rnd.nextInt(TEN_NU.length)];
        return ho + " " + dem + " " + ten;
    }

    private String randomAddress() {
        return "Số " + (1 + rnd.nextInt(300)) + ", đường " + (1 + rnd.nextInt(50)) + ", " + PROVINCES[rnd.nextInt(PROVINCES.length)];
    }

    private LocalDateTime randomDateBetween(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) return start;
        long startSec = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endSec = end.toEpochSecond(java.time.ZoneOffset.UTC);
        long randSec = startSec + (long) (rnd.nextDouble() * (endSec - startSec));
        return LocalDateTime.ofEpochSecond(randSec, 0, java.time.ZoneOffset.UTC);
    }

    private void batchInsert(String sql, List<Object[]> rows) {
        if (rows.isEmpty()) return;
        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            List<Object[]> sub = rows.subList(i, Math.min(i + BATCH_SIZE, rows.size()));
            jdbc.batchUpdate(sql, sub);
        }
    }
}
