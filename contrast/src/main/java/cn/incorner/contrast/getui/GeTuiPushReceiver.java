package cn.incorner.contrast.getui;

import java.util.UUID;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.annotation.SuppressLint;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.GetAccessResultEntity;
import cn.incorner.contrast.data.entity.GetuiResultEntity;
import cn.incorner.contrast.page.MessageActivity;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.QDataModule;

import com.alibaba.fastjson.JSON;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;


public class GeTuiPushReceiver extends BroadcastReceiver {
    public static NotificationManager nm;

    @SuppressLint("NewApi")
	@Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        try {
            switch (bundle.getInt(PushConsts.CMD_ACTION)) {
                case PushConsts.GET_MSG_DATA:
                    // 获取透传数据
                    byte[] payload = bundle.getByteArray("payload");

                    String taskid = bundle.getString("taskid");
                    String messageid = bundle.getString("messageid");

					// smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                    boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);

                    if (payload != null) {
                        String data = new String(payload);
                        try {
						if (data != null) {
							GetuiResultEntity entity = JSON.parseObject(data,GetuiResultEntity.class);
							if (entity != null) {
								QDataModule.getInstance()
										.notifyMessageReceiveChangeListener();
								Builder mBuilder = new Builder(context);
								nm = (NotificationManager) context
										.getSystemService(Context.NOTIFICATION_SERVICE);
								mBuilder.setTicker("无比");
								mBuilder.setSmallIcon(R.drawable.icon_yuanjiao);
								mBuilder.setContentTitle("无比");
								mBuilder.setContentText("您有新的消息，点击查看");
								// 设置点击一次后消失（如果没有点击事件，则该方法无效。）
								mBuilder.setAutoCancel(true);
								// 点击通知之后需要跳转的页面
								Intent resultIntent = new Intent(context,
										MessageActivity.class);
								// 使用TaskStackBuilder为“通知页面”设置返回关系
								TaskStackBuilder stackBuilder = TaskStackBuilder
										.create(context);
								// 为点击通知后打开的页面设定 返回 页面。（在manifest中指定）
								stackBuilder
										.addParentStack(MessageActivity.class);
								stackBuilder.addNextIntent(resultIntent);
								PendingIntent pIntent = stackBuilder
										.getPendingIntent(
												UUID.randomUUID().hashCode(),
												PendingIntent.FLAG_UPDATE_CURRENT);
								mBuilder.setContentIntent(pIntent);
								nm.notify(UUID.randomUUID().hashCode(),
										mBuilder.build());
							}
						}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        }
                    break;

                case PushConsts.GET_CLIENTID:
                    // 获取ClientID(CID)
                    // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                    final String cid = bundle.getString("clientid");
                    new Thread() {
                        public void run() {
                            pushInit(cid);
                        };
                    }.start();
                    break;

                case PushConsts.THIRDPART_FEEDBACK:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	private void pushInit(String cid) {
		RequestParams params = new RequestParams(Config.PATH_ADD_CLIENTIDE);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("deviceToken", cid);
		params.addParameter("fromAndroid", 1);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {}

			@Override
			public void onError(Throwable arg0, boolean arg1) {}

			@Override
			public void onFinished() {}

			@Override
			public void onSuccess(JSONObject result) {
			}
		});
	}
    
}
