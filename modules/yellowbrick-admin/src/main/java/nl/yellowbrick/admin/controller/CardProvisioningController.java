package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.form.CardOrderValidationForm;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;

@Controller
@RequestMapping("/provisioning/cards")
public class CardProvisioningController {

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

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public String showValidationForm(ModelMap model, @PathVariable("id") int id) {
        CardOrder cardOrder = order(id);

        if(!cardOrder.getStatus().equals(INSERTED))
            throw new InconsistentDataException(String.format(
                    "Expected card order %s to be %s but is %s",
                    cardOrder.getId(), INSERTED, cardOrder.getStatus().name()
            ));

        model.addAttribute("order", cardOrder);
        model.addAttribute("customer", customerForOrder(cardOrder));

        if(!model.containsAttribute("form"))
            model.addAttribute("form", new CardOrderValidationForm(cardOrder));

        return "provisioning/cards/validate_order";
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}", params = {"validateCardOrder"})
    public String validateCardOrder(@PathVariable("id") int id,
                                    @ModelAttribute("form") CardOrderValidationForm form,
                                    BindingResult bindingResult,
                                    ModelMap model) {

        if(bindingResult.hasErrors())
            return showValidationForm(model, id);

        CardOrder order = order(id);
        order.setPricePerCard(form.getPricePerCardCents());
        order.setSurcharge(form.getSurchargeCents());

        cardOrderDao.validateCardOrder(order);

        model.clear();
        return "redirect:/provisioning/cards";
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}", params = {"deleteCardOrder"})
    public String deleteCardOrder(@PathVariable("id") int id) {
        cardOrderDao.delete(id);

        return "redirect:/provisioning/cards";
    }

    private Customer customerForOrder(CardOrder order) {
        return customerDao.findById(order.getCustomerId()).orElseThrow(() -> {
            return new InconsistentDataException(String.format(
                    "Couldn't find customer id %s for card %s", order.getCustomerId(), order.getId()
            ));
        });
    }

    private CardOrder order(long orderId) {
        return cardOrderDao.findById(orderId).orElseThrow(ResourceNotFoundException::new);
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
