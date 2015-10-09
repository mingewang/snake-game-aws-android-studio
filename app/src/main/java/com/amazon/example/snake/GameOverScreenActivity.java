package com.amazon.example.snake;

import com.amazon.example.snake.aws.AWSClientManager;
import com.amazon.example.snake.aws.CognitoSyncTask;
import com.amazon.example.snake.aws.DDBGetTask;
import com.amazon.example.snake.aws.DDBGetTask.DDBTaskFinishedListener;
import com.amazon.example.snake.aws.DDBPutTask;
import com.amazon.example.snake.aws.DDBTaskResult;
import com.amazon.example.snake.aws.S3UploadTask;
import com.amazon.example.snake.aws.S3UploadTask.UploadTaskFinishedListener;
import com.amazon.example.snake.helpers.Screenshotter;
import com.amazonaws.mobileconnectors.cognito.Dataset;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class GameOverScreenActivity extends Activity implements
		UploadTaskFinishedListener, DDBTaskFinishedListener {

	private TextView gameOverText;
	int highLevel;
	int highScore;
	int lastLevel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_over_screen);
		setTitle(R.string.activity_game_over_title);

		gameOverText = (TextView) findViewById(R.id.gameover_content);

		highLevel = getIntent().getIntExtra("highLevel", 0);
		highScore = getIntent().getIntExtra("highScore", 0);
		
		
		new DDBGetTask(this).execute();
		new CognitoSyncTask(this).doSync(false);

		
		gameOverText.setText("Your High Level : " + highLevel
				+ "\n Your High Score : " + highScore
				+ "\n Your Last Record Level was : " + lastLevel);

		Button btn_new = (Button) findViewById(R.id.gameover_new_game_button);

		btn_new.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				//Intent intent = new Intent(GameOverScreenActivity.this, SnakeGameActivity.class);
				//startActivity(intent);
				//GameController.CONTROLLER.startGame();
				Intent callIntent = new Intent(Intent.ACTION_CALL); 
				callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				callIntent.setClass(GameOverScreenActivity.this,SnakeGameActivity.class);
				startActivity(callIntent);
			}
		});
		
		
		Button btn_resume = (Button) findViewById(R.id.gameover_resume_game_button);

		btn_resume.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			Dataset dataset = AWSClientManager.getCognitoSync().openOrCreateDataset(AWSClientManager.COGNITO_SYNC_DATASET_NAME);
        	dataset.delete();
			new CognitoSyncTask(GameOverScreenActivity.this).doSync();
			
				
			}
		});
		

		Button but_upload = (Button) findViewById(R.id.gameover_upload_button);

		but_upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				new DDBPutTask().execute(highLevel);

				ProgressDialog progressDialog = new ProgressDialog(
						GameOverScreenActivity.this);
				progressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setTitle(R.string.activity_upload);
				String filepath = Screenshotter.getScreen(getWindow()
						.getDecorView());
				new S3UploadTask(progressDialog, GameOverScreenActivity.this)
						.execute(filepath);

			}
		});

	}

	@Override
	public void onTaskFinished() {
		finish();
	}
	
	@Override
	public void onDDBTaskFinished(DDBTaskResult result) {
		lastLevel = result.getAttributeNumber();
		gameOverText.setText("Your High Level : " + highLevel
				+ "\n Your High Score : " + highScore
				+ "\n Your Last Record Level was : " + lastLevel);
	}

}
