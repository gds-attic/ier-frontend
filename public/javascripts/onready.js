(function(){"use strict";var root=this,$=root.jQuery,GOVUK=root.GOVUK;GOVUK.performance.stageprompt.setupForGoogleAnalytics(),$("header.no-back-link").each(function(idx,elm){new GOVUK.registerToVote.BackButton(elm)}),$(".optional-section, .optional-section-binary").each(function(idx,elm){var toggleClass="optional-section",$elm=$(elm);void 0!==$elm.data("controlText")?new GOVUK.registerToVote.OptionalControl(elm,toggleClass):void 0!==$elm.data("condition")?new GOVUK.registerToVote.ConditionalControl(elm,toggleClass):new GOVUK.registerToVote.OptionalInformation(elm,toggleClass)}),$("#add-countries").each(function(idx,elm){new GOVUK.registerToVote.OtherCountryFields(elm)}),$(".selectable").each(function(idx,elm){{var $label=$(elm),$control=$label.find("input[type=radio], input[type=checkbox]");$control.attr("name")}"radio"===$control.attr("type")&&GOVUK.registerToVote.monitorRadios($control[0]),new GOVUK.registerToVote.MarkSelected(elm),$control.on("focus",function(){$(this).parent("label").addClass("selectable-focus")}),$control.on("blur",function(){$(this).parent("label").removeClass("selectable-focus")})}),$(".country-autocomplete").each(function(idx,elm){GOVUK.registerToVote.autocompletes.add($(elm))}),$.each(["initialized","opened","closed","movedto","updated"],function(idx,evt){$(document).bind("typeahead:"+evt,function(){var autocompleteEvent=GOVUK.registerToVote.autocompletes.createEvent(evt);autocompleteEvent.trigger.apply(GOVUK.registerToVote.autocompletes,arguments)})}),$(document).bind("contentUpdate",function(e,data){var context=data.context;$(".country-autocomplete",context).each(function(idx,elm){GOVUK.registerToVote.autocompletes.add($(elm))})}),$(document).bind("preContentRemoval",function(e,data){var context=data.context;$(".country-autocomplete",context).each(function(idx,elm){GOVUK.registerToVote.autocompletes.remove($(elm))})}),GOVUK.registerToVote.validation.init()}).call(this);