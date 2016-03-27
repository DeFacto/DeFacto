package org.aksw.defacto.restful;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class User {

  public static Logger LOG = LogManager.getLogger(User.class);

  /**
   * Gets user name and id.
   *
   * @param principal
   * @return
   */
  @RequestMapping({"/user", "/me"})
  public Map<String, String> user(final Principal principal) {
    final Map<String, String> map = new LinkedHashMap<>();
    try {

      final JSONObject details = getAuthenticationDetails();

      String id = "NA";
      String host = "NA";

      if (details.has("url")) {
        id = details.getString("url");
        host = "github";
      } else if (details.has("link")) {
        id = details.getString("link");
        host = "google";
      } else if (details.has("id")) {
        id = "https://www.facebook.com/app_scoped_user_id/".concat(details.getString("id"));
        host = "facebook";
      }
      map.put("id", id);
      map.put("host", host);
      map.put("name", details.getString("name"));
      // map.put("principalname", principal.getName());

    } catch (JSONException | JsonProcessingException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return map;
  }

  private Authentication getAuthentication() {
    return ((OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication())
        .getUserAuthentication();
  }

  private JSONObject getAuthenticationDetails() throws JSONException, JsonProcessingException {
    return getAuthenticationDetails(getAuthentication());
  }

  private JSONObject getAuthenticationDetails(final Authentication authentication)
      throws JSONException, JsonProcessingException {
    return new JSONObject(new ObjectMapper().writeValueAsString(authentication.getDetails()));
  }
}
