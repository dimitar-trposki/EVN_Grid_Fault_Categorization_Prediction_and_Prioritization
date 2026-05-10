package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.NotificationResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.NotificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.UnauthorizedException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.NotificationMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.SystemNotification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.NotificationStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CustomerRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.SystemNotificationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final SystemNotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final NotificationMapper mapper;

    public NotificationServiceImpl(SystemNotificationRepository notificationRepo,
                                   UserRepository userRepo,
                                   CustomerRepository customerRepo,
                                   NotificationMapper mapper) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.mapper = mapper;
    }

    // --- Legacy deprecated methods ---

    @Override
    @Deprecated
    public void sendToUser(User user, String message) {
        try {
            SystemNotification n = new SystemNotification();
            n.setUser(user);
            n.setTitle("Notification");
            n.setMessage(message);
            n.setChannel("IN_APP");
            n.setNotificationStatus(NotificationStatus.UNREAD);
            n.setCreatedAt(LocalDateTime.now());
            notificationRepo.save(n);
        } catch (Exception e) {
            log.warn("Failed to persist legacy notification for userId={}: {}", user.getId(), e.getMessage());
        }
    }

    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getForUser(Long userId) {
        return notificationRepo.findByUserId(userId).stream()
                .map(n -> new NotificationResponseDto(n.getId(), n.getMessage(), n.getNotificationStatus()))
                .toList();
    }

    @Override
    @Deprecated
    public void markAsRead(Long notificationId) {
        SystemNotification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        n.setNotificationStatus(NotificationStatus.READ);
        notificationRepo.save(n);
    }

    // --- New internal send methods ---

    @Override
    public void sendToUser(Long userId, String title, String message, String type) {
        try {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot send notification — user {} not found", userId);
                return;
            }
            SystemNotification n = buildNotification(title, message, type);
            n.setUser(user);
            notificationRepo.save(n);
        } catch (Exception e) {
            log.warn("Failed to persist notification for userId={}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendToCustomer(Long customerId, String title, String message, String type) {
        try {
            Customer customer = customerRepo.findById(customerId).orElse(null);
            if (customer == null) {
                log.warn("Cannot send notification — customer {} not found", customerId);
                return;
            }
            SystemNotification n = buildNotification(title, message, type);
            n.setCustomer(customer);
            notificationRepo.save(n);
        } catch (Exception e) {
            log.warn("Failed to persist notification for customerId={}: {}", customerId, e.getMessage());
        }
    }

    // --- New user-facing methods ---

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String callerEmail) {
        return fetchForCaller(callerEmail, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnread(String callerEmail) {
        return fetchForCaller(callerEmail, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnread(String callerEmail) {
        User user = requireUser(callerEmail);
        if (user.getUserRole() == RoleType.CUSTOMER) {
            Customer customer = requireCustomer(user);
            return notificationRepo.countByCustomerIdAndNotificationStatus(customer.getId(), NotificationStatus.UNREAD);
        }
        return notificationRepo.countByUserIdAndNotificationStatus(user.getId(), NotificationStatus.UNREAD);
    }

    @Override
    public void markAsRead(Long id, String callerEmail) {
        SystemNotification n = notificationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        verifyOwnership(n, callerEmail);
        n.setNotificationStatus(NotificationStatus.READ);
        notificationRepo.save(n);
    }

    @Override
    public void markAllMineAsRead(String callerEmail) {
        User user = requireUser(callerEmail);
        List<SystemNotification> unread;
        if (user.getUserRole() == RoleType.CUSTOMER) {
            Customer customer = requireCustomer(user);
            unread = notificationRepo.findByCustomerIdAndNotificationStatus(customer.getId(), NotificationStatus.UNREAD);
        } else {
            unread = notificationRepo.findByUserIdAndNotificationStatus(user.getId(), NotificationStatus.UNREAD);
        }
        unread.forEach(n -> n.setNotificationStatus(NotificationStatus.READ));
        notificationRepo.saveAll(unread);
    }

    // --- Private helpers ---

    private SystemNotification buildNotification(String title, String message, String type) {
        SystemNotification n = new SystemNotification();
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setChannel("IN_APP");
        n.setNotificationStatus(NotificationStatus.UNREAD);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    private List<NotificationResponse> fetchForCaller(String callerEmail, boolean unreadOnly) {
        User user = requireUser(callerEmail);
        List<SystemNotification> notifications;
        if (user.getUserRole() == RoleType.CUSTOMER) {
            Customer customer = requireCustomer(user);
            notifications = unreadOnly
                    ? notificationRepo.findByCustomerIdAndNotificationStatus(customer.getId(), NotificationStatus.UNREAD)
                    : notificationRepo.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        } else {
            notifications = unreadOnly
                    ? notificationRepo.findByUserIdAndNotificationStatus(user.getId(), NotificationStatus.UNREAD)
                    : notificationRepo.findByUserIdOrderByCreatedAtDesc(user.getId());
        }
        return notifications.stream().map(mapper::toResponse).toList();
    }

    private void verifyOwnership(SystemNotification n, String callerEmail) {
        User caller = requireUser(callerEmail);
        if (caller.getUserRole() == RoleType.CUSTOMER) {
            Customer customer = requireCustomer(caller);
            if (n.getCustomer() == null || !n.getCustomer().getId().equals(customer.getId())) {
                throw new UnauthorizedException("You do not have access to this notification");
            }
        } else {
            if (n.getUser() == null || !n.getUser().getId().equals(caller.getId())) {
                throw new UnauthorizedException("You do not have access to this notification");
            }
        }
    }

    private User requireUser(String callerEmail) {
        return userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User for email: " + callerEmail));
    }

    private Customer requireCustomer(User user) {
        return customerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer for userId: " + user.getId()));
    }
}
