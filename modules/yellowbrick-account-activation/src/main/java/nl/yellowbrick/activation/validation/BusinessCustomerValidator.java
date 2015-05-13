package nl.yellowbrick.activation.validation;

import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.BusinessIdentifier;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.List;

@Component
public class BusinessCustomerValidator extends AccountRegistrationValidator {

    private final CustomerDao customerDao;

    @Autowired
    public BusinessCustomerValidator(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    @Override
    protected void doValidate(Customer customer, Errors errors) {
        if(!customer.isBusinessCustomer())
            return;

        List<BusinessIdentifier> businessIdentifiers = customerDao.getBusinessIdentifiers(customer.getCustomerId());

        validateUniquenessByBusinessIdentifiers(businessIdentifiers, errors);
    }

    private void validateUniquenessByBusinessIdentifiers(List<BusinessIdentifier> businessIdentifiers,
                                                         Errors errors) {

        businessIdentifiers.forEach(bi -> {
            if (customerDao.findAllByBusinessIdentifier(bi.getLabel(), bi.getValue()).size() > 1) {
                String field = String.format("businessIdentifiers[%d].value", businessIdentifiers.indexOf(bi));
                errors.rejectValue(field, "errors.duplicate");
            }
        });
    }
}
