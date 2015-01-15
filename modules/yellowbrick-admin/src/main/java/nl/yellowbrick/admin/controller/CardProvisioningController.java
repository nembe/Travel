package nl.yellowbrick.admin.controller;

import nl.yellowbrick.data.domain.CardOrder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;

@Controller
@RequestMapping("/provisioning/cards")
public class CardProvisioningController {

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        model.addAttribute("orders", new ArrayList<CardOrder>());

        return "provisioning/cards/index";
    }
}
