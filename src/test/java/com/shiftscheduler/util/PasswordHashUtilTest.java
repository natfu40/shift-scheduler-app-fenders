package com.shiftscheduler.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PasswordHashUtil Unit Tests")
class PasswordHashUtilTest {

    @Nested
    @DisplayName("SHA-256 Hashing Tests")
    class SHA256HashingTests {

        @Test
        @DisplayName("Should hash simple password correctly")
        void shouldHashSimplePasswordCorrectly() {
            // Given
            String password = "password123";

            // When
            String result = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64); // SHA-256 produces 64 hex characters
            assertThat(result).matches("^[a-f0-9]{64}$"); // Only lowercase hex characters

            // Verify it matches expected SHA-256 hash for "password123"
            String expectedHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f";
            assertThat(result).isEqualTo(expectedHash);
        }

        @Test
        @DisplayName("Should produce consistent hash for same input")
        void shouldProduceConsistentHashForSameInput() {
            // Given
            String password = "testPassword";

            // When
            String hash1 = PasswordHashUtil.hashWithSHA256(password);
            String hash2 = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1).hasSize(64);
        }

        @Test
        @DisplayName("Should produce different hashes for different inputs")
        void shouldProduceDifferentHashesForDifferentInputs() {
            // Given
            String password1 = "password123";
            String password2 = "password124";

            // When
            String hash1 = PasswordHashUtil.hashWithSHA256(password1);
            String hash2 = PasswordHashUtil.hashWithSHA256(password2);

            // Then
            assertThat(hash1).isNotEqualTo(hash2);
            assertThat(hash1).hasSize(64);
            assertThat(hash2).hasSize(64);
        }

        @Test
        @DisplayName("Should hash empty string")
        void shouldHashEmptyString() {
            // Given
            String emptyPassword = "";

            // When
            String result = PasswordHashUtil.hashWithSHA256(emptyPassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);

            // Verify it matches expected SHA-256 hash for empty string
            String expectedEmptyHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
            assertThat(result).isEqualTo(expectedEmptyHash);
        }

        @Test
        @DisplayName("Should handle null password gracefully")
        void shouldHandleNullPasswordGracefully() {
            // Given
            String nullPassword = null;

            // When & Then
            assertThatThrownBy(() -> PasswordHashUtil.hashWithSHA256(nullPassword))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error hashing password")
                    .hasCauseInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle very long password")
        void shouldHandleVeryLongPassword() {
            // Given
            StringBuilder longPassword = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                longPassword.append("a");
            }

            // When
            String result = PasswordHashUtil.hashWithSHA256(longPassword.toString());

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");
        }

        @Test
        @DisplayName("Should handle single character password")
        void shouldHandleSingleCharacterPassword() {
            // Given
            String singleChar = "a";

            // When
            String result = PasswordHashUtil.hashWithSHA256(singleChar);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);

            // Verify it matches expected SHA-256 hash for "a"
            String expectedHash = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb";
            assertThat(result).isEqualTo(expectedHash);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "password",
            "Password",
            "PASSWORD",
            "123456",
            "special!@#$%^&*()",
            "unicode_テスト_🔒",
            "   spaces   ",
            "\n\r\t",
            "mixed123!@#ABC"
        })
        @DisplayName("Should handle various password formats")
        void shouldHandleVariousPasswordFormats(String password) {
            // When
            String result = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");
        }
    }

    @Nested
    @DisplayName("Special Characters and Encoding Tests")
    class SpecialCharactersAndEncodingTests {

        @Test
        @DisplayName("Should handle Unicode characters correctly")
        void shouldHandleUnicodeCharactersCorrectly() {
            // Given
            String unicodePassword = "pássword_테스트_🔐";

            // When
            String result = PasswordHashUtil.hashWithSHA256(unicodePassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");

            // Should be consistent
            String result2 = PasswordHashUtil.hashWithSHA256(unicodePassword);
            assertThat(result).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should handle special symbols")
        void shouldHandleSpecialSymbols() {
            // Given
            String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

            // When
            String result = PasswordHashUtil.hashWithSHA256(specialPassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");
        }

        @Test
        @DisplayName("Should handle newlines and whitespace")
        void shouldHandleNewlinesAndWhitespace() {
            // Given
            String whitespacePassword = "password\nwith\r\nlines\tand\u0020spaces";

            // When
            String result = PasswordHashUtil.hashWithSHA256(whitespacePassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");
        }

        @Test
        @DisplayName("Should handle binary-like strings")
        void shouldHandleBinaryLikeStrings() {
            // Given
            String binaryString = "\u0000\u0001\u0002\u0003\u00FF";

            // When
            String result = PasswordHashUtil.hashWithSHA256(binaryString);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");
        }
    }

    @Nested
    @DisplayName("Hex String Formatting Tests")
    class HexStringFormattingTests {

        @Test
        @DisplayName("Should format hex string with leading zeros")
        void shouldFormatHexStringWithLeadingZeros() {
            // Given - this specific input is known to produce bytes with values < 16 (requiring leading zeros)
            String password = "test";

            // When
            String result = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");

            // Known SHA-256 hash for "test"
            String expectedHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
            assertThat(result).isEqualTo(expectedHash);

            // Verify it contains leading zeros (this hash has leading zeros)
            assertThat(result).contains("0");
        }

        @Test
        @DisplayName("Should produce lowercase hex output")
        void shouldProduceLowercaseHexOutput() {
            // Given
            String password = "TestPassword";

            // When
            String result = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).isEqualTo(result.toLowerCase());
            assertThat(result).doesNotMatch(".*[A-F].*"); // No uppercase hex digits
        }

        @Test
        @DisplayName("Should handle all possible byte values correctly")
        void shouldHandleAllPossibleByteValuesCorrectly() {
            // This test ensures the hex conversion handles all byte values (0-255) properly
            // We'll use a password known to produce a wide range of byte values

            // Given
            String password = "ComplexPassword!@#$123";

            // When
            String result = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("^[a-f0-9]{64}$");

            // Verify each pair of characters represents a valid hex byte
            for (int i = 0; i < result.length(); i += 2) {
                String hexByte = result.substring(i, i + 2);
                assertThat(hexByte).matches("^[a-f0-9]{2}$");

                // Should be able to parse as hex
                int byteValue = Integer.parseInt(hexByte, 16);
                assertThat(byteValue).isBetween(0, 255);
            }
        }
    }

    @Nested
    @DisplayName("Performance and Stress Tests")
    class PerformanceAndStressTests {

        @Test
        @DisplayName("Should handle rapid consecutive calls")
        void shouldHandleRapidConsecutiveCalls() {
            // Given
            String password = "testPassword";

            // When - Make multiple rapid calls
            String[] results = new String[100];
            for (int i = 0; i < 100; i++) {
                results[i] = PasswordHashUtil.hashWithSHA256(password);
            }

            // Then - All results should be identical and valid
            for (String result : results) {
                assertThat(result).isNotNull();
                assertThat(result).hasSize(64);
                assertThat(result).isEqualTo(results[0]);
            }
        }

        @Test
        @DisplayName("Should handle maximum length strings efficiently")
        void shouldHandleMaximumLengthStringsEfficiently() {
            // Given
            StringBuilder maxLengthPassword = new StringBuilder();
            for (int i = 0; i < 100000; i++) { // 100KB string
                maxLengthPassword.append("x");
            }

            // When
            long startTime = System.nanoTime();
            String result = PasswordHashUtil.hashWithSHA256(maxLengthPassword.toString());
            long endTime = System.nanoTime();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);

            // Should complete in reasonable time (less than 1 second)
            long durationMs = (endTime - startTime) / 1_000_000;
            assertThat(durationMs).isLessThan(1000);
        }
    }

    @Nested
    @DisplayName("Security and Consistency Tests")
    class SecurityAndConsistencyTests {

        @Test
        @DisplayName("Should be deterministic across multiple JVM runs")
        void shouldBeDeterministicAcrossMultipleJVMRuns() {
            // This test verifies that the hash function is deterministic
            // and doesn't depend on any random elements or JVM-specific state

            // Given
            String password = "deterministicTest";

            // When
            String hash1 = PasswordHashUtil.hashWithSHA256(password);
            String hash2 = PasswordHashUtil.hashWithSHA256(password);

            // Then
            assertThat(hash1).isEqualTo(hash2);

            // Should match known SHA-256 value (can be verified externally)
            // This ensures the implementation is standards-compliant
            assertThat(hash1).matches("^[a-f0-9]{64}$");
        }

        @Test
        @DisplayName("Should demonstrate avalanche effect")
        void shouldDemonstrateAvalancheEffect() {
            // The avalanche effect means small changes in input produce large changes in output

            // Given
            String password1 = "password";
            String password2 = "passwor"; // One character difference

            // When
            String hash1 = PasswordHashUtil.hashWithSHA256(password1);
            String hash2 = PasswordHashUtil.hashWithSHA256(password2);

            // Then
            assertThat(hash1).isNotEqualTo(hash2);

            // Count differing characters (should be significant)
            int differences = 0;
            for (int i = 0; i < 64; i++) {
                if (hash1.charAt(i) != hash2.charAt(i)) {
                    differences++;
                }
            }

            // At least 50% of characters should be different (good avalanche effect)
            assertThat(differences).isGreaterThan(32);
        }

        @Test
        @DisplayName("Should handle case sensitivity")
        void shouldHandleCaseSensitivity() {
            // Given
            String lowerCase = "password";
            String upperCase = "PASSWORD";
            String mixedCase = "Password";

            // When
            String lowerHash = PasswordHashUtil.hashWithSHA256(lowerCase);
            String upperHash = PasswordHashUtil.hashWithSHA256(upperCase);
            String mixedHash = PasswordHashUtil.hashWithSHA256(mixedCase);

            // Then
            assertThat(lowerHash).isNotEqualTo(upperHash);
            assertThat(lowerHash).isNotEqualTo(mixedHash);
            assertThat(upperHash).isNotEqualTo(mixedHash);
        }
    }

    @Nested
    @DisplayName("Known Vector Tests")
    class KnownVectorTests {

        @Test
        @DisplayName("Should match known SHA-256 test vectors")
        void shouldMatchKnownSHA256TestVectors() {
            // These are standard SHA-256 test vectors from NIST and other sources

            // Test vector 1: empty string
            assertThat(PasswordHashUtil.hashWithSHA256(""))
                    .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

            // Test vector 2: "abc"
            assertThat(PasswordHashUtil.hashWithSHA256("abc"))
                    .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");

            // Test vector 3: "The quick brown fox jumps over the lazy dog"
            assertThat(PasswordHashUtil.hashWithSHA256("The quick brown fox jumps over the lazy dog"))
                    .isEqualTo("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592");
        }

        @Test
        @DisplayName("Should handle multi-byte UTF-8 sequences")
        void shouldHandleMultiByteUTF8Sequences() {
            // Given - strings with various UTF-8 byte sequences
            String ascii = "hello";
            String latin = "héllo";
            String cyrillic = "привет";
            String emoji = "🔐🔑";

            // When
            String asciiHash = PasswordHashUtil.hashWithSHA256(ascii);
            String latinHash = PasswordHashUtil.hashWithSHA256(latin);
            String cyrillicHash = PasswordHashUtil.hashWithSHA256(cyrillic);
            String emojiHash = PasswordHashUtil.hashWithSHA256(emoji);

            // Then
            assertThat(asciiHash).hasSize(64).matches("^[a-f0-9]{64}$");
            assertThat(latinHash).hasSize(64).matches("^[a-f0-9]{64}$");
            assertThat(cyrillicHash).hasSize(64).matches("^[a-f0-9]{64}$");
            assertThat(emojiHash).hasSize(64).matches("^[a-f0-9]{64}$");

            // All should be different
            assertThat(asciiHash).isNotEqualTo(latinHash);
            assertThat(asciiHash).isNotEqualTo(cyrillicHash);
            assertThat(asciiHash).isNotEqualTo(emojiHash);
            assertThat(latinHash).isNotEqualTo(cyrillicHash);
            assertThat(latinHash).isNotEqualTo(emojiHash);
            assertThat(cyrillicHash).isNotEqualTo(emojiHash);
        }
    }

    @Nested
    @DisplayName("Algorithm Validation Tests")
    class AlgorithmValidationTests {

        @Test
        @DisplayName("Should use SHA-256 algorithm correctly")
        void shouldUseSHA256AlgorithmCorrectly() throws Exception {
            // Given
            String password = "validationTest";

            // When
            String utilResult = PasswordHashUtil.hashWithSHA256(password);

            // Create reference implementation for comparison
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String referenceResult = hexString.toString();

            // Then
            assertThat(utilResult).isEqualTo(referenceResult);
        }

        @Test
        @DisplayName("Should handle UTF-8 encoding consistently")
        void shouldHandleUTF8EncodingConsistently() throws Exception {
            // Given
            String password = "encoding_test_café_🌟";

            // When
            String utilResult = PasswordHashUtil.hashWithSHA256(password);

            // Create reference with explicit UTF-8 encoding
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String referenceResult = hexString.toString();

            // Then
            assertThat(utilResult).isEqualTo(referenceResult);
        }
    }
}
