package com.android.tv.tuner.tvinput.factory;

import android.content.Context;
import android.media.tv.TvInputService.Session;

import com.android.tv.tuner.source.TsDataSourceManager;
import com.android.tv.tuner.tvinput.TunerSession;
import com.android.tv.tuner.tvinput.TunerSessionExoV2;
import com.android.tv.tuner.tvinput.datamanager.ChannelDataManager;

import com.android.tv.common.flags.ConcurrentDvrPlaybackFlags;
import com.android.tv.common.flags.LegacyFlags;
import com.android.tv.common.flags.TunerFlags;

import javax.inject.Inject;

/** Creates a {@link TunerSessionFactory}. */
public class TunerSessionFactoryImpl implements TunerSessionFactory {

    private final TunerFlags mTunerFlags;
    private final ConcurrentDvrPlaybackFlags mConcurrentDvrPlaybackFlags;
    private final TsDataSourceManager.Factory mTsDataSourceManagerFactory;
    private final LegacyFlags mLegacyFlags;

    @Inject
    public TunerSessionFactoryImpl(
            TunerFlags tunerFlags,
            ConcurrentDvrPlaybackFlags concurrentDvrPlaybackFlags,
            TsDataSourceManager.Factory tsDataSourceManagerFactory,
            LegacyFlags legacyFlags) {
        mTunerFlags = tunerFlags;
        mConcurrentDvrPlaybackFlags = concurrentDvrPlaybackFlags;
        mTsDataSourceManagerFactory = tsDataSourceManagerFactory;
        mLegacyFlags = legacyFlags;
    }

    @Override
    public Session create(
            Context context,
            ChannelDataManager channelDataManager,
            SessionReleasedCallback releasedCallback,
            SessionRecordingCallback recordingCallback) {
        return mTunerFlags.useExoplayerV2()
                ? new TunerSessionExoV2(
                        context,
                        channelDataManager,
                        releasedCallback,
                        recordingCallback,
                        mConcurrentDvrPlaybackFlags,
                        mLegacyFlags,
                        mTsDataSourceManagerFactory)
                : new TunerSession(
                        context,
                        channelDataManager,
                        releasedCallback,
                        recordingCallback,
                        mConcurrentDvrPlaybackFlags,
                        mLegacyFlags,
                        mTsDataSourceManagerFactory);
    }
}
