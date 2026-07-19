package com.jobbed.application;

import com.jobbed.company.Company;
import com.jobbed.tag.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Baut aus einem {@link ApplicationFilter} eine JPA-Specification. Der
 * {@code userId}-Filter wird immer angewandt (Mandantentrennung).
 */
public final class JobApplicationSpecifications {

    private JobApplicationSpecifications() {
    }

    public static Specification<JobApplication> forFilter(ApplicationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), filter.userId()));

            if (filter.statuses() != null && !filter.statuses().isEmpty()) {
                predicates.add(root.get("currentStatus").in(filter.statuses()));
            }
            if (filter.companyId() != null) {
                predicates.add(cb.equal(root.get("company").get("id"), filter.companyId()));
            }
            if (filter.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.priority()));
            }
            if (filter.workModel() != null) {
                predicates.add(cb.equal(root.get("workModel"), filter.workModel()));
            }
            if (StringUtils.hasText(filter.location())) {
                predicates.add(cb.like(cb.lower(root.get("location")), like(filter.location())));
            }
            if (filter.applicationDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("applicationDate"), filter.applicationDateFrom()));
            }
            if (filter.applicationDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("applicationDate"), filter.applicationDateTo()));
            }
            if (filter.tagIds() != null && !filter.tagIds().isEmpty()) {
                Join<JobApplication, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagJoin.get("id").in(filter.tagIds()));
                if (query != null) {
                    query.distinct(true);
                }
            }
            if (StringUtils.hasText(filter.query())) {
                String pattern = like(filter.query());
                Join<JobApplication, Company> companyJoin = root.join("company", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("jobTitle")), pattern),
                        cb.like(cb.lower(root.get("jobDescription")), pattern),
                        cb.like(cb.lower(root.get("notes")), pattern),
                        cb.like(cb.lower(root.get("location")), pattern),
                        cb.like(cb.lower(companyJoin.get("name")), pattern)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
