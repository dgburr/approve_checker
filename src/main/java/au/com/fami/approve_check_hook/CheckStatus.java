package au.com.fami.approve_check_hook;

/**
 * Encapsulates the status of a check and an optional error message
 */
class CheckStatus {
    private final boolean passed;
    private final String message;

    public static CheckStatus passed() {
        return new CheckStatus(true, "");
    }

    public static CheckStatus failed(String message) {
        return new CheckStatus(false, message);
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    private CheckStatus (boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }
}
