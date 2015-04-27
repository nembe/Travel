package nl.yellowbrick.admin.controller;

import nl.yellowbrick.admin.exceptions.ResourceNotFoundException;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.TransponderCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customers/{id}")
public class CustomerDetailsController {

    @Autowired CustomerDao customerDao;
    @Autowired TransponderCardDao transponderCardDao;
    @Autowired CardOrderDao cardOrderDao;

    @RequestMapping(method = RequestMethod.GET)
    public String show(ModelMap model, @PathVariable("id") Long id) {
        Customer customer = customerDao.findById(id).orElseThrow(ResourceNotFoundException::new);

        List<CardListItem> cards = transponderCardDao.findByCustomerId(customer.getCustomerId())
                .stream()
                .map(card -> new CardListItem(card, cardOrderDao.findById(card.getOrderId())))
                .collect(Collectors.toList());

        model.put("cust", customer);
        model.put("cards", cards);

        return "customers/show";
    }

    private class CardListItem {

        public final String cardNumber;
        public final boolean exported;

        private CardListItem(TransponderCard card, Optional<CardOrder> order) {
            this.cardNumber = card.getCardNumber();
            this.exported = order.map(CardOrder::isExport).orElse(false);
        }
    }
}
