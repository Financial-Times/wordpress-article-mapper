package com.ft.wordpressarticlemapper.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
  "assanka_mockingbird",
  "ftblogs_access",
  "assanka_atompush",
  "emailscrape_include"
})
public class CustomFields {

  @JsonProperty("assanka_mockingbird")
  private List<String> assankaMockingbird = new ArrayList<String>();

  @JsonProperty("ftblogs_access")
  private List<String> ftblogsAccess = new ArrayList<String>();

  @JsonProperty("assanka_atompush")
  private List<String> assankaAtompush = new ArrayList<String>();

  @JsonProperty("emailscrape_include")
  private List<String> emailscrapeInclude = new ArrayList<String>();

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /** @return The assankaMockingbird */
  @JsonProperty("assanka_mockingbird")
  public List<String> getAssankaMockingbird() {
    return assankaMockingbird;
  }

  /** @param assankaMockingbird The assanka_mockingbird */
  @JsonProperty("assanka_mockingbird")
  public void setAssankaMockingbird(List<String> assankaMockingbird) {
    this.assankaMockingbird = assankaMockingbird;
  }

  /** @return The ftblogsAccess */
  @JsonProperty("ftblogs_access")
  public List<String> getFtblogsAccess() {
    return ftblogsAccess;
  }

  /** @param ftblogsAccess The ftblogs_access */
  @JsonProperty("ftblogs_access")
  public void setFtblogsAccess(List<String> ftblogsAccess) {
    this.ftblogsAccess = ftblogsAccess;
  }

  /** @return The assankaAtompush */
  @JsonProperty("assanka_atompush")
  public List<String> getAssankaAtompush() {
    return assankaAtompush;
  }

  /** @param assankaAtompush The assanka_atompush */
  @JsonProperty("assanka_atompush")
  public void setAssankaAtompush(List<String> assankaAtompush) {
    this.assankaAtompush = assankaAtompush;
  }

  /** @return The emailscrapeInclude */
  @JsonProperty("emailscrape_include")
  public List<String> getEmailscrapeInclude() {
    return emailscrapeInclude;
  }

  /** @param emailscrapeInclude The emailscrape_include */
  @JsonProperty("emailscrape_include")
  public void setEmailscrapeInclude(List<String> emailscrapeInclude) {
    this.emailscrapeInclude = emailscrapeInclude;
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

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(assankaMockingbird)
        .append(ftblogsAccess)
        .append(assankaAtompush)
        .append(emailscrapeInclude)
        .append(additionalProperties)
        .toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof CustomFields) == false) {
      return false;
    }
    CustomFields rhs = ((CustomFields) other);
    return new EqualsBuilder()
        .append(assankaMockingbird, rhs.assankaMockingbird)
        .append(ftblogsAccess, rhs.ftblogsAccess)
        .append(assankaAtompush, rhs.assankaAtompush)
        .append(emailscrapeInclude, rhs.emailscrapeInclude)
        .append(additionalProperties, rhs.additionalProperties)
        .isEquals();
  }
}
