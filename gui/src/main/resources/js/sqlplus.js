webclient.sqlplus = webclient.sqlplus || {};

webclient.sqlplus.send = function(data){
	$.ajax({
		url : "/sqlplus/ajax",
		method : "POST",
		data : data
	});
}

$(function(){
	webclient.sqlplus.send("{hello:123}");
});