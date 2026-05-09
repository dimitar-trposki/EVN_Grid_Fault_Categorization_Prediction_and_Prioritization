package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.specification;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class FaultSpecification {

    private FaultSpecification() {}

    public static Specification<FaultReport> hasFaultType(FaultType faultType) {
        return (root, query, cb) ->
                faultType == null ? null : cb.equal(root.get("faultType"), faultType);
    }

    public static Specification<FaultReport> hasFaultPriority(FaultPriority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("faultPriority"), priority);
    }

    public static Specification<FaultReport> hasFaultClassification(FaultClassification classification) {
        return (root, query, cb) ->
                classification == null ? null : cb.equal(root.get("faultClassification"), classification);
    }

    public static Specification<FaultReport> hasLocationId(Long locationId) {
        return (root, query, cb) ->
                locationId == null ? null : cb.equal(root.get("location").get("id"), locationId);
    }

    public static Specification<FaultReport> hasRegionId(Long regionId) {
        return (root, query, cb) ->
                regionId == null ? null : cb.equal(root.get("location").get("region").get("id"), regionId);
    }

    public static Specification<FaultReport> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<FaultReport> reportedAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("reportedAt"), from);
    }

    public static Specification<FaultReport> reportedBefore(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("reportedAt"), to);
    }

    public static Specification<FaultReport> hasCurrentStatus(FaultStatus status) {
        if (status == null) return (root, query, cb) -> null;
        return (root, query, cb) -> {
            var inSub = query.subquery(Long.class);
            var h = inSub.from(FaultStatusHistory.class);

            var maxSub = query.subquery(LocalDateTime.class);
            var h2 = maxSub.from(FaultStatusHistory.class);
            maxSub.select(cb.greatest(h2.<LocalDateTime>get("changedAt")))
                  .where(cb.equal(h2.get("faultReport").get("id"), h.get("faultReport").get("id")));

            inSub.select(h.get("faultReport").get("id"))
                 .where(
                     cb.equal(h.get("faultStatus"), status),
                     cb.equal(h.get("changedAt"), maxSub)
                 );

            return root.get("id").in(inSub);
        };
    }
}
