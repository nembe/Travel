// --- WELCOME_LETTERS.COFFEE ---

// $ ->
//   # disable form submit button once pressed
//   $('#welcome-letters-export form').submit (e) ->
//     $(this).find('input[type=submit]').prop('readonly', yes)

// --- WELCOME_LETTERS.COFFEE ---

$(function() {
  return $('#welcome-letters-export form').submit(function(e) {
    return $(this).find('input[type=submit]').prop('readonly', true);
  });
});