package com.cockandroid.finalcapstone.message


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.cockandroid.finalcapstone.R
import com.cockandroid.finalcapstone.auth.UserDataModel
import com.cockandroid.finalcapstone.message.fcm.NotiModel
import com.cockandroid.finalcapstone.message.fcm.PushNotification
import com.cockandroid.finalcapstone.message.fcm.RetrofitInstance
import com.cockandroid.finalcapstone.utils.FirebaseAuthUtils
import com.cockandroid.finalcapstone.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyLikeListActivity : AppCompatActivity() {

    private val TAG = "MyLikeListActivity"
    private val uid = FirebaseAuthUtils.getUid()
    private var likeUserList = mutableListOf<UserDataModel>()
    private var likeUserListUid = mutableListOf<String>()

    lateinit var listviewAdapter : ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)

        val userListView = findViewById<ListView>(R.id.userListView)
        listviewAdapter = ListViewAdapter(this, likeUserList)
        userListView.adapter = listviewAdapter

//        getUserDataList()

        getMyLikeList()

        userListView.setOnItemClickListener { adapterView, view, i, l ->
            checkMatching(likeUserList[i].uid.toString())

            val notiModel = NotiModel("a","b")
            val pushModel = PushNotification(notiModel,likeUserList[i].token.toString())
            testPush(pushModel)

        }

    }
//
    private fun checkMatching(otherUid:String){

        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

//                if(dataSnapshot.children.count()==0){
//
//                }else{
//                    for(dataModel in dataSnapshot.children){
//                        val likeUserKey = dataModel.key.toString()
//                        if(likeUserKey.equals(uid)){
//                            Toast.makeText(this@MyLikeListActivity,"????????? ?????????", Toast.LENGTH_SHORT).show()
//                        }else{
//
//                        }
//                    }
//                }
                var check = false;
                for (data in dataSnapshot.children) {
                    val likeUid = data.key.toString()
                    if (likeUid == FirebaseAuthUtils.getUid()) {
                        Toast.makeText(baseContext, "????????? ???????????????!!", Toast.LENGTH_SHORT)
                            .show()
                        check = true;
                        break;
                    }
                }
                if (!check) {
                    Toast.makeText(baseContext, "????????? ?????? ???????????? ????????? ???????????????..", Toast.LENGTH_SHORT)
                        .show()
                }


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
    }

    private fun getMyLikeList(){

        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children){
                    likeUserListUid.add(dataModel.key.toString())
                }
                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)

    }

    private fun getUserDataList(){

        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)
                    if(likeUserListUid.contains(user?.uid)){
                        likeUserList.add(user!!)
                    }
                }
                listviewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    private fun testPush(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.api.postNotification(notification)
    }

}