package nl.yellowbrick.admin.controller;

import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.admin.form.CardOrderValidationForm;
import nl.yellowbrick.admin.util.MessageHelper;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.CardType;
import nl.yellowbrick.data.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static nl.yellowbrick.data.domain.CardOrderStatus.INSERTED;

@Controller
@RequestMapping("/provisioning/cards/{id}")
public class CardProvisioningFormController {

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CardAssignmentService cardAssignmentService;

    @RequestMapping(method = RequestMethod.GET)
    public String showValidationForm(ModelMap model,
                                     @PathVariable("id") int id,
                                     RedirectAttributes ra) {
        CardOrder cardOrder = order(id);

        if(!cardOrder.getStatus().equals(INSERTED)) {
            MessageHelper.flashWarning(ra, "cardorder.already.processed", cardOrder.getStatus().name());

            return "redirect:/provisioning/cards";
        }

        model.addAttribute("order", cardOrder);
        model.addAttribute("customer", customerForOrder(cardOrder));

        if(!model.containsAttribute("form"))
            model.addAttribute("form", new CardOrderValidationForm(cardOrder));

        return "provisioning/cards/validate_order";
    }

    @RequestMapping(method = RequestMethod.POST, params = {"validateCardOrder"})
    public String validateCardOrder(@PathVariable("id") int id,
                                    @ModelAttribute("form") CardOrderValidationForm form,
                                    BindingResult bindingResult,
                                    ModelMap model,
                                    RedirectAttributes ra) {

        if(bindingResult.hasErrors())
            return showValidationForm(model, id, ra);

        CardOrder order = order(id);
        order.setPricePerCard(form.getPricePerCardCents());

        cardOrderDao.validateCardOrder(order);

        if(order.getCardType().equals(CardType.TRANSPONDER_CARD))
            cardAssignmentService.assignTransponderCard(order);

        model.clear();
        MessageHelper.flash(ra, "cardorder.validated");
        return "redirect:/provisioning/cards";
    }

    @RequestMapping(method = RequestMethod.POST, params = {"deleteCardOrder"})
    public String deleteCardOrder(@PathVariable("id") int id, RedirectAttributes ra) {
        cardOrderDao.delete(id);

        MessageHelper.flash(ra, "cardorder.deleted");
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
}
