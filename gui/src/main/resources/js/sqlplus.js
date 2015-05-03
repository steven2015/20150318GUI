var webclient = webclient || {};
webclient.sqlplus = webclient.sqlplus || {};

webclient.sqlplus.websocket = new WebSocket("ws://localhost/sqlplus");
webclient.sqlplus.websocket.onopen = function(event){
	webclient.sqlplus.displayText("Connected to server.");
};
webclient.sqlplus.websocket.onclose = function(event){
	webclient.sqlplus.displayText("Disconnected. (code=" + event.code + ", reason=\"" + event.reason + "\")");
};
webclient.sqlplus.websocket.onmessage = function(event){
	$('.webclient:first').append("<div>onmessage</div>");
	console.log('onmessage');
	console.log(event);
};
webclient.sqlplus.websocket.onerror = function(event){
	$('.webclient:first').append("<div>onerror</div>");
	console.log('onerror');
	console.log(event);
};

webclient.sqlplus.pressedEnter = function(event){
	event.preventDefault();
	this.websocket.send(JSON.stringify({
		i : webclient.getInput()
	}));
	webclient.clearInput();
};
webclient.sqlplus.getTimestampString = function(){
	var padZero = function(i){
		if(i < 10){
			return '0' + String(i);
		}else{
			return String(i);
		}
	};
	var now = new Date();
	return padZero(now.getHours()) + ":" + padZero(now.getMinutes()) + ":" + padZero(now.getSeconds());
};
webclient.sqlplus.displayText = function(text){
	webclient.jQueryElement.append("<div><div class='timestamp'>" + this.getTimestampString() + "</div><div class='output'>" + text + "</div></div>");
};
webclient.sqlplus.displayMoreText = function(text){
	webclient.jQueryElement.append("<div><div class='timestamp'></div><div class='output'>" + text + "</div></div>");
};