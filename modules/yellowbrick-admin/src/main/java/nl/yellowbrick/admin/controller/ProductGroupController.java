package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.util.MessageHelper;
import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import nl.yellowbrick.data.domain.ProductSubgroup;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/productgroups/{group}")
public class ProductGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductGroupController.class);

    @Autowired
    private ProductGroupDao productGroupDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showProductGroupForm(ModelMap model, @PathVariable("group") String groupDesc) {
        ProductGroup productGroup = getProductGroupOrFail(groupDesc);
        model.addAttribute("productGroup", productGroup);
        model.addAttribute("subgroups", linksToSubgroups(productGroup));

        return "productgroups/form";
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

    @RequestMapping(value = "/subgroups/{subgroup}", method = RequestMethod.GET)
    public String showProductSubgroupForm(ModelMap model,
                                          @PathVariable("group") String groupDesc,
                                          @PathVariable("subgroup") String subgroupDesc) {

        ProductGroup productGroup = getProductGroupOrFail(groupDesc);
        ProductSubgroup productSubgroup = getSubgroupOrFail(productGroup, subgroupDesc);

        model.addAttribute("productGroup", productGroup);
        model.addAttribute("subgroups", linksToSubgroups(productGroup));
        model.addAttribute("subgroup", productSubgroup);

        return "productgroups/subgroup_form";
    }

    @RequestMapping(value = "/subgroups/{subgroup}", method = RequestMethod.POST)
    public String updateProductSubgroup(@PathVariable("group") String groupDesc,
                                        @PathVariable("subgroup") String subgroupDesc,
                                        @ModelAttribute("productSubgroup") ProductSubgroup productSubgroup,
                                        BindingResult bindingResult,
                                        ModelMap model,
                                        RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return showProductSubgroupForm(model, groupDesc, subgroupDesc);

        ProductGroup productGroup = getProductGroupOrFail(groupDesc);
        ProductSubgroup existingSubgroup = getSubgroupOrFail(productGroup, subgroupDesc);

        existingSubgroup.setDefaultIssuePhysicalCard(productSubgroup.isDefaultIssuePhysicalCard());

        productGroupDao.update(existingSubgroup);

        model.clear();
        MessageHelper.flash(ra, "productsubgroup.updated");

        return String.format("redirect:/productgroups/%s/subgroups/%s", groupDesc, subgroupDesc);
    }

    private ProductGroup getProductGroupOrFail(String group) {
        return productGroupDao
                .findByDescription(group)
                .orElseThrow(() -> new ResourceNotFoundException("Found no product group described as " + group));
    }

    private ProductSubgroup getSubgroupOrFail(ProductGroup productGroup, String subgroupDesc) {
        return productGroupDao
                .findSubgroupsForProductGroup(productGroup.getId())
                .stream()
                .filter(sg -> sg.getDescription().equalsIgnoreCase(subgroupDesc))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Found no subgroup described as " + subgroupDesc));
    }

    private List<LinkHelper> linksToSubgroups(ProductGroup productGroup) {
        return productGroupDao.findSubgroupsForProductGroup(productGroup.getId())
                .stream()
                .map(subgroup -> new LinkHelper(productGroup, subgroup))
                .collect(Collectors.toList());
    }

    private class LinkHelper {

        public final String path;
        public final String text;

        private LinkHelper(ProductGroup productGroup, ProductSubgroup subgroup) {
            path = String.format("/productgroups/%s/subgroups/%s",
                    productGroup.getDescription().toLowerCase(),
                    subgroup.getDescription().toLowerCase());
            text = subgroup.getDescription();
        }
    }
}
