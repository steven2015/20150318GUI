/*
 * declare namespace
 */
var std = std || {};
/*
 * basic
 */
std.find = function(p){
	if(p instanceof jQuery){
		return p;
	}
	var j = null;
	j = jQuery(p);
	if(j == null || j.length == 0){
		try{
			j = jQuery('[id$="' + p + '"]');
		}catch(err){
		}
	}
	if(j == null || j.length == 0){
		try{
			j = jQuery('[id$="' + p + ':0"]');
		}catch(err){
		}
	}
	if(j.length == 0){
		j = null;
	}
	return j;
}

std.focus = function(p){
	if(typeof p == 'string'){
		var j = std.find(p);
		if(j != null && std.isFocusable(j)){
			j.focus().select();
			return true;
		}
	}else if(typeof p == 'object' && p instanceof Array){
		for( var i in p){
			if(std.focus(p[i])){
				return true;
			}
		}
	}else if(p instanceof jQuery){
		if(std.isFocusable(p)){
			j.focus().select();
			return true;
		}
	}
	return false;
};
std.isFocusable = function(p){
	p = std.find(p).first();
	if(p.length > 0){
		var tagName = p.prop('tagName');
		var display = p.css('display');
		var filter = (tagName == 'A') || ((tagName == 'TEXTAREA' || tagName == 'INPUT' || tagName == 'SELECT' || tagName == 'BUTTON') && (display == 'block' || display == 'inline-block'));
		filter = filter && p.prop('type') != 'hidden' && p.prop('disabled') != true && p.prop('readonly') != true;
		filter = filter && p.css('visibility') != 'hidden';
		if(filter){
			var j = p.parents();
			for( var i = 0; i < j.length; i++){
				var k = jQuery(j[i]);
				if(k.css('display') == 'none' || k.css('visibility') == 'hidden'){
					return false;
				}
			}
			return true;
		}
	}
	return false;
};
std.callFunction = function(name){
	if(typeof name == 'function'){
		return name();
	}else if(typeof name == 'string' && eval('typeof ' + name) == 'function'){
		return eval(name + '()');
	}
	return false;
};
/*
 * std.event
 */
std.event = std.event || {};
std.event.normalize = function(event){
	event = event || window.event;
	event.target = event.target || event.currentTarget || event.srcElement;
	event.which = event.which || event.keyCode;
	return event;
};
std.event.focusOnEnter = function(event, target){
	std.event.normalize(event);
	if(event.which == 13){
		jQuery(event.target).blur();
		std.find(target).focus();
	}
};
std.event.clickOnEnter = function(event, target){
	std.event.normalize(event);
	if(event.which == 13){
		jQuery(event.target).blur();
		std.find(target).focus().click();
	}
};
std.event.executeOnEnter = function(event, _this, action){
	std.event.normalize(event);
	if(event.which == 13){
		eval(action);
	}
};
/*
 * std.jsf
 */
std.jsf = std.jsf || {};
std.jsf.addExtraFeatures = function(){
	// jQuery 1.3.2 not support .live() on focus or blur
	jQuery('input[type="text"].std-uppercase').removeClass('std-uppercase').removeClass('std-uppercase-added').addClass('std-uppercase-added').bind('blur', function(){
		jQuery(this).val(jQuery(this).val().toUpperCase());
	});
	jQuery('input[type="text"].std-select-on-focus').removeClass('std-select-on-focus').removeClass('std-select-on-focus-added').addClass('std-select-on-focus-added').bind('focus', function(){
		jQuery(this).select();
	});
	jQuery('input[type="text"].std-clear-if-zero-on-focus').removeClass('std-clear-if-zero-on-focus').removeClass('std-clear-if-zero-on-focus-added').addClass('std-clear-if-zero-on-focus-added').bind('focus', function(){
		var joThis = jQuery(this);
		if(jQuery.trim(joThis.val()) == '0'){
			joThis.val('');
		}
	});
	jQuery('input[type="text"].std-zero-if-blank-on-blur').removeClass('std-zero-if-blank-on-blur').removeClass('std-zero-if-blank-on-blur-added').addClass('std-zero-if-blank-on-blur-added').bind('blur', function(){
		var joThis = jQuery(this);
		if(jQuery.trim(joThis.val()).length == 0){
			joThis.val('0');
		}
	});
};
std.jsf.disableBackspace = function(){
	jQuery('body').keydown(function(event){
		std.event.normalize(event);
		if(event.which == 8){
			var j = std.find(event.target);
			var backspaceAllowed = ((j.prop('tagName') == 'TEXTAREA' || (j.prop('tagName') == 'INPUT' && (j.prop('type') == 'text' || j.prop('type') == 'password'))) && j.prop('disabled') == false && j.prop('readonly') == false && j.css('display') != 'none' && j.css('visibility') != 'hidden');
			if(backspaceAllowed == false){
				stopEvent(event);
			}
		}
	});
};
std.jsf.enableExtraFeatures = function(){
	jQuery('body').on('blur', 'input[type="text"].std-uppercase', function(event){
		jQuery(this).val(jQuery(this).val().toUpperCase());
	});
	jQuery('body').on('keypress', 'input[type="checkbox"],input[type="button"],input[type="submit"]', function(event){
		std.event.normalize(event);
		if(event.which == 13){
			jQuery(this).click();
			event.preventDefault();
		}
	});
};