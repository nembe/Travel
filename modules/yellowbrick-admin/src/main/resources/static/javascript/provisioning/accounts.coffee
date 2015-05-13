togglePoBox = -> $('#billingAddressPoBox').parent().toggleClass('hidden')
toggleBillingAddress = -> $('.billingAddress').toggleClass('hidden')

$ ->
  if $("input[name='billingAddressSameAsMailingAddress']:checked").val() == "true"
    toggleBillingAddress()

  if $("input[name='billingAddressIsPoBox']:checked").val() == "false"
    togglePoBox()

  $("input[name='billingAddressSameAsMailingAddress']").change -> toggleBillingAddress()
  $("input[name='billingAddressIsPoBox']").change -> togglePoBox()

  $('input[name=deleteAccount]').click (e) ->
    e.preventDefault() unless confirm("Are you sure you want to delete the user?")
