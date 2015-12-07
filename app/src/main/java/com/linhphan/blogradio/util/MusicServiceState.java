package com.linhphan.blogradio.util;

/**
 * Created by linhphan on 10/22/15.
 */
public enum MusicServiceState {
    idle,// media player has ben initialized
    retrieving, // the service is looking up the url of the song from server which selected.
    initialized,//media player has been set data source.
    preparing,// media player is look up the song which was set.
    prepared, // media player has been prepared...
    // playback active (media player ready!). (but the media player may actually be
    // paused in this state if we don't have audio focus. But we stay in this state
    // so that we know we have to resume playback once we get focus back)
    playing,
    buffering,
    seekToCompleted,
    paused, // playback paused (media player ready!)
    completed,
    stopped, // media player is stopped and not prepared to onPlay
    error,
    end,
    destroy
}
