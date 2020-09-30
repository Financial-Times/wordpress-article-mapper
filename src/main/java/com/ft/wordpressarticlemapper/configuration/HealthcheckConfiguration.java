package com.ft.wordpressarticlemapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

public class HealthcheckConfiguration {

  @NotNull @JsonProperty private String name;

  @NotNull @JsonProperty private int severity;

  @NotNull @JsonProperty private String businessImpact;

  @NotNull @JsonProperty private String technicalSummary;

  @NotNull @JsonProperty private String panicGuideUrl;

  public HealthcheckConfiguration() {}

  public HealthcheckConfiguration(
      String name,
      int severity,
      String businessImpact,
      String technicalSummary,
      String panicGuideUrl) {
    this.name = name;
    this.severity = severity;
    this.businessImpact = businessImpact;
    this.technicalSummary = technicalSummary;
    this.panicGuideUrl = panicGuideUrl;
  }

  public int getSeverity() {
    return severity;
  }

  public String getBusinessImpact() {
    return businessImpact;
  }

  public String getTechnicalSummary() {
    return technicalSummary;
  }

  public String getPanicGuideUrl() {
    return panicGuideUrl;
  }

  public String getName() {
    return name;
  }
}
