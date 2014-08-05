
package com.ft.fastfttransformer.response;

import java.util.HashMap;
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
    "classname",
    "query",
    "txid",
    "txprefix",
    "tx",
    "tagid",
    "id",
    "tag"
})
public class Tag {

    @JsonProperty("classname")
    private Object classname;
    @JsonProperty("query")
    private String query;
    @JsonProperty("txid")
    private Long txid;
    @JsonProperty("txprefix")
    private String txprefix;
    @JsonProperty("tx")
    private String tx;
    @JsonProperty("tagid")
    private Long tagid;
    @JsonProperty("id")
    private Long id;
    @JsonProperty("tag")
    private String tag;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("classname")
    public Object getClassname() {
        return classname;
    }

    @JsonProperty("classname")
    public void setClassname(Object classname) {
        this.classname = classname;
    }

    @JsonProperty("query")
    public String getQuery() {
        return query;
    }

    @JsonProperty("query")
    public void setQuery(String query) {
        this.query = query;
    }

    @JsonProperty("txid")
    public Long getTxid() {
        return txid;
    }

    @JsonProperty("txid")
    public void setTxid(Long txid) {
        this.txid = txid;
    }

    @JsonProperty("txprefix")
    public String getTxprefix() {
        return txprefix;
    }

    @JsonProperty("txprefix")
    public void setTxprefix(String txprefix) {
        this.txprefix = txprefix;
    }

    @JsonProperty("tx")
    public String getTx() {
        return tx;
    }

    @JsonProperty("tx")
    public void setTx(String tx) {
        this.tx = tx;
    }

    @JsonProperty("tagid")
    public Long getTagid() {
        return tagid;
    }

    @JsonProperty("tagid")
    public void setTagid(Long tagid) {
        this.tagid = tagid;
    }

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
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
