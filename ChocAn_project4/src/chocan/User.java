package chocan;

import java.util.Objects;

/** Authenticated session information consumed by the existing Login and menus.
 * Uses record accessors username(), role(), and providerId() to match that code.
 * Author: Maggie Skinner (AI-assisted implementation).
 */
public record User(String username, Role role, String providerId) {
    public User {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(role, "Role is required");
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (role == Role.PROVIDER &&
                (providerId == null || !providerId.matches("[0-9]{9}"))) {
            throw new IllegalArgumentException("Provider users need a nine-digit provider number");
        }
        if (role != Role.PROVIDER && providerId != null) {
            throw new IllegalArgumentException("Only provider users have a provider number");
        }
    }
}
