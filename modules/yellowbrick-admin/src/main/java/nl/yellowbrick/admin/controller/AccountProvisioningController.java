package nl.yellowbrick.admin.controller;

import nl.yellowbrick.data.dao.CustomerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/provisioning")
public class AccountProvisioningController {

    @Autowired
    private CustomerDao customerDao;

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        model.addAttribute("customers", customerDao.findAllPendingActivation());

        return "provisioning/index";
    }
}
