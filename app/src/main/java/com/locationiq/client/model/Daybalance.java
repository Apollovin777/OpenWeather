/*
 * LocationIQ
 * LocationIQ provides flexible enterprise-grade location based solutions. We work with developers, startups and enterprises worldwide serving billions of requests everyday. This page provides an overview of the technical aspects of our API and will help you get started.
 *
 * OpenAPI spec version: 1.0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.locationiq.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * Daybalance
 */

public class Daybalance {
  public static final String SERIALIZED_NAME_DAY = "day";
  @SerializedName(SERIALIZED_NAME_DAY)
  private Integer day = null;

  public static final String SERIALIZED_NAME_BONUS = "bonus";
  @SerializedName(SERIALIZED_NAME_BONUS)
  private Integer bonus = null;

  public Daybalance day(Integer day) {
    this.day = day;
    return this;
  }

   /**
   * Get day
   * @return day
  **/
  @ApiModelProperty(value = "")
  public Integer getDay() {
    return day;
  }

  public void setDay(Integer day) {
    this.day = day;
  }

  public Daybalance bonus(Integer bonus) {
    this.bonus = bonus;
    return this;
  }

   /**
   * Get bonus
   * @return bonus
  **/
  @ApiModelProperty(value = "")
  public Integer getBonus() {
    return bonus;
  }

  public void setBonus(Integer bonus) {
    this.bonus = bonus;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Daybalance daybalance = (Daybalance) o;
    return Objects.equals(this.day, daybalance.day) &&
        Objects.equals(this.bonus, daybalance.bonus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(day, bonus);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Daybalance {\n");
    
    sb.append("    day: ").append(toIndentedString(day)).append("\n");
    sb.append("    bonus: ").append(toIndentedString(bonus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

