
package com.ft.wordpressarticlemapper.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ft.content.model.AccessLevel;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "type",
    "slug",
    "url",
    "status",
    "title",
    "title_plain",
    "content",
    "excerpt",
    "date",
    "modified",
    "date_gmt",
    "modified_gmt",
    "categories",
    "tags",
    "author",
    "authors",
    "comments",
    "attachments",
    "comment_count",
    "comment_status",
    "custom_fields",
    "accessLevel",
    "defaultAccessLevel"
})
public class Post {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("url")
    private String url;
    @JsonProperty("status")
    private String status;
    @JsonProperty("title")
    private String title;
    @JsonProperty("title_plain")
    private String titlePlain;
    @JsonProperty("content")
    private String content;
    @JsonProperty("excerpt")
    private String excerpt;
    @JsonProperty("date")
    private String date;
    @JsonProperty("modified")
    private String modified;
    @JsonProperty("date_gmt")
    private String dateGmt;
    @JsonProperty("modified_gmt")
    private String modifiedGmt;
    @JsonProperty("categories")
    private List<Object> categories = new ArrayList<Object>();
    @JsonProperty("tags")
    private List<Object> tags = new ArrayList<Object>();
    @JsonProperty("author")
    private Author author;
    @JsonProperty("authors")
    private List<Author> authors;
    @JsonProperty("comments")
    private List<Object> comments = new ArrayList<Object>();
    @JsonProperty("attachments")
    private List<Object> attachments = new ArrayList<Object>();
    @JsonProperty("comment_count")
    private Integer commentCount;
    @JsonProperty("comment_status")
    private String commentStatus;
    @JsonProperty("custom_fields")
    private CustomFields customFields;
    private String uuid;
    private MainImage mainImage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("access_level")
    private AccessLevel accessLevel;
    @JsonProperty("default_access_level")
    private AccessLevel defaultAccessLevel;

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The slug
     */
    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    /**
     * 
     * @param slug
     *     The slug
     */
    @JsonProperty("slug")
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 
     * @return
     *     The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     *     The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * @return
     *     The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return
     *     The titlePlain
     */
    @JsonProperty("title_plain")
    public String getTitlePlain() {
        return titlePlain;
    }

    /**
     * 
     * @param titlePlain
     *     The title_plain
     */
    @JsonProperty("title_plain")
    public void setTitlePlain(String titlePlain) {
        this.titlePlain = titlePlain;
    }

    /**
     * 
     * @return
     *     The content
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * 
     * @param content
     *     The content
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     *     The excerpt
     */
    @JsonProperty("excerpt")
    public String getExcerpt() {
        return excerpt;
    }

    /**
     * 
     * @param excerpt
     *     The excerpt
     */
    @JsonProperty("excerpt")
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    /**
     * 
     * @return
     *     The date
     */
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The modified
     */
    @JsonProperty("modified")
    public String getModified() {
        return modified;
    }

    /**
     * 
     * @param modified
     *     The modified
     */
    @JsonProperty("modified")
    public void setModified(String modified) {
        this.modified = modified;
    }

    @JsonProperty("date_gmt")
    public String getDateGmt() {
        return dateGmt;
    }

    @JsonProperty("date_gmt")
    public void setDateGmt(String dateGmt) {
        this.dateGmt = dateGmt;
    }

    @JsonProperty("modified_gmt")
    public String getModifiedGmt() {
        return modifiedGmt;
    }

    @JsonProperty("modified_gmt")
    public void setModifiedGmt(String modifiedGmt) {
        this.modifiedGmt = modifiedGmt;
    }

    /**
     * 
     * @return
     *     The categories
     */
    @JsonProperty("categories")
    public List<Object> getCategories() {
        return categories;
    }

    /**
     * 
     * @param categories
     *     The categories
     */
    @JsonProperty("categories")
    public void setCategories(List<Object> categories) {
        this.categories = categories;
    }

    /**
     * 
     * @return
     *     The tags
     */
    @JsonProperty("tags")
    public List<Object> getTags() {
        return tags;
    }

    /**
     * 
     * @param tags
     *     The tags
     */
    @JsonProperty("tags")
    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    /**
     * 
     * @return
     *     The author
     */
    @JsonProperty("author")
    @Deprecated()
    public Author getAuthor() {
        return author;
    }

    /**
     * 
     * @param author
     *     The author
     */
    @JsonProperty("author")
    @Deprecated
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * 
     * @return
     *     The authors
     */
    @JsonProperty("authors")
    public List<Author> getAuthors() {
        return authors;
    }

    /**
     * 
     * @param authors
     *     The authors
     */
    @JsonProperty("authors")
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    /**
     * 
     * @return
     *     The comments
     */
    @JsonProperty("comments")
    public List<Object> getComments() {
        return comments;
    }

    /**
     * 
     * @param comments
     *     The comments
     */
    @JsonProperty("comments")
    public void setComments(List<Object> comments) {
        this.comments = comments;
    }

    /**
     * 
     * @return
     *     The attachments
     */
    @JsonProperty("attachments")
    public List<Object> getAttachments() {
        return attachments;
    }

    /**
     * 
     * @param attachments
     *     The attachments
     */
    @JsonProperty("attachments")
    public void setAttachments(List<Object> attachments) {
        this.attachments = attachments;
    }

    /**
     * 
     * @return
     *     The commentCount
     */
    @JsonProperty("comment_count")
    public Integer getCommentCount() {
        return commentCount;
    }

    /**
     * 
     * @param commentCount
     *     The comment_count
     */
    @JsonProperty("comment_count")
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * 
     * @return
     *     The commentStatus
     */
    @JsonProperty("comment_status")
    public String getCommentStatus() {
        return commentStatus;
    }

    /**
     * 
     * @param commentStatus
     *     The comment_status
     */
    @JsonProperty("comment_status")
    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }

    /**
     * 
     * @return
     *     The customFields
     */
    @JsonProperty("custom_fields")
    public CustomFields getCustomFields() {
        return customFields;
    }

    /**
     * 
     * @param customFields
     *     The custom_fields
     */
    @JsonProperty("custom_fields")
    public void setCustomFields(CustomFields customFields) {
        this.customFields = customFields;
    }
    
    @JsonProperty("uuid")
    public void setUuid(String uuid) {
      this.uuid = uuid;
    }
    
    public String getUuid() {
      return uuid;
    }
    
    @JsonProperty("main_image")
    public void setMainImage(MainImage image) {
      this.mainImage = image;
    }
    
    public MainImage getMainImage() {
      return mainImage;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public AccessLevel getDefaultAccessLevel() {
        return defaultAccessLevel;
    }

    @JsonProperty("access_level")
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    @JsonProperty("default_access_level")
    public void setDefaultAccessLevel(AccessLevel defaultAccessLevel) {
        this.defaultAccessLevel = defaultAccessLevel;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(type)
                .append(slug)
                .append(url)
                .append(status)
                .append(title)
                .append(titlePlain)
                .append(content)
                .append(excerpt)
                .append(date)
                .append(modified)
                .append(categories)
                .append(tags)
                .append(author)
                .append(comments)
                .append(attachments)
                .append(commentCount)
                .append(commentStatus)
                .append(customFields)
                .append(additionalProperties)
                .append(accessLevel)
                .append(defaultAccessLevel)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Post) == false) {
            return false;
        }
        Post rhs = ((Post) other);
        return new EqualsBuilder()
                .append(id, rhs.id)
                .append(type, rhs.type)
                .append(slug, rhs.slug)
                .append(url, rhs.url)
                .append(status, rhs.status)
                .append(title, rhs.title)
                .append(titlePlain, rhs.titlePlain)
                .append(content, rhs.content)
                .append(excerpt, rhs.excerpt)
                .append(date, rhs.date)
                .append(modified, rhs.modified)
                .append(categories, rhs.categories)
                .append(tags, rhs.tags)
                .append(author, rhs.author)
                .append(comments, rhs.comments)
                .append(attachments, rhs.attachments)
                .append(commentCount, rhs.commentCount)
                .append(commentStatus, rhs.commentStatus)
                .append(customFields, rhs.customFields)
                .append(additionalProperties, rhs.additionalProperties)
                .append(accessLevel, rhs.accessLevel)
                .append(defaultAccessLevel, rhs.defaultAccessLevel)
                .isEquals();
    }

}
