import java.util.*;

/**
 * Lightweight rate limiter for OTP validation attempts.
 * Locks a username for a configurable duration after too many failed attempts.
 */
public class OTPAttemptTracker {
    private static final Map<String, Deque<Long>> otpFailures = new HashMap<>();
    private static final Map<String, Long> lockedUsers = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Check whether the user is currently locked out.
     */
    public static boolean isLocked(String username) {
        if (lockedUsers.containsKey(username)) {
            long unlockTime = lockedUsers.get(username);
            if (System.currentTimeMillis() < unlockTime) {
                return true;
            } else {
                lockedUsers.remove(username); // unlock after lock period
            }
        }
        return false;
    }

    /**
     * Record a failed OTP attempt and lock the user if threshold is reached quickly.
     */
    public static void recordFailure(String username) {
        otpFailures.putIfAbsent(username, new LinkedList<>());
        Deque<Long> failures = otpFailures.get(username);
        failures.addLast(System.currentTimeMillis());
        if (failures.size() > MAX_ATTEMPTS) {
            failures.removeFirst(); // keep only latest 3 attempts
        }

        if (failures.size() == MAX_ATTEMPTS) {
            long firstAttemptTime = failures.peekFirst();
            if (System.currentTimeMillis() - firstAttemptTime < LOCK_DURATION_MS) {
                lockedUsers.put(username, System.currentTimeMillis() + LOCK_DURATION_MS);
                failures.clear();
            }
        }
    }
}
