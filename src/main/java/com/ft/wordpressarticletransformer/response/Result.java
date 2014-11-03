
package com.ft.wordpressarticletransformer.response;

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
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "sortval",
    "uuidv3",
    "tags",
    "issticky",
    "title",
    "type",
    "url",
    "shorturl",
    "content",
    "abstract",
    "currentrevision",
    "attachments"
})
public class Result {

    @JsonProperty("sortval")
    private String sortval;
    @JsonProperty("uuidv3")
    private String uuidv3;
    @JsonProperty("tags")
    private List<Tag> tags = new ArrayList<Tag>();
    @JsonProperty("issticky")
    private Boolean issticky;
    @JsonProperty("title")
    private String title;
    @JsonProperty("type")
    private String type;
    @JsonProperty("url")
    private String url;
    @JsonProperty("shorturl")
    private String shorturl;
    @JsonProperty("content")
    private String content;
    @JsonProperty("abstract")
    private String _abstract;
    @JsonProperty("currentrevision")
    private Long currentrevision;
    @JsonProperty("attachments")
    private List<Attachment> attachments = new ArrayList<Attachment>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("sortval")
    public String getSortval() {
        return sortval;
    }

    @JsonProperty("sortval")
    public void setSortval(String sortval) {
        this.sortval = sortval;
    }

    @JsonProperty("uuidv3")
    public String getUuidv3() {
        return uuidv3;
    }

    @JsonProperty("uuidv3")
    public void setUuidv3(String uuidv3) {
        this.uuidv3 = uuidv3;
    }

    @JsonProperty("tags")
    public List<Tag> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @JsonProperty("issticky")
    public Boolean getIssticky() {
        return issticky;
    }

    @JsonProperty("issticky")
    public void setIssticky(Boolean issticky) {
        this.issticky = issticky;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("shorturl")
    public String getShorturl() {
        return shorturl;
    }

    @JsonProperty("shorturl")
    public void setShorturl(String shorturl) {
        this.shorturl = shorturl;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    @JsonProperty("abstract")
    public String getAbstract() {
        return _abstract;
    }

    @JsonProperty("abstract")
    public void setAbstract(String _abstract) {
        this._abstract = _abstract;
    }

    @JsonProperty("currentrevision")
    public Long getCurrentrevision() {
        return currentrevision;
    }

    @JsonProperty("currentrevision")
    public void setCurrentrevision(Long currentrevision) {
        this.currentrevision = currentrevision;
    }

    @JsonProperty("attachments")
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @JsonProperty("attachments")
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
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

}
