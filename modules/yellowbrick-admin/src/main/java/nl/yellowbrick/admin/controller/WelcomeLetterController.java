package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.service.WelcomeLetterExportService;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("/provisioning/welcome_letters")
public class WelcomeLetterController {

    @Autowired
    private WelcomeLetterExportService exportService;

    @RequestMapping(method = RequestMethod.GET)
    public String get(@ModelAttribute("allProductGroups") List<ProductGroup> allProductGroups) {
        ProductGroup ybPg = allProductGroups.get(0);

        exportService.exportForProductGroup(ybPg, 398764);

        return "provisioning/welcome_letters/index";
    }
}
