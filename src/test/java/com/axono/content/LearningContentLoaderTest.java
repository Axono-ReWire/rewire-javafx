package com.axono.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LearningContentLoaderTest {

    /** Titles of the three test XML files under src/test/resources. */
    private static final Set<String> EXPECTED_TEST_TITLES = Set.of(
            "Alpha Resource", "Alpha Quiz", "Beta Resource");

    @Test
    void loadAllReturnsNonNullList() {
        assertNotNull(LearningContentLoader.loadAll());
    }

    @Test
    void loadAllFindsThreeTestResources() {
        List<LearningContent> all = LearningContentLoader.loadAll();
        assertFalse(all.isEmpty(),
                "Expected test XML resources to be on the classpath");
        Set<String> titles = all.stream()
                .map(LearningContent::getTitle)
                .collect(Collectors.toSet());
        for (String expected : EXPECTED_TEST_TITLES) {
            assertTrue(titles.contains(expected),
                    "Expected test resource not found: " + expected);
        }
    }

    @Test
    void loadAllExcludesCurriculumXml() {
        for (LearningContent lc : LearningContentLoader.loadAll()) {
            assertFalse(
                    lc.getId().toLowerCase().contains("curriculum"),
                    "curriculum.xml must be excluded but got: "
                            + lc.getId());
        }
    }

    @Test
    void loadAllAllItemsHaveNonNullTitle() {
        for (LearningContent lc : LearningContentLoader.loadAll()) {
            assertNotNull(lc.getTitle(),
                    "Title must not be null for: " + lc.getId());
            assertFalse(lc.getTitle().isEmpty(),
                    "Title must not be empty for: " + lc.getId());
        }
    }

    @Test
    void loadAllResultIsSortedByModuleThenTitle() {
        List<LearningContent> all = LearningContentLoader.loadAll();
        for (int i = 1; i < all.size(); i++) {
            LearningContent prev = all.get(i - 1);
            LearningContent curr = all.get(i);
            int moduleCmp = prev.getModule()
                    .compareToIgnoreCase(curr.getModule());
            assertTrue(moduleCmp <= 0,
                    "List not sorted by module at index " + i
                    + ": " + prev.getModule() + " > "
                    + curr.getModule());
            if (moduleCmp == 0) {
                int topicCmp = prev.getTopic()
                        .compareToIgnoreCase(curr.getTopic());
                assertTrue(topicCmp <= 0,
                        "List not sorted by topic at index " + i
                        + ": " + prev.getTopic() + " > "
                        + curr.getTopic());
            }
        }
    }
}
