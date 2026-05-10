package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.config;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final LocationRepository locationRepository;
    private final FaultReportRepository faultReportRepository;
    private final FaultStatusHistoryRepository historyRepository;
    private final CrewRepository crewRepository;
    private final EquipmentRepository equipmentRepository;
    private final FaultAssigmentRepository assignmentRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                // 1. Regions
                Region skopje = createRegion("Skopje");
                Region ohrid = createRegion("Ohrid");
                Region bitola = createRegion("Bitola");
                Region tetovo = createRegion("Tetovo");

                // 2. Locations
                Location loc1 = createLocation("Partizanski Odredi 1, Skopje", 41.9981, 21.4254, skopje);
                Location loc2 = createLocation("Jane Sandanski 22, Skopje", 41.9856, 21.4682, skopje);
                Location loc3 = createLocation("Tourist Blvd 100, Ohrid", 41.1172, 20.8016, ohrid);
                Location loc4 = createLocation("Shirok Sokak 1, Bitola", 41.0297, 21.3347, bitola);
                Location loc5 = createLocation("Ilindenska, Tetovo", 42.0100, 20.9700, tetovo);

                // 3. Equipment
                createEquipment("Transformer T-101", EquipmentType.TRANSFORMER, loc1);
                createEquipment("Substation S-Skopje-East", EquipmentType.SUBSTATION, loc2);
                createEquipment("Distribution Box DB-Ohrid-1", EquipmentType.DISTRIBUTION_BOX, loc3);
                createEquipment("Overhead Line OHL-Bitola-Central", EquipmentType.OVERHEAD_LINE, loc4);

                // 4. Users
                User admin = createUser("Admin", "User", "admin@evn.mk", "admin123", RoleType.ADMIN);
                User operator = createUser("Jane", "Operator", "operator@evn.mk", "operator123", RoleType.OPERATOR);
                User dispatcher = createUser("John", "Dispatcher", "dispatcher@evn.mk", "dispatch123", RoleType.DISPATCHER);

                // 5. Crews
                Crew alpha = createCrew("Crew Alpha", "CRW-001", CrewStatus.EN_ROUTE, 41.9980, 21.4250, skopje);
                Crew beta = createCrew("Crew Beta", "CRW-002", CrewStatus.AVAILABLE, 41.9800, 21.4600, skopje);
                Crew gamma = createCrew("Crew Gamma", "CRW-003", CrewStatus.AVAILABLE, 41.1100, 20.8000, ohrid);
                
                createCrewMember("Mike", "Technician", alpha, "Lead");
                createCrewMember("Sarah", "Electrician", alpha, "Member");
                createCrewMember("Bob", "Driver", beta, "Member");

                // 6. Fault Reports (Diverse set)
                FaultReport f1 = createFault("Transformer Sparking", "Sparks seen at T-101", FaultType.EQUIPMENT_FAILURE, FaultPriority.CRITICAL, loc1, FaultSourceType.OPERATOR_CALL_CENTER, admin);
                FaultReport f2 = createFault("Low Voltage", "Dim lights in the neighborhood", FaultType.VOLTAGE_DROP, FaultPriority.MEDIUM, loc2, FaultSourceType.CUSTOMER_PORTAL, admin);
                FaultReport f3 = createFault("Power Outage", "Whole street in the dark", FaultType.POWER_OUTAGE, FaultPriority.HIGH, loc3, FaultSourceType.CUSTOMER_PORTAL, admin);
                FaultReport f4 = createFault("Loose Cable", "Cable hanging low after storm", FaultType.INSULATION_FAILURE, FaultPriority.HIGH, loc4, FaultSourceType.OPERATOR_CALL_CENTER, admin);
                FaultReport f5 = createFault("Meter Malfunction", "Smart meter display blank", FaultType.OTHER, FaultPriority.LOW, loc5, FaultSourceType.CUSTOMER_PORTAL, admin);
                
                // Add more faults for stats
                for (int i = 1; i <= 10; i++) {
                    createFault("Fault #" + i, "Automated description " + i, 
                        FaultType.values()[random.nextInt(FaultType.values().length)],
                        FaultPriority.values()[random.nextInt(FaultPriority.values().length)],
                        loc1, FaultSourceType.CUSTOMER_PORTAL, admin);
                }

                // 7. Assignments
                createAssignment(f1, alpha, admin, "Urgent - heavy sparks");
                createAssignment(f4, beta, admin, "Verify cable safety");

                System.out.println("Dev Data Initialized: admin@evn.mk / admin123");
            }
        };
    }

    private Region createRegion(String name) {
        Region r = new Region();
        r.setName(name);
        return regionRepository.save(r);
    }

    private Location createLocation(String addr, double lat, double lng, Region r) {
        Location l = new Location();
        l.setAddress(addr);
        l.setLatitude(lat);
        l.setLongitude(lng);
        l.setRegion(r);
        return locationRepository.save(l);
    }

    private User createUser(String fn, String ln, String email, String pwd, RoleType role) {
        User u = new User();
        u.setFirstName(fn);
        u.setLastName(ln);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pwd));
        u.setUserRole(role);
        return userRepository.save(u);
    }

    private Equipment createEquipment(String name, EquipmentType type, Location loc) {
        Equipment e = new Equipment();
        e.setName(name);
        e.setEquipmentType(type);
        e.setLocation(loc);
        return equipmentRepository.save(e);
    }

    private Crew createCrew(String name, String code, CrewStatus status, double lat, double lng, Region region) {
        Crew c = new Crew();
        c.setName(name);
        c.setCrewCode(code);
        c.setStatus(status);
        c.setCurrentLatitude(lat);
        c.setCurrentLongitude(lng);
        c.setRegion(region);
        return crewRepository.save(c);
    }

    private void createCrewMember(String fn, String ln, Crew c, String pos) {
        CrewMember m = new CrewMember();
        m.setFirstName(fn);
        m.setLastName(ln);
        m.setCrew(c);
        m.setPosition(pos);
        m.setAssignedAt(LocalDateTime.now());
        crewMemberRepository.save(m);
    }

    private FaultReport createFault(String title, String desc, FaultType type, FaultPriority prio, Location loc, FaultSourceType src, User admin) {
        FaultReport f = new FaultReport();
        f.setTitle(title);
        f.setDescription(desc);
        f.setFaultType(type);
        f.setFaultPriority(prio);
        f.setFaultClassification(FaultClassification.OTHER);
        f.setLocation(loc);
        f.setSourceType(src);
        f = faultReportRepository.save(f);
        
        addStatusHistory(f, FaultStatus.REPORTED, admin, "Initial seed data");
        return f;
    }

    private void createAssignment(FaultReport f, Crew c, User u, String note) {
        FaultAssignment a = new FaultAssignment();
        a.setFaultReport(f);
        a.setCrew(c);
        a.setAssignedByUser(u);
        a.setAssignedAt(LocalDateTime.now().minusHours(1));
        a.setAssignmentStatus("ASSIGNED");
        a.setAssignmentNote(note);
        a.setFaultStatus(FaultStatus.ASSIGNED);
        assignmentRepository.save(a);
        
        f.setFaultPriority(FaultPriority.CRITICAL); // Ensure assigned faults are prominent
        faultReportRepository.save(f);
    }

    private void addStatusHistory(FaultReport fault, FaultStatus status, User user, String note) {
        FaultStatusHistory history = new FaultStatusHistory();
        history.setFaultReport(fault);
        history.setFaultStatus(status);
        history.setChangedBy(user);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);
        history.setCustomerVisible(true);
        historyRepository.save(history);
    }
}


