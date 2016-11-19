package cn.incorner.contrast.util;

import java.util.ArrayList;

public class QDataModule {
	private static QDataModule instance = new QDataModule();

    public static QDataModule getInstance() {
        return instance;
    }
    
  //-------------------- 通知更新接口 starting --------------------
    private ArrayList<OnMessageReceiveListener> messageReceiveListenerList = new ArrayList<OnMessageReceiveListener>();

    public void notifyMessageReceiveChangeListener() {
        for (OnMessageReceiveListener listener : messageReceiveListenerList) {
            listener.onReceived();
        }
    }

    public void addMessageReceiveListener(OnMessageReceiveListener listener) {
    	messageReceiveListenerList.add(listener);
    }

    public void removeMessageReceiveListener(OnMessageReceiveListener listener) {
    	messageReceiveListenerList.remove(listener);
    }

    public interface OnMessageReceiveListener {
        public void onReceived();
    }
    //-------------------- 通知更新接口 end --------------------
    
  //-------------------- 隐藏发现页面接口 starting --------------------
    private ArrayList<OnFindGoneListener> onFindGoneListenerList = new ArrayList<OnFindGoneListener>();

    public void notifyOnFindGoneListener() {
        for (OnFindGoneListener listener : onFindGoneListenerList) {
            listener.onReceivedFindGone();
        }
    }

    public void addFindGoneListener(OnFindGoneListener listener) {
    	onFindGoneListenerList.add(listener);
    }

    public void removeOnFindGoneListener(OnFindGoneListener listener) {
    	onFindGoneListenerList.remove(listener);
    }

    public interface OnFindGoneListener {
        public void onReceivedFindGone();
    }
    //-------------------- 隐藏发现页面接口 end --------------------
}
