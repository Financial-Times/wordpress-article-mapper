package com.ft.wordpressarticlemapper.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ComparisonChain;

import javax.validation.constraints.NotNull;


public class Identifier implements Comparable<Identifier> {
    private String authority;
    private String identifierValue;

    public Identifier(@JsonProperty("authority") String authority,
                      @JsonProperty("identifierValue") String identifierValue) {

        this.authority = authority;
        this.identifierValue = identifierValue;
    }

    public String getAuthority() {
        return authority;
    }

    @NotNull
    public String getIdentifierValue() {
        return identifierValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !(o.getClass() == Identifier.class)) return false;

        Identifier that = (Identifier) o;

        return Objects.equals(this.authority, that.authority)
                && Objects.equals(this.identifierValue, that.identifierValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierValue, authority);
    }
    
    @Override
    public String toString() {
        return String.format("%s[authority=%s,identifierValue=%s]",
                super.toString(), authority, identifierValue); 
    }
    
    @Override
    public int compareTo(Identifier that) {
        return ComparisonChain.start()
                .compare(this.authority, that.authority)
                .compare(this.identifierValue, that.identifierValue)
                .result();
    }
}


