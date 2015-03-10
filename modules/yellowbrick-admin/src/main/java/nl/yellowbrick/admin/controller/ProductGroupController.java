package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@Controller
@RequestMapping("/productgroups/{group}")
public class ProductGroupController {

    @Autowired
    private ProductGroupDao productGroupDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showProductGroupForm(ModelMap model, @PathVariable("group") String groupDesc) {
        Optional<ProductGroup> productGroup = productGroupDao.findByDescription(groupDesc);

        return productGroup.map(pg -> {
            model.addAttribute("productGroup", pg);
            return "productgroups/form";
        }).orElseThrow(() -> new ResourceNotFoundException("Found no product group described as " + groupDesc));
    }
}
