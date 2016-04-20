// --- CUSTOMERS.COFFEE ---

// $ ->
//   $('input[name=resetBtn]').click (e) ->
//     e.preventDefault()
//     $(this.form).find("input[type=text]").val("")

// --- CUSTOMERS.COFFEE ---

$(function() {
  return $('input[name=resetBtn]').click(function(e) {
    e.preventDefault();
    return $(this.form).find("input[type=text]").val("");
  });
});