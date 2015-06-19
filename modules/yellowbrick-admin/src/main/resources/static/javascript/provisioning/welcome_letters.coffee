$ ->
  # disable form submit button once pressed
  $('#welcome-letters-export form').submit (e) ->
    $(this).find('input[type=submit]').prop('readonly', yes)
