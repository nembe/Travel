package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.util.MessageHelper;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/productgroups/{group}")
public class ProductGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductGroupController.class);

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

    @RequestMapping(method = RequestMethod.POST)
    public String updateProductGroup(@PathVariable("group") String groupDesc,
                                     @ModelAttribute("productGroup") ProductGroup productGroup,
                                     BindingResult bindingResult,
                                     ModelMap model,
                                     RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return showProductGroupForm(model, groupDesc);

        Optional<ProductGroup> updatedProductGroup = productGroupDao.update(productGroup);

        if(updatedProductGroup.isPresent()) {
            model.clear();
            MessageHelper.flash(ra, "productgroup.updated");

            return "redirect:/productgroups/" + productGroup.getDescription().toLowerCase();
        } else {
            LOG.error("user tried to update unknown product group {} with id {}",
                    productGroup.getDescription(), productGroup.getId());

            throw new ResourceNotFoundException("Unknown product group: " + groupDesc);
        }
    }
}
