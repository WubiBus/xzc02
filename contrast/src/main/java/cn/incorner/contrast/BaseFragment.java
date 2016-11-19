package cn.incorner.contrast;

import org.xutils.x;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 基类Fragment
 * 
 * @author yeshimin
 */
public class BaseFragment extends Fragment {

	private boolean injected = false;
	private LayoutInflater inflater;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		injected = true;
		this.inflater = inflater;
		return x.view().inject(this, inflater, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (!injected) {
			x.view().inject(this, this.getView());
		}
	}

	public LayoutInflater getLayoutInflater() {
		return inflater;
	}

}
