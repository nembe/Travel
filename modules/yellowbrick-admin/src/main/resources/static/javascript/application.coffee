$ ->
  $('button.close').click ->
    target = $(this).attr("data-target")
    $(target).hide('slow')
