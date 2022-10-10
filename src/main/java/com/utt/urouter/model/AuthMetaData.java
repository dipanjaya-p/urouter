package com.utt.urouter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.StringJoiner;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class AuthMetaData {

  @NotNull private String protocol;
  @NotNull private String role;
  @NotNull private String token;

  @Override
  public String toString() {
    return new StringJoiner(", ", AuthMetaData.class.getSimpleName() + "[", "]")
        .add("protocol='" + protocol + "'")
        .add("role='" + role + "'")
        .add("token='" + token + "'")
        .toString();
  }
}
