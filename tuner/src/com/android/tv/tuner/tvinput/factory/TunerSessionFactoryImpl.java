package com.android.tv.tuner.tvinput.factory;

import android.content.Context;
import android.media.tv.TvInputService.Session;

import com.android.tv.tuner.tvinput.TunerSessionExoV2Factory;
import com.android.tv.tuner.tvinput.TunerSessionV1Factory;
import com.android.tv.tuner.tvinput.datamanager.ChannelDataManager;

import com.android.tv.common.flags.TunerFlags;

import javax.inject.Inject;

/** Creates a {@link TunerSessionFactory}. */
public class TunerSessionFactoryImpl implements TunerSessionFactory {

    private final TunerFlags mTunerFlags;
    private final TunerSessionV1Factory mTunerSessionFactory;
    private final TunerSessionExoV2Factory mTunerSessionExoV2Factory;

    @Inject
    public TunerSessionFactoryImpl(
            TunerFlags tunerFlags,
            TunerSessionV1Factory tunerSessionFactory,
            TunerSessionExoV2Factory tunerSessionExoV2Factory) {

        mTunerFlags = tunerFlags;
        mTunerSessionFactory = tunerSessionFactory;
        mTunerSessionExoV2Factory = tunerSessionExoV2Factory;
    }

    @Override
    public Session create(
            Context context,
            ChannelDataManager channelDataManager,
            SessionReleasedCallback releasedCallback,
            SessionRecordingCallback recordingCallback) {
        return mTunerFlags.useExoplayerV2()
                ? mTunerSessionExoV2Factory.create(
                        channelDataManager, releasedCallback, recordingCallback)
                : mTunerSessionFactory.create(
                        channelDataManager, releasedCallback, recordingCallback);
    }
}
