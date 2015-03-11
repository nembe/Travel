package nl.yellowbrick.admin.controller;

import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Injects model attributes required to render the menu
 */
@ControllerAdvice
public class MenuControllerAdvice {

    @Autowired
    private ProductGroupDao productGroupDao;

    @ModelAttribute("allProductGroups")
    public List<ProductGroup> allProductGroups() {
        return productGroupDao.all();
    }
}
