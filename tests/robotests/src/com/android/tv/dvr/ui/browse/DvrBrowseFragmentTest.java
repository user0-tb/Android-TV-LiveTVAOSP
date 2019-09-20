/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.dvr.ui.browse;

import com.android.tv.dvr.data.RecordedProgram;
import com.android.tv.dvr.data.ScheduledRecording;
import com.android.tv.testing.ComparatorTester;
import com.android.tv.testing.constants.ConfigConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Test for {@link DvrBrowseFragment}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = ConfigConstants.SDK)
public class DvrBrowseFragmentTest {

    @Test
    public void testRecentRowComparator_scheduledRecordings_latestFirst() {
        ComparatorTester<Object> comparatorTester =
                ComparatorTester.withoutEqualsTest(DvrBrowseFragment.RECENT_ROW_COMPARATOR);
        // priority (highest to lowest): start time, class, ID
        comparatorTester.addComparableGroup(buildRecordedProgramForTest(2, 120, 150));
        comparatorTester.addComparableGroup(buildRecordedProgramForTest(1, 120, 150));
        comparatorTester.addComparableGroup(buildScheduledRecordingForTest(1, 120, 150));
        comparatorTester.addComparableGroup(buildScheduledRecordingForTest(2, 120, 150));
        comparatorTester.addComparableGroup(buildRecordedProgramForTest(4, 100, 200));
        comparatorTester.addComparableGroup(buildRecordedProgramForTest(3, 100, 200));
        comparatorTester.addComparableGroup(buildScheduledRecordingForTest(3, 100, 200));
        comparatorTester.addComparableGroup(buildScheduledRecordingForTest(4, 100, 200));
        comparatorTester.addComparableGroup(new Object(), Long.valueOf("777"), "string");
        comparatorTester.test();
    }

    private ScheduledRecording buildScheduledRecordingForTest(long id, long start, long end) {
        return ScheduledRecording.builder("test", 1, start, end).setId(id).build();
    }

    private RecordedProgram buildRecordedProgramForTest(long id, long start, long end) {
        return RecordedProgram.builder()
                .setId(id)
                .setInputId("test")
                .setStartTimeUtcMillis(start)
                .setEndTimeUtcMillis(end)
                .build();
    }
}
