/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.tv.data.epg;

import static com.google.common.truth.Truth.assertThat;

import com.android.tv.common.flags.impl.DefaultCloudEpgFlags;
import com.android.tv.common.flags.impl.DefaultLegacyFlags;
import com.android.tv.features.TvFeatures;
import com.android.tv.testing.constants.ConfigConstants;
import com.android.tv.common.flags.proto.TypedFeatures.StringListParam;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

/** Tests for {@link EpgInputAllowList}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = ConfigConstants.SDK)
public class EpgInputAllowListTest {

    private EpgInputAllowList mAllowList;
    private DefaultCloudEpgFlags mCloudEpgFlags;
    private DefaultLegacyFlags mLegacyFlags;

    @Before
    public void setup() {
        TvFeatures.CLOUD_EPG_FOR_3RD_PARTY.enableForTest();
        mCloudEpgFlags = new DefaultCloudEpgFlags();
        mLegacyFlags = DefaultLegacyFlags.DEFAULT;
        mAllowList = new EpgInputAllowList(mCloudEpgFlags, mLegacyFlags);
    }

    @After
    public void after() {
        TvFeatures.CLOUD_EPG_FOR_3RD_PARTY.resetForTests();
    }

    @Test
    public void isInputAllowed_noRemoteConfig() {
        assertThat(mAllowList.isInputAllowed("com.example/.Foo")).isFalse();
    }

    @Test
    public void isInputAllowed_noMatch() {
        mCloudEpgFlags.setThirdPartyEpgInput(asStringListParam("com.example/.Bar"));
        assertThat(mAllowList.isInputAllowed("com.example/.Foo")).isFalse();
    }

    @Test
    public void isInputAllowed_match() {
        mCloudEpgFlags.setThirdPartyEpgInput(asStringListParam("com.example/.Foo"));
        assertThat(mAllowList.isInputAllowed("com.example/.Foo")).isTrue();
    }

    @Test
    public void isInputAllowed_matchWithTwo() {
        mCloudEpgFlags.setThirdPartyEpgInput(
                asStringListParam("com.example/.Foo", "com.example/.Bar"));
        assertThat(mAllowList.isInputAllowed("com.example/.Foo")).isTrue();
    }

    @Test
    public void isPackageAllowListed_noRemoteConfig() {
        assertThat(mAllowList.isPackageAllowed("com.example")).isFalse();
    }

    @Test
    public void isPackageAllowed_noMatch() {
        mCloudEpgFlags.setThirdPartyEpgInput(asStringListParam("com.example/.Bar"));
        assertThat(mAllowList.isPackageAllowed("com.other")).isFalse();
    }

    @Test
    public void isPackageAllowed_match() {
        mCloudEpgFlags.setThirdPartyEpgInput(asStringListParam("com.example/.Foo"));
        assertThat(mAllowList.isPackageAllowed("com.example")).isTrue();
    }

    @Test
    public void isPackageAllowed_matchWithTwo() {
        mCloudEpgFlags.setThirdPartyEpgInput(
                asStringListParam("com.example/.Foo", "com.example/.Bar"));
        assertThat(mAllowList.isPackageAllowed("com.example")).isTrue();
    }

    @Test
    public void isPackageAllowed_matchBadInput() {
        mCloudEpgFlags.setThirdPartyEpgInput(asStringListParam("com.example.Foo"));
        assertThat(mAllowList.isPackageAllowed("com.example")).isFalse();
    }

    @Test
    public void isPackageAllowed_tunerInput() {
        EpgInputAllowList allowList =
                new EpgInputAllowList(new DefaultCloudEpgFlags(), DefaultLegacyFlags.DEFAULT);
        assertThat(
                        allowList.isInputAllowed(
                                "com.google.android.tv/.tuner.tvinput.TunerTvInputService"))
                .isTrue();
    }

    private static StringListParam asStringListParam(String... args) {
        List<String> list = Arrays.asList(args);
        return StringListParam.newBuilder().addAllElement(list).build();
    }
}
