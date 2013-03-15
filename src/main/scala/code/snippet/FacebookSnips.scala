package code
package snippet

import scala.xml.{ NodeSeq, Text }
import net.liftweb._
import common._
import http.{ Factory, NoticeType, S, SHtml }
import http.js.JsCmds._
import http.js.JE._
import util.Helpers._
import code.lib.FacebookGraph
import code.model.User

object Facebook extends Loggable {
  /**
   * If user is already connected, display a button with a direct login, otherwise
   * display a button that opens a facebook auth dialog window.
   */
  def link: NodeSeq = {
    <span id="id_facebooklink">
      <a href="#"><img align="left" src="/images/facebook.png" /></a>
    </span>
    <div lift="embed?what=/templates-hidden/parts/fb-init"></div>
    <script type="text/javascript">
      <![CDATA[
var fbLogin = {
  api: {
    facebook: {
      init: function(data, success) {
        $.ajax({
          type: "POST",
          url: "/api/facebook/init",
          data: data,
          success: success
        });
      },
      login: function(success) {
        $.ajax({
          type: "POST",
          url: "/api/facebook/login",
          success: success
        });
      }
    }
  },
  facebook: {
    init: function(input, func) {
      window.fbAsyncInit = function() {
        FB.init({
          appId : input.appId, // App ID
          channelURL : input.channelUrl,
          status : true, // check login status
          cookie : true, // enable cookies to allow the server to access the session
          oauth : true, // enable OAuth 2.0
          xfbml : true // parse XFBML
        });
        func();
      };
    }
  },
  util: {
  
    wopen: function (url, name, w, h) {
          w += 32;
      h += 96;
      wleft = (screen.width - w) / 2;
      wtop = (screen.height - h) / 2;
      var win = window.open(url,
        name,
        'width=' + w + ', height=' + h + ', ' +
        'left=' + wleft + ', top=' + wtop + ', ' +
        'location=no, menubar=no, ' +
        'status=no, toolbar=no, scrollbars=no, resizable=no');
      win.resizeTo(w, h);
      win.moveTo(wleft, wtop);
      win.focus();
    }
  }
}
      $("#id_facebooklink").click(function() { onClick(); });

      var onClick = function() {
        fbLogin.util.wopen("/facebook/connect", "facebook_connect", 640, 360);
        return false;
      };

      fbLogin.facebook.init(Input, function() {
        FB.getLoginStatus(function(response) {
          if (response.authResponse) {
            fbLogin.api.facebook.init(response.authResponse, function(data) {
              if (data.alert) {
                console.log(data.alert.level+": "+data.alert.message);
              }
              else if (data.status) {
                onClick = function() {
                  fbLogin.api.facebook.login(function(resp) {
                    if (resp.alert) {
                      console.log(resp.alert.level+": "+resp.alert.message);
                    }
                    else if (resp.url) {
                      window.location=resp.url
                    }
                  })
                  return false;
                };
              }
            })
          }
        })
      });
    ]]>
    </script>
  }

  /**
   * Inject the data Facebook needs for initialization
   */
  def init =
    "#id_jsinit" #>
      Script(
        JsCrVar("Input", JsObj(
          ("appId", Str(FacebookGraph.key.vend)),
          ("channelUrl", Str(S.hostAndPath + FacebookGraph.channelUrl.vend)))))

  def close =
    Script(
      JsCrVar("Input", JsObj(
        ("url", Str(S.param("url").openOr(User.loginContinueUrl.is))))))

  /**
   * Only display if connected to facebook and access tokenis empty
   */
  def checkAuthToken(in: NodeSeq): NodeSeq =
    if (User.isConnectedToFaceBook && FacebookGraph.currentAccessToken.is.isEmpty)
      in
    else
      NodeSeq.Empty
}