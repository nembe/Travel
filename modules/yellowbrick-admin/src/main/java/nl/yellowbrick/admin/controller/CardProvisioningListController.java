package nl.yellowbrick.admin.controller;

import com.google.common.collect.Lists;
import nl.yellowbrick.activation.service.CardOrderValidationService;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;

@Controller
@RequestMapping("/provisioning/cards")
public class CardProvisioningListController {

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CardOrderValidationService validationService;

    @RequestMapping(method = RequestMethod.GET)
    public String pendingValidation(Model model) {
        List<CardOrderListItem> flaggedOrders = Lists.newArrayList();
        List<CardOrderListItem> otherOrders = Lists.newArrayList();

        cardOrderDao.findByStatus(INSERTED).forEach(order -> {
            CardOrderListItem orderItem = toListItem(order);

            if(!order.getCardType().equals(CardType.TRANSPONDER_CARD))
                otherOrders.add(orderItem);
            else if(validationService.validate(order).hasErrors())
                flaggedOrders.add(orderItem);
            else
                otherOrders.add(orderItem);
        });

        model.addAttribute("flaggedOrders", flaggedOrders);
        model.addAttribute("otherOrders", otherOrders);

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

        public final CardOrder order;
        public final Customer customer;

        private CardOrderListItem(CardOrder cardOrder, Customer customer) {
            this.order = cardOrder;
            this.customer = customer;
        }
    }
}
