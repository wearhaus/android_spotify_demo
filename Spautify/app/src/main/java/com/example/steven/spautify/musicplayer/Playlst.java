package com.example.steven.spautify.musicplayer;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPublic;

/**
 * Created by Steven on 2/10/2016.
 */
public class Playlst {

    public Source source;
    /** Our assigned id */
    public String playlstId;

    //.


    public String id;
    public String name;
    public String href;


    public Boolean collaborative;
    public Map<String, String> external_urls;
    public List<Image> images;
    public UserPublic owner;
    public Boolean is_public;
    public String snapshot_id;
    public String type;
    public String uri;

    public Playlst(PlaylistSimple p) {
        name = p.name;
        id = p.id;
        href = p.href;
        collaborative = p.collaborative;
        external_urls = p.external_urls;
        images = p.images;
        owner = p.owner;
        is_public = p.is_public;
        snapshot_id = p.snapshot_id;
        type = p.type;
        uri = p.uri;
    }









}
