package com.cockandroid.finalcapstone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.cockandroid.finalcapstone.auth.IntroActivity
import com.cockandroid.finalcapstone.auth.UserDataModel
import com.cockandroid.finalcapstone.setting.SettingActivity
import com.cockandroid.finalcapstone.slider.CardStackAdapter
import com.cockandroid.finalcapstone.utils.FirebaseAuthUtils
import com.cockandroid.finalcapstone.utils.FirebaseRef
import com.cockandroid.finalcapstone.utils.FirebaseRef.Companion.database
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class MainActivity : AppCompatActivity() {

    lateinit var cardStackAdapter : CardStackAdapter
    lateinit var manager : CardStackLayoutManager

    private var TAG ="MainActivity"

    private var userCount = 0

//    private lateinit var currentUserGender: String  - 다른성별 불러오기

    private var uid = FirebaseAuthUtils.getUid()

    private var usersDataList = mutableListOf<UserDataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val setting = findViewById<ImageView>(R.id.settingIcon)
        setting.setOnClickListener {

//            val auth = Firebase.auth
//            auth.signOut()
//
            val intent = Intent(this,SettingActivity::class.java)
            startActivity(intent)

        }

        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)
        manager = CardStackLayoutManager(baseContext, object : CardStackListener{
            override fun onCardDragging(direction: Direction?, ratio: Float) {



            }

            override fun onCardSwiped(direction: Direction?) {

                if(direction==Direction.Right){
                    Toast.makeText(this@MainActivity,"좋아요",Toast.LENGTH_SHORT).show()
                    userLikeOtherUser(uid,usersDataList[userCount].uid.toString())
                }

                if(direction==Direction.Left){
                    Toast.makeText(this@MainActivity,"싫어요",Toast.LENGTH_SHORT).show()
                }

                userCount = userCount+1

                if(userCount == usersDataList.count()){
                    getUserDataList(/*currentUserGender - 다른성별 불러오기 */)
                }

            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }

        })


        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter

        getUserDataList()
//        getMyUserData() - 다른성별 불러오기

    }

//    private fun getMyUserData(){
//        val postListener = object : ValueEventListener {
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                Log.d(TAG,dataSnapshot.toString())
//                val data = dataSnapshot.getValue(UserDataModel::class.java)
//
//                currentUserGender = data?.gender.toString()
//
//                getUserDataList(data?.gender.toString())
//
//
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
//    }  -  다른성별 불러오기

   private fun getUserDataList(/*currentUserGender:String  다른성별 불러오기 */){

        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)

//                    if(user!!.gender.toString().equals(currentUserGender)){
//
//                    }else{
//                        usersDataList.add(user!!)
//                    }  -  다른성별 불러오기

                    usersDataList.add(user!!)

                }

                cardStackAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    private fun userLikeOtherUser(myUid : String, otherUid : String){

        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("true")
        getOtherUserLikeList(otherUid)

    }

    private fun getOtherUserLikeList(otherUid: String){

        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children){

                    val likeUserKey = dataModel.key.toString()
                    if(likeUserKey.equals(uid)){
                        Toast.makeText(this@MainActivity,"서로 좋아합니다",Toast.LENGTH_SHORT).show()
                        createNotificationChannel()
                        sendNotification()
                    }

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_Channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        var builder = NotificationCompat.Builder(this, "Test_Channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("서로 좋아요를 눌렀습니다")
            .setContentText("저 사람도 나를 좋아합니다")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)){
            notify(123,builder.build())
        }
    }

}