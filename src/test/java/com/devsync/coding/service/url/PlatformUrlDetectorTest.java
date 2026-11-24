package com.devsync.coding.service.url;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlatformUrlDetectorTest — Automatic URL Detection & Platform Handle Extraction")
class PlatformUrlDetectorTest {

    // =========================================================================
    // LeetCode Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://leetcode.com/u/tourist/",
            "https://leetcode.com/u/tourist",
            "http://leetcode.com/u/tourist",
            "https://www.leetcode.com/u/tourist/",
            "https://leetcode.com/tourist/",
            "https://leetcode.com/tourist",
            "leetcode.com/u/tourist",
            "https://leetcode.com/u/tourist?tab=solved#recent"
    })
    @DisplayName("LeetCode URL detection — Accurately extracts handle and produces canonical URL")
    void detect_LeetCode_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.LEETCODE, result.platform());
        assertEquals("tourist", result.username());
        assertEquals("https://leetcode.com/u/tourist/", result.canonicalProfileUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://leetcode.com/problems/two-sum",
            "https://leetcode.com/contest/weekly-contest-300",
            "https://leetcode.com/discuss",
            "https://leetcode.com/explore",
            "https://leetcode.com/tag/array"
    })
    @DisplayName("LeetCode reserved path rejection — Rejects non-profile pages")
    void detect_LeetCode_ReservedPaths_ThrowsBadRequest(String url) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(url));
        assertTrue(ex.getMessage().contains("reserved platform path"));
    }

    // =========================================================================
    // Codeforces Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://codeforces.com/profile/tourist",
            "https://codeforces.com/profile/tourist/",
            "http://codeforces.com/profile/tourist",
            "https://www.codeforces.com/profile/tourist",
            "codeforces.com/profile/tourist",
            "https://codeforces.com/profile/tourist?ref=badge"
    })
    @DisplayName("Codeforces URL detection — Accurately extracts handle and produces canonical URL")
    void detect_Codeforces_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.CODEFORCES, result.platform());
        assertEquals("tourist", result.username());
        assertEquals("https://codeforces.com/profile/tourist", result.canonicalProfileUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://codeforces.com/contest/1234",
            "https://codeforces.com/problemset",
            "https://codeforces.com/tourist"
    })
    @DisplayName("Codeforces invalid structure rejection — Enforces /profile/{username}")
    void detect_Codeforces_InvalidStructure_ThrowsBadRequest(String url) {
        assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(url));
    }

    // =========================================================================
    // GitHub Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/torvalds",
            "https://github.com/torvalds/",
            "http://github.com/torvalds",
            "https://www.github.com/torvalds",
            "github.com/torvalds",
            "https://github.com/torvalds?tab=repositories"
    })
    @DisplayName("GitHub URL detection — Accurately extracts handle and produces canonical URL")
    void detect_GitHub_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.GITHUB, result.platform());
        assertEquals("torvalds", result.username());
        assertEquals("https://github.com/torvalds", result.canonicalProfileUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/torvalds/linux",
            "https://github.com/features",
            "https://github.com/pricing",
            "https://github.com/security"
    })
    @DisplayName("GitHub repository & reserved path rejection — Blocks non-profile URLs")
    void detect_GitHub_NonProfile_ThrowsBadRequest(String url) {
        assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(url));
    }

    // =========================================================================
    // AtCoder Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://atcoder.jp/users/chokudai",
            "https://atcoder.jp/users/chokudai/",
            "http://atcoder.jp/users/chokudai",
            "https://www.atcoder.jp/users/chokudai",
            "atcoder.jp/users/chokudai"
    })
    @DisplayName("AtCoder URL detection — Accurately extracts handle and produces canonical URL")
    void detect_AtCoder_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.ATCODER, result.platform());
        assertEquals("chokudai", result.username());
        assertEquals("https://atcoder.jp/users/chokudai", result.canonicalProfileUrl());
    }

    // =========================================================================
    // CodeChef Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.codechef.com/users/gennady",
            "https://codechef.com/users/gennady/",
            "http://codechef.com/users/gennady",
            "codechef.com/users/gennady"
    })
    @DisplayName("CodeChef URL detection — Accurately extracts handle and produces canonical URL")
    void detect_CodeChef_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.CODECHEF, result.platform());
        assertEquals("gennady", result.username());
        assertEquals("https://www.codechef.com/users/gennady", result.canonicalProfileUrl());
    }

    // =========================================================================
    // HackerRank Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.hackerrank.com/profile/alicedev",
            "https://hackerrank.com/profile/alicedev/",
            "http://hackerrank.com/profile/alicedev",
            "https://www.hackerrank.com/alicedev",
            "hackerrank.com/profile/alicedev"
    })
    @DisplayName("HackerRank URL detection — Accurately extracts handle and produces canonical URL")
    void detect_HackerRank_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.HACKERRANK, result.platform());
        assertEquals("alicedev", result.username());
        assertEquals("https://www.hackerrank.com/profile/alicedev", result.canonicalProfileUrl());
    }

    // =========================================================================
    // GeeksforGeeks Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.geeksforgeeks.org/user/tourist_dev/",
            "https://www.geeksforgeeks.org/user/tourist_dev",
            "https://geeksforgeeks.org/user/tourist_dev/",
            "https://www.geeksforgeeks.org/profile/tourist_dev/",
            "https://www.geeksforgeeks.org/user/tourist_dev/profile",
            "https://auth.geeksforgeeks.org/user/tourist_dev/",
            "geeksforgeeks.org/user/tourist_dev"
    })
    @DisplayName("GeeksforGeeks URL detection — Accurately extracts handle and produces canonical URL")
    void detect_GeeksforGeeks_Success(String url) {
        PlatformUrlDetector.DetectedProfileUrl result = PlatformUrlDetector.detect(url);
        assertNotNull(result);
        assertEquals(CodingPlatform.GEEKSFORGEEKS, result.platform());
        assertEquals("tourist_dev", result.username());
        assertEquals("https://www.geeksforgeeks.org/user/tourist_dev/", result.canonicalProfileUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.geeksforgeeks.org/courses",
            "https://www.geeksforgeeks.org/explore",
            "https://www.geeksforgeeks.org/practice",
            "https://www.geeksforgeeks.org/tutorials"
    })
    @DisplayName("GeeksforGeeks reserved path rejection — Rejects non-profile pages")
    void detect_GeeksforGeeks_ReservedPaths_ThrowsBadRequest(String url) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(url));
        assertTrue(ex.getMessage().contains("reserved platform path"));
    }

    // =========================================================================
    // Security & Error Rejection Tests
    // =========================================================================

    @Test
    @DisplayName("Empty or null URL — Rejects with BadRequestException")
    void detect_EmptyOrNull_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(null));
        assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(""));
        assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://evil.com/tourist",
            "https://google.com/search?q=leetcode",
            "https://leetcode.com.attacker.com/u/tourist",
            "http://169.254.169.254/latest/meta-data",
            "http://localhost:8080/u/tourist",
            "ftp://leetcode.com/u/tourist"
    })
    @DisplayName("Unsupported domain & SSRF defense — Strictly enforces domain allowlist")
    void detect_SSRFAndUnsupportedDomains_ThrowsBadRequest(String url) {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(url));
        assertTrue(ex.getMessage().contains("Unsupported platform domain"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://leetcode.com/u/bad user name!/",
            "https://codeforces.com/profile/a", // too short (<3)
            "https://github.com/-leading-hyphen",
            "https://atcoder.jp/users/verylongusernameexceeding16chars"
    })
    @DisplayName("Malformed handle rejection — Enforces platform username regex")
    void detect_MalformedUsername_ThrowsBadRequest(String url) {
        assertThrows(BadRequestException.class, () -> PlatformUrlDetector.detect(url));
    }
}
