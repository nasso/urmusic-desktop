function isNullOrUndef(v) { return (v === null || v === undefined); }

var lerp = function(a, b, x) {
	return a + x * (b - a);
}

var clamp = function(x, a, b) {
	return Math.max(Math.min(x, b), a);
}

var smoothrand = (function() {
	var randset = [];
	
	var flr = 0;
	var ceil = 0;
	
	return function(i) {
		i = i < 0 ? -i : i;
		
		flr = i | 0;
		ceil = (i+1) | 0;
		
		if(isNullOrUndef(randset[flr])) randset[flr] = Math.random();
		if(isNullOrUndef(randset[ceil])) randset[ceil] = Math.random();
		
		return lerp(randset[flr], randset[ceil], Math.cos((i - flr) * -1 * Math.PI) * -0.5 + 0.5);
	};
})();

var rand = Math.random;
var max = Math.max;
var min = Math.min;
var floor = Math.floor;
var ceil = Math.ceil;
var cos = Math.cos;
var sin = Math.sin;
var tan = Math.tan;
var acos = Math.acos;
var asin = Math.asin;
var atan = Math.atan;
var pow = Math.pow;
var pi = Math.PI;
