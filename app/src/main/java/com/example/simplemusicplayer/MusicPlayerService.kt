package com.example.simplemusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class MusicPlayerService : Service() {

    var mMediaPlayer : MediaPlayer? = null      // 미디어 플레이어 객체를 null로 초기화

   // 바인더를 반환해 서비스 함수를 쓸 수 있게 함
    var mBinder : MusicPlayerBinder = MusicPlayerBinder()

    inner class MusicPlayerBinder : Binder() {
        fun getService() : MusicPlayerService{
            return this@MusicPlayerService
        }
    }

    override fun onCreate() {       // 서비스가 생성될 때 딱 한 번만 실행
        super.onCreate()
        startForegroundService()        // 포그라운드 서비스 시작(상태 표시줄에 앱이 실행되고 있다는 알림 생성)
    }

    /* 바인더 반환
    bindService() 함수를 호출할 때 실행,서비스와 구성요소를 이어주는 매개체 반환
    바인드가 필요없는 서비스라면 null 반환*/
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

   /* startService()를 호출하면 실행되는 콜백 함수 → 반드시 정수 반환(서비스를 종료할 때 서비스를 어떻게 유지할지)
    이 함수가 실행되면 서비스는 시작된 상태가 되고 백그라운드에서 쭉 존재하게 됨*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) : Int{
        return START_STICKY     // 시스템이 서비스를 중단하면 서비스를 다시 실행하고 onStartCommand() 함수 호출
    }

    fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
            val mChannel = NotificationChannel(     // 알림 채널 생성
                "CHANNEL_ID",
            "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        // 알림 생성
        val notification: Notification = Notification.Builder(this,"CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_play)       // 알림 아이콘
            .setContentTitle("뮤직 플레이어 앱")       // 알림 제목
            .setContentText("앱이 실행 중입니다.")      // 알림 내용
            .build()

        startForeground(1, notification)        // 인수로 알림 ID와 알림 지정
    }

    // 서비스 생명 주기의 마지막 단계, 서비스 종료
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    // 재생되고 있는지 확인
    fun isPlaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }

    // 음악 재생
    fun play() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate)

            mMediaPlayer?.setVolume(1.0f, 1.0f);
            mMediaPlayer?.isLooping = true      // 반복재생 여부
            mMediaPlayer?.start()
        } else {
            if (mMediaPlayer!!.isPlaying){
                Toast.makeText(this, "이미 음악이 실행 중입니다.", Toast.LENGTH_SHORT).show()
            } else {
                mMediaPlayer?.start()
            }
        }
    }

    // 일시정지
    fun pause() {
        mMediaPlayer?.let {
            if(it.isPlaying){
                it.pause()      // 음악 일시정지
            }
        }
    }

    // 재생중지
    fun stop() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()       // 음악 완전히 멈춤
                it.release()        // 미디어 플레이어에 할당된 자원을 해제
                mMediaPlayer = null
            }
        }
    }
}