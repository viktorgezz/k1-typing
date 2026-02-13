package ru.viktorgezz.coretyping.domain.multiplayer.util;

public class WebsocketTopicStorage {

    private WebsocketTopicStorage() {
    }

    public static final String TOPIC_PROGRESS = "/topic/contest/%d/progress";
    public static final String TOPIC_PLAYER_JOINED = "/topic/contest/%d/player-joined";
    public static final String TOPIC_PLAYER_LEFT = "/topic/contest/%d/player-left";
    public static final String TOPIC_PLAYER_READY = "/topic/contest/%d/player-ready";
    public static final String TOPIC_PLAYER_FINISHED = "/topic/contest/%d/player-finished";
    public static final String TOPIC_COUNTDOWN = "/topic/contest/%d/countdown";
    public static final String TOPIC_START = "/topic/contest/%d/start";
    public static final String TOPIC_FINISHED = "/topic/contest/%d/finished";
}
