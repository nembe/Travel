// --- APPLICATION.COFFEE ---

// $ ->
//   $('button.close').click ->
//     target = $(this).attr("data-target")
//     $(target).hide('slow')

//   $('.menu-back-button').click (e) ->
//     e.stopPropagation()
//     $(this).parent('.side-submenu').removeClass('active')

//   $('#side-menu a.menu-fw').click (e) ->
//     e.stopPropagation()
//     $(this).siblings('.side-submenu').addClass('active')

// --- APPLICATION.COFFEE ---

$(function() {
  $('button.close').click(function() {
    var target;
    target = $(this).attr("data-target");
    return $(target).hide('slow');
  });
  $('.menu-back-button').click(function(e) {
    e.stopPropagation();
    return $(this).parent('.side-submenu').removeClass('active');
  });
  return $('#side-menu a.menu-fw').click(function(e) {
    e.stopPropagation();
    return $(this).siblings('.side-submenu').addClass('active');
  });
});