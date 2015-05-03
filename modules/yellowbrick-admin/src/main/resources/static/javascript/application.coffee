$ ->
  $('button.close').click ->
    target = $(this).attr("data-target")
    $(target).hide('slow')

  $('.menu-back-button').click (e) ->
    e.stopPropagation()
    $(this).parent('.side-submenu').removeClass('active')

  $('#side-menu a.menu-fw').click (e) ->
    e.stopPropagation()
    $(this).siblings('.side-submenu').addClass('active')
