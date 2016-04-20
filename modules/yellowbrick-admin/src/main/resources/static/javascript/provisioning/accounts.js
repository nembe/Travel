 // --- ACCOUNTS.COFFEE ---

 // togglePoBox = -> $('#billingAddressPoBox').parent().toggleClass('hidden')
 // toggleBillingAddress = -> $('.billingAddress').toggleClass('hidden')

 // $ ->
 //   if $("input[name='billingAddressSameAsMailingAddress']:checked").val() == "true"
 //     toggleBillingAddress()

 //   if $("input[name='billingAddressIsPoBox']:checked").val() == "false"
 //     togglePoBox()

 //   $("input[name='billingAddressSameAsMailingAddress']").change -> toggleBillingAddress()
 //   $("input[name='billingAddressIsPoBox']").change -> togglePoBox()

 //   $('input[name=deleteAccount]').click (e) ->
 //     e.preventDefault() unless confirm("Are you sure you want to delete the user?")

 //   $('#validate-personal-account form, #validate-business-account form').submit (e) ->
 //     $(this).find('input[type=submit]').prop('readonly', yes)

 // --- ACCOUNTS.COFFEE ---

var toggleBillingAddress, togglePoBox;
togglePoBox = function() {
  return $('#billingAddressPoBox').parent().toggleClass('hidden');
};
toggleBillingAddress = function() {
  return $('.billingAddress').toggleClass('hidden');
};
$(function() {
  if ($("input[name='billingAddressSameAsMailingAddress']:checked").val() === "true") {
    toggleBillingAddress();
  }
  if ($("input[name='billingAddressIsPoBox']:checked").val() === "false") {
    togglePoBox();
  }
  $("input[name='billingAddressSameAsMailingAddress']").change(function() {
    return toggleBillingAddress();
  });
  $("input[name='billingAddressIsPoBox']").change(function() {
    return togglePoBox();
  });
  $('input[name=deleteAccount]').click(function(e) {
    if (!confirm("Are you sure you want to delete the user?")) {
      return e.preventDefault();
    }
  });
  return $('#validate-personal-account form, #validate-business-account form').submit(function(e) {
    return $(this).find('input[type=submit]').prop('readonly', true);
  });
});