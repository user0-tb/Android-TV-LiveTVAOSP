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

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.tv.common.BuildConfig;

import com.google.common.collect.ImmutableSet;

import com.android.tv.common.flags.CloudEpgFlags;
import com.android.tv.common.flags.LegacyFlags;

import java.util.List;

import javax.inject.Inject;

/** Checks if a package or a input is allowed. */
public final class EpgInputAllowList {
    private static final boolean DEBUG = false;
    private static final String TAG = "EpgInputAllowList";
    private static final ImmutableSet<String> QA_DEV_INPUTS =
            ImmutableSet.of(
                    "com.example.partnersupportsampletvinput/.SampleTvInputService",
                    "com.android.tv.tuner.sample.dvb/.tvinput.SampleDvbTunerTvInputService");
    private final LegacyFlags mLegacyFlags;

    /** Returns the package portion of a inputId */
    @Nullable
    public static String getPackageFromInput(@Nullable String inputId) {
        return inputId == null ? null : inputId.substring(0, inputId.indexOf("/"));
    }

    private final CloudEpgFlags mCloudEpgFlags;

    @Inject
    public EpgInputAllowList(CloudEpgFlags cloudEpgFlags, LegacyFlags legacyFlags) {
        mCloudEpgFlags = cloudEpgFlags;
        mLegacyFlags = legacyFlags;
    }

    public boolean isInputAllowed(String inputId) {
        return getAllowedInputs().contains(inputId);
    }

    public boolean isPackageAllowed(String packageName) {
        if (DEBUG) Log.d(TAG, "isPackageAllowed " + packageName);
        ImmutableSet<String> allowedInputs = getAllowedInputs();
        for (String allowed : allowedInputs) {
            try {
                String allowedPackage = getPackageFromInput(allowed);
                if (allowedPackage.equals(packageName)) {
                    return true;
                }
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Error parsing package name of " + allowed, e);
                continue;
            }
        }
        return false;
    }

    private ImmutableSet<String> getAllowedInputs() {
        ImmutableSet<String> result =
                toInputSet(mCloudEpgFlags.thirdPartyEpgInputs().getElementList());
        if (BuildConfig.ENG || mLegacyFlags.enableQaFeatures()) {
            if (result.isEmpty()) {
                result = QA_DEV_INPUTS;
            } else {
                result =
                        ImmutableSet.<String>builder().addAll(result).addAll(QA_DEV_INPUTS).build();
            }
        }
        if (DEBUG) Log.d(TAG, "getAllowedInputs " + result);
        return result;
    }

    private static ImmutableSet<String> toInputSet(List<String> strings) {
        if (strings.isEmpty()) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<String> result = ImmutableSet.builder();
        for (String s : strings) {
            String trimmed = s.trim();
            if (!TextUtils.isEmpty(trimmed)) {
                result.add(trimmed);
            }
        }
        return result.build();
    }
}
