var webclient = webclient || {};

$(function(){
	webclient.jQueryElement = $('.webclient:first');
	webclient.inputElement = webclient.jQueryElement.find('.input:first');
});

webclient.getInput = function(){
	var i = '';
	var handleContents = function(contents){
		$.each(contents, function(index, element){
			if(element.nodeType == 3){
				i += element.textContent;
			}else if(element.nodeType == 1){
				if(element.tagName == 'DIV'){
					i += '\n';
					handleContents(element.childNodes);
				}
			}
		});
	}
	handleContents(webclient.inputElement.contents());
	return i;
};
webclient.clearInput = function(){
	webclient.inputElement.html('');
};