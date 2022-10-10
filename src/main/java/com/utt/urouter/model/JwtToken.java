package com.utt.urouter.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JwtToken {

  String token;
  String exp;
  String userType;
}
