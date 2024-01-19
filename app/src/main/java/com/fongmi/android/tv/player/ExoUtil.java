package com.fongmi.android.tv.player;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.accessibility.CaptioningManager;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ExtractorsFactory;
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory;
import androidx.media3.extractor.ts.TsExtractor;
import androidx.media3.ui.CaptionStyleCompat;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Drm;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

public class ExoUtil {

    private static HttpDataSource.Factory httpDataSourceFactory;
    private static DataSource.Factory dataSourceFactory;
    private static ExtractorsFactory extractorsFactory;
    private static DatabaseProvider database;
    private static Cache cache;

    public static LoadControl buildLoadControl() {
        return new DefaultLoadControl();
    }

    public static TrackSelector buildTrackSelector() {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(App.get());
        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage(Locale.getDefault().getISO3Language()).setForceHighestSupportedBitrate(true).setTunnelingEnabled(Setting.isTunnel()));
        return trackSelector;
    }

    public static RenderersFactory buildRenderersFactory() {
        return new DefaultRenderersFactory(App.get()).setEnableDecoderFallback(true).setExtensionRendererMode(Math.abs(Setting.getDecode() - 2));
    }

    public static CaptionStyleCompat getCaptionStyle() {
        return Setting.isCaption() ? CaptionStyleCompat.createFromCaptionStyle(((CaptioningManager) App.get().getSystemService(Context.CAPTIONING_SERVICE)).getUserStyle()) : new CaptionStyleCompat(Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_OUTLINE, Color.BLACK, null);
    }

    public static int getRetry(int errorCode) {
        return errorCode >= PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED && errorCode <= PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED ? 2 : 1;
    }

    public static boolean haveTrack(Tracks tracks, int type) {
        int count = 0;
        for (Tracks.Group trackGroup : tracks.getGroups()) if (trackGroup.getType() == type) count += trackGroup.length;
        return count > 0;
    }

    public static void selectTrack(ExoPlayer player, int group, int track) {
        List<Integer> trackIndices = new ArrayList<>();
        selectTrack(player, group, track, trackIndices);
        setTrackParameters(player, group, trackIndices);
    }

    public static void deselectTrack(ExoPlayer player, int group, int track) {
        List<Integer> trackIndices = new ArrayList<>();
        deselectTrack(player, group, track, trackIndices);
        setTrackParameters(player, group, trackIndices);
    }

    public static String getMimeType(String path) {
        if (TextUtils.isEmpty(path)) return "";
        if (path.endsWith(".vtt")) return MimeTypes.TEXT_VTT;
        if (path.endsWith(".ssa") || path.endsWith(".ass")) return MimeTypes.TEXT_SSA;
        if (path.endsWith(".ttml") || path.endsWith(".xml") || path.endsWith(".dfxp")) return MimeTypes.APPLICATION_TTML;
        return MimeTypes.APPLICATION_SUBRIP;
    }

    private static String getMimeType(String format, int errorCode) {
        if (format != null) return format;
        if (errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED || errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED) return MimeTypes.APPLICATION_M3U8;
        return null;
    }

    public static MediaSource getSource(Result result, Sub sub, int errorCode) {
        return getSource(result.getHeaders(), result.getRealUrl(), result.getFormat(), result.getSubs(), sub, null, errorCode);
    }

    public static MediaSource getSource(Channel channel, int errorCode) {
        return getSource(channel.getHeaders(), channel.getUrl(), null, Collections.emptyList(), null, channel.getDrm(), errorCode);
    }

    public static MediaSource getSource(Map<String, String> headers, String url, Sub sub, int errorCode) {
        return getSource(headers, url, null, new ArrayList<>(), sub, null, errorCode);
    }

    private static MediaSource getSource(Map<String, String> headers, String url, String format, List<Sub> subs, Sub sub, Drm drm, int errorCode) {
        Uri uri = UrlUtil.uri(url);
        if (sub != null) subs.add(sub);
        String mimeType = getMimeType(format, errorCode);
        if (uri.getUserInfo() != null) headers.put(HttpHeaders.AUTHORIZATION, Util.basic(uri.getUserInfo()));
        return new DefaultMediaSourceFactory(getDataSourceFactory(headers), getExtractorsFactory()).createMediaSource(getMediaItem(uri, mimeType, subs, drm));
    }

    private static MediaItem getMediaItem(Uri uri, String mimeType, List<Sub> subs, Drm drm) {
        MediaItem.Builder builder = new MediaItem.Builder().setUri(uri);
        if (subs.size() > 0) builder.setSubtitleConfigurations(getSubtitles(subs));
        if (drm != null) builder.setDrmConfiguration(drm.get());
        if (mimeType != null) builder.setMimeType(mimeType);
        return builder.build();
    }

    private static List<MediaItem.SubtitleConfiguration> getSubtitles(List<Sub> subs) {
        List<MediaItem.SubtitleConfiguration> items = new ArrayList<>();
        for (Sub sub : subs) items.add(sub.getExo());
        return items;
    }

    private static void selectTrack(ExoPlayer player, int group, int track, List<Integer> trackIndices) {
        if (group >= player.getCurrentTracks().getGroups().size()) return;
        Tracks.Group trackGroup = player.getCurrentTracks().getGroups().get(group);
        for (int i = 0; i < trackGroup.length; i++) {
            if (i == track || trackGroup.isTrackSelected(i)) trackIndices.add(i);
        }
    }

    private static void deselectTrack(ExoPlayer player, int group, int track, List<Integer> trackIndices) {
        if (group >= player.getCurrentTracks().getGroups().size()) return;
        Tracks.Group trackGroup = player.getCurrentTracks().getGroups().get(group);
        for (int i = 0; i < trackGroup.length; i++) {
            if (i != track && trackGroup.isTrackSelected(i)) trackIndices.add(i);
        }
    }

    private static void setTrackParameters(ExoPlayer player, int group, List<Integer> trackIndices) {
        if (group >= player.getCurrentTracks().getGroups().size()) return;
        player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setOverrideForType(new TrackSelectionOverride(player.getCurrentTracks().getGroups().get(group).getMediaTrackGroup(), trackIndices)).build());
    }

    private static synchronized ExtractorsFactory getExtractorsFactory() {
        if (extractorsFactory == null) extractorsFactory = new DefaultExtractorsFactory().setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS).setTsExtractorTimestampSearchBytes(TsExtractor.DEFAULT_TIMESTAMP_SEARCH_BYTES * 3);
        return extractorsFactory;
    }

    private static synchronized HttpDataSource.Factory getHttpDataSourceFactory() {
        if (httpDataSourceFactory == null) httpDataSourceFactory = Setting.getHttp() == 0 ? new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true) : new OkHttpDataSource.Factory((Call.Factory) OkHttp.client());
        return httpDataSourceFactory;
    }

    private static synchronized DataSource.Factory getDataSourceFactory(Map<String, String> headers) {
        if (dataSourceFactory == null) dataSourceFactory = buildReadOnlyCacheDataSource(new DefaultDataSource.Factory(App.get(), getHttpDataSourceFactory()), getCache());
        httpDataSourceFactory.setDefaultRequestProperties(Players.checkUa(headers));
        return dataSourceFactory;
    }

    private static CacheDataSource.Factory buildReadOnlyCacheDataSource(DataSource.Factory upstreamFactory, Cache cache) {
        return new CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(upstreamFactory).setCacheWriteDataSinkFactory(null).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }

    private static synchronized DatabaseProvider getDatabase() {
        if (database == null) database = new StandaloneDatabaseProvider(App.get());
        return database;
    }

    private static synchronized Cache getCache() {
        if (cache == null) cache = new SimpleCache(Path.exo(), new NoOpCacheEvictor(), getDatabase());
        return cache;
    }

    public static void reset() {
        if (cache != null) cache.release();
        httpDataSourceFactory = null;
        dataSourceFactory = null;
        extractorsFactory = null;
        database = null;
        cache = null;
    }
}
