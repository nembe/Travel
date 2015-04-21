package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("/customers/{id}")
public class CustomerDetailsController {

    @Autowired CustomerDao customerDao;
    @Autowired CardOrderDao cardOrderDao;

    @RequestMapping(method = RequestMethod.GET)
    public String show(ModelMap model, @PathVariable("id") Long id) {
        Customer customer = customerDao.findById(id).orElseThrow(ResourceNotFoundException::new);
        List<CardOrder> cardOrders = cardOrderDao.findTransponderCardsForCustomer(customer);

        model.put("cust", customer);
        model.put("tCardOrders", cardOrders);

        return "customers/show";
    }
}
