package rs.raf.mtomic.ga2026.cvrp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ValidationResult {

    private final boolean valid;
    private final List<String> violations;

    private ValidationResult(boolean valid, List<String> violations) {
        this.valid = valid;
        this.violations = Collections.unmodifiableList(new ArrayList<>(violations));
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(List<String> violations) {
        return new ValidationResult(false, violations);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getViolations() {
        return violations;
    }

    @Override
    public String toString() {
        if (valid) return "VALID";
        return "INVALID: " + String.join("; ", violations);
    }
}
