package gg.feed;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

@Cached
public class News
{
	public static final String DELIMITER = "<>";
	@Id private String id;
	private String url;
	private String time;
	private String brief;
	private String title;
	private String content;
	private String author;
	private String topicId;
	private String picId;
	
	public News(){}
	
	public News(String id, String time, String author, String title, String brief, String content, String topicId, String picId)
	{
		setId(id);
		setTime(time);
		setAuthor(author);
		setTitle(title);
		setBrief(brief);
		setContent(content);
		setTopicId(topicId);
		setPicId(picId);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getId());
		sb.append(DELIMITER);
		sb.append(getTime());
		sb.append(DELIMITER);
		sb.append(getAuthor());
		sb.append(DELIMITER);
		sb.append(getTitle());
		sb.append(DELIMITER);
		sb.append(getBrief());
		sb.append(DELIMITER);
		sb.append(getTopicId());
		sb.append(DELIMITER);
		sb.append(getPicId());
		return sb.toString();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public void setBrief(String brief) {
		this.brief = brief;
	}
	public String getBrief() {
		return brief;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTime() {
		return time;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getAuthor() {
		return author;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	public String getTopicId() {
		return topicId;
	}
	public void setPicId(String pic) {
		this.picId = pic;
	}
	public String getPicId() {
		return picId;
	}
}
