package com.aotemiao.artemis.symphony.core.validation;

import java.util.List;

/**
 * Result of dispatch preflight validation (SPEC Section 6.3).
 */
public record DispatchValidation(boolean ok, List<String> errors) {

    public static DispatchValidation success() {
        return new DispatchValidation(true, List.of());
    }

    public static DispatchValidation failure(String... errors) {
        return new DispatchValidation(false, List.of(errors));
    }

    public static DispatchValidation failure(List<String> errors) {
        return new DispatchValidation(false, errors);
    }
}
