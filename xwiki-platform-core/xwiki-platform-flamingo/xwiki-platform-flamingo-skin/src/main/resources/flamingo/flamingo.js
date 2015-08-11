require(['jquery', 'bootstrap'], function($) {
  // Bugfix: TODO: remove this when https://github.com/twbs/bootstrap/issues/16968 is fixed
  $('.dropdown').on('shown.bs.dropdown', function () {
    var menu = $(this).find('.dropdown-menu');
    var menuLeft = menu.offset().left;
    var menuWidth = menu.outerWidth();
    var documentWidth = $(body).outerWidth();
    if (menuLeft + menuWidth > documentWidth) {
      menu.offset({'left': documentWidth - menuWidth});
    }
  })
});
