$ ->
  $("input[name='deleteCardOrder']").click (e) ->
    e.preventDefault() unless window.confirm("Are you sure you want to delete this order?")
