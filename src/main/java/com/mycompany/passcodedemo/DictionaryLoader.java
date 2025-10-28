package com.mycompany.passcodedemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Loads dictionary data from resources bundled with the application.
 */
public final class DictionaryLoader {

    private static final String COMMON_PASSWORDS_RESOURCE = "common_passwords.txt";

    private DictionaryLoader() {
    }

    /**
     * Loads a set of common passwords from the application resources.
     *
     * @return an immutable set of common passwords
     * @throws IOException when the resource cannot be read
     */
    public static Set<String> loadCommonPasswords() throws IOException {
        InputStream in = DictionaryLoader.class.getClassLoader()
                .getResourceAsStream(COMMON_PASSWORDS_RESOURCE);
        if (in == null) {
            throw new IOException("Resource not found: " + COMMON_PASSWORDS_RESOURCE);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Set<String> passwords = new LinkedHashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    passwords.add(trimmed);
                }
            }
            return Set.copyOf(passwords);
        }
    }
}
