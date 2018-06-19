package dijkspicy.queeng.server.impl;

import dijkspicy.queeng.server.dispatch.HttpContext;
import dijkspicy.queeng.server.dispatch.connector.ConnectorHandler;
import dijkspicy.queeng.server.dispatch.connector.ConnectorTestHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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

    @RequestMapping("/jdbc/{type}/test")
    @PostMapping
    public Object requestTest(HttpServletRequest req, HttpServletResponse resp, @PathVariable(value = "type") String type) {
        return new ConnectorTestHandler(type).execute(new HttpContext(req, resp));
    }
}
