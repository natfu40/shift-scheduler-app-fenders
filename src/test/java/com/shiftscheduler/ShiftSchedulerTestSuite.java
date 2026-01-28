package com.shiftscheduler;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test Suite that runs all tests in the application.
 * This provides an organized way to run different categories of tests.
 */
@Suite
@SuiteDisplayName("Shift Scheduler Backend Test Suite")
@SelectPackages({
    "com.shiftscheduler.service",
    "com.shiftscheduler.repository",
    "com.shiftscheduler.controller",
    "com.shiftscheduler.security",
    "com.shiftscheduler.integration"
})
@IncludeClassNamePatterns(".*Test")
public class ShiftSchedulerTestSuite {
    // This class serves as a test suite definition
    // Actual test execution is handled by JUnit 5
}

/**
 * Unit Test Suite - Service and Security Layer Tests
 */
@Suite
@SuiteDisplayName("Unit Tests")
@SelectPackages({
    "com.shiftscheduler.service",
    "com.shiftscheduler.security"
})
@IncludeClassNamePatterns(".*Test")
class UnitTestSuite {
}

/**
 * Integration Test Suite - Repository and Controller Tests
 */
@Suite
@SuiteDisplayName("Integration Tests")
@SelectPackages({
    "com.shiftscheduler.repository",
    "com.shiftscheduler.controller",
    "com.shiftscheduler.integration"
})
@IncludeClassNamePatterns(".*Test")
class IntegrationTestSuite {
}
