var exprArgs = [
	'maxval',
	'minval',
	'time',
	'duration',
	
	'maxlowval',
	'minlowval',
	'maxhighval',
	'minhighval',
	
	'csize',
	
	// Section type specific
	'imgw',
	'imgh',
	'imgr',
	
	'songtitle',
	'prettytime',
	'prettyduration'
];

function ExpressionProperty(v) {
	if(!v) v = 0;
	
	if(v instanceof ExpressionProperty) {
		v = v.expr;
	}
	
	var expr = v.toString();
	var gtr = new Function(exprArgs.join(','), 'return (' + expr + ')');
	var constantVal = 0;
	
	this.refresh = function(frameProps) {
		return constantVal = gtr(
			frameProps.maxval,
			frameProps.minval,
			frameProps.time,
			frameProps.duration,
			
			frameProps.maxlowval,
			frameProps.minlowval,
			frameProps.maxhighval,
			frameProps.minhighval,
			
			frameProps.csize,
			
			frameProps.imgw,
			frameProps.imgh,
			frameProps.imgr,
			
			frameProps.songtitle,
			frameProps.prettytime,
			frameProps.prettyduration) || 0;
	};
	
	var that = this;
	Object.defineProperty(this, 'value', {
		get: function() {
			return constantVal;
		}
	});
	
	Object.defineProperty(this, 'expr', {
		get: function() {
			return expr;
		},
		
		set: function(val) {
			if(val === '') val = '0';
			
			expr = val;
			
			gtr = new Function(exprArgs.join(','), 'return (' + expr + ')');
		}
	});
}

// ---
var exprStack = {};
var exprResults = {};

function compileExpr(id, expr) {
	var ep = exprStack[id];
	
	if(isNullOrUndef(ep)) exprStack[id] = new ExpressionProperty(expr);
	else exprStack[id].expr = expr;
}

function disposeExpr(id) {
	delete exprStack[id];
	delete exprResults[id];
}

function setExpr(id, expr) {
	var e = exprStack[id];
	
	if(e) e.expr = expr;
	else exprStack[id] = new ExpressionProperty(expr);
}

function recalcAllExpr(fprops) {
	for(var x in exprStack) {
		exprResults[x] = exprStack[x].refresh(fprops);
	}
}

function recalcExpr(mapExprProp) {
	for(var x in mapExprProp) {
		var expr = exprStack[x];
		if(expr) exprResults[x] = expr.refresh(mapExprProp[x]);
	}
}
