package cn.incorner.contrast.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.R;

/**
 * 带 确定/取消 的消息对话框
 * 
 * @author yeshimin
 */
public class MessageWithOkCancelFragment2 extends DialogFragment implements OnClickListener {

	private View view;
	private RelativeLayout rlOk;
	private RelativeLayout rlCancel;
	private TextView tvTitle;
	private TextView tvContent;

	private Callback callback;
	private int layoutRes = -1;

	private Callback2 callback2;

	public MessageWithOkCancelFragment2() {
	}

	@SuppressLint("ValidFragment")
	public MessageWithOkCancelFragment2(Callback callback) {
		this.callback = callback;
	}

	@SuppressLint("ValidFragment")
	public MessageWithOkCancelFragment2(Callback callback, int layoutRes) {
		this.callback = callback;
		this.layoutRes = layoutRes;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		view = inflater.inflate(layoutRes != -1 ? layoutRes
				: R.layout.frag_message_with_ok_cancel_2, null);

		init();
		if (callback2 != null) {
			callback2.onCreated();
		}

		return view;
	}

	private void init() {
		rlOk = (RelativeLayout) view.findViewById(R.id.rl_ok);
		rlCancel = (RelativeLayout) view.findViewById(R.id.rl_cancel);

		rlOk.setOnClickListener(this);
		rlCancel.setOnClickListener(this);

		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvContent = (TextView) view.findViewById(R.id.tv_content);
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	public void setContent(String content) {
		tvContent.setText(content);
	}

	public void setOnCallback2(Callback2 callback2) {
		this.callback2 = callback2;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_ok:
			if (callback != null) {
				callback.onOk();
			}
			dismiss();
			break;
		case R.id.rl_cancel:
			if (callback != null) {
				callback.onCancel();
			}
			dismiss();
			break;
		}
	}

	/**
	 * 回调接口
	 */
	public interface Callback {
		public void onOk();

		public void onCancel();
	}

	public interface Callback2 {
		public void onCreated();
	}

}
