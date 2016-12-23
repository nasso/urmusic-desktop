package io.github.nasso.urmusic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;

public class Utils {
	public static final double SQRT_2 = Math.sqrt(2.0);
	
	private Utils() {
	}
	
	private static Map<Integer, float[]> blackmanWindows = new HashMap<Integer, float[]>();
	
	public static void applyBlackmanWindow(float[] buffer, int length) {
		float[] win;
		if(blackmanWindows.containsKey(length)) {
			win = blackmanWindows.get(length);
			
			for(int i = 0; i < length; ++i)
				buffer[i] = buffer[i] * win[i];
		} else {
			win = new float[length];
			float factor = (float) (Math.PI / (length - 1));
			
			for(int i = 0; i < length; ++i)
				buffer[i] = buffer[i] * (win[i] = (float) (0.42 - (0.5 * Math.cos(2 * factor * i)) + (0.08 * Math.cos(4 * factor * i))));
			
			blackmanWindows.put(length, win);
		}
	}
	
	public static float addressArray(float[] array, int i, float outValue) {
		if(i < 0 || i >= array.length) {
			return outValue;
		} else {
			return array[i];
		}
	}
	
	public static float quadCurve(float p0y, float cpy, float p1y, float t) {
		return (1.0f - t) * (1.0f - t) * p0y + 2.0f * (1.0f - t) * t * cpy + t * t * p1y;
	}
	
	public static float getValue(float[] array, float index, boolean quadInterpolation, float minValue) {
		if(quadInterpolation) {
			int rdn = (int) Math.floor(index + 0.5);
			
			// @format:off
			return quadCurve(
					lerp(
							addressArray(array, rdn - 1, minValue),
							addressArray(array, rdn, minValue),
							0.5f),
						addressArray(array, rdn, minValue),
						lerp(addressArray(array, rdn, minValue),
							addressArray(array, rdn + 1, minValue),
							0.5f),
						index - rdn + 0.5f);
			// @format:on
		} else {
			int flr = (int) Math.floor(index);
			int cel = (int) Math.ceil(index);
			
			float flrv = addressArray(array, flr, minValue);
			float celv = addressArray(array, cel, minValue);
			
			return lerp(flrv, celv, index - flr);
		}
	}
	
	public static String readFile(String filePath, boolean inJar) throws IOException {
		if(inJar) {
			InputStream ressource = Utils.class.getClassLoader().getResourceAsStream(filePath);
			if(ressource == null) {
				System.err.println("Can't find ressource: " + filePath);
				
				return null;
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(ressource));
			
			String line;
			String lines = "";
			
			while((line = reader.readLine()) != null)
				lines += line + "\n";
			
			ressource.close();
			reader.close();
			
			return lines;
		} else {
			Path path = Paths.get(filePath);
			
			if(path == null || !Files.exists(path) || Files.isDirectory(path)) return null;
			
			String str = "";
			
			List<String> lines = Files.readAllLines(path);
			
			for(String l : lines)
				str += l + "\n";
			
			return str;
		}
	}
	
	public static float lerp(float a, float b, float x) {
		return a + x * (b - a);
	}
	
	public static double lerp(double a, double b, double x) {
		return a + x * (b - a);
	}
	
	public static String prettyTime(double s) {
		return prettyTime(s, false);
	}
	
	public static String prettyTime(double s, boolean withMillis) {
		if(s == 0.0f) {
			if(withMillis) return "0:00.000";
			else return "0:00";
		}
		
		int millis = (int) Math.floor((s % 60f) * 1000);
		int seconds = (int) Math.floor(s % 60f);
		int minutes = (int) Math.floor(s / 60f % 60f);
		int hours = (int) Math.floor(s / 3600f);
		
		String zeroMin = "0" + minutes;
		zeroMin = zeroMin.substring(zeroMin.length() - 2);
		
		String zeroSec = "0" + seconds;
		zeroSec = zeroSec.substring(zeroSec.length() - 2);
		
		if(withMillis) {
			String zeroMilli = "000" + millis;
			zeroMilli = zeroMilli.substring(zeroMilli.length() - 3);
			
			zeroSec += "." + zeroMilli;
		}
		
		if(hours != 0) return hours + ":" + zeroMin + ":" + zeroSec;
		else return minutes + ":" + zeroSec;
	}
	
	private static HashMap<Integer, Float> randset = new HashMap<Integer, Float>();
	
	public static float smoothrand(float i) {
		i = i < 0 ? -i : i;
		
		int flr = (int) Math.floor(i);
		int ceil = (int) Math.ceil(i);
		
		if(!randset.containsKey(flr)) randset.put(flr, (float) Math.random());
		if(!randset.containsKey(ceil)) randset.put(ceil, (float) Math.random());
		
		return lerp(randset.get(flr), randset.get(ceil), (float) (Math.cos((i - flr) * -1 * Math.PI) * -0.5 + 0.5));
	}
	
	public static void roundRect(GraphicsContext gtx, float x, float y, float w, float h, float r) {
		r = Math.max(r, 0);
		if(r == 0) {
			gtx.beginPath();
			gtx.rect(x, y, w, h);
			return;
		}
		
		if(w < 2 * r) r = w / 2;
		if(h < 2 * r) r = h / 2;
		gtx.beginPath();
		gtx.moveTo(x + r, y);
		gtx.arcTo(x + w, y, x + w, y + h, r);
		gtx.arcTo(x + w, y + h, x, y + h, r);
		gtx.arcTo(x, y + h, x, y, r);
		gtx.arcTo(x, y, x + w, y, r);
		gtx.closePath();
	}
	
	public static byte clamp(byte x, byte min, byte max) {
		if(min == max) {
			return min;
		} else if(min > max) {
			byte temp = min;
			min = max;
			max = temp;
		}
		
		return (byte) Math.max(Math.min(x, max), min);
	}
	
	public static short clamp(short x, short min, short max) {
		if(min == max) {
			return min;
		} else if(min > max) {
			short temp = min;
			min = max;
			max = temp;
		}
		
		return (short) Math.max(Math.min(x, max), min);
	}
	
	public static int clamp(int x, int min, int max) {
		if(min == max) {
			return min;
		} else if(min > max) {
			int temp = min;
			min = max;
			max = temp;
		}
		
		return Math.max(Math.min(x, max), min);
	}
	
	public static float clamp(float x, float min, float max) {
		if(min == max) {
			return min;
		} else if(min > max) {
			float temp = min;
			min = max;
			max = temp;
		}
		
		return Math.max(Math.min(x, max), min);
	}
	
	public static double clamp(double x, double min, double max) {
		if(min == max) {
			return min;
		} else if(min > max) {
			double temp = min;
			min = max;
			max = temp;
		}
		
		return Math.max(Math.min(x, max), min);
	}
	
	public static long clamp(long x, long min, long max) {
		if(min == max) {
			return min;
		} else if(min > max) {
			long temp = min;
			min = max;
			max = temp;
		}
		
		return Math.max(Math.min(x, max), min);
	}
}
