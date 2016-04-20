// -- CARDS.COFFEE --

// $ ->
//   $("input[name='deleteCardOrder']").click (e) ->
//     e.preventDefault() unless window.confirm("Are you sure you want to delete this order?")

// -- CARDS.COFFEE --

$(function() {
  return $("input[name='deleteCardOrder']").click(function(e) {
    if (!window.confirm("Are you sure you want to delete this order?")) {
      return e.preventDefault();
    }
  });
});