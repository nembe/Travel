package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;

@Controller
@RequestMapping("/provisioning/cards")
public class CardProvisioningListController {

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private CustomerDao customerDao;

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        List<CardOrderListItem> orderList = cardOrderDao.findByStatus(INSERTED)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());

        model.addAttribute("orders", orderList);

        return "provisioning/cards/index";
    }

    private Customer customerForOrder(CardOrder order) {
        return customerDao.findById(order.getCustomerId()).orElseThrow(() -> {
            return new InconsistentDataException(String.format(
                    "Couldn't find customer id %s for card %s", order.getCustomerId(), order.getId()
            ));
        });
    }

    private CardOrderListItem toListItem(CardOrder cardOrder) {
        return new CardOrderListItem(cardOrder, customerForOrder(cardOrder));
    }

    private class CardOrderListItem {

        private final CardOrder order;
        private final Customer customer;

        private CardOrderListItem(CardOrder cardOrder, Customer customer) {
            this.order = cardOrder;
            this.customer = customer;
        }

        public CardOrder getOrder() {
            return order;
        }

        public Customer getCustomer() {
            return customer;
        }
    }
}
