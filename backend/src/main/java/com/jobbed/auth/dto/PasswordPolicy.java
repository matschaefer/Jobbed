package com.jobbed.auth.dto;

/** Zentrale Passwortrichtlinie (server- und dokumentationsseitig). */
public final class PasswordPolicy {

    /** Mindestens 10 Zeichen, je ein Klein-/Großbuchstabe, eine Ziffer und ein Sonderzeichen. */
    public static final String REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,128}$";

    public static final String MESSAGE =
            "Das Passwort muss mindestens 10 Zeichen lang sein und Groß- und "
                    + "Kleinbuchstaben, eine Ziffer sowie ein Sonderzeichen enthalten.";

    private PasswordPolicy() {
    }
}
