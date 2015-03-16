package nl.yellowbrick.admin.validation;

import nl.yellowbrick.data.dao.ProductGroupDao;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ProductGroupValidator implements Validator {

    private final ProductGroupDao productGroupDao;

    public ProductGroupValidator(ProductGroupDao productGroupDao) {
        this.productGroupDao = productGroupDao;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(ProductGroup.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProductGroup productGroup = (ProductGroup) target;

        productGroupDao.findByDescription(productGroup.getDescription()).ifPresent(pg -> {
            if(!pg.getId().equals(productGroup.getId()))
                errors.rejectValue("description", "errors.duplicate");
        });
    }
}
