package com.android.tv.samples.sampletunertvinput;

import android.content.Context;
import android.media.tv.tuner.dvr.DvrPlayback;
import android.media.tv.tuner.dvr.DvrSettings;
import android.media.tv.tuner.filter.AvSettings;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.media.tv.tuner.frontend.AtscFrontendSettings;
import android.media.tv.tuner.frontend.DvbtFrontendSettings;
import android.media.tv.tuner.frontend.FrontendSettings;
import android.media.tv.tuner.frontend.OnTuneEventListener;
import android.media.tv.tuner.Tuner;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.FileNotFoundException;


/** SampleTunerTvInputService */
public class SampleTunerTvInputService extends TvInputService {
    private static final String TAG = "SampleTunerTvInput";
    private static final boolean DEBUG = true;

    private static final int AUDIO_TPID = 257;
    private static final int VIDEO_TPID = 256;
    private static final int STATUS_MASK = 0xf;
    private static final int LOW_THRESHOLD = 0x1000;
    private static final int HIGH_THRESHOLD = 0x07fff;
    private static final int FREQUENCY = 578000;
    private static final int PACKET_SIZE = 188;
    private static final int FILTER_BUFFER_SIZE = 16000000;
    private static final int DVR_BUFFER_SIZE = 4000000;
    private static final int INPUT_FILE_MAX_SIZE = 700000;

    public static final String INPUT_ID =
            "com.android.tv.samples.sampletunertvinput/.SampleTunerTvInputService";
    private String mSessionId;

    @Override
    public TvInputSessionImpl onCreateSession(String inputId, String sessionId) {
        TvInputSessionImpl session =  new TvInputSessionImpl(this);
        if (DEBUG) {
            Log.d(TAG, "onCreateSession(inputId=" + inputId + ", sessionId=" + sessionId + ")");
        }
        mSessionId = sessionId;
        return session;
    }

    @Override
    public TvInputSessionImpl onCreateSession(String inputId) {
        return new TvInputSessionImpl(this);
    }

    class TvInputSessionImpl extends Session {

        private Surface surface;
        private final Context mContext;
        private Handler mHandler;

        private Filter audioFilter;
        private Filter videoFilter;
        private DvrPlayback dvr;
        Tuner tuner;


        public TvInputSessionImpl(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public void onRelease() {
            if (DEBUG) {
                Log.d(TAG, "onRelease");
            }
        }

        @Override
        public boolean onSetSurface(Surface surface) {
            if (DEBUG) {
                Log.d(TAG, "onSetSurface");
            }
            this.surface = surface;
            return true;
        }

        @Override
        public void onSetStreamVolume(float v) {
            if (DEBUG) {
                Log.d(TAG, "onSetStreamVolume " + v);
            }
        }

        @Override
        public boolean onTune(Uri uri) {
            if (DEBUG) {
                Log.d(TAG, "onTune " + uri);
            }
            tuner = new Tuner(mContext, mSessionId,
                    TvInputService.PRIORITY_HINT_USE_CASE_TYPE_LIVE);

            mHandler = new Handler();
            audioFilter = audioFilter();
            videoFilter = videoFilter();
            audioFilter.start();
            videoFilter.start();

            int feCount = tuner.getFrontendIds().size();
            if (feCount <= 0) return false;

            tune();
            // use dvr playback to feed the data on platform without physical tuner
            dvr = dvrPlayback();
            dvr.start();

            return true;
        }

        @Override
        public void onSetCaptionEnabled(boolean b) {
            if (DEBUG) {
                Log.d(TAG, "onSetCaptionEnabled " + b);
            }
        }

        private Filter audioFilter() {
            Filter audioFilter = tuner.openFilter(Filter.TYPE_TS, Filter.SUBTYPE_AUDIO,
                    FILTER_BUFFER_SIZE, new HandlerExecutor(mHandler),
                    new FilterCallback() {
                        @Override
                        public void onFilterEvent(Filter filter, FilterEvent[] events) {
                            if (DEBUG) {
                                Log.d(TAG, "onFilterEvent audio, size=" + events.length);
                                for (int i = 0; i < events.length; i++) {
                                    Log.d(TAG, "events[" + i + "] is "
                                        + events[i].getClass().getSimpleName());
                                }
                            }
                        }

                        @Override
                        public void onFilterStatusChanged(Filter filter, int status) {
                            if (DEBUG) {
                                Log.d(TAG, "onFilterEvent audio, status=" + status);
                            }
                        }
                    });
            AvSettings settings =
                    AvSettings.builder(Filter.TYPE_TS, true).setPassthrough(false).build();
            audioFilter.configure(
                    TsFilterConfiguration.builder().setTpid(AUDIO_TPID)
                            .setSettings(settings).build());
            return audioFilter;
        }

        private Filter videoFilter() {
            Filter videoFilter = tuner.openFilter(Filter.TYPE_TS, Filter.SUBTYPE_VIDEO,
                    FILTER_BUFFER_SIZE, new HandlerExecutor(mHandler),
                    new FilterCallback() {
                        @Override
                        public void onFilterEvent(Filter filter, FilterEvent[] events) {
                            if (DEBUG) {
                                Log.d(TAG, "onFilterEvent video, size=" + events.length);
                                for (int i = 0; i < events.length; i++) {
                                    Log.d(TAG, "events[" + i + "] is "
                                        + events[i].getClass().getSimpleName());
                                }
                            }
                        }

                        @Override
                        public void onFilterStatusChanged(Filter filter, int status) {
                            if (DEBUG) {
                                Log.d(TAG, "onFilterEvent video, status=" + status);
                            }
                        }
                    });
            AvSettings settings =
                    AvSettings.builder(Filter.TYPE_TS, false).setPassthrough(false).build();
            videoFilter.configure(
                    TsFilterConfiguration.builder().setTpid(VIDEO_TPID)
                            .setSettings(settings).build());
            return videoFilter;
        }

        private DvrPlayback dvrPlayback() {
            DvrPlayback dvr = tuner.openDvrPlayback(DVR_BUFFER_SIZE, new HandlerExecutor(mHandler),
                    status -> {
                        if (DEBUG) {
                            Log.d(TAG, "onPlaybackStatusChanged status=" + status);
                        }
                    });
            int res = dvr.configure(
                    DvrSettings.builder()
                            .setStatusMask(STATUS_MASK)
                            .setLowThreshold(LOW_THRESHOLD)
                            .setHighThreshold(HIGH_THRESHOLD)
                            .setDataFormat(DvrSettings.DATA_FORMAT_ES)
                            .setPacketSize(PACKET_SIZE)
                            .build());
            if (DEBUG) {
                Log.d(TAG, "config res=" + res);
            }
            File file = new File("/data/local/tmp/test.es");
            if (file.exists()) {
                try {
                    dvr.setFileDescriptor(
                            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE));
                } catch (FileNotFoundException e) {
                        Log.e(TAG, "Failed to create FD");
                }
            } else {
                Log.w(TAG, "File not existing");
            }
            return dvr;
        }

        private void tune() {
            DvbtFrontendSettings feSettings = DvbtFrontendSettings.builder()
                    .setFrequency(FREQUENCY)
                    .setTransmissionMode(DvbtFrontendSettings.TRANSMISSION_MODE_AUTO)
                    .setBandwidth(DvbtFrontendSettings.BANDWIDTH_8MHZ)
                    .setConstellation(DvbtFrontendSettings.CONSTELLATION_AUTO)
                    .setHierarchy(DvbtFrontendSettings.HIERARCHY_AUTO)
                    .setHighPriorityCodeRate(DvbtFrontendSettings.CODERATE_AUTO)
                    .setLowPriorityCodeRate(DvbtFrontendSettings.CODERATE_AUTO)
                    .setGuardInterval(DvbtFrontendSettings.GUARD_INTERVAL_AUTO)
                    .setHighPriority(true)
                    .setStandard(DvbtFrontendSettings.STANDARD_T)
                    .build();
            tuner.setOnTuneEventListener(new HandlerExecutor(mHandler), new OnTuneEventListener() {
                @Override
                public void onTuneEvent(int tuneEvent) {
                    if (DEBUG) {
                        Log.d(TAG, "onTuneEvent " + tuneEvent);
                    }
                    long read = dvr.read(INPUT_FILE_MAX_SIZE);
                    if (DEBUG) {
                        Log.d(TAG, "read=" + read);
                    }
                }
            });
            tuner.tune(feSettings);
        }
    }
}