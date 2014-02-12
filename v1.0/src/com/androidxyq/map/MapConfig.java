package com.androidxyq.map;

/**
 * @author 龚德伟
 * @history 2008-5-21 龚德伟 新建
 */
public class MapConfig {
	private String id;

	private String name;

	private String path;

	private String music;

	public MapConfig(String id, String name) {
		this.id = id;
		this.name = name;
		this.path = "assets/scene/"+id+".map";
		this.music = "assets/music/"+id+".mp3";
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public String getType() {
		return "map";
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "MapConfig{" + id + "," + name + "," + path + "}";
	}

	public String getMusic() {
		return music;
	}
}
