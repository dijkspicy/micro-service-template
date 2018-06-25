package dijkspicy.ms.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dijkspicy.ms.server.dispatch.HttpContext;
import dijkspicy.ms.server.dispatch.ServiceResponse;
import dijkspicy.ms.server.dispatch.dao.DAOHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProxyController
 *
 * @author dijkspicy
 * @date 2018/6/25
 */
@RestController
public class ProxyController {

    @RequestMapping("/ms/dao/{type}")
    @GetMapping
    public ServiceResponse request(HttpServletRequest req, HttpServletResponse resp, @PathVariable(value = "type") String type) {
        return new DAOHandler(type).execute(new HttpContext(req, resp));
    }
}
