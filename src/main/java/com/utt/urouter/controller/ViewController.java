package com.utt.urouter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Important configuration to work refresh for angular application **/
@Controller
public class ViewController {

	@GetMapping(value ="/**/{[path:[^\\.]*}")
	public String redirect() {
		// Forward to home page so that route is preserved.
		return "forward:/";
	}

}

