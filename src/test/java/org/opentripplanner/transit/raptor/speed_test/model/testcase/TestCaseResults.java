package org.opentripplanner.transit.raptor.speed_test.model.testcase;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.util.DiffTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Contains the expected, actual and matched test results. The responsibility is
 * match all expected with actual results and produce a list of results:
 * <ul>
 *     <li> Matched actual results, status : OK
 *     <li> Expected results NOT found in actual results, status: FAILED
 *     <li> Actual results NOT found in expected, status: WARN
 * </ul>
 */
class TestCaseResults {
    private final String testCaseId;
    private final boolean skipCost;
    private final List<Result> expected;
    private final List<Result> actual = new ArrayList<>();
    private final List<DiffTool.Entry<Result>> matchedResults = new ArrayList<>();
    private TestStatus status = TestStatus.NA;
    private int transitTimeMs = 0;
    private int totalTimeMs = 0;

    TestCaseResults(String testCaseId, boolean skipCost, Collection<Result> expected) {
        this.testCaseId = testCaseId;
        this.skipCost  = skipCost;
        this.expected = List.copyOf(expected);
    }

    void addTimes(int transitTimeMs, int totalTimeMs) {
        this.transitTimeMs = transitTimeMs;
        this.totalTimeMs = totalTimeMs;
    }

    public int transitTimeMs() {
        return transitTimeMs;
    }

    public int totalTimeMs() {
        return totalTimeMs;
    }

    void matchItineraries(Collection<Itinerary> itineraries) {
        actual.addAll(ItineraryResultMapper.map(testCaseId, itineraries, skipCost));
        matchedResults.clear();
        matchedResults.addAll(DiffTool.diff(expected, actual, Result.comparator(skipCost)));
        status = resolveStatus();
    }

    List<Result> actualResults() {
        return actual;
    }

    /**
     * All test results are OK.
     */
    public boolean success() {
        return status.ok();
    }

    /**
     * At least one expected result is missing.
     */
    public boolean failed() {
        return status.failed();
    }

    /**
     * No test results is found. This indicates that the test is not run or
     * that the route had no itineraries.
     */
    public boolean noResults() {
        return matchedResults.isEmpty();
    }

    private TestStatus resolveStatus() {
        if(matchedResults.isEmpty()) { return TestStatus.NA; }
        if(matchedResults.stream().anyMatch(DiffTool.Entry::leftOnly)) { return TestStatus.FAILED; }
        if(matchedResults.stream().anyMatch(DiffTool.Entry::rightOnly)) { return TestStatus.WARN; }
        return TestStatus.OK;
    }

    @Override
    public String toString() {
        return TableTestReport.report(matchedResults);
    }

}
