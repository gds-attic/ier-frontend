window.GOVUK = window.GOVUK || {};

(function () {
  "use strict"
  var root = this,
      $ = root.jQuery;

  var toggleContent,
      toggleHelp,
      optionalInformation,
      conditionalControl,
      makeToggle;

  toggleContent = {
    toggle : function () {
      if (this.$content.css("display") === "none") {
        this.$content.show();
        this.$toggle.removeClass("toggle-closed");
        this.$toggle.addClass("toggle-open");
      } else {
        this.$content.hide();
        this.$toggle.removeClass("toggle-open");
        this.$toggle.addClass("toggle-closed");
      }
    },
    setup : function () {
      this.$heading = this.$content.find("h1,h2,h3,h4").first();
      this.$toggle = $('<a href="#" class="toggle">' + this.$heading.text() + '</a>');
      this.$toggle.insertBefore(this.$content);
    },
    bindEvents : function () {
      var inst = this;

      this.$toggle.on('click', function () {
        inst.toggle();
        return false;
      });
    }
  };

  makeToggle = function () {
    var method,
        func = function (elm) {
          this.$content = $(elm);
          this.setup();
          this.bindEvents();
        };

    for (method in toggleContent) {
      func.prototype[method] = toggleContent[method];
    }
    return func;
  };

  toggleHelp = makeToggle();
  toggleHelp.prototype.setup = function () {
    this.$heading = this.$content.find("h1,h2,h3,h4").first();
    this.$toggle = $('<a href="#" class="toggle toggle-closed" title="show or hide help">' + this.$heading.text() + '</a>');
    this.$toggle.insertBefore(this.$content);
    this.$heading.addClass("visuallyhidden");
  };

  optionalInformation = makeToggle();
  optionalInformation.prototype.setup = function () {
    this.$heading = this.$content.find("h1,h2,h3,h4").first();
    this.$toggle = $('<a href="#" class="toggle toggle-closed">' + this.$heading.text() + '</a>');
    this.$toggle.insertBefore(this.$content);
    this.$heading.remove();
  };

  conditionalControl = makeToggle();
  conditionalControl.prototype.setup = function () {
    this.$toggle = $(document.getElementById(this.$content.data('condition')));
  }; 
  conditionalControl.prototype.bindEvents = function () {
    var inst = this;

    this.$toggle.on('change', function () {
      inst.toggle();
    });

    if (this.$toggle.is(":checked")) {
      this.$toggle.trigger("change");
    }
  };
  conditionalControl.prototype.toggle = function () {
    if (this.$toggle.is(":checked")) {
      this.$content.show();
    } else {
      this.$content.hide();
    }
  };

  GOVUK.registerToVote = {
    "toggleHelp" : toggleHelp,
    "optionalInformation" : optionalInformation,
    "conditionalControl" : conditionalControl
  };

  $(document).on('ready', function () { 
    $('.help-content').each(function (idx, elm) {
      new GOVUK.registerToVote.toggleHelp(elm);
    });
    $('.optional-section').each(function (idx, elm) {
      if ($(elm).data('condition') !== undefined) {
        new GOVUK.registerToVote.conditionalControl(elm);
      } else {
        new GOVUK.registerToVote.optionalInformation(elm);
      }
    });
  });
}.call(this));

$('body').on('change', 'input[type="checkbox"]', function(e){
    var $this = $(this);
    $this.closest('label').toggleClass('selected', $this.is(':checked'));
});

$('body').on('change', 'input[type="radio"]', function(e){
    var $this = $(this);
    var $step = $this.closest('.step');
    $step.find('input[type="radio"][name="' + $this.attr('name') + '"]').closest('label').removeClass('selected');
    $this.closest('label').toggleClass('selected', $this.is(':checked'));
});

$('.back').on('click', function(e){
    e.preventDefault();
    window.history.back();
});

$('#step-nationality #no-nationality-link').on('click', function(e) {
    e.preventDefault();
    show($('#optional-section-no-nationality'));
});

$('#step-previous-name #input-name-change-yes').on('click', function(e) {
    show($('#optionalSectionPreviousName'));
});

$('#step-previous-name #input-name-change-no').on('click', function(e) {
    hide($('#optionalSectionPreviousName'));
});

$('#step-address #find-address').on('click', function(e) {
    e.preventDefault();
    addressLookup($('#step-address'));
});

$('#step-previous-address #input-previous-address_false').on('click', function(e) {
    hide($('#optional-section-previous-address'));
});

$('#step-previous-address #input-previous-address_true').on('click', function(e) {
    show($('#optional-section-previous-address'));
});

$('#step-previous-address #find-previous-address').on('click', function(e) {
    e.preventDefault();
    addressLookup($('#step-previous-address'));
});

$('#step-other-address #input-other-address-no').on('click', function(e) {
    hide($('#optional-section-other-address'));
});

$('#step-other-address #input-other-address-yes').on('click', function(e) {
    show($('#optional-section-other-address'));
});

$('#step-other-address #find-other-address').on('click', function(e) {
    e.preventDefault();
    addressLookup($('#step-other-address'));
});

$('#step-contact #contact-post').on('click', function(e) {
    show($('#optional-section-contact-post'));
    hide($('#optional-section-contact-phone'));
    hide($('#optional-section-contact-email'));
    hide($('#optional-section-contact-text'));
})

$('#step-contact #contact-phone').on('click', function(e) {
    show($('#optional-section-contact-phone'));
    hide($('#optional-section-contact-email'));
    hide($('#optional-section-contact-text'));
    hide($('#optional-section-contact-post'));
})

$('#step-contact #contact-email').on('click', function(e) {
    hide($('#optional-section-contact-phone'));
    show($('#optional-section-contact-email'));
    hide($('#optional-section-contact-text'));
    hide($('#optional-section-contact-post'));
})

$('#step-contact #contact-text').on('click', function(e) {
    hide($('#optional-section-contact-phone'));
    hide($('#optional-section-contact-email'));
    show($('#optional-section-contact-text'));
    hide($('#optional-section-contact-post'));
})

function show($block) {
    $block.addClass('visible');
    $block.removeClass('hidden');
}

function hide($block) {
    $block.addClass('hidden');
    $block.removeClass('visible');
}

function addressLookup($step) {

    var postcode = $step.find('.postcode').val();
    if(!postcode || postcode == ''){
        console.log('no postcode entered - cannot run addressLookup');
        return;
    }

    $.ajax({
        url : '/address/' + postcode.replace(/\s/g,'')
    }).done(function(data){

            $step.find('.address-step-2').show();
            $step.find('.postcode-search-results').show();
            $step.find('.type-in-address').hide();

            var addressesHTML = '<option>Select your address</option><option>';
            var addresses = [];

            if (data.addresses[0].addressLine){
                for (var i = 0; i < data.addresses.length; i++){
                    addresses.push(data.addresses[i].addressLine);
                }
            } else {
                addresses = data.addresses;
            }

            addressesHTML += addresses.join('</option><option>') + '</option>';
            $step.find('.addressList').html(addressesHTML);
        });
}
