package com.android.tv.tuner.tvinput.factory;

import android.content.Context;
import android.media.tv.TvInputService.RecordingSession;
import android.media.tv.TvInputService.Session;
import android.net.Uri;

import com.android.tv.tuner.tvinput.datamanager.ChannelDataManager;

/** {@link android.media.tv.TvInputService.Session} factory */
public interface TunerSessionFactory {

    /** Called when a session is released */
    interface SessionReleasedCallback {

        /**
         * Called when the given session is released.
         *
         * @param session The session that has been released.
         */
        void onReleased(Session session);
    }

    /** Called when a recording session is released */
    interface RecordingSessionReleasedCallback {

        /**
         * Called when the given recording session is released.
         *
         * @param session The recording session that has been released.
         */
        void onReleased(RecordingSession session);
    }

    /** Called when recording URI is required for playback */
    interface SessionRecordingCallback {

        /**
         * Called when recording URI is required for playback.
         *
         * @param channelUri for which recording URI is requested.
         */
        Uri getRecordingUri(Uri channelUri);
    }

    Session create(
            Context context,
            ChannelDataManager channelDataManager,
            SessionReleasedCallback releasedCallback,
            SessionRecordingCallback recordingCallback);
}
