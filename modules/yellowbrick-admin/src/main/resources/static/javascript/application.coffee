$ ->
  $('button.close').click ->
    target = $(this).attr("data-target")
    $(target).hide('slow')

  $('#side-menu > ul > li').click ->
    $(this).children('ul').addClass('active')

  $('.menu-back-button').click (e) ->
    e.stopPropagation()
    $(this).parent('.side-submenu').removeClass('active')
