package org.aksw.defacto.restful.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class FaviconController {
  @RequestMapping("favicon.ico")
  @ResponseBody
  void favicon() {}
}
