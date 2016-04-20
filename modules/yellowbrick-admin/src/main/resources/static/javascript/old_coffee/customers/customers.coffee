$ ->
  $('input[name=resetBtn]').click (e) ->
    e.preventDefault()
    $(this.form).find("input[type=text]").val("")
