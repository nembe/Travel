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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customers/{id}")
public class CustomerDetailsController {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Autowired CustomerDao customerDao;
    @Autowired TransponderCardDao transponderCardDao;
    @Autowired CardOrderDao cardOrderDao;

    @ModelAttribute("dateFormat")
    private Function<Date, String> dateFormat() {
        return date -> date != null ? DATE_FORMAT.format(date) : "";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String show(ModelMap model, @PathVariable("id") Long id) {
        Customer customer = customerDao.findById(id).orElseThrow(ResourceNotFoundException::new);

        List<CardListItem> cards = transponderCardDao.findByCustomerId(customer.getCustomerId())
                .stream()
                .map(card -> {
                    return card.getOrderId() != null
                            ? new CardListItem(card, cardOrderDao.findById(card.getOrderId()))
                            : new CardListItem(card);
                })
                .collect(Collectors.toList());

        List<String> mobileNumbers = customerDao.getMobileNumbers(id);

        model.put("cust", customer);
        model.put("cards", cards);
        model.put("mobiles", mobileNumbers);

        return "customers/show";
    }

    private class CardListItem {

        public final String cardNumber;
        public final boolean exported;

        private CardListItem(TransponderCard card, Optional<CardOrder> order) {
            this.cardNumber = card.getCardNumber();
            this.exported = order.map(CardOrder::isExport).orElse(true);
        }

        private CardListItem(TransponderCard card) {
            this(card, Optional.empty());
        }
    }
}
