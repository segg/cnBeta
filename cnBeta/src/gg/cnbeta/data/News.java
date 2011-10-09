package gg.cnbeta.data;

public class News
{
	public static final String DELIMITER_FIELD = "<>";
	public static final String DELIMITER_LINE = "<<>>";
	private String id;
	private String url;
	private String time;
	private String brief;
	private String title;
	private String content;
	private String author;
	private String topicId;
	private String picId;
	
	public News(String id, String time, String author, String title, String brief, String topicId, String picId) {
		setId(id);
		setTime(time);
		setAuthor(author);
		setTitle(title);
		setBrief(brief);
		setTopicId(topicId);
		setPicId(picId);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getId());
		sb.append(DELIMITER_FIELD);
		sb.append(getTime());
		sb.append(DELIMITER_FIELD);
		sb.append(getAuthor());
		sb.append(DELIMITER_FIELD);
		sb.append(getTitle());
		sb.append(DELIMITER_FIELD);
		sb.append(getBrief());
		sb.append(DELIMITER_FIELD);
		sb.append(getTopicId());
		sb.append(DELIMITER_FIELD);
		sb.append(getPicId());
		return sb.toString();
	}
	
	public static News createNews(String s) {
		String [] tokens = s.split(DELIMITER_FIELD);
		if(tokens.length != 7)
			return null;
		return new News(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6]);
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
