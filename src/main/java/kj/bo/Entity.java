package kj.bo;

public class Entity {
	private long id;
	private String url;
	private String[] tags;

	public Entity() {
	}

	public Entity(long id) {
		this.id = id;
	}

	public Entity(String url, String[] tags) {
		this.url = url;
		this.tags = tags;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

}
