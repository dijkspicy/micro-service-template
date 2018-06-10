package dijkspicy.queeng.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dijkspicy.queeng.server.dispatch.HttpContext;
import dijkspicy.queeng.server.dispatch.connector.ConnectorHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * queeng
 *
 * @author dijkspicy
 * @date 2018/5/28
 */
@RestController
public class ConnectorController {

    @RequestMapping("/jdbc/{type}")
    @PostMapping
    public String request(HttpServletRequest req, HttpServletResponse resp, @PathVariable(value = "type") String type) {
        return new ConnectorHandler(type).execute(new HttpContext(req, resp));
    }

}
