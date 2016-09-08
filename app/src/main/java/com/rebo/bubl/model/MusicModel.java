package com.rebo.bubl.model;

/**
 * Created by guodunsong on 16/7/14.
 */
public class MusicModel {
    private Integer musicId;
    private String name;
    private String path;

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
