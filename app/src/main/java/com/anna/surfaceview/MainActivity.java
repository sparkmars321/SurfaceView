package com.anna.surfaceview;

import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYAdListener;
import com.iflytek.voiceads.IFLYAdSize;
import com.iflytek.voiceads.IFLYFullScreenAd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Rotate rotate;
	private ImageView start_btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		createAd();
		setContentView(R.layout.activity_main);
		rotate = (Rotate) this.findViewById(R.id.rotate);
        
		start_btn = (ImageView) this.findViewById(R.id.start_btn);
		start_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!rotate.isRuning()){
					rotate.luckyStart(3);
					start_btn.setImageResource(R.drawable.stop);
				}else{
					if (!rotate.isShoundEnd()) {
						rotate.luckyEnd();
						start_btn.setImageResource(R.drawable.start);
					}
				}
			}
		});
	}
	private IFLYFullScreenAd ad;
	private void createAd() {
		ad = IFLYFullScreenAd.createFullScreenAd(this,
				"4C85AAE75D338543F9B2792E2EF8A190");
		ad.setAdSize(IFLYAdSize.FULLSCREEN);
		ad.setParameter(AdKeys.SHOW_TIME_FULLSCREEN, "6000");
		ad.setParameter(AdKeys.DOWNLOAD_ALERT, "true");
		ad.loadAd(new IFLYAdListener() {
			
			@Override
			public void onAdReceive() {
				System.out.println("===================");
				ad.showAd();
			}
			
			@Override
			public void onAdFailed(AdError arg0) {
				Toast.makeText(
						getApplicationContext(),
						arg0.getErrorCode() + "****"
								+ arg0.getErrorDescription(), Toast.LENGTH_SHORT).show();
				
			}
			
			@Override
			public void onAdExposure() {
				
			}
			
			@Override
			public void onAdClose() {
				
			}
			
			@Override
			public void onAdClick() {
				
			}
		});
		
	}
}
