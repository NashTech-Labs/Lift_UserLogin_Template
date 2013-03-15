package code
package snippet
import net.liftweb.http.S
import net.liftweb.common.Loggable

class BetaSnippet extends Loggable {

  def render = {
    S.redirectTo("/home")
  }
}

