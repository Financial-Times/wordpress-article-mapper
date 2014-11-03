
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
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "tid",
    "t",
    "pr",
    "txid"
})
public class Tm {

    @JsonProperty("tid")
    private Object tid;
    @JsonProperty("t")
    private String t;
    @JsonProperty("pr")
    private String pr;
    @JsonProperty("txid")
    private Object txid;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("tid")
    public Object getTid() {
        return tid;
    }

    @JsonProperty("tid")
    public void setTid(Object tid) {
        this.tid = tid;
    }

    @JsonProperty("t")
    public String getT() {
        return t;
    }

    @JsonProperty("t")
    public void setT(String t) {
        this.t = t;
    }

    @JsonProperty("pr")
    public String getPr() {
        return pr;
    }

    @JsonProperty("pr")
    public void setPr(String pr) {
        this.pr = pr;
    }

    @JsonProperty("txid")
    public Object getTxid() {
        return txid;
    }

    @JsonProperty("txid")
    public void setTxid(Object txid) {
        this.txid = txid;
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
