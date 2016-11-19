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
import cn.incorner.contrast.R;

/**
 * 带 确定/取消 的消息对话框
 * 
 * @author yeshimin
 */
public class MessageWithOkCancelFragment extends DialogFragment implements OnClickListener {

	private View view;
	private RelativeLayout rlOk;
	private RelativeLayout rlCancel;

	private Callback callback;

	public MessageWithOkCancelFragment() {
	}

	@SuppressLint("ValidFragment")
	public MessageWithOkCancelFragment(Callback callback) {
		this.callback = callback;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		view = inflater.inflate(R.layout.frag_message_with_ok_cancel_logout, null);

		init();

		return view;
	}

	private void init() {
		rlOk = (RelativeLayout) view.findViewById(R.id.rl_ok);
		rlCancel = (RelativeLayout) view.findViewById(R.id.rl_cancel);

		rlOk.setOnClickListener(this);
		rlCancel.setOnClickListener(this);
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

}
