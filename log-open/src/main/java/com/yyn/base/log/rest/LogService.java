/**
 * 
 */
package com.yyn.base.log.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dandan
 *
 */
@RestController
@RequestMapping(value = "/public/rest/log", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class LogService {

}
