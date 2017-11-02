package doext.implement;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoTextHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_BaiduNavigate_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_BaiduNavigate_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_BaiduNavigate_Model extends DoSingletonModule implements do_BaiduNavigate_IMethod {
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	private String appFolderName = null;
	private String mSDCardPath = null;
	private Activity mContext;
	public static List<Activity> activityList = new LinkedList<Activity>();

	public do_BaiduNavigate_Model() throws Exception {
		super();
		mContext = DoServiceContainer.getPageViewFactory().getAppContext();
		appFolderName = mContext.getPackageName();
		if (initDirs()) {
			initNavi();
		}
	}

	private void initNavi() {
		BaiduNaviManager.getInstance().init(mContext, mSDCardPath, appFolderName, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 != status) {
					fireFailedEvent("key校验失败, " + msg);
				}
			}

			public void initSuccess() {
			}

			public void initStart() {
			}

			public void initFailed() {
				fireFailedEvent("百度导航引擎初始化失败，请检查key是否正确！");
			}
		}, null);
	}

	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, appFolderName);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private void fireFailedEvent(String _msg) {
		DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
		_result.setResultText(_msg);
		getEventCenter().fireEvent("failed", _result);

	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("start".equals(_methodName)) {
			this.start(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		// ...do something
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 开始导航；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void start(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

		String _startPoint = DoJsonHelper.getString(_dictParas, "startPoint", null);
		String _endPoint = DoJsonHelper.getString(_dictParas, "endPoint", null);
		if (_startPoint == null || _endPoint == null) {
			throw new Exception("startPoint 或  endPoint 参数值不能为空！");
		}
		String[] _latLng1 = _startPoint.split(",");
		String[] _latLng2 = _endPoint.split(",");
		if (_latLng1 == null || _latLng2 == null || _latLng1.length != 2 || _latLng2.length != 2) {
			throw new Exception("startPoint 或  endPoint 参数值非法！");
		}
		double _p1_lat = DoTextHelper.strToDouble(_latLng1[0], 0);
		double _p1_lng = DoTextHelper.strToDouble(_latLng1[1], 0);
		double _p2_lat = DoTextHelper.strToDouble(_latLng2[0], 0);
		double _p2_lng = DoTextHelper.strToDouble(_latLng2[1], 0);

		String _coType = DoJsonHelper.getString(_dictParas, "coType", "BD09LL");

		CoordinateType _type = CoordinateType.BD09LL;
		// BD09_MC：百度墨卡托坐标，BD09LL：百度经纬度坐标，GCJ02：国测局坐标，WGS84：GPS坐标
		if ("BD09_MC".equals(_coType)) {
			_type = CoordinateType.BD09_MC;
		} else if ("GCJ02".equals(_coType)) {
			_type = CoordinateType.GCJ02;
		} else if ("WGS84".equals(_coType)) {
			_type = CoordinateType.WGS84;
		}
		BNRoutePlanNode sNode = new BNRoutePlanNode(_p1_lng, _p1_lat, null, null, _type);
		BNRoutePlanNode eNode = new BNRoutePlanNode(_p2_lng, _p2_lat, null, null, _type);

		List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
		list.add(sNode);
		list.add(eNode);
		BaiduNaviManager.getInstance().launchNavigator(mContext, list, 1, true, new MyRoutePlanListener(sNode));

	}

	public class MyRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public MyRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
			// 设置途径点以及resetEndNode会回调该接口
			for (Activity ac : activityList) {
				if (ac.getClass().getName().endsWith("BaiduNaviGuideActivity")) {
					return;
				}
			}
			mContext = DoServiceContainer.getPageViewFactory().getAppContext();
			Intent intent = new Intent(mContext, BaiduNaviGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			mContext.startActivity(intent);
		}

		@Override
		public void onRoutePlanFailed() {
			fireFailedEvent("导航失败，算路失败！");
		}
	}
}