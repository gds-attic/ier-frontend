(function(){"use strict";function _hideContent(){$target.removeClass(_toggleClass),$target.attr({"aria-hidden":!0,"aria-expanded":!1})}function _showContent(){$target.addClass(_toggleClass),$target.attr({"aria-hidden":!1,"aria-expanded":!0})}var root=this,$=root.jQuery,$selectedRadioInput=$(".selectable.selected>input"),selectedRadioInput=$selectedRadioInput.length>0?$selectedRadioInput[0]:null,$target=$(".local-fix-target"),$source=$(".local-fix-source"),_toggleClass="optional-section-open",_inclusionListIds=["previousName_hasPreviousNameOption_true","overseasParentName_parentPreviousName_hasPreviousNameOption_true","overseasParentName_parentPreviousName_hasPreviousNameOption_other"];$source.on("change",function(event){for(var targetId=event.target.id,show=!1,i=0,max=_inclusionListIds.length;max>i;i++)if(_inclusionListIds[i]===targetId){show=!0;break}show?_showContent():_hideContent()}),null!==selectedRadioInput&&(selectedRadioInput.checked=!0,$selectedRadioInput.trigger("change"))}).call(this);