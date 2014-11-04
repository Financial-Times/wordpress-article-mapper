
package com.ft.wordpressarticletransformer.response;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "status",
    "error",
    "post",
    "previous_url"
})
public class WordPressResponse {

    @JsonProperty("status")
    private String status;
    @JsonProperty("error")
    private String error;
    @JsonProperty("post")
    private Post post;
    @JsonProperty("previous_url")
    private String previousUrl;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     *     The error 
     */
    @JsonProperty("error")
    public String getError() {
        return error;
    }

    /**
     * 
     * @param error
     *     The error
     */
    @JsonProperty("error")
    public void setError(String error) {
        this.error = error;
    }
    
    /**
     * 
     * @return
     *     The post
     */
    @JsonProperty("post")
    public Post getPost() {
        return post;
    }

    /**
     * 
     * @param post
     *     The post
     */
    @JsonProperty("post")
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * 
     * @return
     *     The previousUrl
     */
    @JsonProperty("previous_url")
    public String getPreviousUrl() {
        return previousUrl;
    }

    /**
     * 
     * @param previousUrl
     *     The previous_url
     */
    @JsonProperty("previous_url")
    public void setPreviousUrl(String previousUrl) {
        this.previousUrl = previousUrl;
    }

    @Override
    public String toString() {
        //TODO replace with non-reflection based version
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(error).append(post).append(previousUrl).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WordPressResponse) == false) {
            return false;
        }
        WordPressResponse rhs = ((WordPressResponse) other);
        return new EqualsBuilder().append(status, rhs.status).append(error, rhs.error).append(post, rhs.post).append(previousUrl, rhs.previousUrl).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
