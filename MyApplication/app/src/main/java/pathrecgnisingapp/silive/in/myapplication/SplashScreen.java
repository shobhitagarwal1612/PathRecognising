package pathrecgnisingapp.silive.in.myapplication;

        import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends Activity {
    MediaPlayer ourSong;

    @Override
    protected void onCreate(Bundle splashbackground) {
        // TODO Auto-generated method stub
        super.onCreate(splashbackground);
        setContentView(R.layout.splash);

       /* View view=(TextView)findViewById(R.id.textView1);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.animation);
        view.startAnimation(anim);
       */ ImageView myImageView = (ImageView) findViewById(R.id.imageView1);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.blink);
        myImageView.startAnimation(myFadeInAnimation);


        ourSong = MediaPlayer.create(SplashScreen.this, R.raw.rename);
        ourSong.start();
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    try    {
                        Intent openCounter = new Intent("pathrecgnisingapp.silive.in.myapplication.MAP");
                        startActivity(openCounter);
                        overridePendingTransition(R.anim.lr,
                                R.anim.rl);   } catch(Exception ex) {
                    }
                    Intent openCounter = new Intent("pathrecgnisingapp.silive.in.myapplication.Map");
                    startActivity(openCounter);
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        ourSong.release();
        finish();
    }

}
