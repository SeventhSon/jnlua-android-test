package com.guru.jnlua_android_test;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.NamedJavaFunction;

public class MainActivity extends Activity {
	private static final String TAG = "LuaTest";
	private LuaState luaState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			luaState = new LuaState();
			luaState.openLibs();
			// Register the module
			luaState.register("simple",
					new NamedJavaFunction[] { new Divide(), new DebugLog() }, true);

			// Set a field 'VERSION' with the value 1
			luaState.pushInteger(1);
			luaState.setField(-2, "VERSION");

			// Pop the module table
			luaState.pop(1);

			AssetManager asset = getAssets();
			InputStream is = null;

			try {
				is = asset.open("script.lua");
				luaState.load(is, "=jnlua_android", "bt");
			} catch (IOException e) {
				Log.e(TAG, "Error loading script", e);
				return;
			}

			luaState.call(0, 0); // No arguments, no returns

			luaState.getGlobal("main"); // Push the function on the stack

			// Call
			luaState.call(0, 1); // 0 arguments, 1 return

			stackDump();
			String result = luaState.checkString(-1);
			luaState.pop(1);

			TextView outputWidget = (TextView) findViewById(R.id.textBoxOut);
			outputWidget.setText(result);

		} catch (LuaRuntimeException e) {
			Log.e(TAG, e.getMessage() + e.getLuaStackTrace());
			e.printLuaStackTrace();

		} finally {
			luaState.close();
		}
	}

	private void stackDump() {
		int i;
		int top = luaState.getTop();
		for (i = 1; i <= top; i++) {
			LuaType t = luaState.type(i);
			//Log.d(TAG, t.displayText());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
class DebugLog implements NamedJavaFunction {
	@Override
	public int invoke(LuaState luaState) {
		// Get arguments using the check APIs; these throw exceptions with
		// meaningful error messages if there is a mismatch
		String msg = luaState.checkString(2);
		String TAG = luaState.checkString(1);

		Log.d(TAG,msg);
		
		return 0;
	}

	@Override
	public String getName() {
		return "log";
	}
}
class Divide implements NamedJavaFunction {
	@Override
	public int invoke(LuaState luaState) {
		// Get arguments using the check APIs; these throw exceptions with
		// meaningful error messages if there is a mismatch
		double number1 = luaState.checkNumber(1);
		double number2 = luaState.checkNumber(2);

		// Do the calculation (may throw a Java runtime exception)
		double result = number1 / number2;

		// Push the result on the Lua stack
		luaState.pushNumber(result);

		// Signal 1 return value
		return 1;
	}

	@Override
	public String getName() {
		return "divide";
	}
}
